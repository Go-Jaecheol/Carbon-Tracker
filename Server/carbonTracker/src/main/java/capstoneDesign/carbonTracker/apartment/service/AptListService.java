package capstoneDesign.carbonTracker.apartment.service;

import capstoneDesign.carbonTracker.apartment.dto.AptListRequest;
import capstoneDesign.carbonTracker.apartment.dto.AptListResponse;
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
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class AptListService {

    @Value("${aptListUrl}")
    private String aptListUrl;

    @Value("${aptListKey}")
    private String aptListKey;

    @Value("${aptBasicInfoUrl}")
    private String aptBasicInfoUrl;

    @Value("${aptBasicInfoKey}")
    private String aptBasicInfoKey;

    // 주소 결과를 얻지 못한 경우 check
    private int noneCount = 0;
    private final KafkaTemplate<String, JSONObject> kafkaTemplate;
    private final AptListRepository aptListRepository;

    public String aptLists(AptListRequest aptListRequest) throws Exception {
        log.info("aptLists(), 시도코드: {}", aptListRequest.getCode());
        String reqBuilder = aptListUrl +
                "?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + aptListKey + /*Service Key*/
                "&" + URLEncoder.encode("sidoCode", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(aptListRequest.getCode()), "UTF-8") + /*시도코드*/
                "&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(aptListRequest.getPageNum()), "UTF-8") + /*페이지번호*/
                "&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(aptListRequest.getCount()), "UTF-8"); /*목록 건수*/
        String attributeList = "";

//        return "";

         return callApi(reqBuilder, "aptLists", "date", attributeList);
    }

    public String aptListUpdate() throws Exception {
        log.info("aptListUpdate()");
        // API 호출 결과가 없을 때까지 단지를 1개씩 받아 좌표 변환을 수행
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
            // if (idx > 5) break;

            // 단지 코드를 이용해 도로명 주소와 법정동 주소를 반환하는 API
            String reqBuilder2 = aptBasicInfoUrl +
                    "?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + aptBasicInfoKey + /*Key*/
                    "&" + URLEncoder.encode("kaptCode", "UTF-8") + "=" + URLEncoder.encode(toDB1[0], "UTF-8"); /*단지코드*/

            // getDoroJuso의 반환 값은 도로명 주소와 법정동 주소
            String[] toDB2 = getDoroJuso(reqBuilder2);

            // JSON 반환 객체 생성
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("kaptCode", toDB1[0]);
            jsonObject.put("kaptName", toDB1[1]);
            jsonObject.put("bjdJuso", toDB2[0]);
            jsonObject.put("doroJuso", toDB2[1]);
            jsonObject.put("bjdCode", toDB2[2]);

            // Kafka로 JSON 객체 produce
            log.info(String.format("Produce message : %s", jsonObject));
            kafkaTemplate.send(topic, jsonObject);

            resultArray.add(jsonObject);

            idx++;
        }
        log.info(String.valueOf(resultArray));
        log.info("좌표를 얻지 못한 주소: {}", noneCount);

        return resultArray.toJSONString();
    }

    public List<AptListResponse> aptListAll() throws Exception {
        log.info("aptListAll()");
        return aptListRepository.findAll().getContent();
    }

    // private init methods

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

    private String[] getDoroJuso(String reqBuilder) throws Exception {
        Element eElement = initElement(initDocument(reqBuilder));

        String kaptAddr = getTagValue("kaptAddr", eElement);
        String doroJuso = getTagValue("doroJuso", eElement);
        String bjdCode = getTagValue("bjdCode", eElement);

        log.info("단지의 법정동주소 : {}, 도로명주소 : {}, 법정동코드: {}", kaptAddr, doroJuso, bjdCode);

        return new String[] {kaptAddr, doroJuso, bjdCode};
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

        return sb.toString();
    }
}
