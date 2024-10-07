package jmri.jmrit.logixng.configurexml2;

import jmri.configurexml.*;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.SystemConnectionMemo;
import jmri.jmrit.entryexit.DestinationPoints;
import jmri.jmrit.entryexit.EntryExitPairs;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.TransitScaffold;
import jmri.jmrix.loconet.*;
import jmri.jmrix.mqtt.MqttSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test that configuration files can be read and then stored again consistently.
 * When done across various versions of schema, this checks ability to read
 * older files in newer versions; completeness of reading code; etc.
 * <p>
 * Functional checks, that e.g. check the details of a specific type are being
 * read properly, should go into another type-specific test class.
 * <p>
 * The functionality comes from the common base class, this is just here to
 * insert the test suite into the JUnit hierarchy at the right place.
 * <p>
 * The difference between jmri.jmrit.logixng.configurexml and
 * jmri.jmrit.logixng.configurexml2 is that this package tests with LogixNG
 * debugger _not_ installed.
 *
 * @author Bob Jacobsen Copyright 2009, 2014
 * @since 2.5.5 (renamed & reworked in 3.9 series)
 */
public class LoadAndStoreTest extends LoadAndStoreTestBase {

    public static Stream<Arguments> data() {
        return getFiles(new File("java/test/jmri/jmrit/logixng/configurexml2"), false, true);
    }

    @ParameterizedTest(name = "{index}: {0} (pass={1})")
    @MethodSource("data")
    public void loadAndStoreTest(File file, boolean pass) throws Exception {
        super.loadLoadStoreFileCheck(file);
    }

    public LoadAndStoreTest() {
        // LogixNG cannot be loaded twice
        super(SaveType.Config, true);
    }

    @Override
    protected void postLoadProcessing() {
        InstanceManager.getDefault(LogixNG_Manager.class).setupAllLogixNGs();

        // We do this to test that DestinationPoints are stored in the file
        // as system name, not as user name.
        DestinationPoints dp1 = InstanceManager.getDefault(EntryExitPairs.class).getBySystemName("DP1");
        NamedBeanHandleManager nbm = InstanceManager.getDefault(NamedBeanHandleManager.class);
        NamedBeanHandle nb = nbm.getNamedBeanHandle(dp1.getSystemName(), dp1);
        nb.setName(dp1.getUserName());
    }

    @BeforeEach
    @Override
    public void setUp(@TempDir java.io.File tempDir) throws IOException  {
        super.setUp(tempDir);
//        super.setUp(new File("temp"));

        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initDebugCommandStation();

        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();
        SlotManager sm = new SlotManager(lnis);
        LocoNetSystemConnectionMemo locoNetMemo = new LocoNetSystemConnectionMemo(lnis, sm);
        locoNetMemo.setThrottleManager(new LnThrottleManager(locoNetMemo));
        sm.setSystemConnectionMemo(locoNetMemo);
        InstanceManager.setDefault(LocoNetSystemConnectionMemo.class, locoNetMemo);
        InstanceManager.setDefault(SystemConnectionMemo.class, locoNetMemo);

        MqttSystemConnectionMemo mqttMemo = new MqttSystemConnectionMemo();
        InstanceManager.store(mqttMemo, SystemConnectionMemo.class);
        InstanceManager.setDefault(MqttSystemConnectionMemo.class, mqttMemo);

        TransitScaffold.initTransits();

        InstanceManager.getDefault(jmri.jmrit.logixng.LogixNGPreferences.class).setInstallDebugger(false);
    }

    @AfterEach
    @Override
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        super.tearDown();
    }

}
