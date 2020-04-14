package jmri.jmrix.oaktree;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SerialLightManagerTest {

    private OakTreeSystemConnectionMemo memo = null;

    @Test
    public void testCTor() {
        SerialLightManager t = new SerialLightManager(memo);
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testCTor2() {
        memo = new OakTreeSystemConnectionMemo("O", "Oak Tree");
        // create and register the light manager object
        SerialLightManager lm = new SerialLightManager(memo);
        Assert.assertNotNull("Oaktree Light Manager creation with memo", lm);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new OakTreeSystemConnectionMemo("O", "Oak Tree");
        SerialTrafficController t = new SerialTrafficControlScaffold(memo);
        memo.setTrafficController(t);
        t.registerNode(new SerialNode(0, SerialNode.IO48, memo));
    }

    @After
    public void tearDown() {

        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(SerialLightManagerTest.class);

}
