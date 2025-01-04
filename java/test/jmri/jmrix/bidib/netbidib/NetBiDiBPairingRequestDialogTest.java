package jmri.jmrix.bidib.netbidib;

import java.awt.GraphicsEnvironment;
import org.junit.Assert;
import jmri.util.JUnitUtil;
//import jmri.util.JUnitAppender;

import org.junit.jupiter.api.*;

import org.bidib.jbidibc.messages.helpers.Context;
import org.bidib.jbidibc.messages.helpers.DefaultContext;

/**
 * Tests for the NetBiDiBPairingRequestDialog class
 * 
 * @author  Eckart Meyer  Copyright (C) 2024
 */
public class NetBiDiBPairingRequestDialogTest {

    @Test
    public void testCTor() {
        Context context = new DefaultContext();
        if(!GraphicsEnvironment.isHeadless()) {
            NetBiDiBPairingRequestDialog t = new NetBiDiBPairingRequestDialog(context, null, null);
            Assert.assertNotNull("exists", t);
        }
    }

    // The minimal setup for log4J
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // Temporary let the error messages from this test be shown to the user
//        JUnitAppender.end();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NetBiDiBPairingRequestDialogTest.class);
}

