package jmri.util.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2020
 */
public class ValidationNotificationsTest {

    @Test
    public void testBinDecHexValid() {
        // Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        assertEquals( 7,
            ValidationNotifications.parseBinDecHexByte("7",10,true,"NoMessage 21",null),
            "valid decimal 7");
        
        assertEquals( 255,
            ValidationNotifications.parseBinDecHexByte("0xff",777,true,"NoMessage 24",null),
            "valid hex dec true 255");
        
        assertEquals( 7,
            ValidationNotifications.parseBinDecHexByte("0d7",100,false,"NoMessage 27",null),
            "valid dec false 7");
        
        assertEquals( 21,
            ValidationNotifications.parseBinDecHexByte("0b10101",40,false,"NoMessage 30",null),
            "valid binary false 21");

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(BusyDialogTest.class);

}
