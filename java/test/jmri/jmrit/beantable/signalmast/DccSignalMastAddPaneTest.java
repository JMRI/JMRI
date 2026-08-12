package jmri.jmrit.beantable.signalmast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.*;
import jmri.implementation.*;
import jmri.util.*;

import java.util.*;

import org.junit.jupiter.api.*;

/**
 * @author Bob Jacobsen Copyright 2018
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
        assertEquals(77, s1.getDccSignalMastAddress(), "DCC Address should be 77");

        // PacketSendCount default is 3
        assertEquals(3, s1.getDccSignalMastPacketSendCount(), "Default should be 3");
        s1.setDccSignalMastPacketSendCount(1);
        assertEquals(1, s1.getDccSignalMastPacketSendCount(), "Should have updated to 1");

        MatrixSignalMast m1 = new MatrixSignalMast("IF$xsm:basic:one-low($0001)-3t", "user");

        DccSignalMastAddPane vp = new DccSignalMastAddPane();

        assertTrue(vp.canHandleMast(s1));
        assertFalse(vp.canHandleMast(m1));

        vp.setMast(null);
        SignalSystem aar1946system = InstanceManager.getDefault(SignalSystemManager.class).getSystem("AAR-1946");
        assertNotNull(aar1946system);
        vp.setAspectNames(s1.getAppearanceMap(), aar1946system );
        vp.setMast(s1);

        SignalSystem basic = InstanceManager.getDefault(SignalSystemManager.class).getSystem("basic");
        assertNotNull(basic);
        vp.setAspectNames(m1.getAppearanceMap(), basic );
        vp.setMast(m1);
        JUnitAppender.assertErrorMessage("mast was wrong type: IF$xsm:basic:one-low($0001)-3t jmri.implementation.MatrixSignalMast");
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();

        JUnitUtil.initInternalTurnoutManager();

        CommandStation c = new CommandStation() {
            @Override
            public boolean sendPacket(byte[] packet, int repeats) {
                // lastSentPacket = packet;
                // sentPacketCount++;
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
        // lastSentPacket = null;
        // sentPacketCount = 0;
    }

    // byte[] lastSentPacket;
    // int sentPacketCount;

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
