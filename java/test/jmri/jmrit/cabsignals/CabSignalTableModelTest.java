package jmri.jmrit.cabsignals;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of CabSignalTableModel
 *
 * @author	Paul Bender Copyright (C) 2019
 */
public class CabSignalTableModelTest {
        
 
    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CabSignalTableModel model = new CabSignalTableModel(5,CabSignalTableModel.MAX_COLUMN);
        Assert.assertNotNull("exists", model);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();

    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
