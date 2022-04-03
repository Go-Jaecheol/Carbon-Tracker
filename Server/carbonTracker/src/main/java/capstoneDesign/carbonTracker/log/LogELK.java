package capstoneDesign.carbonTracker.log;

import lombok.Setter;

@Setter
public class LogELK {
    String timestamp;
    String hostname;
    String hostIp;
    String clientIp;
    String clientUrl;
    String callFunction;
    String type;
    String parameter;
}