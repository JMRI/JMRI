package jmri.jmrix.mqtt;

import jmri.*;
import jmri.util.*;

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

    MqttAdapterScaffold a = null;

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        // prepare an interface
        a = new MqttAdapterScaffold(true);
        r = new MqttReporter(a, "MR1", "track/reporter/1");
    }

    @Override
    @AfterEach
    public void tearDown() {
        InstanceManager.getDefault(IdTagManager.class).dispose();
        r.dispose();
        a.dispose();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MqttReporterTest.class);

}
