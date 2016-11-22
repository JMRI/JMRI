package jmri.jmrix.rps.aligntable;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import jmri.InstanceManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

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
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
    }
}
