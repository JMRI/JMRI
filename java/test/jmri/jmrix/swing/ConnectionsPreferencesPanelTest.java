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

    private static final String CONSOLE = "jmri.jmrix.can.cbus.swing.console.CbusConsolePane$Default";
    private static final String NODE_MANAGER = "jmri.jmrix.can.cbus.swing.nodeconfig.NodeConfigToolPane$Default";

    private static PerformActionModel action(String className, String prefix) {
        PerformActionModel model = new PerformActionModel();
        model.setClassName(className);
        model.setSystemPrefix(prefix);
        return model;
    }

    private static PerformActionModel action(String prefix) {
        return action(CONSOLE, prefix);
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

    private static ConnectionsPreferencesPanel.StartupActionChoice choice(
            ConnectionsPreferencesPanel.StartupActionChoice.Kind kind, String prefix) {
        return new ConnectionsPreferencesPanel.StartupActionChoice(kind, prefix, kind.name());
    }

    @Test
    public void testApplyRemove() {
        StartupActionsManager manager = new StartupActionsManager();
        PerformActionModel model = action("M");
        manager.addAction(model);
        ConnectionsPreferencesPanel.applyStartupActionChoice(manager, model,
                choice(ConnectionsPreferencesPanel.StartupActionChoice.Kind.REMOVE, null));
        assertThat(manager.getActions()).isEmpty();
    }

    @Test
    public void testApplyNoChoiceRemoves() {
        StartupActionsManager manager = new StartupActionsManager();
        PerformActionModel model = action("M");
        manager.addAction(model);
        ConnectionsPreferencesPanel.applyStartupActionChoice(manager, model, null);
        assertThat(manager.getActions()).isEmpty();
    }

    @Test
    public void testApplyKeep() {
        StartupActionsManager manager = new StartupActionsManager();
        PerformActionModel model = action("M");
        manager.addAction(model);
        ConnectionsPreferencesPanel.applyStartupActionChoice(manager, model,
                choice(ConnectionsPreferencesPanel.StartupActionChoice.Kind.KEEP, null));
        assertThat(manager.getActions()).containsExactly(model);
        assertThat(model.getSystemPrefix()).as("still for the deleted connection").isEqualTo("M");
    }

    @Test
    public void testApplyMove() {
        StartupActionsManager manager = new StartupActionsManager();
        PerformActionModel model = action("M");
        manager.addAction(model);
        ConnectionsPreferencesPanel.applyStartupActionChoice(manager, model,
                choice(ConnectionsPreferencesPanel.StartupActionChoice.Kind.MOVE, "M2"));
        assertThat(manager.getActions()).containsExactly(model);
        assertThat(model.getSystemPrefix()).isEqualTo("M2");
    }

    @Test
    public void testChoicesRemoveFirstThenKeepThenMoves() {
        cbusMemo("M", "CBUS Sim");
        cbusMemo("M2", "CBUS hardware");
        cbusMemo("M3", "CBUS spare");
        PerformActionModel model = action("M");
        StartupActionsManager manager = managerWith(model);

        java.util.List<ConnectionsPreferencesPanel.StartupActionChoice> choices =
                ConnectionsPreferencesPanel.getChoices(manager, model, "M");
        assertThat(choices).extracting(c -> c.kind).containsExactly(
                ConnectionsPreferencesPanel.StartupActionChoice.Kind.REMOVE,
                ConnectionsPreferencesPanel.StartupActionChoice.Kind.KEEP,
                ConnectionsPreferencesPanel.StartupActionChoice.Kind.MOVE,
                ConnectionsPreferencesPanel.StartupActionChoice.Kind.MOVE);
        assertThat(choices.get(0)).hasToString("Remove");
        assertThat(choices).extracting(c -> c.systemPrefix).containsExactlyInAnyOrder(null, null, "M2", "M3");
    }

    @Test
    public void testChoicesLeaveOutConnectionThatAlreadyHasTheAction() {
        cbusMemo("M", "CBUS Sim");
        cbusMemo("M2", "CBUS hardware");
        PerformActionModel console = action("M");
        PerformActionModel nodeManager = action(NODE_MANAGER, "M");
        StartupActionsManager manager = managerWith(console, nodeManager, action("M2"));

        assertThat(ConnectionsPreferencesPanel.getChoices(manager, console, "M"))
                .as("M2 already opens a console").hasSize(2);
        assertThat(ConnectionsPreferencesPanel.getChoices(manager, nodeManager, "M"))
                .as("M2 has no node manager action").hasSize(3);
    }

    @Test
    public void testNoSystemPrefixWithoutConnection() {
        assertThat(ConnectionsPreferencesPanel.getSystemPrefix(null)).isNull();
    }

    @Test
    @DisabledIfHeadless
    public void testNoActionsAsksYesNo() {
        StartupActionsManager manager = managerWith(action("M2"));
        Thread t = JemmyUtil.createModalDialogOperatorThread("Delete Connection", "Yes");
        boolean carryOn = ThreadingUtil.runOnGUIwithReturn(() -> prefsPanel.confirmDelete("M", "CBUS Sim"));
        JUnitUtil.waitFor(() -> !t.isAlive(), "dialog closed");
        assertThat(carryOn).isTrue();
        assertThat(manager.getActions()).as("other connection's action untouched").hasSize(1);
    }

    @Test
    @DisabledIfHeadless
    public void testCancelKeepsConnectionAndActions() {
        cbusMemo("M", "CBUS Sim");
        cbusMemo("M2", "CBUS hardware");
        PerformActionModel model = action("M");
        StartupActionsManager manager = managerWith(model);

        Thread t = JemmyUtil.createModalDialogOperatorThread("Delete Connection", "Cancel");
        boolean carryOn = ThreadingUtil.runOnGUIwithReturn(() -> prefsPanel.confirmDelete("M", "CBUS Sim"));
        JUnitUtil.waitFor(() -> !t.isAlive(), "dialog closed");

        assertThat(carryOn).as("deletion cancelled").isFalse();
        assertThat(manager.getActions()).containsExactly(model);
        assertThat(model.getSystemPrefix()).isEqualTo("M");
    }

    @Test
    @DisabledIfHeadless
    public void testDeleteWithDefaultsRemovesActions() {
        cbusMemo("M", "CBUS Sim");
        cbusMemo("M2", "CBUS hardware");
        PerformActionModel model = action("M");
        StartupActionsManager manager = managerWith(model);

        Thread t = JemmyUtil.createModalDialogOperatorThread("Delete Connection", "Delete Connection");
        boolean carryOn = ThreadingUtil.runOnGUIwithReturn(() -> prefsPanel.confirmDelete("M", "CBUS Sim"));
        JUnitUtil.waitFor(() -> !t.isAlive(), "dialog closed");

        assertThat(carryOn).isTrue();
        assertThat(manager.getActions()).isEmpty();
    }

    @Test
    @DisabledIfHeadless
    public void testDeleteWithKeepAndMove() {
        cbusMemo("M", "CBUS Sim");
        cbusMemo("M2", "CBUS hardware");
        PerformActionModel console = action("M");
        PerformActionModel nodeManager = action(NODE_MANAGER, "M");
        StartupActionsManager manager = managerWith(console, nodeManager);

        Thread t = new Thread(() -> {
            JDialogOperator dialog = new JDialogOperator("Delete Connection");
            JComboBoxOperator consoleBox = new JComboBoxOperator(dialog, 0);
            JComboBoxOperator nodeBox = new JComboBoxOperator(dialog, 1);
            assertThat(consoleBox.getSelectedIndex()).as("remove is the default").isZero();
            consoleBox.setSelectedIndex(2); // move to M2
            nodeBox.setSelectedIndex(1); // keep
            JemmyUtil.pressButton(dialog, "Delete Connection");
        });
        t.start();
        boolean carryOn = ThreadingUtil.runOnGUIwithReturn(() -> prefsPanel.confirmDelete("M", "CBUS Sim"));
        JUnitUtil.waitFor(() -> !t.isAlive(), "dialog closed");

        assertThat(carryOn).isTrue();
        assertThat(manager.getActions()).containsExactly(console, nodeManager);
        assertThat(console.getSystemPrefix()).isEqualTo("M2");
        assertThat(nodeManager.getSystemPrefix()).as("kept for the deleted connection").isEqualTo("M");
    }

    // private static final Logger log = LoggerFactory.getLogger(ConnectionsPreferencesPanelTest.class);

}
