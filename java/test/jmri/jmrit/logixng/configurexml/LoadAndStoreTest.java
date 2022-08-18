package jmri.jmrit.logixng.configurexml;

import jmri.configurexml.*;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

import jmri.InstanceManager;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrix.loconet.*;
import jmri.jmrix.mqtt.MqttSystemConnectionMemo;

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
 *
 * @author Bob Jacobsen Copyright 2009, 2014
 * @since 2.5.5 (renamed & reworked in 3.9 series)
 */
public class LoadAndStoreTest extends LoadAndStoreTestBase {

    public static Stream<Arguments> data() {
        return getFiles(new File("java/test/jmri/jmrit/logixng/configurexml"), false, true);
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
    }

    @BeforeEach
    @Override
    public void setUp(@TempDir java.io.File tempDir) throws IOException  {
        super.setUp(tempDir);

        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();
        SlotManager sm = new SlotManager(lnis);
        LocoNetSystemConnectionMemo locoNetMemo = new LocoNetSystemConnectionMemo(lnis, sm);
        sm.setSystemConnectionMemo(locoNetMemo);
        InstanceManager.setDefault(LocoNetSystemConnectionMemo.class, locoNetMemo);

        MqttSystemConnectionMemo mqttMemo = new MqttSystemConnectionMemo();
        InstanceManager.setDefault(MqttSystemConnectionMemo.class, mqttMemo);
    }

    @AfterEach
    @Override
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        super.tearDown();
    }

}
