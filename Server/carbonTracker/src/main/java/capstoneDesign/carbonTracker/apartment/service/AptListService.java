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
import org.w3c.dom.Element;

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

    private final KafkaTemplate<String, JSONObject> kafkaTemplate;
    private final AptListRepository aptListRepository;

    private final CommonService commonService;

    public String aptLists(AptListRequest aptListRequest) throws Exception {
        log.info("aptLists(), 시도코드: {}", aptListRequest.getCode());
        String reqBuilder = aptListUrl +
                "?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + aptListKey + /*Service Key*/
                "&" + URLEncoder.encode("sidoCode", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(aptListRequest.getCode()), "UTF-8") + /*시도코드*/
                "&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(aptListRequest.getPageNum()), "UTF-8") + /*페이지번호*/
                "&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(aptListRequest.getCount()), "UTF-8"); /*목록 건수*/

         return commonService.callApi(reqBuilder);
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

            // 단지 코드를 이용해 도로명 주소와 법정동 주소를 반환하는 API
            String reqBuilder2 = aptBasicInfoUrl +
                    "?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + aptBasicInfoKey + /*Key*/
                    "&" + URLEncoder.encode("kaptCode", "UTF-8") + "=" + URLEncoder.encode(toDB1[0], "UTF-8"); /*단지코드*/

            // getDoroJuso의 반환 값은 도로명 주소, 법정동 주소, 법정동 코드, 세대 수
            String[] toDB2 = getDoroJuso(reqBuilder2);

            // JSON 반환 객체 생성
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("kaptCode", toDB1[0]);
            jsonObject.put("kaptName", toDB1[1]);
            jsonObject.put("bjdJuso", toDB2[0]);
            jsonObject.put("doroJuso", toDB2[1]);
            jsonObject.put("bjdCode", toDB2[2]);
            jsonObject.put("kaptdaCnt", toDB2[3]);

            // Kafka로 JSON 객체 produce
            log.info(String.format("Produce message : %s", jsonObject));
            kafkaTemplate.send(topic, jsonObject);

            resultArray.add(jsonObject);

            idx++;
        }
        log.info(String.valueOf(resultArray));

        return resultArray.toJSONString();
    }

    public List<AptListResponse> aptListAll() {
        log.info("aptListAll()");
        return aptListRepository.findAll().getContent();
    }

    // private detail service methods

    private String[] getDoroJuso(String reqBuilder) throws Exception {
        Element eElement = commonService.initElement(commonService.initDocument(reqBuilder));

        String kaptAddr = commonService.getTagValue("kaptAddr", eElement);
        String doroJuso = commonService.getTagValue("doroJuso", eElement);
        String bjdCode = commonService.getTagValue("bjdCode", eElement);
        String kaptdaCnt = commonService.getTagValue("kaptdaCnt", eElement);

        log.info("단지의 법정동주소 : {}, 도로명주소 : {}, 법정동코드: {}, 세대 수: {}", kaptAddr, doroJuso, bjdCode, kaptdaCnt);

        return new String[] {kaptAddr, doroJuso, bjdCode, kaptdaCnt};
    }

    private String[] getAptCodeAndName(String reqBuilder) throws Exception {
        Element eElement = commonService.initElement(commonService.initDocument(reqBuilder));

        int thisCount = Integer.parseInt(Objects.requireNonNull(commonService.getTagValue("pageNo", eElement)));
        int totalCount = Integer.parseInt(Objects.requireNonNull(commonService.getTagValue("totalCount", eElement)));

        // API 호출 결과가 없는 경우
        if (thisCount > totalCount) return new String[] {"X"};

        // API 호출 결과가 존재, 필요한 값을 추출
        String[] result = new String[2];
        result[0] = commonService.getTagValue("kaptCode", eElement);
        result[1] = commonService.getTagValue("kaptName", eElement);

        log.info("단지의 단지 코드 : {}, 단지 명 : {}", result[0], result[1]);

        return result;
    }
}
