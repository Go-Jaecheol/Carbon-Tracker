package capstoneDesign.carbonTracker.service;

import capstoneDesign.carbonTracker.dto.AptEnergyRequest;
import capstoneDesign.carbonTracker.dto.AptListRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

@Service
public class AptService {

    @Value("${aptListUrl}")
    private String aptListUrl;

    @Value("${aptListKey}")
    private String aptListKey;

    @Value("${aptEnergyUrl}")
    private String aptEnergyUrl;

    @Value("${aptEnergyKey}")
    private String aptEnergyKey;

    public String aptLists(AptListRequest aptListRequest) throws IOException {
        String urlBuilder = aptListUrl +
                "?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + aptListKey + /*Service Key*/
                "&" + URLEncoder.encode("sidoCode", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(aptListRequest.code), "UTF-8") + /*시도코드*/
                "&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(aptListRequest.pageNum), "UTF-8") + /*페이지번호*/
                "&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(aptListRequest.count), "UTF-8"); /*목록 건수*/

        return callApi(urlBuilder);
    }

    public String aptEnergy(AptEnergyRequest aptEnergyRequest) throws IOException {
        String urlBuilder = aptEnergyUrl +
                "?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + aptEnergyKey + /*Service Key*/
                "&" + URLEncoder.encode("kaptCode", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(aptEnergyRequest.code), "UTF-8") + /*단지코드*/
                "&" + URLEncoder.encode("reqDate", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(aptEnergyRequest.date), "UTF-8"); /*발생년월*/

        return callApi(urlBuilder);
    }

    private String callApi(String urlBuilder) throws IOException {
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

        return sb.toString();
    }
}
