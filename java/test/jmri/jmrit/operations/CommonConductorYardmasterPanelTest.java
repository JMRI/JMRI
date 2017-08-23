package jmri.jmrit.operations;

import apps.tests.Log4JFixture;
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
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        p = new CommonConductorYardmasterPanel(){
            @Override
            protected void update(){
            }
        };
    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
    }

}
