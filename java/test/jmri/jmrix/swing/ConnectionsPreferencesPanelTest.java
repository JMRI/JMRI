package jmri.jmrix.swing;

import jmri.InstanceManager;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.ConfigurationManager;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.swing.PreferencesPanelTestBase;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;
import jmri.util.swing.JemmyUtil;
import jmri.util.startup.PerformActionModel;
import jmri.util.startup.StartupActionsManager;

import org.junit.jupiter.api.*;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ConnectionsPreferencesPanelTest extends PreferencesPanelTestBase<ConnectionsPreferencesPanel> {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initConnectionConfigManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        prefsPanel = new ConnectionsPreferencesPanel();
    }

    @Override
    @AfterEach
    public void tearDown() {
        // CAN connections created by the tests add shutdown tasks
        JUnitUtil.clearShutDownManager();
        super.tearDown();
    }

    @Override
    @Test
    public void isRestartRequired(){
        assertThat(prefsPanel.isRestartRequired()).isTrue();
    }

    @Override
    @Test
    public void isDirty(){
        assertThat(prefsPanel.isDirty()).isTrue();
    }

    private static PerformActionModel action(String prefix) {
        PerformActionModel model = new PerformActionModel();
        model.setClassName("jmri.jmrix.can.cbus.swing.console.CbusConsolePane$Default");
        model.setSystemPrefix(prefix);
        return model;
    }

    @Test
    public void testStartupActionChoiceRemove() {
        StartupActionsManager manager = new StartupActionsManager();
        PerformActionModel model = action("M");
        manager.addAction(model);
        ConnectionsPreferencesPanel.applyStartupActionChoice(manager, model, null);
        assertThat(manager.getActions()).isEmpty();
    }

    @Test
    public void testStartupActionChoiceMove() {
        StartupActionsManager manager = new StartupActionsManager();
        PerformActionModel model = action("M");
        manager.addAction(model);
        ConnectionsPreferencesPanel.applyStartupActionChoice(manager, model, "M2");
        assertThat(manager.getActions()).containsExactly(model);
        assertThat(model.getSystemPrefix()).isEqualTo("M2");
    }

    @Test
    public void testStartupActionChoiceText() {
        assertThat(new ConnectionsPreferencesPanel.StartupActionChoice(null, "Remove")).hasToString("Remove");
        assertThat(new ConnectionsPreferencesPanel.StartupActionChoice("M2", "Move").systemPrefix).isEqualTo("M2");
    }

    @Test
    public void testNoSystemPrefixWithoutConnection() {
        assertThat(ConnectionsPreferencesPanel.getSystemPrefix(null)).isNull();
    }

    private static CanSystemConnectionMemo cbusMemo(String prefix, String userName) {
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo(prefix);
        memo.setUserName(userName);
        memo.setTrafficController(new TrafficControllerScaffold());
        memo.setProtocol(ConfigurationManager.MERGCBUS);
        memo.register();
        return memo;
    }

    private StartupActionsManager managerWith(PerformActionModel... models) {
        StartupActionsManager manager = new StartupActionsManager();
        for (PerformActionModel model : models) {
            manager.addAction(model);
        }
        InstanceManager.setDefault(StartupActionsManager.class, manager);
        return manager;
    }

    @Test
    public void testResolveWithNoActionsCarriesOn() {
        StartupActionsManager manager = managerWith(action("M2"));
        // no dialog: the connection being deleted has no Start Up actions
        assertThat(prefsPanel.resolveStartupActions("M", "CBUS")).isTrue();
        assertThat(prefsPanel.resolveStartupActions(null, "New connection")).isTrue();
        assertThat(manager.getActions()).hasSize(1);
    }

    @Test
    @DisabledIfHeadless
    public void testResolveCancelKeepsConnectionAndActions() {
        cbusMemo("M", "CBUS Sim");
        cbusMemo("M2", "CBUS hardware");
        PerformActionModel model = action("M");
        StartupActionsManager manager = managerWith(model);

        Thread t = JemmyUtil.createModalDialogOperatorThread("Delete Connection", "Cancel");
        boolean carryOn = ThreadingUtil.runOnGUIwithReturn(() -> prefsPanel.resolveStartupActions("M", "CBUS Sim"));
        JUnitUtil.waitFor(() -> !t.isAlive(), "dialog closed");

        assertThat(carryOn).as("deletion cancelled").isFalse();
        assertThat(manager.getActions()).containsExactly(model);
        assertThat(model.getSystemPrefix()).isEqualTo("M");
    }

    @Test
    @DisabledIfHeadless
    public void testResolveRemoveIsTheDefault() {
        cbusMemo("M", "CBUS Sim");
        cbusMemo("M2", "CBUS hardware");
        PerformActionModel model = action("M");
        StartupActionsManager manager = managerWith(model);

        Thread t = JemmyUtil.createModalDialogOperatorThread("Delete Connection", "OK");
        boolean carryOn = ThreadingUtil.runOnGUIwithReturn(() -> prefsPanel.resolveStartupActions("M", "CBUS Sim"));
        JUnitUtil.waitFor(() -> !t.isAlive(), "dialog closed");

        assertThat(carryOn).isTrue();
        assertThat(manager.getActions()).isEmpty();
    }

    @Test
    @DisabledIfHeadless
    public void testResolveMoveToOtherConnection() {
        cbusMemo("M", "CBUS Sim");
        cbusMemo("M2", "CBUS hardware");
        PerformActionModel model = action("M");
        StartupActionsManager manager = managerWith(model);

        Thread t = new Thread(() -> {
            JDialogOperator dialog = new JDialogOperator("Delete Connection");
            JComboBoxOperator box = new JComboBoxOperator(dialog);
            assertThat(box.getItemCount()).as("remove, or move to M2").isEqualTo(2);
            assertThat(box.getItemAt(1).toString()).contains("CBUS hardware").contains("M2");
            box.setSelectedIndex(1);
            JemmyUtil.pressButton(dialog, "OK");
        });
        t.start();
        boolean carryOn = ThreadingUtil.runOnGUIwithReturn(() -> prefsPanel.resolveStartupActions("M", "CBUS Sim"));
        JUnitUtil.waitFor(() -> !t.isAlive(), "dialog closed");

        assertThat(carryOn).isTrue();
        assertThat(manager.getActions()).containsExactly(model);
        assertThat(model.getSystemPrefix()).isEqualTo("M2");
    }

    // private static final Logger log = LoggerFactory.getLogger(ConnectionsPreferencesPanelTest.class);

}
