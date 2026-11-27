package jmri.jmrit.logixng.util.swing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;

import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test ThrottleProtocolComboBox.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class ThrottleProtocolComboBoxTest {

    private static List<String> items(ThrottleProtocolComboBox box) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < box.getItemCount(); i++) {
            list.add(box.getItemAt(i).toString());
        }
        return list;
    }

    @Test
    public void testDefaultConnectionProtocols() {
        JUnitUtil.initDebugThrottleManager();
        ThrottleProtocolComboBox box = ThreadingUtil.runOnGUIwithReturn(() -> new ThrottleProtocolComboBox());

        assertEquals( List.of("Default", "DCC", "DCC Short", "DCC Long"), items(box),
                "Default, then the protocols of the default throttle manager");
        assertNull( box.getSelectedProtocol(), "Default selected");
    }

    @Test
    public void testSelectProtocolOfConnection() {
        JUnitUtil.initDebugThrottleManager();
        InternalSystemConnectionMemo memo = InstanceManager.getDefault(InternalSystemConnectionMemo.class);
        ThrottleProtocolComboBox box = ThreadingUtil.runOnGUIwithReturn(() -> new ThrottleProtocolComboBox());

        ThreadingUtil.runOnGUI(() -> box.setConnection(memo, LocoAddress.Protocol.DCC_LONG));
        assertEquals( LocoAddress.Protocol.DCC_LONG, box.getSelectedProtocol());

        ThreadingUtil.runOnGUI(() -> box.setConnection(memo, null));
        assertNull( box.getSelectedProtocol(), "null selects Default");
    }

    @Test
    public void testUnsupportedProtocolSelectsDefault() {
        JUnitUtil.initDebugThrottleManager();
        InternalSystemConnectionMemo memo = InstanceManager.getDefault(InternalSystemConnectionMemo.class);
        ThrottleProtocolComboBox box = ThreadingUtil.runOnGUIwithReturn(() -> new ThrottleProtocolComboBox());

        ThreadingUtil.runOnGUI(() -> box.setConnection(memo, LocoAddress.Protocol.MFX));
        assertNull( box.getSelectedProtocol(), "MFX isn't a protocol of this connection");
        assertEquals( 4, box.getItemCount(), "Default and three DCC protocols");
    }

    @Test
    public void testNoThrottleManager() {
        ThrottleProtocolComboBox box = ThreadingUtil.runOnGUIwithReturn(() -> new ThrottleProtocolComboBox());

        assertEquals( List.of("Default"), items(box), "only Default without a throttle manager");
        assertNull( box.getSelectedProtocol());
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
