package jmri.jmris;

import java.io.IOException;
import jmri.JmriException;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Tests for the jmri.jmris.ServiceHandler class 
 *
 * @author Paul Bender
 */
public class ServiceHandlerTest extends TestCase {

    public void testCtorDefault() {
        ServiceHandler a = new ServiceHandler();
        Assert.assertNotNull(a);
    }

    public void testSetAndGetPowerServer(){
        AbstractPowerServer ps = new AbstractPowerServer(){
            @Override
            public void sendStatus(int Status) throws IOException{}
            @Override
            public void sendErrorStatus() throws IOException {}
            @Override
            public void parseStatus(String statusString) throws JmriException,IOException {}
        };
        ServiceHandler a = new ServiceHandler();
        // set the value
        a.setPowerServer(ps);
        // make sure we can retrieve it.
        Assert.assertEquals("Power Server Get or Set failed",ps,a.getPowerServer());
    }

    public void testSetAndGetTurnoutServer(){
        AbstractTurnoutServer ts = new AbstractTurnoutServer(){
            @Override
            public void sendStatus(String message, int Status) throws IOException{}
            @Override
            public void sendErrorStatus(String status) throws IOException {}
            @Override
            public void parseStatus(String statusString) throws JmriException,IOException {}
        };
        ServiceHandler a = new ServiceHandler();
        // set the value
        a.setTurnoutServer(ts);
        // make sure we can retrieve it.
        Assert.assertEquals("Turnout Server Get or Set failed",ts,a.getTurnoutServer());
   }

    public void testSetAndGetSensorServer(){
        AbstractSensorServer ts = new AbstractSensorServer(){
            @Override
            public void sendStatus(String message, int Status) throws IOException{}
            @Override
            public void sendErrorStatus(String status) throws IOException {}
            @Override
            public void parseStatus(String statusString) throws JmriException,IOException {}
        };
        ServiceHandler a = new ServiceHandler();
        // set the value
        a.setSensorServer(ts);
        // make sure we can retrieve it.
        Assert.assertEquals("Sensor Server Get or Set failed",ts,a.getSensorServer());

   }

    public void testSetAndGetLightServer(){
        AbstractLightServer ts = new AbstractLightServer(){
            @Override
            public void sendStatus(String lightName, int Status) throws IOException{}
            @Override
            public void sendErrorStatus(String lightName) throws IOException {}
            @Override
            public void parseStatus(String statusString) throws JmriException,IOException {}
        };
        ServiceHandler a = new ServiceHandler();
        // set the value
        a.setLightServer(ts);
        // make sure we can retrieve it.
        Assert.assertEquals("Light Server Get or Set failed",ts,a.getLightServer());
    }

    public void testSetAndGetProgrammerServer(){
        AbstractProgrammerServer ts = new AbstractProgrammerServer(){
            @Override
            public void sendStatus(int CV, int value, int status) throws IOException{}
            @Override
            public void sendNotAvailableStatus() throws IOException{}
            @Override
            public void parseRequest(String statusString) throws JmriException,IOException {}
        };
        ServiceHandler a = new ServiceHandler();
        // set the value
        a.setProgrammerServer(ts);
        // make sure we can retrieve it.
        Assert.assertEquals("Programmer Server Get or Set failed",ts,a.getProgrammerServer());
    }

    public void testSetAndGetTimeServer(){
        AbstractTimeServer ts = new AbstractTimeServer(){
            @Override
            public void sendTime() throws IOException{}
            @Override
            public void sendRate() throws IOException{}
            @Override
            public void sendStatus() throws IOException{}
            @Override
            public void sendErrorStatus() throws IOException {}
            @Override
            public void parseTime(String status) throws IOException {}
            @Override
            public void parseRate(String status) throws IOException {}
        };
        ServiceHandler a = new ServiceHandler();
        // set the value
        a.setTimeServer(ts);
        // make sure we can retrieve it.
        Assert.assertEquals("Light Server Get or Set failed",ts,a.getTimeServer());
    }



    // from here down is testing infrastructure
    public ServiceHandlerTest(String s) {
        super(s);
    }


    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {ServiceHandlerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(jmri.jmris.ServiceHandlerTest.class);

        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }

}
