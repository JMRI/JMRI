package jmri.jmrix.nce;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jmri.JmriException;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class NceMessageCheckTest {

    // no Ctor test, class only supplies static methods

    @Test
    public void testThrows() {

        var memo = new NceSystemConnectionMemo();
        var tc = new NceTrafficControlScaffold();
        memo.setNceTrafficController(tc);
        memo.setNceUsbSystem( NceTrafficController.USB_SYSTEM_POWERCAB);
        NceMessage m = new NceMessage(13);
        m.setOpCode( NceMessage.CLOCK_RATIO_CMD);

        JmriException ex = assertThrows( JmriException.class,
            () -> NceMessageCheck.checkMessage(memo, m),
            "Should have thrown exception as invalid message");
        assertNotNull( ex);
        tc.terminateThreads();
        memo.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(NceMessageCheckTest.class);

}
