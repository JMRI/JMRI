package jmri.jmrix.grapevine.nodetable;

import java.awt.GraphicsEnvironment;
import org.junit.After;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrix.grapevine.GrapevineSystemConnectionMemo;
import jmri.jmrix.grapevine.SerialTrafficController;
import jmri.jmrix.grapevine.SerialTrafficControlScaffold;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class RenumberFrameTest {

    private GrapevineSystemConnectionMemo memo = null; 

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        RenumberFrame t = new RenumberFrame(memo);
        Assert.assertNotNull("exists",t);
        t.dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        SerialTrafficController tc = new SerialTrafficControlScaffold();
        memo = new GrapevineSystemConnectionMemo();
        memo.setTrafficController(tc);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(RenumberFrameTest.class);

}
