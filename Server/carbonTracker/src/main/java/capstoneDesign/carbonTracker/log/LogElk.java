package capstoneDesign.carbonTracker.log;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogElk {
    private String timestamp;
    private String hostname;
    private String hostIp;
    private String clientIp;
    private String clientUrl;
    private String callFunction;
    private String type;
    private String parameter;
}