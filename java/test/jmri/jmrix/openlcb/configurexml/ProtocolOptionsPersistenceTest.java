package jmri.jmrix.openlcb.configurexml;

import org.hamcrest.core.IsCollectionContaining;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private Path workspace;
    private Profile profile;
    private ConnectionConfigManager connectionConfigManager;
    private CanSystemConnectionMemo canSystemConnectionMemo;
    private String profileId;
    private PortAdapter adapter;

    @Before
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
        this.workspace = FileSystems.getDefault().getPath("/tmp/testprofile");
        //this.workspace = Files.createTempDirectory(this.getClass().getSimpleName());
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
    }

    @After
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        jmri.util.JUnitUtil.tearDown();

        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.resetFileUtilSupport();
        //FileUtil.delete(this.workspace.toFile());
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
        assertThat(canSystemConnectionMemo.getProtocolsWithOptions(), IsCollectionContaining.hasItems("Ident", "Throttles", "Single"));
        assertEquals(3, canSystemConnectionMemo.getProtocolsWithOptions().size());

        assertThat(canSystemConnectionMemo.getProtocolAllOptions("Ident").keySet(), IsCollectionContaining.hasItems("UserName", "Description"));
        assertEquals(2, canSystemConnectionMemo.getProtocolAllOptions("Ident").keySet().size());

        assertThat(canSystemConnectionMemo.getProtocolAllOptions("Throttles").keySet(), IsCollectionContaining.hasItems("Used", "Steal", "MaxFn"));
        assertEquals(3, canSystemConnectionMemo.getProtocolAllOptions("Throttles").keySet().size());

        assertThat(canSystemConnectionMemo.getProtocolAllOptions("Single").keySet(), IsCollectionContaining.hasItems("Foo"));
        assertEquals(1, canSystemConnectionMemo.getProtocolAllOptions("Single").keySet().size());

        expectOptionValue("Ident", "UserName", "Hello, World");
        expectOptionValue("Ident", "Description", "something");
        expectOptionValue("Throttles", "Used", "1");
        expectOptionValue("Throttles", "Steal", "2");
        expectOptionValue("Throttles", "MaxFn", "29");
        expectOptionValue("Single", "Foo", "");
    }

    // private final static Logger log = LoggerFactory.getLogger(ProtocolOptionsPersistenceTest.class);
}
