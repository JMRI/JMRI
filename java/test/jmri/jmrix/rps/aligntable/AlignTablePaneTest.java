package jmri.jmrix.rps.aligntable;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test simple functioning of AlignTablePane
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class AlignTablePaneTest {


    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        AlignTablePane action = new AlignTablePane(new jmri.ModifiedFlag(){
           @Override
           public void setModifiedFlag(boolean flag) {}
           @Override
           public boolean getModifiedFlag() { return false; }
        });
        Assert.assertNotNull("exists", action);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initRosterConfigManager();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {        JUnitUtil.tearDown();    }
}
