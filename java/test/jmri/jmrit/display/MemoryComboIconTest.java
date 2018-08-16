package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Test simple functioning of MemoryComboIcon
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class MemoryComboIconTest extends PositionableJPanelTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("MemoryComboIcon Constructor",p);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();

        jmri.InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);
        if (!GraphicsEnvironment.isHeadless()) {
            editor = new EditorScaffold();
            String args[] = {"foo","bar"};
            MemoryComboIcon bci = new MemoryComboIcon(editor,args);
            bci.setMemory("IM1");
            p = bci;
        }
    }

    @After
    public void tearDown() {
        editor = null;
        p = null;
        JUnitUtil.tearDown();
    }

}
