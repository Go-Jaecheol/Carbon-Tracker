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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
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

    private int noneCount = 0;

    private final Map<String, String> sigunguCodeMap = new HashMap<>();

    private final KafkaTemplate<String, JSONObject> kafkaTemplate;
    private final AptEnergyRepository aptEnergyRepository;
    private final AptListRepository aptListRepository;

    public String aptEnergy(AptEnergyRequest aptEnergyRequest) throws Exception {
        log.info("aptEnergy(), 단지코드: {}, 발생년월: {}", aptEnergyRequest.getCode(), aptEnergyRequest.getDate());
        String reqBuilder = aptEnergyUrl +
                "?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + aptEnergyKey + /*Service Key*/
                "&" + URLEncoder.encode("kaptCode", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(aptEnergyRequest.getCode()), "UTF-8") + /*단지코드*/
                "&" + URLEncoder.encode("reqDate", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(aptEnergyRequest.getDate()), "UTF-8"); /*발생년월*/
        String attributeList = "kaptCode,helect,hgas,hheat,hwaterCool,reqDate\n";

        return callApi(reqBuilder, "aptEnergy", aptEnergyRequest.getDate(), attributeList);
    }

    public String aptEnergyAll(AptEnergyRequest aptEnergyRequest) throws Exception {
        log.info("aptEnergyAll(), 단지코드: {}, 현재년월: {}", aptEnergyRequest.getCode(), aptEnergyRequest.getDate());

        String[] date = initDates(aptEnergyRequest.getDate());
        String topic = "energy";

        // 반환할 결과 JSON 배열
        JSONArray resultArray = new JSONArray();
        initSigunguCodeMap();

        int i = 0;
        while(i < date.length) {
            String kaptCode = String.valueOf(aptEnergyRequest.getCode());
            // JSON 배열 반환 형태 생성
            JSONObject jsonObject = new JSONObject();
            // 단지코드와 발생년월로 조회
            Optional<AptEnergyResponse> aptEnergyResponse = aptEnergyRepository.findByKaptCodeAndDate(kaptCode, date[i]);
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
                        "&" + URLEncoder.encode("reqDate", "UTF-8") + "=" + URLEncoder.encode(date[i], "UTF-8"); /*발생년월*/

                jsonObject.put("kaptCode", kaptCode);
                jsonObject.put("date", date[i]);
                // parsing 결과 jsonObject에 추가하기 위해 파라미터로 보내고, 반환 받음
                jsonObject = getEnergyUsage(urlBuilder, jsonObject);

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
                String gasUrlBuilder = getOneUsageUrl(gasUsageUrl, sigunguCode, bjdongCode, bun, ji, date[i]);

                log.info(gasUrlBuilder);
                jsonObject = getGasUsage(gasUrlBuilder, jsonObject);

                if (jsonObject.get(("helect")).equals(0)) {
                    String electUrlBuilder = getOneUsageUrl(electUsageUrl, sigunguCode, bjdongCode, bun, ji, date[i]);
                    jsonObject = getElectUsage(electUrlBuilder, jsonObject);
                }

                // 탄소 사용량 계산 및 추가
                int helect = (int) (Double.parseDouble(Objects.toString(jsonObject.get("helect"))) * 0.4663);
                int hgas = (int) (Double.parseDouble(Objects.toString(jsonObject.get("hgas"))) * 2.22);
                int hwaterCool = (int) (Double.parseDouble(Objects.toString(jsonObject.get("hwaterCool"))) * 0.3332);

                jsonObject.put("carbonEnergy", helect + hgas + hwaterCool);

                // Kafka로 JSON 객체 produce
                log.info(String.format("Produce message : %s", jsonObject));
                kafkaTemplate.send(topic, jsonObject);
            }

            // TODO ES로부터 단지코드를 이용해 도로명주소와 법정동코드를 조회해야 함

            resultArray.add(jsonObject);
            i++;
        }
        return resultArray.toJSONString();
    }

    // private init methods

    private String[] initDates(String date) {
        ArrayList<String> dates = new ArrayList<>();

        int year = Integer.parseInt(date.substring(0, 4));
        int month = Integer.parseInt(date.substring(4)) - 1;

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

    private Document initDocument(String reqBuilder) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(reqBuilder);
        doc.getDocumentElement().normalize();
        return doc;
    }

    private Element initElement(Document doc) {
        NodeList nList = doc.getElementsByTagName("body");
        Node nNode = nList.item(0);
        return (Element) nNode;
    }

    private HttpURLConnection initConnection(String reqBuilder) throws Exception {
        URL url = new URL(reqBuilder);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        log.info("Response code: {}", conn.getResponseCode());

        return conn;
    }

    private BufferedReader initBufferedReader(HttpURLConnection conn) throws Exception {
        BufferedReader br;

        if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }

        return br;
    }

    // private methods

    private String getOneUsageUrl(String url, String sigunguCode, String bjdongCode, String bun, String ji, String date) throws Exception {
        return url +
                "?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + gasUsageKey + /*Service Key*/
                "&" + URLEncoder.encode("sigunguCd", "UTF-8") + "=" + URLEncoder.encode(sigunguCode, "UTF-8") + /*시군구코드*/
                "&" + URLEncoder.encode("bjdongCd", "UTF-8") + "=" + URLEncoder.encode(bjdongCode, "UTF-8") + /*법정동코드*/
                "&" + URLEncoder.encode("bun", "UTF-8") + "=" + URLEncoder.encode(bun, "UTF-8") + /*번*/
                "&" + URLEncoder.encode("ji", "UTF-8") + "=" + URLEncoder.encode(ji, "UTF-8") + /*지*/
                "&" + URLEncoder.encode("useYm", "UTF-8") + "=" + URLEncoder.encode(date, "UTF-8"); /*사용년월*/
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
        Element eElement = initElement(initDocument(reqBuilder));

        // 사용량 태그 값 추출
        return getTagValue("useQty", eElement);
    }

    private JSONObject getEnergyUsage(String reqBuilder, JSONObject jsonObject) throws Exception {
        Element eElement = initElement(initDocument(reqBuilder));

        // 전기, 수도 사용량 태그 값 추출
        String helect = getTagValue("helect", eElement);
        String hwaterCool = getTagValue("hwaterCool", eElement);

        // 각각 사용량 배열 형태가 아닌 key: value 형태로 바로 저장
        jsonObject.put("helect", Integer.parseInt(helect == null ? "0" : helect));
        jsonObject.put("hwaterCool", Integer.parseInt(hwaterCool == null ? "0" : hwaterCool));

        log.info("전기 사용량: {}, 수도 사용량: {}", helect, hwaterCool);

        return jsonObject;
    }

    private String getTagValue(String tag, Element eElement) {
        //결과를 저장할 result
        String result = "";

        // 태그 값을 읽을 수 없는 경우는 해당 결과가 없다는 의미
        if (eElement.getElementsByTagName(tag).item(0) == null) {
            noneCount++;
            return null;
        }

        NodeList nlList = eElement.getElementsByTagName(tag).item(0).getChildNodes();

        result = nlList.item(0).getTextContent();

        return result;
    }

    private String callApi(String reqBuilder, String apiName, String date, String attributeList) throws Exception {
        HttpURLConnection conn = initConnection(reqBuilder);
        BufferedReader br = initBufferedReader(conn);

        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = br.readLine()) != null) {
            sb.append(line);
        }

        br.close();
        conn.disconnect();

        return sb.toString();
    }
}
