package jmri.jmrix.loconet.alm.almi;

import jmri.jmrix.loconet.LocoNetMessage;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author B. Milhaupt  Copyright (C) 2022
 */
public class AlmiTest {

    @Test
    public void testInterpretAlm() {

        LocoNetMessage l;

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals("Get Aliasing Information.\n",
                Almi.interpretAlm(l));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x00, 0x00, 0x08, 0x00, 0x0b, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals("Aliasing Report: 16 aliases supported.\n",
                Almi.interpretAlm(l));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x00, 0x02, 0x07, 0x00, 0x0f, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals("Get Alias pair 7.\n",
                Almi.interpretAlm(l));

        l = new LocoNetMessage(new int[] {0xE6, 0x10, 0x00, 0x02, 0x09, 0x00, 0x0f, 0x08, 0x07, 0x06, 0x00, 0x05, 0x04, 0x03, 0x00, 0x00});
        Assert.assertEquals("Report Alias pair 9: 904 is an alias for 6; 517 is an alias for 3.\n",
                Almi.interpretAlm(l));

        l = new LocoNetMessage(new int[] {0xE6, 0x10, 0x00, 0x43, 0x05, 0x00, 0x0f, 0x14, 0x31, 0x0d, 0x00, 0x13, 0x30, 0x7c, 0x00, 0x00});
        Assert.assertEquals("Set Alias pair 5: 6292 is an alias for 13; 6163 is an alias for 124.\n",
                Almi.interpretAlm(l));

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
