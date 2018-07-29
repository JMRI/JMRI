package jmri.jmrit.beantable.signalmast;

import jmri.*;
import jmri.implementation.*;
import jmri.util.*;

import java.util.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author	Bob Jacobsen Copyright 2018
 */
public class DccSignalMastAddPaneTest {

    @Test
    public void testSetMast() {
        DccSignalMast s1 = new DccSignalMast("IF$dsm:AAR-1946:PL-1-high-abs(1)", "user name");
        MatrixSignalMast m1 = new MatrixSignalMast("IF$xsm:basic:one-low($0001)-3t", "user");

        DccSignalMastAddPane vp = new DccSignalMastAddPane();
        
        Assert.assertFalse(vp.canHandleMast(null));
        Assert.assertTrue(vp.canHandleMast(s1));
        Assert.assertFalse(vp.canHandleMast(m1));
        
        vp.setMast(null);
        vp.setMast(s1);
        vp.setMast(m1);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();

        JUnitUtil.initInternalTurnoutManager();

        CommandStation c = new CommandStation() {
            @Override
            public boolean sendPacket(byte[] packet, int repeats) {
                lastSentPacket = packet;
                sentPacketCount++;
                return true;
            }

            @Override
            public String getUserName() {
                return null;
            }

            @Override
            public String getSystemPrefix() {
                return "I";
            }
        };
        InstanceManager.store(c, CommandStation.class);
        lastSentPacket = null;
        sentPacketCount = 0;
    }
    byte[] lastSentPacket;
    int sentPacketCount;

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
