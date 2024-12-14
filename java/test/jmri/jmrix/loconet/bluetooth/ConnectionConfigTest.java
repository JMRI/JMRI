package jmri.jmrix.loconet.bluetooth;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for ConnectionConfig class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/
public class ConnectionConfigTest extends jmri.jmrix.AbstractSerialConnectionConfigTestBase  {

    @Test
    @Override
    public void testLoadDetails(){
        jmri.util.ThreadingUtil.runOnGUI( () -> {
            // verify no exceptions thrown
            Assertions.assertDoesNotThrow( () -> {
                cc.loadDetails(new javax.swing.JPanel());});
            // a bluetooth device may be present but disabled
            jmri.util.JUnitAppender.suppressErrorMessageStartsWith("Unable to use bluetooth device");
            // load details MAY produce an error message if no ports are found.
            jmri.util.JUnitAppender.suppressErrorMessage("No usable ports returned");
        });
    }

   @BeforeEach
   @Override
   public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.initDefaultUserMessagePreferences();
        cc = new ConnectionConfig();
   }

   @AfterEach
   @Override
   public void tearDown(){
        cc = null;
        JUnitUtil.tearDown();
   }

}
