package jmri.util.swing;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2020
 */
public class ValidationNotificationsTest {

    @Test
    public void testBinDecHexValid() {
        // Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals("valid decimal 7",7, 
            ValidationNotifications.parseBinDecHexByte("7",10,true,"NoMessage 21",null));
        
        Assert.assertEquals("valid hex dec true 255",255, 
            ValidationNotifications.parseBinDecHexByte("0xff",777,true,"NoMessage 24",null));
        
        Assert.assertEquals("valid dec false 7",7, 
            ValidationNotifications.parseBinDecHexByte("0d7",100,false,"NoMessage 27",null));
        
        Assert.assertEquals("valid binary false 21",21, 
            ValidationNotifications.parseBinDecHexByte("0b10101",40,false,"NoMessage 30",null));
        
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(BusyDialogTest.class);

}
