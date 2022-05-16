package capstoneDesign.carbonTracker.apartment.service;

import capstoneDesign.carbonTracker.apartment.dto.AptEnergyRequest;
import capstoneDesign.carbonTracker.apartment.dto.AptListRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class ApartmentService {

    @Value("${aptListUrl}")
    private String aptListUrl;

    @Value("${aptListKey}")
    private String aptListKey;

    @Value("${aptEnergyUrl}")
    private String aptEnergyUrl;

    @Value("${aptEnergyKey}")
    private String aptEnergyKey;

    @Value("${aptBasicInfoUrl}")
    private String aptBasicInfoUrl;

    @Value("${aptBasicInfoKey}")
    private String aptBasicInfoKey;

    @Value("${electUsageUrl}")
    private String electUsageUrl;

    @Value("${gasUsageUrl}")
    private String gasUsageUrl;

    @Value("${gasUsageKey}")
    private String gasUsageKey;

    private final Map<String, String> sigunguCodeMap = new HashMap<>();

    // 주소 결과를 얻지 못한 경우 check
    private int noneCount = 0;
    private final KafkaTemplate<String, JSONObject> kafkaTemplate;

    public String aptLists(AptListRequest aptListRequest) throws Exception {
        String reqBuilder = aptListUrl +
                "?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + aptListKey + /*Service Key*/
                "&" + URLEncoder.encode("sidoCode", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(aptListRequest.getCode()), "UTF-8") + /*시도코드*/
                "&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(aptListRequest.getPageNum()), "UTF-8") + /*페이지번호*/
                "&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(aptListRequest.getCount()), "UTF-8"); /*목록 건수*/
        String attributeList = "";

        return callApi(reqBuilder, "aptLists", "date", attributeList);
    }

    public String aptEnergy(AptEnergyRequest aptEnergyRequest) throws Exception {
        String reqBuilder = aptEnergyUrl +
                "?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + aptEnergyKey + /*Service Key*/
                "&" + URLEncoder.encode("kaptCode", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(aptEnergyRequest.getCode()), "UTF-8") + /*단지코드*/
                "&" + URLEncoder.encode("reqDate", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(aptEnergyRequest.getDate()), "UTF-8"); /*발생년월*/
        String attributeList = "kaptCode,helect,hgas,hheat,hwaterCool,reqDate\n";

        return callApi(reqBuilder, "aptEnergy", aptEnergyRequest.getDate(), attributeList);
    }

    public String aptEnergyAll(AptEnergyRequest aptEnergyRequest) throws Exception {
        String topic = "energy";
        String[] date = {"202001","202002","202003","202004","202005","202006","202007","202008","202009","202010","202111","202112","202101","202102","202103","202104","202105","202106","202107","202108","202109","202110","202111","202112"};
        // 반환할 결과 JSON 배열
        JSONArray resultArray = new JSONArray();
        initSigunguCodeMap();

        int i = 0;
        while(i < date.length) {
            // 특정 단지 코드에 대해 202001 ~ 202112까지의 에너지 사용량을 구하는 API
            String urlBuilder = aptEnergyUrl +
                    "?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + aptEnergyKey + /*Service Key*/
                    "&" + URLEncoder.encode("kaptCode", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(aptEnergyRequest.getCode()), "UTF-8") + /*단지코드*/
                    "&" + URLEncoder.encode("reqDate", "UTF-8") + "=" + URLEncoder.encode(date[i], "UTF-8"); /*발생년월*/

            // JSON 배열 반환 형태 생성
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("kaptCode", String.valueOf(aptEnergyRequest.getCode()));
            jsonObject.put("date", date[i]);
            // parsing 결과 jsonObject에 추가하기 위해 파라미터로 보내고, 반환 받음
            jsonObject = getEnergyUsage(urlBuilder, jsonObject);

            // TODO ES로부터 단지코드를 이용해 도로명주소와 법정동코드를 조회해야 함
            String doroJuso = "대구광역시 중구 동인동1가 33-1 동인시티타운";
            String bjdongCode = "10100";

            String[] tokens = doroJuso.split(" ");

            String sigunguCode = sigunguCodeMap.get(tokens[1]);
            String[] tmp = tokens[3].split("-");
            String bun = "0".repeat(4 - tmp[0].length()) + tmp[0];
            String ji = (tmp.length == 1 ? "0000" : "0".repeat(4 - tmp[1].length()) + tmp[1]);

            log.info("시군구코드 : {}, 법정동코드: {}, 번: {}, 지: {}", sigunguCode, bjdongCode, bun, ji);

            // 국토교통부_건물에너지정보_서비스 사용
            String gasUrlBuilder = gasUsageUrl +
                    "?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + gasUsageKey + /*Service Key*/
                    "&" + URLEncoder.encode("sigunguCd", "UTF-8") + "=" + URLEncoder.encode(sigunguCode, "UTF-8") + /*시군구코드*/
                    "&" + URLEncoder.encode("bjdongCd", "UTF-8") + "=" + URLEncoder.encode(bjdongCode, "UTF-8") + /*법정동코드*/
                    "&" + URLEncoder.encode("bun", "UTF-8") + "=" + URLEncoder.encode(bun, "UTF-8") + /*번*/
                    "&" + URLEncoder.encode("ji", "UTF-8") + "=" + URLEncoder.encode(ji, "UTF-8") + /*지*/
                    "&" + URLEncoder.encode("useYm", "UTF-8") + "=" + URLEncoder.encode(date[i], "UTF-8"); /*사용년월*/

            jsonObject = getOneUsage(gasUrlBuilder, jsonObject, "hgas");

            if (jsonObject.get(("helect")).equals(0)) {
                String electUrlBuilder = electUsageUrl +
                        "?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + gasUsageKey + /*Service Key*/
                        "&" + URLEncoder.encode("sigunguCd", "UTF-8") + "=" + URLEncoder.encode(sigunguCode, "UTF-8") + /*시군구코드*/
                        "&" + URLEncoder.encode("bjdongCd", "UTF-8") + "=" + URLEncoder.encode(bjdongCode, "UTF-8") + /*법정동코드*/
                        "&" + URLEncoder.encode("bun", "UTF-8") + "=" + URLEncoder.encode(bun, "UTF-8") + /*번*/
                        "&" + URLEncoder.encode("ji", "UTF-8") + "=" + URLEncoder.encode(ji, "UTF-8") + /*지*/
                        "&" + URLEncoder.encode("useYm", "UTF-8") + "=" + URLEncoder.encode(date[i], "UTF-8"); /*사용년월*/

                jsonObject = getOneUsage(electUrlBuilder, jsonObject, "helect");
            }

            // 탄소 사용량 계산 및 추가
            int helect = (int) (Double.parseDouble(Objects.toString(jsonObject.get("helect"))) * 0.4663);
            int hgas = (int) (Double.parseDouble(Objects.toString(jsonObject.get("hgas"))) * 2.22);
            int hwaterCool = (int) (Double.parseDouble(Objects.toString(jsonObject.get("hwaterCool"))) * 0.3332);

            jsonObject.put("carbonEnergy", helect + hgas + hwaterCool);

            // Kafka로 JSON 객체 produce
            log.info(String.format("Produce message : %s", jsonObject));
            // kafkaTemplate.send(topic, jsonObject);

            resultArray.add(jsonObject);
            i++;
        }
        return resultArray.toJSONString();
    }

    public String aptListAll() throws Exception {
        // API 호출 결과가 없을 때까지 단지를 1개씩 받아 좌표 변환을 수행
        // 결과는 일단 toDB에 append
        String topic = "apt";
        JSONArray resultArray = new JSONArray();
        int idx = 1;

        while (true) {
            // 시도 코드를 인자로, 단지 코드와 단지 명을 반환하는 API
            String reqBuilder = aptListUrl +
                    "?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + aptListKey + /*Service Key*/
                    "&" + URLEncoder.encode("sidoCode", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(27), "UTF-8") + /*시도코드*/
                    "&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(idx), "UTF-8") + /*페이지번호*/
                    "&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(1), "UTF-8"); /*목록 건수*/

            String[] toDB1 = getAptCodeAndName(reqBuilder);
            // 호출 결과가 없는 경우(단지 코드가 없음)
            if (toDB1[0].equals("X")) break;
            // test용 조건문
            if (idx > 10) break;

            // 단지 코드를 이용해 도로명 주소와 법정동 주소를 반환하는 API
            String reqBuilder2 = aptBasicInfoUrl +
                    "?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + aptBasicInfoKey + /*Key*/
                    "&" + URLEncoder.encode("kaptCode", "UTF-8") + "=" + URLEncoder.encode(toDB1[0], "UTF-8"); /*단지코드*/

            // getDoroJuso의 반환 값은 도로명 주소와 법정동 주소
            String[] toDB2 = getDoroJuso(reqBuilder2);

            // JSON 반환 객체 생성
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("단지코드", toDB1[0]);
            jsonObject.put("단지명", toDB1[1]);
            jsonObject.put("도로명주소", toDB2[0]);
            jsonObject.put("법정동주소", toDB2[1]);
            jsonObject.put("법정동코드", toDB2[2]);

            // Kafka로 JSON 객체 produce
            log.info(String.format("Produce message : %s", jsonObject));
            kafkaTemplate.send(topic, jsonObject);

            resultArray.add(jsonObject);

//            // 지도 좌표 API 사용 코드
//            String reqBuilder2 = vWorldUrl +
//                    "?" + URLEncoder.encode("key", "UTF-8") + "=" + vWorldKey + /*Key*/
//                    "&" + URLEncoder.encode("service", "UTF-8") + "=" + URLEncoder.encode("search", "UTF-8") +
//                    "&" + URLEncoder.encode("request", "UTF-8") + "=" + URLEncoder.encode("search", "UTF-8") +
//                    "&" + URLEncoder.encode("version", "UTF-8") + "=" + URLEncoder.encode("2.0", "UTF-8") +
//                    "&" + URLEncoder.encode("crs", "UTF-8") + "=" + URLEncoder.encode("EPSG:4326", "UTF-8") +
//                    "&" + URLEncoder.encode("size", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8") +
//                    "&" + URLEncoder.encode("page", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8") +
//                    "&" + URLEncoder.encode("type", "UTF-8") + "=" + URLEncoder.encode("place", "UTF-8") +
//                    "&" + URLEncoder.encode("format", "UTF-8") + "=" + URLEncoder.encode("json", "UTF-8") +
//                    "&" + URLEncoder.encode("errorformat", "UTF-8") + "=" + URLEncoder.encode("json", "UTF-8") +
//                    "&" + URLEncoder.encode("query", "UTF-8") + "=" + URLEncoder.encode(toDB1[1], "UTF-8"); /*목록 건수*/
//
//            // getCoordinate의 반환 값은 도로명 주소, 지번 주소, x,y좌표
//            String[] toDB2 = getCoordinate(reqBuilder2);

            idx++;
        }
        log.info(String.valueOf(resultArray));
        log.info("좌표를 얻지 못한 주소: {}", noneCount);

        // TODO toDB는 DB에 추가하기 위한 형태로 변환이 필요, 1 row = (단지 명, 단지 코드(ID), 도로명 주소, 법정동 주소, 법정동 코드)
        return resultArray.toJSONString();
    }

    // private init methods

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

    // private detail service methods

    private JSONObject getOneUsage(String reqBuilder, JSONObject jsonObject, String type) throws Exception {
        Element eElement = initElement(initDocument(reqBuilder));

        // 가스 사용량 태그 값 추출
        String tmp = getTagValue("useQty", eElement);
        int useQty;
        if (tmp == null) useQty = 0;
        else {
            useQty = (int) Double.parseDouble(tmp);
        }
        // 각각 사용량 배열 형태가 아닌 key: value 형태로 바로 저장
        jsonObject.put(type, useQty);

        log.info("{} 사용량: {}", Objects.equals(type, "hgas") ? "가스" : "전기", useQty);

        return jsonObject;
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

    private String[] getDoroJuso(String reqBuilder) throws Exception {
        Element eElement = initElement(initDocument(reqBuilder));

        String kaptAddr = getTagValue("kaptAddr", eElement);
        String doroJuso = getTagValue("doroJuso", eElement);
        String bjdCode = getTagValue("bjdCode", eElement);

        log.info("단지의 법정동주소 : {}, 도로명주소 : {}, 법정동코드: {}", kaptAddr, doroJuso, bjdCode);

        return new String[] {kaptAddr, doroJuso, bjdCode};
    }

    private String[] getCoordinate(String reqBuilder) throws Exception {
        HttpURLConnection conn = initConnection(reqBuilder);
        BufferedReader br = initBufferedReader(conn);

        // log.info(String.valueOf(result));
        JSONParser parser = new JSONParser();
        JSONObject objData = (JSONObject) parser.parse(br);
        JSONObject response = (JSONObject) objData.get("response");
        // log.info(String.valueOf(response));
        JSONObject result = (JSONObject) response.get("result");
        // 지도 좌표 API로 좌표를 찾을 수 없는 경우
        if (result == null) {
            noneCount++;
            return new String[] {"null"};
        }
        // log.info(String.valueOf(result));
        JSONArray items = (JSONArray) result.get("items");
        // log.info(String.valueOf(items));
        JSONObject tmp = (JSONObject) items.get(0);
        // log.info(String.valueOf(tmp));
        JSONObject address = (JSONObject) tmp.get("address");
        // log.info(String.valueOf(address));

        String parcel = (String) address.get("parcel");
        // log.info(parcel);
        String road = (String) address.get("road");
        // log.info(road);

        JSONObject point = (JSONObject) tmp.get("point");
        String y = (String) point.get("y");
        // log.info(y);
        String x = (String) point.get("x");
        // log.info(x);

        br.close();
        conn.disconnect();

        return new String[] {road, parcel, y, x};
    }

    private String[] getAptCodeAndName(String reqBuilder) throws Exception {
        Element eElement = initElement(initDocument(reqBuilder));

        int thisCount = Integer.parseInt(Objects.requireNonNull(getTagValue("pageNo", eElement)));
        int totalCount = Integer.parseInt(Objects.requireNonNull(getTagValue("totalCount", eElement)));

        // API 호출 결과가 없는 경우
        if (thisCount > totalCount) return new String[] {"X"};

        // API 호출 결과가 존재, 필요한 값을 추출
        String[] result = new String[2];
        result[0] = getTagValue("kaptCode", eElement);
        result[1] = getTagValue("kaptName", eElement);
        // result[2] = getTagValue("bjdCode", eElement);

        log.info("단지의 단지 코드 : {}, 단지 명 : {}", result[0], result[1]);

        return result;
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

        // xmlToCsv(sb.toString(), apiName, date, attributeList);

        return sb.toString();
    }

//    private void xmlToCsv(String xml, String apiName, String date, String attributeList) throws Exception{
//        File stylesheet = new File("src/main/resources/data/" + apiName + "Style.xsl");
//
//        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//        DocumentBuilder builder = factory.newDocumentBuilder();
//        Document document = builder.parse(new InputSource(new StringReader(xml)));
//
//        StreamSource stylesource = new StreamSource(stylesheet);
//        Transformer transformer = TransformerFactory.newInstance()
//                .newTransformer(stylesource);
//        Source source = new DOMSource(document);
//
//        File f = new File("src/main/resources/data/" + apiName + ".csv");
//        BufferedWriter bw;
//
//        if (!f.exists()) {
//            bw = new BufferedWriter(new FileWriter(f));
//            bw.write(attributeList);
//            bw.close();
//        }
//        bw = new BufferedWriter(new FileWriter(f, true));
//        bw.append(date).append(",");
//        bw.close();
//
//        Result outputTarget = new StreamResult(new FileOutputStream("src/main/resources/data/" + apiName + ".csv", true));
//        transformer.transform(source, outputTarget);
//    }
}
