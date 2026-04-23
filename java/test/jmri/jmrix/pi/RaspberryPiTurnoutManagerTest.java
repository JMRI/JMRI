package jmri.jmrix.pi;

import java.beans.PropertyVetoException;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for RaspberryPiTurnoutManager.
 * <p>
 * Resets the GPIO support by disposing the turnouts + pins.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class RaspberryPiTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    @Override
    public String getSystemName(int i) {
        return l.getSystemPrefix() + "T" + i;
    }
    
    @Override
    protected String getASystemNameWithNoPrefix() {
        return "" + getNumToTest2();
    }

    @Test
    public void testCtor() {
        Assert.assertNotNull(l);
    }

    @Test
    public void checkPrefix() {
        Assert.assertEquals("Prefix", "P", l.getSystemPrefix());
    }

    @Test
    @Override
    @jmri.util.junit.annotations.ToDo("investigate why fails in super class")
    @Disabled("Test requires further development")
    public void testRegisterDuplicateSystemName() throws PropertyVetoException,
            NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
    }

    private PiGpioProviderScaffold myProvider = null;

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        myProvider = new PiGpioProviderScaffold();
        l = new RaspberryPiTurnoutManager(new RaspberryPiSystemConnectionMemo());
    }

    @AfterEach
    public void tearDown() {
        RaspberryPiTurnout t1 = (RaspberryPiTurnout) l.getTurnout(getSystemName(getNumToTest1()));
        if (t1 != null) { t1.dispose(); }
        t1 = (RaspberryPiTurnout) l.getTurnout(getSystemName(getNumToTest2()));
        if (t1 != null) { t1.dispose(); }
        t1 = (RaspberryPiTurnout) l.getTurnout(getSystemName(1));
        if (t1 != null) { t1.dispose(); }
        t1 = (RaspberryPiTurnout) l.getTurnout(getSystemName(2));
        if (t1 != null) { t1.dispose(); }
        RaspberryPiSensor s1 = (RaspberryPiSensor) InstanceManager.sensorManagerInstance().getSensor("PS1");
        if (s1 != null) { s1.dispose(); }
        s1 = (RaspberryPiSensor) InstanceManager.sensorManagerInstance().getSensor("PS2");
        if (s1 != null) { s1.dispose(); }
        Assertions.assertNotNull(myProvider);
        myProvider.shutdown();
        l.dispose();

        JUnitUtil.tearDown();
    }

}
