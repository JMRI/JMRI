package jmri.jmrix.acela;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AcelaSensorManagerTest {

    private AcelaSystemConnectionMemo memo = null;

    @Test
    public void testCTor() {
        AcelaSensorManager t = new AcelaSensorManager(memo);
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        AcelaTrafficController tc = new AcelaTrafficControlScaffold();
        memo = new AcelaSystemConnectionMemo(tc);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(AcelaSensorManagerTest.class);

}
