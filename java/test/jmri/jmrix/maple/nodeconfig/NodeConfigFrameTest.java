package jmri.jmrix.maple.nodeconfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jmri.jmrix.maple.MapleSystemConnectionMemo;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of NodeConfigFrame
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class NodeConfigFrameTest {

    private MapleSystemConnectionMemo memo = null;

    @Test
    @DisabledIfHeadless
    public void testMemoCtor() {
        NodeConfigFrame action = new NodeConfigFrame(memo);
        assertNotNull( action, "exists");
    }

    @Test
    @DisabledIfHeadless
    public void testInitComponents() {
        NodeConfigFrame t = new NodeConfigFrame(memo);
        // for now, just makes sure there isn't an exception.
        t.initComponents();
        t.dispose();
    }

    @Test
    @DisabledIfHeadless
    public void testGetTitle(){
        NodeConfigFrame t = new NodeConfigFrame(memo);
        t.initComponents();
        assertEquals( "Configure Maple Nodes", t.getTitle(), "title");
        t.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();

        memo = new MapleSystemConnectionMemo();
    }

    @AfterEach
    public void tearDown() {

        memo.dispose();
        memo = null;
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }
}
