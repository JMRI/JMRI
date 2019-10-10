package jmri.jmrix.rps.aligntable;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of AlignTablePane
 *
 * @author	Paul Bender Copyright (C) 2016
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

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initRosterConfigManager();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
