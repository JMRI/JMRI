package jmri.server.json.turnout;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.DataOutputStream;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import jmri.server.json.JSON;
import jmri.server.json.JsonMockConnection;

public class JsonTurnoutServiceFactoryTest {

    @Test
    public void testConstants() {
        assertThat(JsonTurnoutServiceFactory.TURNOUT).isEqualTo(JsonTurnout.TURNOUT);
        assertThat(JsonTurnoutServiceFactory.TURNOUTS).isEqualTo(JsonTurnout.TURNOUTS);
    }

    @Test
    public void testGetHttpService() {
        assertThat(new JsonTurnoutServiceFactory().getHttpService(new ObjectMapper(), JSON.V5)).isNotNull();
    }

    @Test
    public void testGetSocketService() {
        assertThat(new JsonTurnoutServiceFactory().getSocketService(new JsonMockConnection((DataOutputStream) null), JSON.V5)).isNotNull();
    }
}
