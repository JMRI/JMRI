package jmri.jmrit.display;

import static jmri.InstanceManager.getDefault;

import java.awt.GraphicsEnvironment;
import jmri.BlockManager;
import jmri.util.JUnitUtil;
import jmri.jmrit.catalog.NamedIcon;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of BlockContentsIcon
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class BlockContentsIconTest extends PositionableLabelTest {
    
    @Test
    public void testStringCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("BlockContentsIcon Constructor",p);
    }

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        new BlockContentsIcon("foo",editor);
        Assert.assertNotNull("BlockContentsIcon Constructor",p);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        if(!GraphicsEnvironment.isHeadless()){
           editor = new EditorScaffold();
           jmri.Block block = jmri.InstanceManager.getDefault(BlockManager.class).provideBlock("B1");
           NamedIcon icon = new NamedIcon("resources/icons/redTransparentBox.gif", "box"); // 13x13
           BlockContentsIcon bci = new BlockContentsIcon(icon,editor);
           bci.setIcon(icon);
           bci.setBlock(new jmri.NamedBeanHandle<>("B1",block));
           bci.setMemory("B1");
           p = to = bci;
        }
    }

    @After
    public void tearDown() {
        if(!GraphicsEnvironment.isHeadless()){
           editor.dispose();
           p = null; 
           to = null;
        }
        JUnitUtil.tearDown();
    }

}
