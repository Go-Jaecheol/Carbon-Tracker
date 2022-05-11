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
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
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

    private int noneCount = 0;
    private final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

    public String aptLists(AptListRequest aptListRequest) throws Exception {
        String urlBuilder = aptListUrl +
                "?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + aptListKey + /*Service Key*/
                "&" + URLEncoder.encode("sidoCode", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(aptListRequest.getCode()), "UTF-8") + /*시도코드*/
                "&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(aptListRequest.getPageNum()), "UTF-8") + /*페이지번호*/
                "&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(aptListRequest.getCount()), "UTF-8"); /*목록 건수*/
        String attributeList = "";

        return callApi(urlBuilder, "aptLists", "df", attributeList);
    }

    public String aptEnergy(AptEnergyRequest aptEnergyRequest) throws Exception {
        String urlBuilder = aptEnergyUrl +
                "?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + aptEnergyKey + /*Service Key*/
                "&" + URLEncoder.encode("kaptCode", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(aptEnergyRequest.getCode()), "UTF-8") + /*단지코드*/
                "&" + URLEncoder.encode("reqDate", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(aptEnergyRequest.getDate()), "UTF-8"); /*발생년월*/
        String attributeList = "kaptCode,helect,hgas,hheat,hwaterCool,reqDate\n";

        return callApi(urlBuilder, "aptEnergy", aptEnergyRequest.getDate(), attributeList);
    }

    public String aptListAll() throws Exception {
        // API 호출 결과가 없을 때까지 단지를 1개씩 받아 좌표 변환을 수행한다. 더 이상 받을 수 없는 경우 <item>이 비어있을 것
        // 결과는 일단 toDB에 append한다.
        StringBuilder toDB = new StringBuilder();
        int idx = 1;

        while (true) {
            String reqBuilder = aptListUrl +
                    "?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + aptListKey + /*Service Key*/
                    "&" + URLEncoder.encode("sidoCode", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(27), "UTF-8") + /*시도코드*/
                    "&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(idx), "UTF-8") + /*페이지번호*/
                    "&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(1), "UTF-8"); /*목록 건수*/

            String[] toDB1 = getApt(reqBuilder);
            // 호출 결과가 없는 경우(단지 코드가 없음)
            if (toDB1[0].equals("X")) break;

            String reqBuilder2 = aptBasicInfoUrl +
                    "?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + aptBasicInfoKey + /*Key*/
                    "&" + URLEncoder.encode("kaptCode", "UTF-8") + "=" + URLEncoder.encode(toDB1[0], "UTF-8"); /*단지코드*/

            // 단지코드를 API에 전달, getDoroJuso의 반환 값은 도로명주소와 법정동주소
            String[] toDB2 = getDoroJuso(reqBuilder2);

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
//
            toDB.append(Arrays.toString(toDB1));
            toDB.append(Arrays.toString(toDB2));
            toDB.append("\n");

            idx++;

            // log.info(Arrays.toString(toDB1) + Arrays.toString(toDB2));
        }
        log.info("좌표를 얻지 못한 주소: {}", noneCount);

        // TODO toDB는 DB에 추가하기 위한 형태로 변환이 필요, 1 row = (단지 명, 단지 코드(ID), 도로명 주소, 법정동 주소)
        // TODO 프론트에서 사용할 수 있도록 각각의 단지 코드에 대해 단지명, 단지코드, 도로명주소, 법정동주소를 가지는 JSON 배열로 변환
        return toDB.toString();
    }

    private String[] getDoroJuso(String reqBuilder) throws Exception {
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(reqBuilder);

        // 제일 첫번째 태그
        doc.getDocumentElement().normalize();

        // 파싱할 tag
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

        // 제일 첫번째 태그
        doc.getDocumentElement().normalize();

        // 파싱할 tag
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

    private String getTagValue(String tag, Element eElement) {
        //결과를 저장할 result 변수 선언
        String result = "";

        if (eElement.getElementsByTagName(tag).item(0) == null) {
            noneCount++;
            return null;
        }

        NodeList nlList = eElement.getElementsByTagName(tag).item(0).getChildNodes();

        result = nlList.item(0).getTextContent();

        return result;
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
