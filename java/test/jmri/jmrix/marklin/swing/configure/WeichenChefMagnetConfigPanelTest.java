package jmri.jmrix.marklin.swing.configure;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import jmri.jmrix.marklin.MarklinReply;
import jmri.jmrix.marklin.MarklinSystemConnectionMemo;
import jmri.jmrix.marklin.MarklinTrafficControlScaffold;
import jmri.jmrix.marklin.McanDevice;
import jmri.jmrix.marklin.McanDeviceIdentity;
import jmri.jmrix.marklin.WeichenChefMagnetGeneration;
import jmri.jmrix.marklin.WeichenChefMagnetService;
import jmri.jmrix.marklin.mcan.cdb.CdBConfigCommands;
import jmri.jmrix.marklin.mcan.cdb.CdBWeichenChefCommands;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import static org.junit.jupiter.api.Assertions.*;

@jmri.util.junit.annotations.DisabledIfHeadless
class WeichenChefMagnetConfigPanelTest {
    private MarklinTrafficControlScaffold tc;
    private MarklinSystemConnectionMemo memo;
    private WeichenChefMagnetService service;
    private WeichenChefMagnetConfigPanel panel;
    private final List<int[]> sent = new ArrayList<>();

    @BeforeEach
    void setUp() {
        JUnitUtil.setUp();
        tc = new MarklinTrafficControlScaffold();
        memo = new MarklinSystemConnectionMemo(tc);
        memo.configureManagers();
        McanDevice device = memo.getMcanDeviceManager().provideDevice(McanDeviceIdentity.forCdB(0x22, 3));
        service = new WeichenChefMagnetService(device, sent::add, (action, delay) -> {});
        device.setDeviceService(service);
        ThreadingUtil.runOnGUI(() -> panel = new WeichenChefMagnetConfigPanel(device, memo));
    }

    @AfterEach
    void tearDown() {
        ThreadingUtil.runOnGUI(panel::dispose);
        tc.dispose();
        memo.dispose();
        JUnitUtil.tearDown();
    }

    private void load(boolean extended) {
        load(extended, true);
    }

    private void load(boolean extended, boolean secondAddress) {
        for (int[] request : CdBWeichenChefCommands.buildMagnetReads(3, extended)) {
            int reg = request[6];
            int slot = request[5];
            if (reg == 224) {
                continue;
            }
            int value = reg == 1 ? 0x020F : reg == 6 || reg == 8 ? 0x3800 + slot
                    : reg == 7 ? 0x123 : reg == 2 ? 100 : reg == 16 ? 40 : reg == 17 ? 35 : 1;
            if (reg == 9) {
                value = secondAddress ? 1 : 0;
            }
            if (reg == 6 || reg == 7 || reg == 8) {
                value = ((value & 0xFF) << 8) | (value >> 8);
            }
            MarklinReply reply = new MarklinReply(CdBConfigCommands.buildReadReply(0x22, 3, slot, reg, value));
            assertTrue(tc.getCdBFilter().offer(reply));
        }
    }

    private List<Component> components(Container root) {
        List<Component> result = new ArrayList<>();
        for (Component component : root.getComponents()) {
            result.add(component);
            if (component instanceof Container) {
                result.addAll(components((Container) component));
            }
        }
        return result;
    }

    private <T extends Component> T named(Class<T> type, String name) {
        return type.cast(components(panel).stream().filter(c -> name.equals(c.getName())).findFirst().orElseThrow());
    }

    private JRadioButton radio(String key) {
        return components(panel).stream().filter(c -> c instanceof JRadioButton)
                .map(JRadioButton.class::cast).filter(b -> b.getText().equals(Bundle.getMessage(key)))
                .findFirst().orElseThrow();
    }

    @Test
    void unknownGatesWritesAndPositiveDetectionShowsPairedEditorsWithoutModalWarnings() {
        assertFalse(named(JButton.class, "savePROPERTIES").isEnabled());
        load(true);
        ThreadingUtil.runOnGUI(() -> {
            assertTrue(service.isReady());
            assertTrue(radio("WcMagnetNewer").isSelected());
            assertTrue(named(JSpinner.class, "reg2slot5").getParent().isVisible());
            assertEquals(1, components(panel).stream().filter(c -> c instanceof JTabbedPane).count());
            assertEquals(8, components(panel).stream().filter(c -> c instanceof JButton
                    && c.getName() != null && c.getName().startsWith("test")).count());
            assertTrue(components(panel).stream().filter(c -> c instanceof JLabel).map(JLabel.class::cast)
                    .anyMatch(l -> l.getText().equals(Bundle.getMessage("WcMagnetFirmwareWarning"))));
            JCheckBox timeout = named(JCheckBox.class, "reg3slot1");
            timeout.doClick();
            assertFalse(named(JSpinner.class, "reg2slot1").isEnabled());
            assertFalse(named(JSpinner.class, "reg2slot5").isEnabled());
            timeout.doClick();
            assertTrue(named(JSpinner.class, "reg2slot5").isEnabled());
            assertFalse(named(JCheckBox.class, "reg4slot1").isSelected());
            named(JCheckBox.class, "reg18slot1").doClick();
            assertFalse(named(JSpinner.class, "reg16slot5").isEnabled());
            assertFalse(named(JSpinner.class, "reg17slot1").isEnabled());
            named(JButton.class, "retest").doClick();
            assertEquals(WeichenChefMagnetGeneration.UNKNOWN, service.getGeneration());
            assertFalse(named(JButton.class, "savePROPERTIES").isEnabled());
        });
    }

    @Test
    void olderProfileKeepsFourTurnoutsAndHidesExtraTimingEditors() {
        ThreadingUtil.runOnGUI(() -> service.useProfile(WeichenChefMagnetGeneration.GEN_001));
        load(false);
        ThreadingUtil.runOnGUI(() -> {
            assertTrue(service.isReady());
            assertFalse(named(JSpinner.class, "reg2slot5").getParent().isVisible());
            assertTrue(named(JSpinner.class, "reg2slot1").getParent().isVisible());
            assertTrue(named(JButton.class, "test4green").isEnabled());
            assertTrue(named(JButton.class, "saveADDRESSES").isEnabled());
            assertTrue(named(JButton.class, "saveFEEDBACK").isEnabled());
            assertEquals(Bundle.getMessage("WcMagnetSharedOutputs", 1),
                    named(JLabel.class, "timingLabel1").getText());
            named(JCheckBox.class, "reg9slot1").doClick();
            assertFalse(named(JPanel.class, "reg8slot1").isEnabled());
            assertTrue(named(JSpinner.class, "reg2slot1").isEnabled());
            assertTrue(named(JSpinner.class, "reg16slot1").isEnabled());
            assertTrue(named(JSpinner.class, "reg17slot1").isEnabled());
        });
    }

    @Test
    void secondaryEditorsFollowModeAndTimingFlagsWithoutLosingValues() {
        load(true, false);
        ThreadingUtil.runOnGUI(() -> {
            JPanel address = named(JPanel.class, "reg8slot1");
            JSpinner number = components(address).stream().filter(c -> c instanceof JSpinner)
                    .map(JSpinner.class::cast).findFirst().orElseThrow();
            JComboBox<?> format = components(address).stream().filter(c -> c instanceof JComboBox)
                    .map(JComboBox.class::cast).findFirst().orElseThrow();
            JCheckBox mode = named(JCheckBox.class, "reg9slot1");
            named(JCheckBox.class, "reg9slot2").doClick();
            assertFalse(address.isEnabled());
            assertFalse(number.isEnabled());
            assertFalse(format.isEnabled());
            for (int reg : new int[]{2, 16, 17}) {
                assertFalse(named(JSpinner.class, "reg" + reg + "slot5").isEnabled());
                assertTrue(named(JSpinner.class, "reg" + reg + "slot1").isEnabled());
            }
            assertFalse(named(JLabel.class, "timingLabel5").isEnabled());
            mode.doClick();
            assertTrue(number.isEnabled());
            assertTrue(format.isEnabled());
            assertTrue(named(JLabel.class, "timingLabel5").isEnabled());
            number.setValue(77);
            named(JSpinner.class, "reg2slot5").setValue(234);
            mode.doClick();
            assertFalse(number.isEnabled());
            assertEquals(77, number.getValue());
            assertEquals(234, named(JSpinner.class, "reg2slot5").getValue());
            mode.doClick();
            assertTrue(named(JSpinner.class, "reg2slot5").isEnabled());
            assertEquals(77, number.getValue());
            assertEquals(234, named(JSpinner.class, "reg2slot5").getValue());
            named(JCheckBox.class, "reg3slot1").doClick();
            named(JCheckBox.class, "reg18slot1").doClick();
            mode.doClick();
            mode.doClick();
            for (int reg : new int[]{2, 16, 17}) {
                assertFalse(named(JSpinner.class, "reg" + reg + "slot5").isEnabled());
            }
            assertTrue(named(JSpinner.class, "reg2slot6").isEnabled(), "other ports are unaffected");
            assertTrue(sent.isEmpty(), "editing the mode must not write it automatically");
        });
    }

    @Test
    void overridingDetectedProfileRequiresWarningAndCanBeCancelled() {
        load(true);
        java.util.concurrent.atomic.AtomicReference<String> warning = new java.util.concurrent.atomic.AtomicReference<>();
        Thread responder = new Thread(() -> {
            JDialogOperator dialog = new JDialogOperator(Bundle.getMessage("WcMagnetTitle"));
            JOptionPane option = components((JDialog) dialog.getSource()).stream()
                    .filter(c -> c instanceof JOptionPane).map(JOptionPane.class::cast).findFirst().orElseThrow();
            warning.set(option.getMessage().toString());
            new JButtonOperator(dialog, UIManager.getString("OptionPane.noButtonText")).push();
        });
        responder.start();
        ThreadingUtil.runOnGUI(() -> radio("WcMagnetOlder").doClick());
        JUnitUtil.waitFor(() -> !responder.isAlive(), "profile warning dismissed");
        assertTrue(warning.get().contains(Bundle.getMessage("WcMagnetNewer")));
        assertEquals(WeichenChefMagnetGeneration.GEN_002_PLUS, service.getGeneration());
        assertTrue(radio("WcMagnetNewer").isSelected());
    }

    @Test
    void closingDetachesStatusListenerAndDeactivatesCoilImmediately() {
        load(true);
        int listeners = tc.getNumberListeners();
        ThreadingUtil.runOnGUI(() -> named(JButton.class, "test2red").doClick());
        assertEquals(1, sent.size());
        ThreadingUtil.runOnGUI(panel::dispose);
        assertEquals(listeners - 1, tc.getNumberListeners());
        assertEquals(2, sent.size());
        assertEquals(0, sent.get(1)[10]);
        assertFalse(service.isConfigurationOpen());
    }
}
