package jmri.jmris;

import java.io.IOException;
import jmri.JmriException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmris.ServiceHandler class 
 *
 * @author Paul Bender
 */
public class ServiceHandlerTest {

    @Test
    public void testCtorDefault() {
        ServiceHandler a = new ServiceHandler();
        Assert.assertNotNull(a);
    }

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Before
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();

    }

}
