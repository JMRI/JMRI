package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import java.awt.Font;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

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
    public void setUp() {
        JUnitUtil.setUp();
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
