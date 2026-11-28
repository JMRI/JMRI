package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import java.awt.Font;
import java.util.Iterator;

import jmri.InstanceManager;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.symbolicprog.ProgrammerConfigManager;
import jmri.jmrit.throttle.ThrottleFrameManager;
import jmri.jmrit.throttle.interfaces.ThrottleControllerUI;
import jmri.jmrit.throttle.interfaces.ThrottleControllersUIContainer;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of LocoIcon
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Keith Bruce Copyright (C) 2026
 */
public class LocoIconTest extends PositionableTestBase {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LocoIcon Constructor",p);
    }

    @Test
    @Override
    public void testGetAndSetPositionable() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertTrue("Defalt Positionable", p.isPositionable());
        // LocoIcon's ignore setting Positionable to false
        p.setPositionable(false);
        Assert.assertTrue("Positionable after set false", p.isPositionable());
        p.setPositionable(true);
        Assert.assertTrue("Positionable after set true", p.isPositionable());
    }

    @Override
    @Test
    public void testGetAndSetShowToolTip() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertFalse("Defalt ShowToolTip", p.showToolTip());
        // LocoIcon's ignore setting ShowToolTip to true
        p.setShowToolTip(true);
        Assert.assertFalse("showToolTip after set true", p.showToolTip());
        p.setShowToolTip(false);
        Assert.assertFalse("showToolTip after set false", p.showToolTip());
    }

    @Override
    @Test
    @Disabled("not supported for LocoIcon")
    public void testDoViemMenu(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertTrue("Do View Menu",p.doViemMenu());
    }

    @Test
    @DisabledIfHeadless
    public void testThrottleBringsTheOpenOneToTheFront() {
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDebugThrottleManager();
        InstanceManager.setDefault(ProgrammerConfigManager.class, new ProgrammerConfigManager());
        RosterEntry re = new RosterEntry();
        re.setId("Loco 3");
        re.setFileName("Loco_3.xml");
        re.setDccAddress("3");
        re.setLongAddress(false);
        LocoIcon li = (LocoIcon) p;
        li.setRosterEntry(re);
        ThreadingUtil.runOnGUI(li::showThrottle);
        ThrottleControllerUI first = li.tf;
        Assertions.assertNotNull(first, "a throttle opened");
        ThreadingUtil.runOnGUI(li::showThrottle);
        Assertions.assertSame(first, li.tf, "the open throttle, not a new one");
        ThrottleFrameManager tfm = InstanceManager.getDefault(ThrottleFrameManager.class);
        int throttles = 0;
        Iterator<ThrottleControllersUIContainer> windows = tfm.iterator();
        while (windows.hasNext()) {
            throttles += windows.next().getNbThrottlesControllers();
        }
        Assertions.assertEquals(1, throttles, "one throttle open");
        ThreadingUtil.runOnGUI(() -> tfm.requestThrottleWindowDestruction(first.getThrottleControllersContainer()));
        JUnitUtil.resetWindows(false, false);
    }

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
           editor = new EditorScaffold();
           LocoIcon li = new LocoIcon(editor);
           // for rotate to work, must set font.
           li.setFont(new Font("Serif", Font.BOLD, 10));
           li.setText("1234");
           p = li;
        }
    }

}
