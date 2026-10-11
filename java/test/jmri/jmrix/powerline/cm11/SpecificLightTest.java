package jmri.jmrix.powerline.cm11;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for SpecificLight class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/
public class SpecificLightTest {

    private SpecificTrafficController tc = null;

    @Test
    public void testCm11SpecificLightConstructor(){
        Assertions.assertNotNull( new SpecificLight("PLA2",tc), "SpecificLight constructor");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.initDefaultUserMessagePreferences();
        SpecificSystemConnectionMemo memo = new SpecificSystemConnectionMemo();
        tc = new SpecificTrafficController(memo);
        memo.setTrafficController(tc);
        memo.configureManagers();
        memo.setSerialAddress(new jmri.jmrix.powerline.SerialAddress(memo));
    }

    @AfterEach
    public void tearDown(){
        tc.terminateThreads();
        tc = null;
        JUnitUtil.tearDown();
    }

}
