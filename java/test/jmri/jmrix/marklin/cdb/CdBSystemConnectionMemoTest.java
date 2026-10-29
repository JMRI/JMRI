package jmri.jmrix.marklin.cdb;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
 
/**
 * Tests for the CdBSystemConnectionMemo class.
 * @author Steve Young Copyright (C) 2024
 */
public class CdBSystemConnectionMemoTest extends
    jmri.jmrix.SystemConnectionMemoTestBase<CdBSystemConnectionMemo> {

    @Test
    public void testConfiguredLightManager() {
        jmri.jmrix.marklin.MarklinTrafficControlScaffold tc =
                new jmri.jmrix.marklin.MarklinTrafficControlScaffold();
        scm.setMarklinTrafficController(tc);
        scm.configureManagers();
        Assertions.assertTrue(scm.provides(jmri.LightManager.class));
        Assertions.assertSame(scm.getLightManager(), scm.get(jmri.LightManager.class));
        jmri.Light light = jmri.InstanceManager.lightManagerInstance().provideLight("MLCAN7168");
        Assertions.assertSame(light, scm.getLightManager().getLight("MLCAN7168"));
        light.setState(jmri.Light.ON);
        Assertions.assertEquals("00 16 03 00 06 00 00 1C 00 01 01 00 00",
                tc.getSentMessages().get(0).toString());
        light.dispose();
        scm.dispose();
        tc.dispose();
    }

    @Test
    @Disabled("Tested class requires further development")
    @Override
    public void testGetActionFactory(){}

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        scm = new CdBSystemConnectionMemo();
    }

    @AfterEach
    @Override
    public void tearDown() {
        scm = null;
        JUnitUtil.tearDown();
    }

}
