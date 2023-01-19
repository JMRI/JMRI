package jmri.jmrix.mqtt;

import jmri.*;
import jmri.util.*;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Bob Jacobsen Coyright (C) 2023
 */
public class MqttReporterTest extends jmri.implementation.AbstractReporterTestBase {

    // concrete classes should generate an appropriate report.
    @Override
    protected Object generateObjectToReport() {
        return InstanceManager.getDefault(IdTagManager.class).provideIdTag("123");
    }

    MqttAdapter a;
    String saveTopic;
    byte[] savePayload;

    @Override
    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        // prepare an interface
        saveTopic = null;
        savePayload = null;
        a = new MqttAdapter(){
                @Override
                public void publish(String topic, byte[] payload) {
                    saveTopic = topic;
                    savePayload = payload;
                }
            };
        r = new MqttReporter(a, "MR1", "track/reporter/1");
        JUnitAppender.assertWarnMessage("Trying to subscribe before connect/configure is done");
    }

    @Override
    @AfterEach
    public void tearDown() {
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).dispose();
        r.dispose();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MqttReporterTest.class);

}
