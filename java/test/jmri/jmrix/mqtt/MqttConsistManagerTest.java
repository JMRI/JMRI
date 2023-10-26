package jmri.jmrix.mqtt;

import jmri.implementation.AbstractConsistManager;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 *
 * @author Dean Cording Copyright (C) 2023
 */
public class MqttConsistManagerTest extends jmri.implementation.AbstractConsistManagerTestBase {

    private MqttAdapterScaffold a;
    private MqttSystemConnectionMemo memo;

    @Override
    @Test
    public void testIsCommandStationConsistPossible(){
       // default is false, override if necessary
       Assert.assertTrue("CS Consist Possible",cm.isCommandStationConsistPossible());
    }

    @Override
    @Test
    public void testCsConsistNeedsSeperateAddress(){
       Assume.assumeTrue(cm.isCommandStationConsistPossible());
       // default is false, override if necessary
       Assert.assertTrue("CS Consist Needs Seperate Address",cm.csConsistNeedsSeperateAddress());
    }

    @Override
    @Test
    public void testShouldRequestUpdateFromLayout(){
       Assume.assumeTrue(cm instanceof AbstractConsistManager);
       // default is true, override if necessary
       Assert.assertFalse("Should Request Update From Layout",((MqttConsistManager)cm).shouldRequestUpdateFromLayout());
    }


    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        a = new MqttAdapterScaffold(true);
        memo = new MqttSystemConnectionMemo();
        memo.setMqttAdapter(a);
        cm = new MqttConsistManager(memo);
        ((MqttConsistManager)cm).setSendTopic("cab/$address/consist");
        memo.setConsistManager(cm);
    }

    @AfterEach
    @Override
    public void tearDown() {
        memo.dispose();
        memo = null;
        a = null;
        cm = null;
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

}
