package jmri.jmrix.lenz.swing.stackmon;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(StackMonDataModelTest.class);

}
