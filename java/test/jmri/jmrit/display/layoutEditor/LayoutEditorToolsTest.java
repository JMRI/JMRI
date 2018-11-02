package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;
import jmri.InstanceManager;
import jmri.implementation.VirtualSignalHead;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import org.junit.*;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JButtonOperator;

/**
 * Test simple functioning of LayoutEditorTools
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class LayoutEditorToolsTest {
    
    // allow 2 retries of intermittent tests
    @Rule
    public jmri.util.junit.rules.RetryRule retryRule = new jmri.util.junit.rules.RetryRule(2);
    
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
           // this causes a "set Signal frame" to be displayed.
           let.setSignalsAtTurnout(le.signalIconEditor,le.getTargetFrame());    
        });
        // the JFrameOperator waits for the set signal frame to appear,
        // then closes it.
        JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("SignalsAtTurnout"));
        jfo.requestClose();
    }

    @Test
    public void testSetSignalsAtTurnoutWithDone(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayoutEventually( () -> {
           Point2D point = new Point2D.Double(150.0, 100.0);
           LayoutTurnout to = new LayoutTurnout("Right Hand",
                 LayoutTurnout.RH_TURNOUT, point, 33.0, 1.1, 1.2, le);
           // this causes a "set Signal frame" to be displayed.
           let.setSignalsAtTurnoutFromMenu(to,le.signalIconEditor,le.getTargetFrame());    
        });
        // the JFrameOperator waits for the set signal frame to appear
        JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("SignalsAtTurnout"));
        // then we find and press the "Done" button.
        JButtonOperator jbo = new JButtonOperator(jfo,Bundle.getMessage("ButtonDone"));
        jbo.press();
        jfo.requestClose();
    }

    @Test
    public void testSetSignalsAtTurnoutWithCancel(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayoutEventually( () -> {
           Point2D point = new Point2D.Double(150.0, 100.0);
           LayoutTurnout to = new LayoutTurnout("Right Hand",
                 LayoutTurnout.RH_TURNOUT, point, 33.0, 1.1, 1.2, le);
           // this causes a "set Signal frame" to be displayed.
           let.setSignalsAtTurnoutFromMenu(to,le.signalIconEditor,le.getTargetFrame());    
        });
        // the JFrameOperator waits for the set signal frame to appear
        JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("SignalsAtTurnout"));
        // then we find and press the "Done" button.
        JButtonOperator jbo = new JButtonOperator(jfo,Bundle.getMessage("ButtonCancel"));
        jbo.press();
        jfo.requestClose();
    }


    @Test
    public void testSetSignalsAtTurnoutFromMenu(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayoutEventually( () -> {
           Point2D point = new Point2D.Double(150.0, 100.0);
           LayoutTurnout to = new LayoutTurnout("Right Hand",
                 LayoutTurnout.RH_TURNOUT, point, 33.0, 1.1, 1.2, le);
           // this causes a "set Signal frame" to be displayed.
           let.setSignalsAtTurnoutFromMenu(to,le.signalIconEditor,le.getTargetFrame());    
        });
        // the JFrameOperator waits for the set signal frame to appear,
        // then closes it.
        JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("SignalsAtTurnout"));
        jfo.requestClose();
    }

    @Test
    @Ignore("NPE during execution due to missing frame")
    public void testSetSignalsAtLevelXing(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayoutEventually( () -> {
           // this causes a "set Signal frame" to be displayed.
           let.setSignalsAtLevelXing(le.signalIconEditor,le.getTargetFrame());    
        });
        // the JFrameOperator waits for the set signal frame to appear,
        // then closes it.
        JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("SignalsAtLevelXing"));
        jfo.requestClose();
    }

    @Test
    @Ignore("NPE during execution due to missing frame")
    public void testSetSignalsAtLevelXingFromMenu(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayoutEventually( () -> {
           Point2D point = new Point2D.Double(150.0, 100.0);
           LevelXing to = new LevelXing("LevelCrossing",
                 point, le);
           // this causes a "set Signal frame" to be displayed.
           let.setSignalsAtLevelXingFromMenu(to,le.signalIconEditor,le.getTargetFrame());    
        });
        // the JFrameOperator waits for the set signal frame to appear,
        // then closes it.
        JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("SignalsAtLevelXing"));
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
    public void testGetHeadFromNameValid(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        VirtualSignalHead h = new VirtualSignalHead("IH1");
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(h);

        Assert.assertEquals("signal head for valid name",h,let.getHeadFromName("IH1"));
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

    @Test
    public void testSetSignalHeadOnPanelAtXYIntAndRemove(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        VirtualSignalHead h = new VirtualSignalHead("IH1");
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(h);
        Assert.assertFalse("Signal head not on panel before set",let.isHeadOnPanel(h));
        let.setSignalHeadOnPanel(0,"IH1",0,0);
        // setSignalHeadOnPanel  performs some GUI actions, so give
        // the AWT queue some time to clear.
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        Assert.assertTrue("Signal head on panel after set",let.isHeadOnPanel(h));
        let.removeSignalHeadFromPanel("IH1");
        // removeSignalHeadFromPanel  performs some GUI actions, so give
        // the AWT queue some time to clear.
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        Assert.assertFalse("Signal head not on panel after remove",let.isHeadOnPanel(h));

    }

    @Test
    public void testSetSignalHeadOnPanelAtPointAndRemove(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        VirtualSignalHead h = new VirtualSignalHead("IH1");
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(h);
        Assert.assertFalse("Signal head not on panel before set",let.isHeadOnPanel(h));
        Point2D point = new Point2D.Double(150.0, 100.0);
        let.setSignalHeadOnPanel(0,"IH1",point);
        // setSignalHeadOnPanel  performs some GUI actions, so give
        // the AWT queue some time to clear.
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        Assert.assertTrue("Signal head on panel after set",let.isHeadOnPanel(h));
        let.removeSignalHeadFromPanel("IH1");
        // removeSignalHeadFromPanel  performs some GUI actions, so give
        // the AWT queue some time to clear.
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        Assert.assertFalse("Signal head not on panel after remove",let.isHeadOnPanel(h));

    }

    @Test
    public void testSetSignalHeadOnPanelAtXYDoubleAndRemove(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        VirtualSignalHead h = new VirtualSignalHead("IH1");
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(h);
        Assert.assertFalse("Signal head not on panel before set",let.isHeadOnPanel(h));
        let.setSignalHeadOnPanel(0.0,"IH1",0,0);
        // setSignalHeadOnPanel  performs some GUI actions, so give
        // the AWT queue some time to clear.
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        Assert.assertTrue("Signal head on panel after set",let.isHeadOnPanel(h));
        let.removeSignalHeadFromPanel("IH1");
        // removeSignalHeadFromPanel  performs some GUI actions, so give
        // the AWT queue some time to clear.
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        Assert.assertFalse("Signal head not on panel after remove",let.isHeadOnPanel(h));

    }

    @Test
    public void testGetSignalHeadIcon(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        VirtualSignalHead h = new VirtualSignalHead("IH1");
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(h);
        Assert.assertNotNull("Signal head icon for panel",let.getSignalHeadIcon("IH1"));
    }


    @Test
    public void testIsHeadOnPanel(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        VirtualSignalHead h = new VirtualSignalHead("IH1");
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(h);
        Assert.assertFalse("Signal head not on panel",let.isHeadOnPanel(h));
    }

    @Test
    public void testIsHeadAssignedAnywhere(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        VirtualSignalHead h = new VirtualSignalHead("IH1");
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(h);
        Assert.assertFalse("Signal head not on panel",let.isHeadAssignedAnywhere(h));
    }

    @Test
    public void testRemoveSignalHeadAssignment(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        VirtualSignalHead h = new VirtualSignalHead("IH1");
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(h);
        // just verify this doesn't thrown an error.
        let.removeAssignment(h);
    }

    @Test
    public void testInitializeBlockBossLogic(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        VirtualSignalHead h = new VirtualSignalHead("IH1");
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(h);
        Assert.assertTrue("Signal head block boss logic started",let.initializeBlockBossLogic("IH1"));
    }

    // from here down is testing infrastructure
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        if(!GraphicsEnvironment.isHeadless()) {
            jmri.util.JUnitUtil.resetProfileManager();
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
