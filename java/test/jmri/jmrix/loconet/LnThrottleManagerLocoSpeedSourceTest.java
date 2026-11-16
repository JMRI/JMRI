package jmri.jmrix.loconet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import jmri.CommandStation;
import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.LocoSpeedSource;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The LocoNet throttle manager as a {@link LocoSpeedSource}: a speed or
 * direction another throttle sets for a loco JMRI also holds, seen on
 * LocoNet, is reported too.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class LnThrottleManagerLocoSpeedSourceTest {

    private LocoNetInterfaceScaffold lnis;
    private SlotManager slotmanager;
    private LocoNetSystemConnectionMemo memo;

    @Test
    public void testAnotherThrottlesCommandReported() {
        List<String> told = new ArrayList<>();
        LnThrottleManager manager = (LnThrottleManager) memo.getThrottleManager();
        manager.addLocoSpeedListener(new LocoSpeedSource.Listener() {
            @Override
            public void speedCommanded(@Nonnull LocoAddress address, float speed, boolean forward, long timeNanos) {
                told.add(address + " " + speed + " " + (forward ? "forward" : "reverse"));
            }

            @Override
            public void released(@Nonnull LocoAddress address, long timeNanos) {
                told.add(address + " released");
            }
        });
        LocoNetThrottle throttle = new LocoNetThrottle(memo, slotmanager.slot(3));
        manager.notifyThrottleKnown(throttle, throttle.getLocoAddress());
        assertEquals(new DccLocoAddress(3, false), throttle.getLocoAddress());

        // another throttle sets slot 3's speed, then its direction
        LocoNetMessage speed = new LocoNetMessage(4);
        speed.setOpCode(LnConstants.OPC_LOCO_SPD);
        speed.setElement(1, 3);
        speed.setElement(2, 64);
        slotmanager.message(speed);
        float setting = throttle.getSpeedSetting();
        assertTrue(setting > 0, "the throttle took the other throttle's speed");
        LocoNetMessage dirf = new LocoNetMessage(4);
        dirf.setOpCode(LnConstants.OPC_LOCO_DIRF);
        dirf.setElement(1, 3);
        dirf.setElement(2, LnConstants.DIRF_DIR);
        slotmanager.message(dirf);

        assertEquals(List.of("3(S) " + setting + " forward", "3(S) " + setting + " reverse"), told,
                "the speed and direction the throttle took, as they changed");
        assertFalse(throttle.getIsForward());
    }

    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        lnis = new LocoNetInterfaceScaffold();
        slotmanager = new SlotManager(lnis);
        // slot 3 holds address 3, in use
        LocoNetMessage m = new LocoNetMessage(13);
        m.setOpCode(LnConstants.OPC_WR_SL_DATA);
        m.setElement(1, 0x0E);
        m.setElement(2, 0x03);
        m.setElement(3, LnConstants.LOCO_IN_USE);
        m.setElement(4, 0x03);
        slotmanager.slot(3).setSlot(m);
        memo = new LocoNetSystemConnectionMemo(lnis, slotmanager);
        memo.setThrottleManager(new LnThrottleManager(memo));
        memo.store(slotmanager, CommandStation.class);
    }

    @AfterEach
    public void tearDown() {
        memo.getThrottleManager().dispose();
        memo.dispose();
        slotmanager.dispose();
        JUnitUtil.tearDown();
    }
}
