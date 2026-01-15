package jmri.jmrix.powerline.dmx512;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for SpecificLight class.
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Ken Cameron Copyright (C) 2023
 **/
public class SpecificLightTest {

    private SpecificTrafficController tc = null;

    @Test
    public void testSpecificLightConstructor(){
        assertNotNull( new SpecificLight("PL1",tc), "SpecificLight constructor");
        assertNotNull( new SpecificLight("PL256",tc), "SpecificLight constructor");
        assertNotNull( new SpecificLight("PL512",tc), "SpecificLight constructor");
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
