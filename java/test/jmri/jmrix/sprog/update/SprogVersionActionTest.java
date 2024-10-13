package jmri.jmrix.sprog.update;

import jmri.jmrix.sprog.SprogSystemConnectionMemo;
import jmri.jmrix.sprog.SprogTrafficControlScaffold;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SprogVersionActionTest {

    @Test
    public void testCTor() {
        SprogSystemConnectionMemo m = new SprogSystemConnectionMemo();
        m.setSprogTrafficController(new SprogTrafficControlScaffold(m));
        SprogVersionAction t = new SprogVersionAction("test",m);
        Assert.assertNotNull("exists",t);
        m.getSprogTrafficController().dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SprogVersionActionTest.class);

}
