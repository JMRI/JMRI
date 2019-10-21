package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import org.junit.*;

/**
 * Test simple functioning of IconAdder.
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class IconAdderTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        IconAdder frame = new IconAdder();
        Assert.assertNotNull("exists", frame );
        frame.dispose();
    }

    @Test
    public void testBoolCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        IconAdder frame = new IconAdder(true);
        Assert.assertNotNull("exists", frame );
        frame.dispose();
    }

    @Test
    public void testGetNumIcons() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        IconAdder frame = new IconAdder("LeftTurnout");
        Assert.assertEquals("Icon count",0, frame.getNumIcons() );
        frame.dispose();
    }

    @Test
    public void testAddCatalog() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        IconAdder frame = new IconAdder("LeftTurnout");
        JmriJFrame parentFrame = new JmriJFrame("Icon Adder Parent Frame");
        frame.setParent(parentFrame);
        frame.addCatalog();
        frame.dispose();
        parentFrame.dispose();
    }

    @Test
    public void testAddDirectoryToCatalog() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        IconAdder frame = new IconAdder("LeftTurnout");
        JmriJFrame parentFrame = new JmriJFrame("Icon Adder Parent Frame");
        frame.setParent(parentFrame);
        frame.addDirectoryToCatalog();
        frame.dispose();
        parentFrame.dispose();
    }

    @Test
    public void testMakeIconPanel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        IconAdder frame = new IconAdder("LeftTurnout");
        JmriJFrame parentFrame = new JmriJFrame("Icon Adder Parent Frame");
        frame.setParent(parentFrame);
        frame.makeIconPanel(true);
        frame.dispose();
        parentFrame.dispose();
    }

    @Test
    public void testReset() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        IconAdder frame = new IconAdder("LeftTurnout");
        JmriJFrame parentFrame = new JmriJFrame("Icon Adder Parent Frame");
        frame.setParent(parentFrame);
        frame.reset();
        frame.dispose();
        parentFrame.dispose();
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        jmri.InstanceManager.store(new jmri.jmrit.catalog.DefaultCatalogTreeManager(), jmri.CatalogTreeManager.class);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
