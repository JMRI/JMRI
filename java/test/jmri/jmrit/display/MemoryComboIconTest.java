package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

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
        jmri.InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);
        if (!GraphicsEnvironment.isHeadless()) {
            editor = new EditorScaffold();
            String args[] = {"foo","bar"};
            MemoryComboIcon bci = new MemoryComboIcon(editor,args);
            bci.setMemory("IM1");
            p = bci;
        }
    }

}
