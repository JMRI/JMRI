package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Test simple functioning of LayoutEditorTools
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class LayoutEditorToolsTest {
        
    private LayoutEditor le = null;
    private LayoutEditorTools let = null;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("exists", let);
    }

    @Test
    public void testHitEndBumper() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // we haven't done anything, so reachedEndBumper should return false.
        Assert.assertFalse("reached end bumper", let.reachedEndBumper());
    }

    @Test
    public void testSetSignalsAtTurnout(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayoutEventually( () -> {
           MultiIconEditor mie = new MultiIconEditor(4);
           // this causes a "set Signal frame" to be displayed.
           let.setSignalsAtTurnout(mie,le.getTargetFrame());    
        });
        // the JFrameOperator waits for the set signal frame to appear,
        // then closes it.
        JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("SignalsAtTurnout"));
        jfo.requestClose();
    }

    @Test
    public void testSetSignalsAtTurnoutFromMenu(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayoutEventually( () -> {
           MultiIconEditor mie = new MultiIconEditor(4);
           Point2D point = new Point2D.Double(150.0, 100.0);
           LayoutTurnout to = new LayoutTurnout("Right Hand",
                 LayoutTurnout.RH_TURNOUT, point, 33.0, 1.1, 1.2, le);
           // this causes a "set Signal frame" to be displayed.
           let.setSignalsAtTurnoutFromMenu(to,mie,le.getTargetFrame());    
        });
        // the JFrameOperator waits for the set signal frame to appear,
        // then closes it.
        JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("SignalsAtTurnout"));
        jfo.requestClose();
    }

    @Test
    public void testGetHeadFromNameNullName(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNull("null signal head for null name",let.getHeadFromName(null));
    }

    @Test
    public void testGetHeadFromNameEmptyName(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNull("null signal head for empty name",let.getHeadFromName(""));
    }

    @Test
    public void testRemoveSignalHeadFromPanelNameNullName(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // this test verifies there is no exception
        let.removeSignalHeadFromPanel(null);
    }

    @Test
    public void testRemoveSignalHeadFromPanelEmptyName(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // this test verifies there is no exception
        let.removeSignalHeadFromPanel("");
    }

    @Test
    public void testFinalizeBlockBossLogicNullInput(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // this test verifies there is no exception
        let.finalizeBlockBossLogic();
        
    }

    // from here down is testing infrastructure
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        if(!GraphicsEnvironment.isHeadless()) {
           le = new LayoutEditor();
           let = new LayoutEditorTools(le);
        }
    }

    @After
    public void tearDown() throws Exception {
        if(!GraphicsEnvironment.isHeadless()) {
           JUnitUtil.dispose(le);
        }
        le = null;
        let = null;
        JUnitUtil.tearDown();
    }
}
