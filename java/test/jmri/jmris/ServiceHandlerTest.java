package jmri.jmris;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the jmri.jmris.ServiceHandler class 
 *
 * @author Paul Bender
 */
public class ServiceHandlerTest {

    @Test
    public void testCtorDefault() {
        ServiceHandler a = new ServiceHandler();
        assertThat(a).isNotNull();
    }

    @Test
    public void testSetAndGetPowerServer(){
        AbstractPowerServer ps = new AbstractPowerServer(){
            @Override
            public void sendStatus(int Status) {}
            @Override
            public void sendErrorStatus() {}
            @Override
            public void parseStatus(String statusString) {}
        };
        ServiceHandler a = new ServiceHandler();
        // set the value
        a.setPowerServer(ps);
        // make sure we can retrieve it.
        assertThat(a.getPowerServer()).withFailMessage("Power Server Get or Set failed").isEqualTo(ps);
    }

    @Test
    public void testSetAndGetTurnoutServer(){
        AbstractTurnoutServer ts = new AbstractTurnoutServer(){
            @Override
            public void sendStatus(String message, int Status) {}
            @Override
            public void sendErrorStatus(String status) {}
            @Override
            public void parseStatus(String statusString) {}
        };
        ServiceHandler a = new ServiceHandler();
        // set the value
        a.setTurnoutServer(ts);
        // make sure we can retrieve it.
        assertThat(a.getTurnoutServer()).withFailMessage("Turnout Server Get or Set failed").isEqualTo(ts);
   }

    @Test
    public void testSetAndGetSensorServer(){
        AbstractSensorServer ts = new AbstractSensorServer(){
            @Override
            public void sendStatus(String message, int Status) {}
            @Override
            public void sendErrorStatus(String status) {}
            @Override
            public void parseStatus(String statusString) {}
        };
        ServiceHandler a = new ServiceHandler();
        // set the value
        a.setSensorServer(ts);
        // make sure we can retrieve it.
        assertThat(a.getSensorServer()).withFailMessage("Sensor Server Get or Set failed").isEqualTo(ts);

   }

    @Test
    public void testSetAndGetLightServer(){
        AbstractLightServer ts = new AbstractLightServer(){
            @Override
            public void sendStatus(String lightName, int Status) {}
            @Override
            public void sendErrorStatus(String lightName) {}
            @Override
            public void parseStatus(String statusString) {}
        };
        ServiceHandler a = new ServiceHandler();
        // set the value
        a.setLightServer(ts);
        // make sure we can retrieve it.
        assertThat(a.getLightServer()).withFailMessage("Light Server Get or Set failed").isEqualTo(ts);
    }

    @Test
    public void testSetAndGetProgrammerServer(){
        AbstractProgrammerServer ts = new AbstractProgrammerServer(){
            @Override
            public void sendStatus(int CV, int value, int status) {}
            @Override
            public void sendNotAvailableStatus() {}
            @Override
            public void parseRequest(String statusString) {}
        };
        ServiceHandler a = new ServiceHandler();
        // set the value
        a.setProgrammerServer(ts);
        // make sure we can retrieve it.
        assertThat(a.getProgrammerServer()).withFailMessage("Programmer Server Get or Set failed").isEqualTo(ts);
    }

    @Test
    public void testSetAndGetTimeServer(){
        AbstractTimeServer ts = new AbstractTimeServer(){
            @Override
            public void sendTime() {}
            @Override
            public void sendRate() {}
            @Override
            public void sendStatus() {}
            @Override
            public void sendErrorStatus() {}
            @Override
            public void parseTime(String status) {}
            @Override
            public void parseRate(String status) {}
        };
        ServiceHandler a = new ServiceHandler();
        // set the value
        a.setTimeServer(ts);
        // make sure we can retrieve it.
        assertThat(a.getTimeServer()).withFailMessage("Light Server Get or Set failed").isEqualTo(ts);
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();

    }

}
