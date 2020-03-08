package jmri.server.json.turnout;

import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jmri.util.JUnitUtil;

public class JsonTurnoutTest {

    @Test
    public void testConstants() {
        assertThat(JsonTurnout.TURNOUT).isEqualTo("turnout");
        assertThat(JsonTurnout.TURNOUTS).isEqualTo("turnouts");
        assertThat(JsonTurnout.FEEDBACK_MODE).isEqualTo("feedbackMode");
        assertThat(JsonTurnout.FEEDBACK_MODES).isEqualTo("feedbackModes");
    }

    @Test
    public void testConstructor() throws Exception {
        try {
            Constructor<JsonTurnout> constructor;
            constructor = JsonTurnout.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
            fail("Instance of JsonTurnout created");
        } catch (InvocationTargetException ex) {
            // because the constructor throws UnsupportedOperationException, and
            // that is thrown by newInstance() into an InvocationTargetException
            // we assert the exception cause is the correct class
            assertThat(ex.getCause().getClass()).isEqualTo(UnsupportedOperationException.class);
        }
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
