package jmri.jmris;

import java.io.IOException;
import jmri.JmriException;
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
        assertThat(a.getPowerServer()).isEqualTo(ps).withFailMessage("Power Server Get or Set failed");
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
        assertThat(a.getTurnoutServer()).isEqualTo(ts).withFailMessage("Turnout Server Get or Set failed");
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
        assertThat(a.getSensorServer()).isEqualTo(ts).withFailMessage("Sensor Server Get or Set failed");

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
        assertThat(a.getLightServer()).isEqualTo(ts).withFailMessage("Light Server Get or Set failed");
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
        assertThat(a.getProgrammerServer()).isEqualTo(ts).withFailMessage("Programmer Server Get or Set failed");
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
        assertThat(a.getTimeServer()).isEqualTo(ts).withFailMessage("Light Server Get or Set failed");
    }

    @BeforeEach
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();

    }

}
