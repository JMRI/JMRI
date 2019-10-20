package jmri.jmrix.jmriclient;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JMRIClientSystemConnectionMemoTest.java
 *
 * Description:	tests for the
 * jmri.jmrix.jmriclient.JMRIClientSystemConnectionMemo class
 *
 * @author	Bob Jacobsen
 */
public class JMRIClientSystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {

    private JMRIClientTrafficControlScaffold jcins;
    private JMRIClientSystemConnectionMemo memo;

    @Override
    @Test
    public void testProvidesConsistManager(){
       Assert.assertFalse("Provides ConsistManager",scm.provides(jmri.ConsistManager.class));
    }

    @Test
    public void testDefaultCtor(){
       Assert.assertNotNull("Default Ctor",new JMRIClientSystemConnectionMemo());
    }

    @Test
    public void testSetTrafficController(){
        jcins = new JMRIClientTrafficControlScaffold();
        memo.setJMRIClientTrafficController(jcins);
        Assert.assertEquals("memo after set",jcins,memo.getJMRIClientTrafficController());
    }

    @Test
    public void testConfigureManagers(){
        memo.configureManagers();
        Assert.assertNotNull("Power Manager set",memo.getPowerManager());
        Assert.assertNotNull("Turnout Manager set",memo.getTurnoutManager());
        Assert.assertNotNull("Sensor Manager set",memo.getSensorManager());
        Assert.assertNotNull("Light Manager set",memo.getLightManager());
        Assert.assertNotNull("Reporter Manager set",memo.getReporterManager());
    }

    @Test
    public void testGetAndSetTransmitPrefix() {
       Assert.assertEquals("default transmit prefix",memo.getSystemPrefix(),memo.getTransmitPrefix());
       memo.setTransmitPrefix("F1");
       Assert.assertEquals("Transmit Prefix","F1",memo.getTransmitPrefix());
    }


    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jcins = new JMRIClientTrafficControlScaffold();
        scm = memo = new JMRIClientSystemConnectionMemo(jcins);
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
