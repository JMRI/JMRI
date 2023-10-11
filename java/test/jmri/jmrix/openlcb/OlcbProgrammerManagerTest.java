package jmri.jmrix.openlcb;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.openlcb.IdentifyProducersMessage;

/**
 * OlcbProgrammerManagerTest.java
 *
 * Test for the jmri.jmrix.openlcb.OlcbProgrammerManager class
 *
 * @author Bob Jacobsen
 */
public class OlcbProgrammerManagerTest {
    protected OlcbSystemConnectionMemoScaffold adapterMemo = new OlcbSystemConnectionMemoScaffold();
    protected OlcbTestHelper h = new OlcbTestHelper();

    @Test
    public void testCtor() {
        OlcbProgrammerManager s = new OlcbProgrammerManager(adapterMemo);
        Assert.assertNotNull(s);
        h.expectMessage(new IdentifyProducersMessage(h.iface.getNodeId(), OlcbProgrammer.IS_PROGRAMMINGTRACK_EVENT));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        adapterMemo.setInterface(h.iface);
    }

    @AfterEach
    public void tearDown() {
        h.dispose();
        adapterMemo.dispose();
        JUnitUtil.tearDown();
    }
}
