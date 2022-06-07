package capstoneDesign.carbonTracker.apartment.service;

import capstoneDesign.carbonTracker.apartment.dto.AptEnergyRequest;
import capstoneDesign.carbonTracker.apartment.dto.AptEnergyResponse;
import capstoneDesign.carbonTracker.apartment.dto.AptListResponse;
import capstoneDesign.carbonTracker.apartment.repository.AptEnergyRepository;
import capstoneDesign.carbonTracker.apartment.repository.AptListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Element;

import java.net.URLEncoder;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class AptEnergyService {
    @Value("${aptEnergyUrl}")
    private String aptEnergyUrl;

    @Value("${aptEnergyKey}")
    private String aptEnergyKey;

    @Value("${electUsageUrl}")
    private String electUsageUrl;

    @Value("${gasUsageUrl}")
    private String gasUsageUrl;

    @Value("${gasUsageKey}")
    private String gasUsageKey;

    @Value("${weatherRetrieveUrl1}")
    private String weatherRetrieveUrl1;

    @Value("${weatherRetrieveUrl2}")
    private String weatherRetrieveUrl2;

    @Value("${weatherRetrieveKey}")
    private String weatherRetrieveKey;

    @Value("${MLElectUrl}")
    private String MLElectUrl;

    @Value("${MLGasUrl}")
    private String MLGasUrl;

    private final Map<String, String> sigunguCodeMap = new HashMap<>();

    private final KafkaTemplate<String, JSONObject> kafkaTemplate;
    private final AptEnergyRepository aptEnergyRepository;
    private final AptListRepository aptListRepository;

    private final CommonService commonService;

    public String aptEnergyAll(AptEnergyRequest aptEnergyRequest) throws Exception {
        String kaptCode = String.valueOf(aptEnergyRequest.getCode());
        String nowDate = aptEnergyRequest.getDate();
        log.info("aptEnergyAll(), 단지코드: {}, 현재년월: {}", kaptCode, nowDate);

        String[] date = initDates(aptEnergyRequest.getDate());
        String topic = "energy";

        // 반환할 결과 JSON 배열
        JSONArray resultArray = new JSONArray();
        initSigunguCodeMap();

        int i = 0;
        while(i < date.length) {
            String nDate = date[i];
            // JSON 배열 반환 형태 생성
            JSONObject jsonObject = new JSONObject();
            // 단지코드와 발생년월로 조회
            Optional<AptEnergyResponse> aptEnergyResponse = aptEnergyRepository.findByKaptCodeAndDate(kaptCode, nDate);
            // 값이 이미 존재하면 바로 jsonObject에 추가
            if(aptEnergyResponse.isPresent()) {
                AptEnergyResponse res = aptEnergyResponse.get();
                jsonObject.put("kaptCode", res.getKaptCode());
                jsonObject.put("date", res.getDate());
                jsonObject.put("helect", res.getHelect());
                jsonObject.put("hgas", res.getHgas());
                jsonObject.put("hwaterCool", res.getHwaterCool());
                jsonObject.put("carbonEnergy", res.getCarbonEnergy());

                log.info(String.valueOf(jsonObject));
            } else {
                // 없으면 공공데이터 API 호출해서 jsonObject에 추가
                log.info("저장된 정보가 없습니다.");
                // 특정 단지 코드에 대해 202001 ~ 202112까지의 에너지 사용량을 구하는 API
                String urlBuilder = aptEnergyUrl +
                        "?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + aptEnergyKey + /*Service Key*/
                        "&" + URLEncoder.encode("kaptCode", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(aptEnergyRequest.getCode()), "UTF-8") + /*단지코드*/
                        "&" + URLEncoder.encode("reqDate", "UTF-8") + "=" + URLEncoder.encode(nDate, "UTF-8"); /*발생년월*/

                jsonObject.put("kaptCode", kaptCode);
                jsonObject.put("date", nDate);
                // parsing 결과 jsonObject에 추가하기 위해 파라미터로 보내고, 반환 받음
                jsonObject = getUsage(urlBuilder, jsonObject);

                // 법정동주소, 법정동코드를 받아오기 위해 단지코드로 단지 정보 조회
                Optional<AptListResponse> aptListResponse = aptListRepository.findByKaptCode(kaptCode);
                AptListResponse res = aptListResponse.orElseThrow(IllegalArgumentException::new);

                String BjdJuso = res.getBjdJuso();
                String bjdongCode = res.getBjdCode().substring(5);

                String[] tokens = BjdJuso.split(" ");

                String bunji;

                if (tokens[1].charAt(tokens[1].length() - 1) == '군') bunji = tokens[4];
                else bunji = tokens[3];

                String sigunguCode = sigunguCodeMap.get(tokens[1]);
                String[] tmp = bunji.split("-");
                String bun = "0".repeat(4 - tmp[0].length()) + tmp[0];
                String ji = (tmp.length == 1 ? "0000" : "0".repeat(4 - tmp[1].length()) + tmp[1]);

                log.info("시군구코드 : {}, 법정동코드: {}, 번: {}, 지: {}", sigunguCode, bjdongCode, bun, ji);
                // 국토교통부_건물에너지정보_서비스 사용
                String gasUrlBuilder = getOneUsageUrl(gasUsageUrl, sigunguCode, bjdongCode, bun, ji, nDate);

                log.info(gasUrlBuilder);
                jsonObject = getGasUsage(gasUrlBuilder, jsonObject);

                // 전기 사용량이 조회되지 않은 경우, 국토교통부_건물에너지정보 서비스 API 사용
                if (jsonObject.get(("helect")).equals(0)) {
                    String electUrlBuilder = getOneUsageUrl(electUsageUrl, sigunguCode, bjdongCode, bun, ji, nDate);
                    jsonObject = getElectUsage(electUrlBuilder, jsonObject);
                }

                boolean hasNullInfo = false;
                // 전기, 가스 사용량이 0인 경우는 ML 모델을 통해 예상 사용량으로 대체
                int electUsage = Integer.parseInt(Objects.toString(jsonObject.get("helect")));
                Map<String, Double> pastWeatherInfo = new LinkedHashMap<>();
                if (electUsage == 0) {
                    pastWeatherInfo = getPastWeatherInfo(nDate);
                    if (pastWeatherInfo == null) hasNullInfo = true;
                    else electUsage = getExpectedUsageForElec(res.getKaptdaCnt(), pastWeatherInfo);

                    jsonObject.put("helect", electUsage);
                }

                int gasUsage = Integer.parseInt(Objects.toString(jsonObject.get("hgas")));
                if (gasUsage == 0) {
                    if (!hasNullInfo && pastWeatherInfo.isEmpty()) {
                        pastWeatherInfo = getPastWeatherInfo(nDate);
                        if (pastWeatherInfo != null)
                            gasUsage = getExpectedUsageForGas(res.getKaptdaCnt(), pastWeatherInfo) / 10;
                    } else if (!hasNullInfo) gasUsage = getExpectedUsageForGas(res.getKaptdaCnt(), pastWeatherInfo) / 10;
                    jsonObject.put("hgas", gasUsage);
                }

                double waterUsage = Double.parseDouble(Objects.toString(jsonObject.get("hwaterCool")));

                // 탄소 사용량 계산 및 추가
                int helect = (int) (electUsage * 0.4663);
                int hgas = (int) (gasUsage * 2.22);
                int hwaterCool = (int) (waterUsage * 0.3332);

                jsonObject.put("carbonEnergy", helect + hgas + hwaterCool);

                // Kafka로 JSON 객체 produce
                log.info(String.format("Produce message : %s", jsonObject));
                kafkaTemplate.send(topic, jsonObject);
            }
            resultArray.add(jsonObject);
            i++;
        }

        JSONObject pointObject = new JSONObject();
        pointObject.put("예상 탄소 포인트", getCarbonPoint(kaptCode, nowDate));

        resultArray.add(pointObject);

        return resultArray.toJSONString();
    }

    // private methods

    // 단지코드와 현재년월을 받아 예상 탄소 포인트 반환
    private JSONObject getCarbonPoint(String kaptCode, String nowDate) {
        // 사용량 조회가 필요한 날짜 배열 생성
        String[] dates = getDates(nowDate);

        // 올해 상(하)반기 사용량 총합 조회
        ArrayList<Long> nowUsage = getThisYearEnergyUsage(kaptCode, dates);

        // 날짜 배열과 단지 코드를 이용해 과거 사용량 총합 조회, Map<년, 사용량 총합>의 형태
        ArrayList<Long> pastUsage = getEnergyUsage(kaptCode, dates);

        double[] reductionRate = new double[3];
        for (int i = 0; i < 3; i++) {
            if (nowUsage.get(i) >= pastUsage.get(i)) continue;
            reductionRate[i] = computeReduction(nowUsage.get(i), pastUsage.get(i));
        }

        int point = 0;
        point += computePoint(reductionRate[0], new int[] {5000, 10000, 15000});
        point += computePoint(reductionRate[1], new int[] {750, 1500, 2000});
        point += computePoint(reductionRate[2], new int[] {3000, 6000, 8000});

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("전기 에너지 감축률", (int) reductionRate[0]);
        jsonObject.put("수도 에너지 감축률", (int) reductionRate[1]);
        jsonObject.put("가스 에너지 감축률", (int) reductionRate[2]);
        jsonObject.put("예상 탄소 포인트", point);

        return jsonObject;
    }

    private String[] getDates(String nowDate) {
        // 현재년월 기준으로 과거 2년의 년월을 반환
        String[] dates = new String[12];
        int year = Integer.parseInt(nowDate.substring(0,4));
        int month = Integer.parseInt(nowDate.substring(4));
        int idx = 0;

        for (int i = year - 1; i < year; i++) {
            StringBuilder sb;
            if (month < 7) {
                for (int j = 1; j < 7; j++) {
                    sb = new StringBuilder();
                    sb.append(i).append("0").append(j);
                    dates[idx++] = sb.toString();
                }
            } else {
                for (int j = 7; j < 10; j++) {
                    sb = new StringBuilder();
                    sb.append(i).append("0").append(j);
                    dates[idx++] = sb.toString();
                }
                for (int j = 10; j < 13; j++) {
                    sb = new StringBuilder();
                    sb.append(i).append(j);
                    dates[idx++] = sb.toString();
                }
            }
        }

        return dates;
    }

    private ArrayList<Long> getThisYearEnergyUsage(String kaptCode, String[] dates) {
        String thisYear = String.valueOf(Integer.parseInt(dates[0].substring(0, 4)) + 1);

        // 전기, 수도, 가스 사용량의 합을 구하기 위한 변수 초기화
        long electUsageSum = 0;
        long waterUsageSum = 0;
        long gasUsageSum = 0;

        for (int i = 0; i < 6; i++) {
            String date = thisYear + dates[i].charAt(4) + dates[i].charAt(5);
            Optional<AptEnergyResponse> aptEnergyResponse = aptEnergyRepository.findByKaptCodeAndDate(kaptCode, date);
            if (aptEnergyResponse.isPresent()) {
                AptEnergyResponse res = aptEnergyResponse.get();

                long thisYearElect = res.getHelect();
                long thisYearWater = res.getHwaterCool();
                long thisYearGas = res.getHgas();

                // 조회한 사용량이 0이라면 작년 사용량을 사용
                if (thisYearElect == 0) thisYearElect = getPastUsage(kaptCode, date, "elect");
                if (thisYearWater == 0) thisYearWater = getPastUsage(kaptCode, date, "water");
                if (thisYearGas == 0) thisYearGas = getPastUsage(kaptCode, date, "gas");

                // 전기, 수도, 가스 사용량 순으로 저장
                electUsageSum += thisYearElect;
                waterUsageSum += thisYearWater;
                gasUsageSum += thisYearGas;
            }
        }

        return new ArrayList<>(Arrays.asList(electUsageSum, waterUsageSum, gasUsageSum));
    }

    private long getPastUsage(String kaptCode, String date, String type) {
        AptEnergyResponse aptEnergyResponse = getPastEnergyResponse(kaptCode, date);

        if (aptEnergyResponse == null) return 0;
        switch(type) {
            case "elect":
                return aptEnergyResponse.getHelect();
            case "water":
                return aptEnergyResponse.getHwaterCool();
            default:
                return aptEnergyResponse.getHgas();
        }
    }

    private ArrayList<Long> getEnergyUsage(String kaptCode, String[] dates) {
        // 전기, 수도, 가스 사용량의 합을 구하기 위한 변수 초기화
        long electUsageSum = 0;
        long waterUsageSum = 0;
        long gasUsageSum = 0;

        for (String date : dates) {
            Optional<AptEnergyResponse> aptEnergyResponse = aptEnergyRepository.findByKaptCodeAndDate(kaptCode, date);
            if (aptEnergyResponse.isPresent()) {
                AptEnergyResponse res = aptEnergyResponse.get();
                // 전기, 수도, 가스 사용량 순으로 저장
                electUsageSum += res.getHelect();
                waterUsageSum += res.getHwaterCool();
                gasUsageSum += res.getHgas();
            }
        }

        return new ArrayList<>(Arrays.asList(electUsageSum, waterUsageSum, gasUsageSum));
    }

    private JSONObject getGasUsage(String reqBuilder, JSONObject jsonObject) throws Exception {
        String tmp = getUsage(reqBuilder);
        int useQty;

        if (tmp == null) useQty = 0;
        else {
            useQty = (int) (Double.parseDouble(tmp) * 0.09);
            if (useQty < 0) useQty = 0;
        }

        log.info("{} 사용량: {}", "가스", useQty);

        jsonObject.put("hgas", useQty);

        return jsonObject;
    }

    private JSONObject getElectUsage(String reqBuilder, JSONObject jsonObject) throws Exception {
        String tmp = getUsage(reqBuilder);
        int useQty;

        if (tmp == null) useQty = 0;
        else {
            useQty = (int) Double.parseDouble(tmp);
            if (useQty < 0) useQty = 0;
        }

        log.info("{} 사용량: {}", "전기", useQty);

        jsonObject.put("helect", useQty);

        return jsonObject;
    }

    private String getUsage(String reqBuilder) throws Exception {
        Element eElement = commonService.initElement(commonService.initDocument(reqBuilder));

        // 사용량 태그 값 추출
        return commonService.getTagValue("useQty", eElement);
    }

    private JSONObject getUsage(String reqBuilder, JSONObject jsonObject) throws Exception {
        Element eElement = commonService.initElement(commonService.initDocument(reqBuilder));

        // 전기, 수도 사용량 태그 값 추출
        String helect = commonService.getTagValue("helect", eElement);
        String hwaterCool = commonService.getTagValue("hwaterCool", eElement);

        // 각각 사용량 배열 형태가 아닌 key: value 형태로 바로 저장
        jsonObject.put("helect", Integer.parseInt(helect == null ? "0" : helect));
        jsonObject.put("hwaterCool", Integer.parseInt(hwaterCool == null ? "0" : hwaterCool));

        log.info("전기 사용량: {}, 수도 사용량: {}", helect, hwaterCool);

        return jsonObject;
    }

    private int getExpectedUsageForElec(int kaptdaCnt, Map<String, Double> pastWeatherInfo) {
        // body 생성
        JSONObject params = getMLReqBodyForElec(kaptdaCnt, pastWeatherInfo);

        // header 생성
        HttpHeaders httpHeaders = initHeaders();

        // header와 body 합치기
        HttpEntity<JSONObject> entity = new HttpEntity<>(params, httpHeaders);

        // 요청
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> responseEntity = rt.exchange(MLElectUrl, HttpMethod.POST, entity, String.class);

        return Integer.parseInt(Objects.requireNonNull(responseEntity.getBody()).replaceAll("\\D", ""));
    }

    private int getExpectedUsageForGas(int kaptdaCnt, Map<String, Double> pastWeatherInfo) {
        // body 생성
        JSONObject params = getMLReqBodyForGas(kaptdaCnt, pastWeatherInfo);

        // header 생성
        HttpHeaders httpHeaders = initHeaders();

        // header와 body 합치기
        HttpEntity<JSONObject> entity = new HttpEntity<>(params, httpHeaders);

        // 요청
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> responseEntity = rt.exchange(MLGasUrl, HttpMethod.POST, entity, String.class);

        log.info(String.valueOf(responseEntity));
        return Integer.parseInt(Objects.requireNonNull(responseEntity.getBody()).replaceAll("\\D", ""));
    }

    private JSONObject getMLReqBodyForElec(int kaptdaCnt, Map<String, Double> pastWeatherInfo) {
        JSONObject params = new JSONObject();
        params.put("household", kaptdaCnt);
        params.put("avg_temp", pastWeatherInfo.get("평균 기온"));
        params.put("max_temp", pastWeatherInfo.get("최고 기온"));
        params.put("min_temp", pastWeatherInfo.get("최저 기온"));
        params.put("avg_humid", pastWeatherInfo.get("평균 습도"));
        return params;
    }

    private JSONObject getMLReqBodyForGas(int kaptdaCnt, Map<String, Double> pastWeatherInfo) {
        JSONObject params = getMLReqBodyForElec(kaptdaCnt, pastWeatherInfo);
        params.put("avg_wind", pastWeatherInfo.get("평균 풍속"));
        return params;
    }

    private HttpHeaders initHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        return httpHeaders;
    }

    private Map<String, Double> getPastWeatherInfo(String date) throws Exception {
        String year = date.substring(0, 4);
        String month = date.substring(4);
        Map<String, Double> infoMap = new LinkedHashMap<>();

        String reqBuilder = getWeatherInfoUrl(weatherRetrieveUrl1, year, month);
        Element eElement = commonService.initElement(commonService.initDocument(reqBuilder));

        if (eElement == null) return null;

        infoMap.put("평균 기온", Double.parseDouble(commonService.getTagValue("taavg", eElement)));
        infoMap.put("최고 기온", Double.parseDouble(commonService.getTagValue("tamax", eElement)));
        infoMap.put("최저 기온", Double.parseDouble(commonService.getTagValue("tamin", eElement)));
        infoMap.put("평균 습도", Double.parseDouble(commonService.getTagValue("avghm", eElement)));

        reqBuilder = getWeatherInfoUrl(weatherRetrieveUrl2, year, month);
        eElement = commonService.initElement(commonService.initDocument(reqBuilder));

        infoMap.put("평균 풍속", Double.parseDouble(commonService.getTagValue("ws", eElement)));

        return infoMap;
    }

    // private detail service methods

    private String[] initDates(String date) {
        ArrayList<String> dates = new ArrayList<>();

        int year = Integer.parseInt(date.substring(0, 4));
        int month = Integer.parseInt(date.substring(4));

        for (int i = year - 2; i <= year; i++) {
            for (int j = 1; j <= 12; j++) {
                StringBuilder d = new StringBuilder();
                d.append(i);
                if (j < 10) d.append(0);
                d.append(j);
                dates.add(d.toString());
                if (i == year && j == month) break;
            }
        }

        return dates.toArray(String[]::new);
    }

    private void initSigunguCodeMap() {
        sigunguCodeMap.put("남구", "27200");
        sigunguCodeMap.put("달서구", "27290");
        sigunguCodeMap.put("달성군", "27710");
        sigunguCodeMap.put("동구", "27140");
        sigunguCodeMap.put("북구", "27230");
        sigunguCodeMap.put("서구", "27170");
        sigunguCodeMap.put("수성구", "27260");
        sigunguCodeMap.put("중구", "27110");
    }

    private String getOneUsageUrl(String url, String sigunguCode, String bjdongCode, String bun, String ji, String date) throws Exception {
        return url +
                "?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + gasUsageKey + /*Service Key*/
                "&" + URLEncoder.encode("sigunguCd", "UTF-8") + "=" + URLEncoder.encode(sigunguCode, "UTF-8") + /*시군구코드*/
                "&" + URLEncoder.encode("bjdongCd", "UTF-8") + "=" + URLEncoder.encode(bjdongCode, "UTF-8") + /*법정동코드*/
                "&" + URLEncoder.encode("bun", "UTF-8") + "=" + URLEncoder.encode(bun, "UTF-8") + /*번*/
                "&" + URLEncoder.encode("ji", "UTF-8") + "=" + URLEncoder.encode(ji, "UTF-8") + /*지*/
                "&" + URLEncoder.encode("useYm", "UTF-8") + "=" + URLEncoder.encode(date, "UTF-8"); /*사용년월*/
    }

    private String getWeatherInfoUrl(String url, String year, String month) throws Exception {
        return url +
                "?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + weatherRetrieveKey + /*Service Key*/
                "&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(1), "UTF-8") + /*시군구코드*/
                "&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(1), "UTF-8") + /*법정동코드*/
                "&" + URLEncoder.encode("year", "UTF-8") + "=" + URLEncoder.encode(year, "UTF-8") + /*번*/
                "&" + URLEncoder.encode("month", "UTF-8") + "=" + URLEncoder.encode(month, "UTF-8") + /*지*/
                "&" + URLEncoder.encode("dataType", "UTF-8") + "=" + URLEncoder.encode("XML", "UTF-8"); /*사용년월*/
    }

    private AptEnergyResponse getPastEnergyResponse(String kaptCode, String date) {
        date = parseDate(date);

        Optional<AptEnergyResponse> aptEnergyResponse = aptEnergyRepository.findByKaptCodeAndDate(kaptCode, date);
        return aptEnergyResponse.orElse(null);
    }

    private String parseDate(String date) {
        return Integer.parseInt(date.substring(0, 4)) - 1 + date.substring(4);
    }

    private double computeReduction(long nowUsage, long pastUsage) {
        return (double) (pastUsage - nowUsage) / (double) pastUsage * 100;
    }

    private int computePoint(double reductionRate, int[] insentive) {
        if (reductionRate < 5.0) return 0;
        else if (5.0 <= reductionRate && reductionRate < 10.0) return insentive[0];
        else if (10.0 <= reductionRate && reductionRate < 15.0) return insentive[1];
        else return insentive[2];
    }
}
