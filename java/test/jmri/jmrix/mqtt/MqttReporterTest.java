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
        tag = InstanceManager.getDefault(IdTagManager.class).provideIdTag("1234");
        return tag;
    }


    // check combination of MQTT and internal reporters
    @Test
    public void testTwoReporters() {
        // create an Internal Reporter
        var rm = InstanceManager.getDefault(ReporterManager.class);
        var ir = rm.provideReporter("IR1");
        
        // send a report to the MQTT reporter
        ((MqttReporter)r).notifyMqttMessage("track/reporter/1", "1234 random text");
        
        // send a report to the internal ReporterManager
        ir.setReport("1234");
        
        // check results
        Assert.assertEquals("Initial Internal Current Report OK", "1234", ir.getCurrentReport());
        Assert.assertEquals("Initial Internal Last Report OK", "1234", ir.getLastReport());
        Assert.assertEquals("Initial MQTT Current Report OK", tag, r.getCurrentReport());
        Assert.assertEquals("Initial MQTT Last Report OK", tag, r.getLastReport());
        
        // clear the internal ReporterManager's report
        ir.setReport(null);
        
        // check results
        Assert.assertEquals("Updated Internal Current Report OK", null, ir.getCurrentReport());
        Assert.assertEquals("Updated Internal Last Report OK", "1234", ir.getLastReport());
        Assert.assertEquals("Updated MQTT Current Report OK", tag, r.getCurrentReport());
        Assert.assertEquals("Updated MQTT Last Report OK", tag, r.getLastReport());
        
    }
    
    MqttAdapterScaffold a = null;
    IdTag tag;
    
    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        // prepare an interface
        a = new MqttAdapterScaffold(true);
        r = new MqttReporter(a, "MR1", "track/reporter/1");
        
        generateObjectToReport();
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
