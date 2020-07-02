package jmri.jmrix.tams;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TamsProgrammerManagerTest {

    @Test
    public void testCTor() {
        TamsTrafficController tc = new TamsTrafficController();
        TamsSystemConnectionMemo memo = new TamsSystemConnectionMemo(tc);  
        TamsProgrammerManager t = new TamsProgrammerManager(new TamsProgrammer(tc),memo);
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

    // private final static Logger log = LoggerFactory.getLogger(TamsProgrammerManagerTest.class);

}
