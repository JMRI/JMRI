package jmri.jmrix.marklin.configurexml;

import java.util.ArrayList;
import jmri.InstanceManager;
import jmri.Light;
import jmri.implementation.DefaultLightControl;
import jmri.jmrix.marklin.MarklinLightManager;
import jmri.jmrix.marklin.MarklinSystemConnectionMemo;
import jmri.jmrix.marklin.MarklinTrafficControlScaffold;
import jmri.util.JUnitUtil;
import org.jdom2.Element;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Round-trip tests for Marklin lights and their controls.
 */
public class MarklinLightManagerXmlTest {

    private MarklinSystemConnectionMemo memo;
    private MarklinTrafficControlScaffold tc;
    private MarklinLightManager manager;

    @Test
    public void testRoundTrip() {
        String[] names = {"ML45", "MLDCC46", "MLMM123", "MLCAN7168"};
        for (String name : names) {
            Light light = manager.newLight(name, "user " + name);
            light.setComment("comment " + name);
        }
        DefaultLightControl control = new DefaultLightControl(manager.getBySystemName("MLMM123"));
        control.setControlType(Light.FAST_CLOCK_CONTROL);
        control.setFastClockControlSchedule(8, 15, 22, 30);
        manager.getBySystemName("MLMM123").addLightControl(control);

        MarklinLightManagerXml xml = new MarklinLightManagerXml();
        Element stored = xml.store(manager);
        assertNotNull(stored);
        assertEquals(MarklinLightManagerXml.class.getName(), stored.getAttributeValue("class"));
        for (Light light : new ArrayList<>(manager.getNamedBeanSet())) {
            manager.deregister(light);
            light.dispose();
        }
        assertTrue(xml.load(stored, null));
        for (String name : names) {
            Light light = manager.getBySystemName(name);
            assertNotNull(light);
            assertEquals("user " + name, light.getUserName());
            assertEquals("comment " + name, light.getComment());
        }
        assertEquals(1, manager.getBySystemName("MLMM123").getLightControlList().size());
        assertEquals(Light.FAST_CLOCK_CONTROL,
                manager.getBySystemName("MLMM123").getLightControlList().get(0).getControlType());
        assertEquals(8 * 60 + 15,
                manager.getBySystemName("MLMM123").getLightControlList().get(0).getFastClockOnCombined());
        assertEquals(22 * 60 + 30,
                manager.getBySystemName("MLMM123").getLightControlList().get(0).getFastClockOffCombined());
        assertFalse(tc.getSentMessages().isEmpty(), "loading activates the saved fast-clock control");
        assertEquals(0x30, tc.getSentMessages().get(0).getElement(7));
        assertEquals(0x7A, tc.getSentMessages().get(0).getElement(8));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tc = new MarklinTrafficControlScaffold();
        memo = new MarklinSystemConnectionMemo(tc);
        manager = new MarklinLightManager(memo);
        InstanceManager.setLightManager(manager);
    }

    @AfterEach
    public void tearDown() {
        manager.getNamedBeanSet().forEach(Light::dispose);
        manager.dispose();
        memo.dispose();
        tc.dispose();
        JUnitUtil.tearDown();
    }
}
