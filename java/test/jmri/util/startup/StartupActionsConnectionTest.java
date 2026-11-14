package jmri.util.startup;

import java.io.File;
import java.io.IOException;

import jmri.JmriException;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.ConfigurationManager;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.profile.NullProfile;
import jmri.profile.Profile;
import jmri.profile.ProfileUtils;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.jdom.JDOMUtil;

import org.jdom2.Element;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Startup actions that belong to a system connection must survive the
 * connection being disabled, removed, or not starting, and must not be run
 * against a missing or wrong connection.
 * <p>
 * Uses CBUS connections, because several CBUS connections offer the same
 * action classes, which is how the actions of one connection were lost when
 * another was removed.
 */
public class StartupActionsConnectionTest {

    private static final String CONSOLE = "jmri.jmrix.can.cbus.swing.console.CbusConsolePane$Default";

    private Profile profile;

    private CanSystemConnectionMemo cbusMemo(String prefix, String userName) {
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo(prefix);
        memo.setUserName(userName);
        memo.setTrafficController(new TrafficControllerScaffold());
        memo.setProtocol(ConfigurationManager.MERGCBUS);
        memo.register();
        return memo;
    }

    private PerformActionModel consoleAction(String prefix) {
        PerformActionModel model = new PerformActionModel();
        model.setClassName(CONSOLE);
        model.setSystemPrefix(prefix);
        model.setEnabled(true);
        return model;
    }

    private Element savedStartup(StartupActionsManager manager) {
        manager.savePreferences(profile);
        return JDOMUtil.toJDOMElement(ProfileUtils.getAuxiliaryConfiguration(profile)
                .getConfigurationFragment(StartupActionsManager.STARTUP, StartupActionsManager.NAMESPACE, true));
    }

    @Test
    public void testActionSavedWhenConnectionNotRunning() {
        // no CBUS connection: as when the connection is disabled or its port fails to open
        PerformActionModel model = consoleAction("M");
        assertNull(model.getName(), "name unknown without a CBUS connection");
        assertTrue(model.isSystemConnectionAction(), "recognised from the class");
        assertFalse(model.isConnectionAvailable());
        assertFalse(model.isValid(), "not run while its connection is missing");
        assertTrue(model.toString().contains("not available"), model.toString());

        StartupActionsManager manager = new StartupActionsManager();
        manager.addAction(model);
        Element saved = savedStartup(manager);

        assertEquals(1, saved.getChildren().size(), "action kept in preferences");
        Element perform = saved.getChildren().get(0);
        assertEquals(CONSOLE, perform.getAttributeValue("name"));
        Element property = perform.getChildren().stream()
                .filter(e -> "property".equals(e.getName())).findFirst().orElseThrow();
        assertEquals("systemPrefix", property.getAttributeValue("name"));
        assertEquals("M", property.getAttributeValue("value"), "connection prefix kept");
    }

    @Test
    public void testSkippedActionShownWhenAnotherConnectionOfSameTypeRuns() {
        // M is running and offers the console action, so its name is known,
        // but the action belongs to M2, which is not running
        cbusMemo("M", "CBUS");
        PerformActionModel running = consoleAction("M");
        PerformActionModel skipped = consoleAction("M2");
        assertNotNull(skipped.getName(), "name known from the running connection");
        assertFalse(skipped.isConnectionAvailable());
        assertTrue(skipped.toString().contains("not available"), skipped.toString());
        assertTrue(skipped.toString().contains(skipped.getName()), skipped.toString());
        assertTrue(skipped.toString().contains("M2"), skipped.toString());
        assertFalse(running.toString().contains("not available"), running.toString());
    }

    @Test
    public void testActionNotCreatedWhenConnectionNotRunning() throws JmriException {
        // Before the fix the action was created anyway, and the CBUS console
        // action's constructor fails when there is no CAN connection.
        PerformActionModel model = consoleAction("M");
        model.performAction();
        JUnitAppender.assertWarnMessageStartsWith("Connection \"M\" does not exist and cannot be assigned to action " + CONSOLE);
    }

    @Test
    public void testActionNotRunOnAnotherConnection() {
        // Only connection M2 is running; an action for M must not run on it.
        CanSystemConnectionMemo m2 = cbusMemo("M2", "CBUS hardware");
        PerformActionModel model = consoleAction("M");
        assertNotNull(model.getName(), "name known from M2");
        assertFalse(model.isValid(), "M is not running");
        m2.dispose();
    }

    @Test
    public void testActionNamesKeptWhenOneOfTwoConnectionsRemoved() {
        CanSystemConnectionMemo sim = cbusMemo("M", "CBUS Sim");
        CanSystemConnectionMemo hw = cbusMemo("M2", "CBUS hardware");
        PerformActionModel model = consoleAction("M");
        assertNotNull(model.getName());

        hw.dispose();
        assertNotNull(model.getName(), "still offered by connection M");
        assertTrue(model.isValid());

        StartupActionsManager manager = new StartupActionsManager();
        manager.addAction(model);
        assertEquals(1, savedStartup(manager).getChildren().size());

        sim.dispose();
        assertNull(model.getName(), "no CBUS connection offers it now");
        assertEquals(1, savedStartup(manager).getChildren().size(), "but it is still saved");
    }

    @Test
    public void testActionWorksAgainWhenConnectionReturns() {
        // Switching: the session starts without connection M (simulator
        // disabled), preferences are saved, then a later session has M again.
        PerformActionModel model = consoleAction("M");
        StartupActionsManager manager = new StartupActionsManager();
        manager.addAction(model);
        assertFalse(model.isValid());
        assertEquals(1, savedStartup(manager).getChildren().size());

        CanSystemConnectionMemo sim = cbusMemo("M", "CBUS Sim");
        assertTrue(model.isValid());
        assertEquals("Open CBUS Console", model.getName());
        assertTrue(model.toString().contains("CBUS Sim"), model.toString());
        sim.dispose();
    }

    @Test
    public void testActionsForConnection() {
        cbusMemo("M", "CBUS Sim");
        PerformActionModel forM = consoleAction("M");
        PerformActionModel forM2 = consoleAction("M2");
        PerformActionModel general = new PerformActionModel();
        general.setClassName("jmri.jmrit.beantable.SensorTableAction");
        StartupActionsManager manager = new StartupActionsManager();
        manager.addAction(forM);
        manager.addAction(general);
        manager.addAction(forM2);

        assertEquals(java.util.List.of(forM), manager.getActionsForConnection("M"));
        assertEquals(java.util.List.of(forM2), manager.getActionsForConnection("M2"));
        assertTrue(manager.getActionsForConnection("L").isEmpty());
    }

    @Test
    public void testConnectionsOfferingAction() {
        CanSystemConnectionMemo sim = cbusMemo("M", "CBUS Sim");
        CanSystemConnectionMemo hw = cbusMemo("M2", "CBUS hardware");
        PerformActionModel model = consoleAction("M");

        assertEquals(java.util.List.of(hw), StartupActionsManager.getConnectionsOffering(model, "M"),
                "the other CBUS connection, not the one being deleted");
        assertEquals(2, StartupActionsManager.getConnectionsOffering(model, null).size());

        hw.dispose();
        assertTrue(StartupActionsManager.getConnectionsOffering(model, "M").isEmpty(), "no other CBUS connection");
        sim.dispose();
    }

    @Test
    public void testReassignAction() {
        cbusMemo("M", "CBUS Sim");
        cbusMemo("M2", "CBUS hardware");
        PerformActionModel model = consoleAction("M");
        StartupActionsManager manager = new StartupActionsManager();
        manager.addAction(model);
        manager.savePreferences(profile);
        assertFalse(manager.isDirty());

        java.util.List<java.beans.PropertyChangeEvent> events = new java.util.ArrayList<>();
        manager.addPropertyChangeListener(StartupActionsManager.STARTUP, events::add);
        manager.reassignAction(model, "M2");

        assertEquals("M2", model.getSystemPrefix());
        assertTrue(manager.isDirty(), "change will be saved");
        assertEquals(1, events.size(), "table told the row changed");
        assertEquals(0, ((java.beans.IndexedPropertyChangeEvent) events.get(0)).getIndex());
        assertEquals(1, manager.getActions().length, "still one action");

        Element perform = savedStartup(manager).getChildren().get(0);
        Element property = perform.getChildren().stream()
                .filter(e -> "property".equals(e.getName())).findFirst().orElseThrow();
        assertEquals("M2", property.getAttributeValue("value"), "saved against the new connection");
    }

    @Test
    public void testReassignActionNotInManagerIgnored() {
        PerformActionModel model = consoleAction("M");
        StartupActionsManager manager = new StartupActionsManager();
        manager.reassignAction(model, "M2");
        assertEquals("M", model.getSystemPrefix(), "unchanged");
        assertFalse(manager.isDirty());
    }

    @Test
    public void testActionWithMissingClassStillInvalid() {
        PerformActionModel model = new PerformActionModel();
        model.setClassName("jmri.NoSuchStartupActionClass");
        model.setSystemPrefix("M");
        assertFalse(model.isSystemConnectionAction());
        assertFalse(model.isValid());
        String text = model.toString();
        assertTrue(text.startsWith("Unable to find class"), text);
        // logged by the name lookup in toString(), as before this change
        JUnitAppender.assertErrorMessage("Did not find class \"jmri.NoSuchStartupActionClass\"");
    }

    @BeforeEach
    public void setUp(@TempDir File folder) throws IOException {
        JUnitUtil.setUp();
        profile = new NullProfile(folder);
        JUnitUtil.resetProfileManager(profile);
        JUnitUtil.initConfigureManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

}
