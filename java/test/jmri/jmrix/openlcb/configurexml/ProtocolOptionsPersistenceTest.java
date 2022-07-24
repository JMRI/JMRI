package jmri.jmrix.openlcb.configurexml;

import static org.junit.Assert.*;

import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Date;

import jmri.InstanceManager;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.ConnectionConfigManager;
import jmri.jmrix.PortAdapter;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.profile.Profile;
import jmri.util.JUnitUtil;
import jmri.util.prefs.HasConnectionButUnableToConnectException;

/**
 * @author Balazs Racz, (C) 2018.
 */

public class ProtocolOptionsPersistenceTest {

    private Path workspace = null;
    private Profile profile;
    private ConnectionConfigManager connectionConfigManager;
    private CanSystemConnectionMemo canSystemConnectionMemo;
    private String profileId;
    private PortAdapter adapter;

    @BeforeAll
    static public void checkSeparate() {
       // this test is run separately because it leaves a lot of threads behind
        org.junit.Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        this.workspace = FileSystems.getDefault().getPath("/tmp/testprofile");
        //this.workspace = Files.createTempDirectory(this.getClass().getSimpleName());
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly

        JUnitUtil.resetProfileManager();
        JUnitUtil.resetFileUtilSupport();
        //FileUtil.delete(this.workspace.toFile());
        JUnitUtil.tearDown();
    }

    private void resetSystem() {
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.resetFileUtilSupport();

        JUnitUtil.initConfigureManager();

        profile = null;
        connectionConfigManager = null;
        canSystemConnectionMemo = null;
        adapter = null;
    }

    private void createEmptyProfile() throws IOException, HasConnectionButUnableToConnectException {
        profileId = Long.toString((new Date()).getTime());
        Assertions.assertNotNull(this.workspace);
        profile = new Profile(this.getClass().getSimpleName(), profileId, new File(this.workspace.toFile(), profileId));
        JUnitUtil.initConnectionConfigManager();
        connectionConfigManager = InstanceManager.getDefault(ConnectionConfigManager.class);
        connectionConfigManager.initialize(profile);
    }

    private void addLoopbackCanAdapter() {
        jmri.jmrix.SerialPortAdapter a = new jmri.jmrix.can.adapters.loopback.Port();
        adapter = a;
        jmri.jmrix.can.adapters.loopback.ConnectionConfig cfg = new jmri.jmrix.can.adapters
                .loopback.ConnectionConfig(a);
        canSystemConnectionMemo = (CanSystemConnectionMemo) a.getSystemConnectionMemo();
        assertNotNull(canSystemConnectionMemo);
        cfg.setManufacturer("Foo Bar");
        a.setUserName("OOOO");
        a.setOptionState("Protocol", "OpenLCB");
        connectionConfigManager.add(cfg);
        assertEquals(1, connectionConfigManager.getConnections().length);
    }

    private void restart() throws IOException, HasConnectionButUnableToConnectException {
        resetSystem();

        JUnitUtil.initConnectionConfigManager();
        connectionConfigManager = InstanceManager.getDefault(ConnectionConfigManager.class);
        Assertions.assertNotNull(this.workspace);
        profile = new Profile(new File(this.workspace.toFile(), profileId));
        connectionConfigManager.initialize(profile);

        ConnectionConfig[] connList = connectionConfigManager.getConnections();
        assertEquals(1, connList.length);
        canSystemConnectionMemo = (CanSystemConnectionMemo)connList[0].getAdapter().getSystemConnectionMemo();
        assertNotNull(canSystemConnectionMemo);
    }

    private void saveAndRestart() throws HasConnectionButUnableToConnectException, IOException {
        connectionConfigManager.savePreferences(profile);
        restart();
    }

    @Test
    public void testSaveAndRestart() throws IOException, HasConnectionButUnableToConnectException {
        createEmptyProfile();
        addLoopbackCanAdapter();
        saveAndRestart();
    }

    @Test
    public void testSaveSCMOptionsForOpenLCB() throws IOException,
            HasConnectionButUnableToConnectException {
        createEmptyProfile();
        addLoopbackCanAdapter();
        canSystemConnectionMemo.setProtocolOption("Ident", "UserName", "Hello, World");
        saveAndRestart();
        assertEquals("Hello, World", canSystemConnectionMemo.getProtocolOption("Ident", "UserName"));
    }

    @Test
    public void testNotSaveSCMOptionsForNotOpenLCB() throws IOException,
            HasConnectionButUnableToConnectException {
        createEmptyProfile();
        addLoopbackCanAdapter();
        adapter.setOptionState("Protocol", "MERG");
        canSystemConnectionMemo.setProtocolOption("Ident", "UserName", "Hello, World");
        saveAndRestart();
        assertNull(canSystemConnectionMemo.getProtocolOption("Ident", "UserName"));
    }

    private void expectOptionValue(String protocol, String option, String value) {
        assertEquals(value, canSystemConnectionMemo.getProtocolOption(protocol, option));
    }

    @Test
    public void testManyOptions() throws IOException,
            HasConnectionButUnableToConnectException {
        createEmptyProfile();
        addLoopbackCanAdapter();
        canSystemConnectionMemo.setProtocolOption("Ident", "UserName", "Hello, World");
        canSystemConnectionMemo.setProtocolOption("Ident", "Description", "something");
        canSystemConnectionMemo.setProtocolOption("Throttles", "Used", "1");
        canSystemConnectionMemo.setProtocolOption("Throttles", "Steal", "2");
        canSystemConnectionMemo.setProtocolOption("Throttles", "MaxFn", "29");
        canSystemConnectionMemo.setProtocolOption("Single", "Foo", "");

        saveAndRestart();
        assertEquals("Hello, World", canSystemConnectionMemo.getProtocolOption("Ident", "UserName"));

        assertTrue("getProtocolsWithOptions has Ident", canSystemConnectionMemo.getProtocolsWithOptions().contains("Ident"));
        assertTrue("getProtocolsWithOptions has Throttles", canSystemConnectionMemo.getProtocolsWithOptions().contains("Throttles"));
        assertTrue("getProtocolsWithOptions has Single", canSystemConnectionMemo.getProtocolsWithOptions().contains("Single"));
        assertEquals(3, canSystemConnectionMemo.getProtocolsWithOptions().size());

        assertTrue("Ident has UserName", canSystemConnectionMemo.getProtocolAllOptions("Ident").keySet().contains("UserName"));
        assertTrue("Ident has Description",canSystemConnectionMemo.getProtocolAllOptions("Ident").keySet().contains("Description"));
        assertEquals(2, canSystemConnectionMemo.getProtocolAllOptions("Ident").size());

        assertTrue("Throttles has Used", canSystemConnectionMemo.getProtocolAllOptions("Throttles").keySet().contains("Used"));
        assertTrue("Throttles has Steal", canSystemConnectionMemo.getProtocolAllOptions("Throttles").keySet().contains("Steal"));
        assertTrue("Throttles has MaxFn", canSystemConnectionMemo.getProtocolAllOptions("Throttles").keySet().contains("MaxFn"));
        assertEquals(3, canSystemConnectionMemo.getProtocolAllOptions("Throttles").size());

        assertTrue("Single has Foo", canSystemConnectionMemo.getProtocolAllOptions("Single").keySet().contains("Foo"));
        assertEquals(1, canSystemConnectionMemo.getProtocolAllOptions("Single").size());

        expectOptionValue("Ident", "UserName", "Hello, World");
        expectOptionValue("Ident", "Description", "something");
        expectOptionValue("Throttles", "Used", "1");
        expectOptionValue("Throttles", "Steal", "2");
        expectOptionValue("Throttles", "MaxFn", "29");
        expectOptionValue("Single", "Foo", "");
    }

    // private final static Logger log = LoggerFactory.getLogger(ProtocolOptionsPersistenceTest.class);
}
