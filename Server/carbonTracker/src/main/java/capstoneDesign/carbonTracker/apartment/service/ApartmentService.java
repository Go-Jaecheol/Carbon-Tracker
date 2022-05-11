package capstoneDesign.carbonTracker.apartment.service;

import capstoneDesign.carbonTracker.apartment.dto.AptEnergyRequest;
import capstoneDesign.carbonTracker.apartment.dto.AptListRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

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

    @Value("${vWorldUrl}")
    private String vWorldUrl;

    @Value("${vWorldKey}")
    private String vWorldKey;

    // 주소 결과를 얻지 못한 경우 check
    private int noneCount = 0;
    private final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

    public String aptLists(AptListRequest aptListRequest) throws Exception {
        String urlBuilder = aptListUrl +
                "?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + aptListKey + /*Service Key*/
                "&" + URLEncoder.encode("sidoCode", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(aptListRequest.getCode()), "UTF-8") + /*시도코드*/
                "&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(aptListRequest.getPageNum()), "UTF-8") + /*페이지번호*/
                "&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(aptListRequest.getCount()), "UTF-8"); /*목록 건수*/
        String attributeList = "";

        return callApi(urlBuilder, "aptLists", "date", attributeList);
    }

    public String aptEnergy(AptEnergyRequest aptEnergyRequest) throws Exception {
        String urlBuilder = aptEnergyUrl +
                "?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + aptEnergyKey + /*Service Key*/
                "&" + URLEncoder.encode("kaptCode", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(aptEnergyRequest.getCode()), "UTF-8") + /*단지코드*/
                "&" + URLEncoder.encode("reqDate", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(aptEnergyRequest.getDate()), "UTF-8"); /*발생년월*/
        String attributeList = "kaptCode,helect,hgas,hheat,hwaterCool,reqDate\n";

        return callApi(urlBuilder, "aptEnergy", aptEnergyRequest.getDate(), attributeList);
    }

    public String aptEnergyAll(AptEnergyRequest aptEnergyRequest) throws Exception {
        String[] date = {"202001","202002","202003","202004","202005","202006","202007","202008","202009","202010","202111","202112","202101","202102","202103","202104","202105","202106","202107","202108","202109","202110","202111","202112"};
        // 반환할 결과 JSON 배열
        JSONArray resultArray = new JSONArray();

        int i = 0;
        while(i < date.length) {
            // 특정 단지 코드에 대해 202001 ~ 202112까지의 에너지 사용량을 구하는 API
            String urlBuilder = aptEnergyUrl +
                    "?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + aptEnergyKey + /*Service Key*/
                    "&" + URLEncoder.encode("kaptCode", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(aptEnergyRequest.getCode()), "UTF-8") + /*단지코드*/
                    "&" + URLEncoder.encode("reqDate", "UTF-8") + "=" + URLEncoder.encode(date[i], "UTF-8"); /*발생년월*/

            String[] result = parsing(urlBuilder);

            // JSON 배열 반환 형태 생성
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("date", date[i]);

            JSONArray energyArray = new JSONArray();
            Collections.addAll(energyArray, result);
            jsonObject.put("energy", energyArray);

            resultArray.add(jsonObject);
            i++;
        }
        return resultArray.toJSONString();
    }

    private String[] parsing(String reqBuilder) throws Exception {
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(reqBuilder);
        doc.getDocumentElement().normalize();

        // 파싱할 root tag
        NodeList nList = doc.getElementsByTagName("body");
        Node nNode = nList.item(0);
        Element eElement = (Element) nNode;

        // 전기, 가스, 수도 사용량 태그 값 추출
        String helect = getTagValue("helect", eElement);
        String hgas = getTagValue("hgas", eElement);
        String hwaterCool = getTagValue("hwaterCool", eElement);

        log.info("전기 사용량: {}, 가스 사용량: {}, 수도 사용량: {}", helect, hgas, hwaterCool);

        return new String[] {helect, hgas, hwaterCool};
    }

    public String aptListAll() throws Exception {
        // API 호출 결과가 없을 때까지 단지를 1개씩 받아 좌표 변환을 수행
        // 결과는 일단 toDB에 append
        StringBuilder toDB = new StringBuilder();
        JSONArray resultArray = new JSONArray();
        int idx = 1;

        while (true) {
            // 시도 코드를 인자로, 단지 코드와 단지 명을 반환하는 API
            String reqBuilder = aptListUrl +
                    "?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + aptListKey + /*Service Key*/
                    "&" + URLEncoder.encode("sidoCode", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(27), "UTF-8") + /*시도코드*/
                    "&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(idx), "UTF-8") + /*페이지번호*/
                    "&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(1), "UTF-8"); /*목록 건수*/

            String[] toDB1 = getApt(reqBuilder);
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
            toDB.append(Arrays.toString(toDB1));
            toDB.append(Arrays.toString(toDB2));
            toDB.append("\n");

            idx++;

            // log.info(Arrays.toString(toDB1) + Arrays.toString(toDB2));
        }
        log.info(String.valueOf(resultArray));
        log.info("좌표를 얻지 못한 주소: {}", noneCount);

        // TODO toDB는 DB에 추가하기 위한 형태로 변환이 필요, 1 row = (단지 명, 단지 코드(ID), 도로명 주소, 법정동 주소)
        return resultArray.toJSONString();
    }

    private String[] getDoroJuso(String reqBuilder) throws Exception {
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(reqBuilder);
        doc.getDocumentElement().normalize();

        // 파싱할 root tag
        NodeList nList = doc.getElementsByTagName("body");
        Node nNode = nList.item(0);
        Element eElement = (Element) nNode;

        String kaptAddr = getTagValue("kaptAddr", eElement);
        String doroJuso = getTagValue("doroJuso", eElement);

        log.info("단지의 법정동주소 : {}, 도로명주소 : {}", kaptAddr, doroJuso);

        return new String[] {kaptAddr, doroJuso};
    }

    private String[] getCoordinate(String reqBuilder) throws Exception {
        URL url = new URL(reqBuilder);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        log.info("Response code: {}", conn.getResponseCode());

        BufferedReader rd;

        if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }

        // log.info(String.valueOf(result));
        JSONParser parser = new JSONParser();
        JSONObject objData = (JSONObject) parser.parse(rd);
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

        rd.close();
        conn.disconnect();

        return new String[] {road, parcel, y, x};
    }

    private String[] getApt(String reqBuilder) throws Exception {
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(reqBuilder);
        doc.getDocumentElement().normalize();

        // 파싱할 root tag
        NodeList nList = doc.getElementsByTagName("body");
        Node nNode = nList.item(0);
        Element eElement = (Element) nNode;

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

        // 태그 값을 읽을 수 없는 경우는 해당 결과가 없다느느 의미
        if (eElement.getElementsByTagName(tag).item(0) == null) {
            noneCount++;
            return null;
        }

        NodeList nlList = eElement.getElementsByTagName(tag).item(0).getChildNodes();

        result = nlList.item(0).getTextContent();

        return result;
    }

    private String callApi(String urlBuilder, String apiName, String date, String attributeList) throws Exception {
        URL url = new URL(urlBuilder);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        log.info("Response code: {}", conn.getResponseCode());

        BufferedReader rd;

        if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }

        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }

        rd.close();
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
