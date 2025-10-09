package jmri.jmrix.acela.nodeconfig;

import jmri.jmrix.acela.AcelaSystemConnectionMemo;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of NodeConfigFrame
 *
 * @author Paul Bender Copyright (C) 2016
 */
@DisabledIfHeadless
public class NodeConfigFrameTest extends jmri.util.JmriJFrameTestBase {

    private AcelaSystemConnectionMemo memo = null;

    @Test
    public void testGetTitle(){
        frame.initComponents();
        Assertions.assertEquals("Configure Nodes",frame.getTitle(), "title");
    }


    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();

        memo = new AcelaSystemConnectionMemo(); 
        frame = new NodeConfigFrame(memo);
    }

    @AfterEach
    @Override
    public void tearDown() {
        memo.getTrafficController().terminateThreads();
        memo.dispose();
        memo = null;
        super.tearDown();
    }
}
