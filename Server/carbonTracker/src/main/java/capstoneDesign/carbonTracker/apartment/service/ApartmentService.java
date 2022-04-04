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

        return callApi(urlBuilder, "aptLists");
    }

    public String aptEnergy(AptEnergyRequest aptEnergyRequest) throws Exception {
        String urlBuilder = aptEnergyUrl +
                "?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + aptEnergyKey + /*Service Key*/
                "&" + URLEncoder.encode("kaptCode", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(aptEnergyRequest.getCode()), "UTF-8") + /*단지코드*/
                "&" + URLEncoder.encode("reqDate", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(aptEnergyRequest.getDate()), "UTF-8"); /*발생년월*/

        return callApi(urlBuilder, "aptEnergy");
    }

    private String callApi(String urlBuilder, String apiName) throws Exception {
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

        xmlToCsv(sb.toString(), apiName);

        return sb.toString();
    }

    private void xmlToCsv(String xml, String apiName) throws Exception{
        File stylesheet = new File("src/main/resources/data/" + apiName + "Style.xsl");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xml)));

        StreamSource stylesource = new StreamSource(stylesheet);
        Transformer transformer = TransformerFactory.newInstance()
                .newTransformer(stylesource);
        Source source = new DOMSource(document);
        Result outputTarget = new StreamResult(new File("src/main/resources/data/" + apiName + ".csv"));
        transformer.transform(source, outputTarget);
    }
}
