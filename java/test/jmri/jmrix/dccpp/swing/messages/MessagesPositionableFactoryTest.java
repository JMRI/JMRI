package jmri.jmrix.dccpp.swing.messages;

import jmri.jmrix.dccpp.DCCppCommandStation;
import jmri.jmrix.dccpp.DCCppInterfaceScaffold;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/** Tests for MessagesPositionableFactory. */
public class MessagesPositionableFactoryTest {

    private DCCppSystemConnectionMemo memo;

    @Test
    public void testIdentifierAndDescription() {
        MessagesPositionableFactory factory = new MessagesPositionableFactory();
        Assertions.assertEquals("DCC-EX-Messages", factory.getIdentifier());
        Assertions.assertEquals(Bundle.getMessage("MessagesFrameTitle"), factory.getDescription());
    }

    @Test
    public void testEnabledOnlyWithDccExConnection() {
        MessagesPositionableFactory factory = new MessagesPositionableFactory();
        Assertions.assertFalse(factory.isEnabled());
        memo = new DCCppSystemConnectionMemo(new DCCppInterfaceScaffold(new DCCppCommandStation()));
        Assertions.assertTrue(factory.isEnabled());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        if (memo != null) {
            memo.getDCCppTrafficController().terminateThreads();
            memo.dispose();
        }
        memo = null;
        JUnitUtil.tearDown();
    }

}
