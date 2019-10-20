package jmri.jmrix.can.adapters.gridconnect.canrs;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for MergTrafficController.
 * @author Paul Bender Copyright (C) 2016
 */
public class MergTrafficControllerTest extends jmri.jmrix.can.adapters.gridconnect.GcTrafficControllerTest {

    @Override
    @Test
    public void testGetCanid(){
        Assert.assertEquals("default canid value",122,((MergTrafficController)tc).getCanid());
    }

   
    @Override
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp(); 
        JUnitUtil.resetInstanceManager();
        tc = new MergTrafficController();
    }

    @Override
    @After
    public void tearDown(){
       tc = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
 
    }

}
