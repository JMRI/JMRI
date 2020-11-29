package jmri.jmrix.can.adapters.gridconnect.canrs;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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
    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp(); 
        JUnitUtil.resetInstanceManager();
        tc = new MergTrafficController();
    }

    @Override
    @AfterEach
    public void tearDown(){
       tc = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
 
    }

}
