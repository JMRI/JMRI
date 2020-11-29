package jmri.jmrix.jmriclient;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * JMRIClientSystemConnectionMemoTest.java
 * <p>
 * Test for the jmri.jmrix.jmriclient.JMRIClientSystemConnectionMemo class
 *
 * @author Bob Jacobsen
 */
public class JMRIClientSystemConnectionMemoTest extends SystemConnectionMemoTestBase<JMRIClientSystemConnectionMemo> {

    @Override
    @Test
    public void testProvidesConsistManager() {
        Assert.assertFalse("Provides ConsistManager", scm.provides(jmri.ConsistManager.class));
    }

    @Test
    public void testDefaultCtor() {
        JMRIClientSystemConnectionMemo memo = new JMRIClientSystemConnectionMemo();
        Assert.assertNotNull("Default Ctor", memo);
        memo.getJMRIClientTrafficController().terminateThreads();
        memo.dispose();
    }

    @Test
    public void testSetTrafficController() {
        // cleanup traffic controller from setup
        scm.getJMRIClientTrafficController().terminateThreads();
        JMRIClientTrafficControlScaffold jcins = new JMRIClientTrafficControlScaffold();
        scm.setJMRIClientTrafficController(jcins);
        Assert.assertEquals("scm after set", jcins, scm.getJMRIClientTrafficController());
    }

    @Test
    public void testConfigureManagers() {
        scm.configureManagers();
        Assert.assertNotNull("Power Manager set", scm.getPowerManager());
        Assert.assertNotNull("Turnout Manager set", scm.getTurnoutManager());
        Assert.assertNotNull("Sensor Manager set", scm.getSensorManager());
        Assert.assertNotNull("Light Manager set", scm.getLightManager());
        Assert.assertNotNull("Reporter Manager set", scm.getReporterManager());
    }

    @Test
    public void testGetAndSetTransmitPrefix() {
        Assert.assertEquals("default transmit prefix", scm.getSystemPrefix(), scm.getTransmitPrefix());
        scm.setTransmitPrefix("F1");
        Assert.assertEquals("Transmit Prefix", "F1", scm.getTransmitPrefix());
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        scm = new JMRIClientSystemConnectionMemo(new JMRIClientTrafficControlScaffold());
    }

    @Override
    @AfterEach
    public void tearDown() {
        scm.getJMRIClientTrafficController().terminateThreads();
        scm.dispose();
        JUnitUtil.tearDown();
    }

}
