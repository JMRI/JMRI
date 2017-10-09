package jmri.util.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import jmri.util.node.HostName;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.log4j.Layout;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

/**
 * Log4j layout that produces Logstash JSON logging version 1 input format.
 * Taken from code at https://github.com/logstash/log4j-jsonevent-layout
 *
 * @author Randall Wood Copyright 2017
 */
public class LogstashLog4jJsonLayout extends Layout {

    private static Integer version = 1;
    public static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    public static final FastDateFormat ISO_DATETIME_TIME_ZONE_FORMAT_WITH_MILLIS = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", UTC);
    public static final String ADDITIONAL_DATA_PROPERTY = "net.logstash.log4j.JSONEventLayoutV1.UserFields";

    public static String dateFormat(long timestamp) {
        return ISO_DATETIME_TIME_ZONE_FORMAT_WITH_MILLIS.format(timestamp);
    }

    private boolean locationInfo = false;
    private String customUserFields;

    private final String hostname = new HostName().getHostName();
    private String threadName;
    private long timestamp;
    private String ndc;
    private Map mdc; // log4j events return a raw Map for getProperties()
    private LocationInfo info;
    private Map<String, Object> exceptionInformation;

    private ObjectNode event;
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * The default is to generate location information in the log messages.
     */
    public LogstashLog4jJsonLayout() {
        this(true);
    }

    /**
     * Create a layout that optionally inserts location information into log
     * messages.
     *
     * @param locationInfo true to include location information in the log
     *                     messages; false otherwise
     */
    public LogstashLog4jJsonLayout(boolean locationInfo) {
        this.locationInfo = locationInfo;
    }

    @Override
    public String format(LoggingEvent loggingEvent) {
        threadName = loggingEvent.getThreadName();
        timestamp = loggingEvent.getTimeStamp();
        exceptionInformation = new HashMap<>();
        mdc = loggingEvent.getProperties();
        ndc = loggingEvent.getNDC();

        event = mapper.createObjectNode();
        String whoami = this.getClass().getSimpleName();

        // All v1 of the event format requires is "@timestamp" and "@version".
        // Every other field is arbitrary.
        event.put("@version", version);
        event.put("@timestamp", dateFormat(timestamp));

        // Extract and add fields from log4j config, if defined
        if (getUserFields() != null) {
            String userFlds = getUserFields();
            LogLog.debug("[" + whoami + "] Got user data from log4j property: " + userFlds);
            addUserFields(userFlds);
        }

        /**
         * Extract fields from system properties, if defined Note that CLI props
         * will override conflicts with log4j config
         */
        if (System.getProperty(ADDITIONAL_DATA_PROPERTY) != null) {
            if (getUserFields() != null) {
                LogLog.warn("[" + whoami + "] Loading UserFields from command-line. This will override any UserFields set in the log4j configuration file");
            }
            String userFieldsProperty = System.getProperty(ADDITIONAL_DATA_PROPERTY);
            LogLog.debug("[" + whoami + "] Got user data from system property: " + userFieldsProperty);
            addUserFields(userFieldsProperty);
        }

        /**
         * Now we start injecting our own stuff.
         */
        event.put("source_host", hostname);
        event.put("message", loggingEvent.getRenderedMessage());

        if (loggingEvent.getThrowableInformation() != null) {
            final ThrowableInformation throwableInformation = loggingEvent.getThrowableInformation();
            if (throwableInformation.getThrowable().getClass().getCanonicalName() != null) {
                exceptionInformation.put("exception_class", throwableInformation.getThrowable().getClass().getCanonicalName());
            }
            if (throwableInformation.getThrowable().getMessage() != null) {
                exceptionInformation.put("exception_message", throwableInformation.getThrowable().getMessage());
            }
            if (throwableInformation.getThrowableStrRep() != null) {
                String stackTrace = StringUtils.join(throwableInformation.getThrowableStrRep(), "\n");
                exceptionInformation.put("stacktrace", stackTrace);
            }
            event.putPOJO("exception", exceptionInformation);
        }

        if (locationInfo) {
            info = loggingEvent.getLocationInformation();
            event.put("file", info.getFileName());
            event.put("line_number", Integer.valueOf(info.getLineNumber()));
            event.put("class", info.getClassName());
            event.put("method", info.getMethodName());
        }

        event.put("logger_name", loggingEvent.getLoggerName());
        event.putPOJO("mdc", mdc);
        event.putPOJO("ndc", ndc);
        event.put("level", loggingEvent.getLevel().toString());
        event.put("thread_name", threadName);

        return event.toString() + "\n";
    }

    @Override
    public boolean ignoresThrowable() {
        return false;
    }

    /**
     * Query whether log messages include location information.
     *
     * @return true if location information is included in log messages, false
     *         otherwise.
     */
    public boolean getLocationInfo() {
        return locationInfo;
    }

    /**
     * Set whether log messages should include location information.
     *
     * @param locationInfo true if location information should be included,
     *                     false otherwise.
     */
    public void setLocationInfo(boolean locationInfo) {
        this.locationInfo = locationInfo;
    }

    public String getUserFields() {
        return customUserFields;
    }

    public void setUserFields(String userFields) {
        this.customUserFields = userFields;
    }

    @Override
    public void activateOptions() {
        // do nothing
    }

    private void addUserFields(String data) {
        if (null != data) {
            String[] pairs = data.split(",");
            for (String pair : pairs) {
                String[] userField = pair.split(":", 2);
                if (userField[0] != null) {
                    String key = userField[0];
                    String val = userField[1];
                    event.put(key, val);
                }
            }
        }
    }
}
