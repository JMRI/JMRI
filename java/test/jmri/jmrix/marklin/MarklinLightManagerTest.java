package jmri.jmrix.marklin;

import java.util.Locale;
import jmri.JmriException;
import jmri.Light;
import jmri.Manager.NameValidity;
import jmri.NamedBean;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for protocol-prefixed and legacy Marklin light names.
 */
public class MarklinLightManagerTest extends jmri.managers.AbstractLightMgrTestBase {

    private MarklinSystemConnectionMemo memo;
    private MarklinTrafficControlScaffold tc;

    @Override
    public String getSystemName(int i) {
        return "ML" + i;
    }

    @ParameterizedTest
    @ValueSource(strings = {"45", "DCC45", "MM123", "CAN7168", "CAN0", "00045"})
    public void testProtocolNames(String address) throws JmriException {
        assertEquals("ML" + address, l.createSystemName(address, "M"));
        assertEquals(NameValidity.VALID, l.validSystemNameFormat("ML" + address));
        Light light = l.newLight("ML" + address, "test light");
        assertInstanceOf(MarklinLight.class, light);
        assertEquals("ML" + address, light.getSystemName());
        assertEquals("test light", light.getUserName());
        assertSame(light, l.provideLight("ML" + address));
        assertSame(light, l.provideLight("test light"));
        assertTrue(tc.getSentMessages().isEmpty(), "creating a light must not switch the layout");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "M", "ML", "MLDCC", "MLMM", "MLCAN", "ML-1", "ML1A",
        "MLDCC-1", "MLDCC1.5", "MLSX1", "MLmm1", "MT1", "XL1", "ML99999999999999999"})
    public void testInvalidNames(String name) {
        assertEquals(NameValidity.INVALID, l.validSystemNameFormat(name));
        assertThrows(NamedBean.BadSystemNameException.class,
                () -> l.validateSystemNameFormat(name, Locale.ENGLISH));
        assertFalse(l.validSystemNameConfig(name));
    }

    @Test
    public void testInvalidTableAddress() {
        assertThrows(JmriException.class, () -> l.createSystemName("SX1", "M"));
        assertThrows(JmriException.class, () -> l.createSystemName("99999999999999999", "M"));
    }

    @Test
    public void testCustomPrefix() throws JmriException {
        l.dispose();
        memo.setSystemPrefix("MC");
        l = new MarklinLightManager(memo);
        assertEquals("MCLMM12", l.createSystemName("MM12", "MC"));
        assertEquals(NameValidity.INVALID, l.validSystemNameFormat("MLMM12"));
        Light light = l.provideLight("MCLMM12");
        assertEquals("MCLMM12", light.getSystemName());
        light.setState(Light.ON);
        assertEquals("00 16 03 00 06 00 00 30 0B 01 01 00 00",
                tc.getSentMessages().get(0).toString());
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        tc = new MarklinTrafficControlScaffold();
        memo = new MarklinSystemConnectionMemo(tc);
        l = new MarklinLightManager(memo);
    }

    @AfterEach
    public void tearDown() {
        l.getNamedBeanSet().forEach(Light::dispose);
        l.dispose();
        memo.dispose();
        tc.dispose();
        JUnitUtil.tearDown();
    }
}
