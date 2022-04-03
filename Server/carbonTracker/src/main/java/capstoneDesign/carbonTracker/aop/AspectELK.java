package capstoneDesign.carbonTracker.aop;

import capstoneDesign.carbonTracker.log.LogELK;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

@Aspect
@Component
public class AspectELK {
    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final Logger log = LoggerFactory.getLogger("ELK_LOGGER");
    private ObjectMapper objectMapper = new ObjectMapper();

    private String host = "";
    private String ip = "";
    private String clientIp = "";
    private String clientUrl = "";

    @PostConstruct
    public void init() throws UnknownHostException {
        InetAddress addr = InetAddress.getLocalHost();
        this.host = addr.getHostName();
        this.ip = addr.getHostAddress();
    }

    @Around("bean(*Controller)")
    public Object controllerAroundLogging(ProceedingJoinPoint pjp) throws Throwable {
        String timeStamp = new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Timestamp(System.currentTimeMillis()));
        HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
        this.clientIp = request.getRemoteAddr();
        this.clientUrl = request.getRequestURL().toString();
        String callFunction = pjp.getSignature().getDeclaringTypeName() + "." + pjp.getSignature().getName();

        LogELK logelk = new LogELK();
        logelk.setTimestamp(timeStamp);
        logelk.setHostname(host);
        logelk.setHostIp(ip);
        logelk.setClientIp(clientIp);
        logelk.setClientUrl(clientUrl);
        logelk.setCallFunction(callFunction);
        logelk.setType("CONTROLLER_REQ");
        logelk.setParameter(objectMapper.writeValueAsString(request.getParameterMap()));
        log.info("{}", objectMapper.writeValueAsString(logelk));

        Object result = pjp.proceed();

        timeStamp = new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Timestamp(System.currentTimeMillis()));

        logelk.setTimestamp(timeStamp);
        logelk.setType("CONTROLLER_RES");
        logelk.setParameter(objectMapper.writeValueAsString(result));
        log.info("{}", objectMapper.writeValueAsString(logelk));

        return result;
    }

    @Before("bean(*ServiceImpl)")
    public void serviceBeforeLogging(JoinPoint pjp) throws Throwable {
        String timeStamp = new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Timestamp(System.currentTimeMillis()));
        String callFunction = pjp.getSignature().getDeclaringTypeName() + "." + pjp.getSignature().getName();

        Object[] argNames = pjp.getArgs();

        LogELK logelk = new LogELK();
        logelk.setTimestamp(timeStamp);
        logelk.setHostname(host);
        logelk.setHostIp(ip);
        logelk.setClientIp(clientIp);
        logelk.setClientUrl(clientUrl);
        logelk.setCallFunction(callFunction);
        logelk.setType("SERVICE_REQ");
        logelk.setParameter(objectMapper.writeValueAsString(argNames));
        log.info("{}", objectMapper.writeValueAsString(logelk));
    }

    @AfterReturning(pointcut="bean(*ServiceImpl)", returning="retVal")
    public void serviceAfterReturningLogging(JoinPoint pjp, Object retVal) throws Throwable {
        String timeStamp = new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Timestamp(System.currentTimeMillis()));
        String callFunction = pjp.getSignature().getDeclaringTypeName() + "." + pjp.getSignature().getName();

        LogELK logelk = new LogELK();
        logelk.setTimestamp(timeStamp);
        logelk.setHostname(host);
        logelk.setHostIp(ip);
        logelk.setClientIp(clientIp);
        logelk.setClientUrl(clientUrl);
        logelk.setCallFunction(callFunction);
        logelk.setType("SERVICE_RES");
        logelk.setParameter(objectMapper.writeValueAsString(retVal));
        log.info("{}", objectMapper.writeValueAsString(logelk));
    }
}
