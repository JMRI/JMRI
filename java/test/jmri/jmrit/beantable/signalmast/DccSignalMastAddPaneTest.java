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
public class DccSignalMastAddPaneTest extends AbstractSignalMastAddPaneTestBase {

    /** {@inheritDoc} */
    @Override
    protected SignalMastAddPane getOTT() { return new DccSignalMastAddPane(); }    
    
    @Test
    public void testSetMast() {
        DccSignalMast s1 = new DccSignalMast("IF$dsm:AAR-1946:PL-1-high-abs(77)", "user name");
        // has to have its outputs configured so they exist      
        Enumeration<String> aspects = s1.getAppearanceMap().getAspects();
        while (aspects.hasMoreElements()) {
            s1.setOutputForAppearance(aspects.nextElement(), 0);
        }
        Assert.assertEquals("DCC Address should be 77",77, s1.getDccSignalMastAddress());

        // PacketSendCount default is 3
        Assert.assertEquals("Default should be 3",3, s1.getDccSignalMastPacketSendCount());
        s1.setDccSignalMastPacketSendCount(1);
        Assert.assertEquals("Should have updated to 1",1, s1.getDccSignalMastPacketSendCount());

        MatrixSignalMast m1 = new MatrixSignalMast("IF$xsm:basic:one-low($0001)-3t", "user");

        DccSignalMastAddPane vp = new DccSignalMastAddPane();
        
        Assert.assertTrue(vp.canHandleMast(s1));
        Assert.assertFalse(vp.canHandleMast(m1));
        
        vp.setMast(null);
        
        vp.setAspectNames(s1.getAppearanceMap(), 
            InstanceManager.getDefault(jmri.SignalSystemManager.class).getSystem("AAR-1946"));
        vp.setMast(s1);
        
        vp.setAspectNames(m1.getAppearanceMap(),
            InstanceManager.getDefault(jmri.SignalSystemManager.class).getSystem("basic"));
        vp.setMast(m1);
        JUnitAppender.assertErrorMessage("mast was wrong type: IF$xsm:basic:one-low($0001)-3t jmri.implementation.MatrixSignalMast");
    }

    @Before
    @Override
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
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
