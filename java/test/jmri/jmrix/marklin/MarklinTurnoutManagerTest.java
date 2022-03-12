package jmri.jmrix.marklin;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class MarklinTurnoutManagerTest {

    @Test
    public void testCTor() {
        MarklinTrafficController tc = new MarklinTrafficController();
        MarklinSystemConnectionMemo c = new MarklinSystemConnectionMemo(tc);
        MarklinTurnoutManager t = new MarklinTurnoutManager(c);
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

    // private final static Logger log = LoggerFactory.getLogger(MarklinTurnoutManagerTest.class);

}
