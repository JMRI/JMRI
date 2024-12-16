package jmri.jmrix.marklin;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class MarklinTurnoutManagerTest  extends jmri.managers.AbstractTurnoutMgrTestBase {

    private MarklinSystemConnectionMemo memo;

    @Override
    public String getSystemName(int i) {
        return "MT"+i;
    }

    @Test
    public void testCTor() {
        Assertions.assertNotNull(l, "exists");
    }

    @Test
    @Override
    @Disabled("Tested class requires further development")
    public void testMakeSystemNameWithNoPrefixNotASystemName(){}

    @Test
    @Override
    @Disabled("Tested class requires further development")
    public void testMakeSystemNameWithPrefixNotASystemName(){}


    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        MarklinTrafficControlScaffold tc = new MarklinTrafficControlScaffold();
        memo = new MarklinSystemConnectionMemo(tc);
        l = new MarklinTurnoutManager(memo);
    }

    @AfterEach
    public void tearDown() {
        l.dispose();
        l = null;
        memo.getTrafficController().dispose();
        memo.dispose();
        memo = null;
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(MarklinTurnoutManagerTest.class);

}
