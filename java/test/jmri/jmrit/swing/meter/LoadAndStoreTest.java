package jmri.jmrit.swing.meter;

import java.io.File;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import jmri.InstanceManager;
import jmri.jmrix.roco.z21.RocoZ21CommandStation;
import jmri.jmrix.roco.z21.Z21InterfaceScaffold;
import jmri.jmrix.roco.z21.Z21PredefinedMeters;
import jmri.jmrix.roco.z21.Z21SystemConnectionMemo;
import jmri.util.JUnitUtil;

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
 * @author Bob Jacobsen      Copyright 2009, 2014
 * @author Daniel Bergqvist  Copyright 2020
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
@Disabled("Failing consistently on Jenkins for months")
public class LoadAndStoreTest extends jmri.configurexml.LoadAndStoreTestBase {

    private Z21InterfaceScaffold tc;
    
    public static Stream<Arguments> data() {
        return getFiles(new File("java/test/jmri/jmrit/swing/meter"), false, true);
    }

    @ParameterizedTest(name = "{index}: {0} (pass={1})")
    @MethodSource("data")
    public void loadAndStoreTest(File file, boolean pass) throws Exception {
        this.loadLoadStoreFileCheck(file);
    }

    public LoadAndStoreTest() {
        super(SaveType.User, true);
    }
    
    @TempDir 
    protected java.nio.file.Path tempDir;

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        try {
            JUnitUtil.resetProfileManager( new jmri.profile.NullProfile( tempDir.toFile()));
        }
        catch (java.io.IOException e){
            Assert.fail("Unable to create temp directory");
        }
        
        // This test requires a registred connection config since ProxyMeterManager
        // auto creates system meter managers using the connection configs.
        InstanceManager.setDefault(jmri.jmrix.ConnectionConfigManager.class, new jmri.jmrix.ConnectionConfigManager());
        jmri.jmrix.NetworkPortAdapter pa = new jmri.jmrix.roco.z21.Z21Adapter();
        pa.setSystemPrefix("Z");
        jmri.jmrix.ConnectionConfig cc = new jmri.jmrix.roco.z21.ConnectionConfig(pa);
        InstanceManager.getDefault(jmri.jmrix.ConnectionConfigManager.class).add(cc);
        
        // infrastructure objects
        tc = new Z21InterfaceScaffold();
        Z21SystemConnectionMemo memo = new Z21SystemConnectionMemo();
        memo.setTrafficController(tc);  
        memo.setRocoZ21CommandStation(new RocoZ21CommandStation());
        new Z21PredefinedMeters(memo);
    }

    @AfterEach
    @Override
    public void tearDown() {
        // since each file tested will open its own windows, just close any
        // open windows since we can't accurately list them here
        JUnitUtil.resetWindows(false, false);
        tc.terminateThreads();
        tc = null;
        super.tearDown();
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoadAndStoreTest.class);

}
