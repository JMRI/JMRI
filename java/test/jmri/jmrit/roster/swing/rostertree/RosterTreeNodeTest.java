package jmri.jmrit.roster.swing.rostertree;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of RosterTreeNode
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class RosterTreeNodeTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        RosterTreeNode p = new RosterTreeNode(); 
        Assert.assertNotNull("exists", p);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
