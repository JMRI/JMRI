package jmri.jmrix.lenz.swing.stackmon;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.lenz.swing.stackmon.StackMonDataModel class
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class StackMonDataModelTest {

    @Test
    public void testCTor() {
        jmri.jmrix.lenz.XNetInterfaceScaffold cs = new jmri.jmrix.lenz.XNetInterfaceScaffold(new jmri.jmrix.lenz.LenzCommandStation());
        jmri.jmrix.lenz.XNetSystemConnectionMemo memo = new jmri.jmrix.lenz.XNetSystemConnectionMemo(cs);

        StackMonDataModel t = new StackMonDataModel(1,4,memo);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(StackMonDataModelTest.class);

}
