package jmri.jmrit.roster.swing.rostertree;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test simple functioning of RosterTreeNode
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class RosterTreeNodeTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        RosterTreeNode p = new RosterTreeNode(); 
        Assert.assertNotNull("exists", p);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
    }

    @AfterEach
    public void tearDown() {        JUnitUtil.tearDown();    }
}
