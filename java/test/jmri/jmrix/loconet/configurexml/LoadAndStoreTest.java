package jmri.jmrix.loconet.configurexml;

import jmri.jmrix.loconet.*;

import java.io.File;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
public class LoadAndStoreTest extends jmri.configurexml.LoadAndStoreTestBase {

    public static Stream<Arguments> data() {
        return getFiles(new File("java/test/jmri/jmrix/loconet/configurexml"), false, true);
    }

    @ParameterizedTest(name = "{index}: {0} (pass={1})")
    @MethodSource("data")
    public void loadAndStoreTest(File file, boolean pass) throws Exception {
        super.loadLoadStoreFileCheck(file);
    }

    public LoadAndStoreTest() {
        super(SaveType.Config, false);
    }

    
    LocoNetSystemConnectionMemo memo1;
    LocoNetInterfaceScaffold lnis1;
    
    LocoNetSystemConnectionMemo memo2;
    LocoNetInterfaceScaffold lnis2;
    
    /**
     * {@inheritDoc}
     * Ensure that a LocoNet connection is available
     */
    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();

        // 1st LocoNet connection L
        memo1 = new LocoNetSystemConnectionMemo();
        lnis1 = new LocoNetInterfaceScaffold(memo1);
        memo1.setLnTrafficController(lnis1);
        jmri.InstanceManager.store(lnis1, jmri.jmrix.loconet.LnTrafficController.class);
        memo1.configureCommandStation(LnCommandStationType.COMMAND_STATION_DCS100,false,false,false);
        memo1.configureManagers(); // Does this generate autonomous loconet traffic? Needs a wait?
        jmri.InstanceManager.store(memo1,LocoNetSystemConnectionMemo.class);
        
        // 2nd LocoNet connection L2
        memo2 = new LocoNetSystemConnectionMemo();
        lnis2 = new LocoNetInterfaceScaffold(memo1);
        memo2.setLnTrafficController(lnis2);
        jmri.InstanceManager.store(lnis2, jmri.jmrix.loconet.LnTrafficController.class);
        memo2.configureCommandStation(LnCommandStationType.COMMAND_STATION_DCS100,false,false,false);
        memo2.configureManagers(); // Does this generate autonomous loconet traffic? Needs a wait?
        jmri.InstanceManager.store(memo2,LocoNetSystemConnectionMemo.class);
        
        jmri.InstanceManager.setDefault(jmri.jmrix.loconet.LnTrafficController.class, lnis1);
    }

    /**
     * {@inheritDoc}
     * Ensure that a LocoNet connection is available
     */
    @AfterEach
    @Override
    public void tearDown() {
        memo1.dispose();
        memo2.dispose();
        super.tearDown();
    }
}
