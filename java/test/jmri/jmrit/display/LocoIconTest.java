package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import java.awt.Font;
import org.junit.*;

/**
 * Test simple functioning of LocoIcon
 *
 * @author	Paul Bender Copyright (C) 2016
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
    @Ignore("not supported for LocoIcon")
    public void testDoViemMenu(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertTrue("Do View Menu",p.doViemMenu());
    }

    @Before
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
