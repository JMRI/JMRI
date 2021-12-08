package jmri.jmrix.loconet.loconetovertcp;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for LnOverTcpPacketizer class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class LnOverTcpPacketizerTest {

   @BeforeEach
   public void setUp() {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
   }

   @AfterEach
   public void tearDown(){
        JUnitUtil.tearDown();
   }

}
