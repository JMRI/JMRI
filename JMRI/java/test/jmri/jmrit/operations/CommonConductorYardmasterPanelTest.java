package jmri.jmrit.operations;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the CommonConductorYardmasterPanel class
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class CommonConductorYardmasterPanelTest  {

    protected CommonConductorYardmasterPanel p = null;

    @Test
    public void testCtor(){
       Assert.assertNotNull("exists",p);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();        p = new CommonConductorYardmasterPanel(){
            @Override
            protected void update(){
            }
        };
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }

}
