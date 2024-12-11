package jmri.jmrix.marklin;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class MarklinThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    private MarklinTrafficController tc = null;
    private MarklinSystemConnectionMemo memo = null;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        tc = new MarklinTrafficControlScaffold();
        memo = new MarklinSystemConnectionMemo(tc);
        memo.configureManagers();
        tm = memo.getThrottleManager();
    }

    @AfterEach
    public void tearDown() {
        Assertions.assertNotNull(tc);
        Assertions.assertNotNull(memo);
        tc.dispose();
        tc = null;
        memo.dispose();
        memo = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MarklinThrottleManagerTest.class);

}
