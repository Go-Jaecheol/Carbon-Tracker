package capstoneDesign.carbonTracker.apartment.service;

import capstoneDesign.carbonTracker.apartment.dto.AptEnergyRequest;
import capstoneDesign.carbonTracker.apartment.dto.AptListRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public String aptLists(AptListRequest aptListRequest) throws Exception {
        String urlBuilder = aptListUrl +
                "?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + aptListKey + /*Service Key*/
                "&" + URLEncoder.encode("sidoCode", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(aptListRequest.getCode()), "UTF-8") + /*시도코드*/
                "&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(aptListRequest.getPageNum()), "UTF-8") + /*페이지번호*/
                "&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(aptListRequest.getCount()), "UTF-8"); /*목록 건수*/
        String attributeList = "";

        //return callApi(urlBuilder, "aptLists", attributeList);
        return "";
    }

    public String aptEnergy(AptEnergyRequest aptEnergyRequest) throws Exception {
        String urlBuilder = aptEnergyUrl +
                "?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + aptEnergyKey + /*Service Key*/
                "&" + URLEncoder.encode("kaptCode", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(aptEnergyRequest.getCode()), "UTF-8") + /*단지코드*/
                "&" + URLEncoder.encode("reqDate", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(aptEnergyRequest.getDate()), "UTF-8"); /*발생년월*/
        String attributeList = "kaptCode,helect,hgas,hheat,hwaterCool,reqDate\n";

        return callApi(urlBuilder, "aptEnergy", aptEnergyRequest.getDate(), attributeList);
    }

    public void aptListAll() throws Exception {
        String urlBuilder = aptListUrl +
                "?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + aptListKey + /*Service Key*/
                "&" + URLEncoder.encode("sidoCode", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(27), "UTF-8") + /*시도코드*/
                "&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(1), "UTF-8") + /*페이지번호*/
                "&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(943), "UTF-8"); /*목록 건수*/
        String attributeList = "";

        //callApi(urlBuilder, "aptListAll", attributeList);
    }

//    public void aptEnergyAll() throws Exception {
//        // csv 파일에서 단지 코드 가져옴, 한 단지코드당 202101 ~ 202201까지 탄소 사용량 합계
//        String kaptCode;
//        String[] year = { "2009", "2010", "2011", "2012", "2013", "2014", "2015", "2016", "2017", "2018", "2019", "2020", "2021", "2022" };
//        String[] month = { "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12" };
//
//        File aptListAllCsv = new File("src/main/resources/data/aptListAll.csv");
//        BufferedReader br = null;
//        String line = "";
//
//        try {
//            br = new BufferedReader(new FileReader(aptListAllCsv));
//            while ((line = br.readLine()) != null) { // readLine()은 파일에서 개행된 한 줄의 데이터를 읽어온다.
//                String[] lineArr = line.split(","); // 파일의 한 줄을 ,로 나누어 배열에 저장 후 리스트로 변환한다.
//                kaptCode = lineArr[0];
//                for (int i = 0; i < year.length; i++) {
//                    for (int j = 0; j < month.length; j++) {
//                        String date = year[i] + month[j];
//                        String urlBuilder = aptEnergyUrl +
//                                "?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + aptEnergyKey + /*Service Key*/
//                                "&" + URLEncoder.encode("kaptCode", "UTF-8") + "=" + URLEncoder.encode(kaptCode, "UTF-8") + /*단지코드*/
//                                "&" + URLEncoder.encode("reqDate", "UTF-8") + "=" + URLEncoder.encode(date, "UTF-8"); /*발생년월*/
//
//                    }
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (br != null) {
//                    br.close();
//                }
//            } catch(IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    private String callApi(String urlBuilder, String apiName, String date, String attributeList) throws Exception {
        URL url = new URL(urlBuilder);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        System.out.println("Response code: " + conn.getResponseCode());

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

        xmlToCsv(sb.toString(), apiName, date, attributeList);

        return sb.toString();
    }

    private void xmlToCsv(String xml, String apiName, String date, String attributeList) throws Exception{
        File stylesheet = new File("src/main/resources/data/" + apiName + "Style.xsl");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xml)));

        StreamSource stylesource = new StreamSource(stylesheet);
        Transformer transformer = TransformerFactory.newInstance()
                .newTransformer(stylesource);
        Source source = new DOMSource(document);

        File f = new File("src/main/resources/data/" + apiName + ".csv");
        BufferedWriter bw;

        if (!f.exists()) {
            bw = new BufferedWriter(new FileWriter(f));
            bw.write(attributeList);
            bw.close();
        }
        bw = new BufferedWriter(new FileWriter(f, true));
        bw.append(date).append(",");
        bw.close();

        Result outputTarget = new StreamResult(new FileOutputStream("src/main/resources/data/" + apiName + ".csv", true));
        transformer.transform(source, outputTarget);
    }
}
