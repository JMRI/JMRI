package jmri.jmrit.jython;

import java.io.File;
import java.util.*;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.ShutDownManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.util.JUnitUtil;
import jmri.script.JmriScriptEngineManager;
import jmri.util.JUnitAppender;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import org.junit.jupiter.api.io.TempDir;

/**
 * Invokes Python-language scripts in jython/tests
 *
 * @author Bob Jacobsen Copyright 2016
 * @author Paul Bender Copyright 2017
 * @author Randall Wood Copyright 2020
 * @since JMRI 4.3.6
 */
public class SampleScriptTest {

    /**
     * Create the tests from each sample script in test directory.
     *
     * @return the scripts to test
     */
    @Nonnull
    public static Stream<File> testsFromDirectory() {

        // first get a list of file suffixes
        List<String> allExtensions = new ArrayList<>();
        JmriScriptEngineManager.getDefault().getManager().getEngineFactories().stream().forEach((var factory) -> {
            if (factory.getEngineVersion() != null) {
                List<String> extensions = factory.getExtensions();
                allExtensions.addAll(extensions);
            }
        });

        File[] files = (new File("jython/test")).listFiles((File a, String b) -> {
                                for (var ext : allExtensions) {
                                    if (b.endsWith(ext)) return true;
                                }
                                return false;
                             });
        Arrays.sort(files);  // process in known (alphanumeric) order
        return files != null ? Arrays.stream(files) : Stream.empty();
    }

    @ParameterizedTest
    @MethodSource("testsFromDirectory")
    @DisabledIfSystemProperty(named = "jmri.skipjythontests", matches = "true")
    public void runTest(File file) {
        try {
            jmri.script.JmriScriptEngineManager.getDefault().eval(file);
        } catch (javax.script.ScriptException ex1) {
            log.error("ScriptException during test of {}", file, ex1);
            Assertions.fail("ScriptException during test of " + file, ex1);
        } catch (java.io.IOException ex2) {
            log.error("IOException during test of {}", file, ex2);
            Assertions.fail("IOException during test of " + file, ex2);
        }

        if (file.toPath().endsWith("LoggingTest.py")) {
            JUnitAppender.assertWarnMessage("This WARN is OK, it's emitted from LoggingTest.py on purpose");
        }
        else if (file.toPath().endsWith("JavaScriptTest.js")) {
            JUnitAppender.assertWarnMessage("JavaScriptTest: Turnout.THROWN is 4 (WARN OK here)");
        }

    }

    // test for jython/TurnoutStatePersistence.py
    @Test
    public void testTurnoutStatePersistencePy() {

        TurnoutManager turnouts = InstanceManager.getDefault(TurnoutManager.class);
        Assertions.assertEquals(0, turnouts.getNamedBeanSet().size());

        File turnoutFile = new File(jmri.util.FileUtil.getUserFilesPath() + "TurnoutState.csv");
        Assertions.assertFalse(turnoutFile.exists());

        Turnout myt1 = turnouts.provideTurnout("IT701");
        myt1.setCommandedState(Turnout.THROWN);
        Turnout myt2 = turnouts.provideTurnout("IT702");
        myt2.setCommandedState(Turnout.CLOSED);
        Turnout myt3 = turnouts.provideTurnout("IT703");
        myt3.setCommandedState(Turnout.INCONSISTENT);
        Turnout myt4 = turnouts.provideTurnout("IT704");
        myt4.setCommandedState(Turnout.UNKNOWN);
        Assertions.assertEquals(4, turnouts.getNamedBeanSet().size());

        int initialSize = InstanceManager.getDefault(ShutDownManager.class).getRunnables().size();

        // register Shutdown Task
        File file = new File("jython/TurnoutStatePersistence.py");
        try {
            jmri.script.JmriScriptEngineManager.getDefault().eval(file);
        } catch (javax.script.ScriptException ex1) {
            Assertions.fail("ScriptException during register Shutdown Task of " + file, ex1);
        } catch (java.io.IOException ex2) {
            Assertions.fail("IOException during register Shutdown Task of " + file, ex2);
        }
        JUnitAppender.suppressWarnMessageStartsWith("Turnout state file "); // does not exist . . . .

        int size = InstanceManager.getDefault(ShutDownManager.class).getRunnables().size();
        Assertions.assertNotEquals(initialSize, size, "ShutDown task not registered");

        // run shutdown task with blocking
        ((jmri.managers.DefaultShutDownManager)InstanceManager.getDefault(ShutDownManager.class)).setBlockingShutdown(true);
        ((jmri.managers.DefaultShutDownManager)InstanceManager.getDefault(ShutDownManager.class)).shutdown(0, false);

        File newTurnoutFile = new File(jmri.util.FileUtil.getUserFilesPath() + "TurnoutState.csv");
        Assertions.assertTrue(newTurnoutFile.exists(),"user TurnoutState.csv exists");

        // set the Turnouts to a different state
        myt1.setCommandedState(Turnout.CLOSED);
        myt2.setCommandedState(Turnout.THROWN);
        myt3.setCommandedState(Turnout.UNKNOWN);
        myt4.setCommandedState(Turnout.INCONSISTENT);

        // re-run the script to reload the Turnout States
        try {
            jmri.script.JmriScriptEngineManager.getDefault().eval(file);
        } catch (javax.script.ScriptException ex1) {
            Assertions.fail("ScriptException during Restore of " + file, ex1);
        } catch (java.io.IOException ex2) {
            Assertions.fail("IOException during Restore of " + file, ex2);
        }

        // # clear Shutdown tasks as no longer required
        jmri.util.JUnitUtil.clearShutDownManager();

        // check Turnout State
        JUnitUtil.waitFor(() ->  ( myt1.getCommandedState() == Turnout.THROWN),"Turnout did not return to Thrown" );
        JUnitUtil.waitFor(() ->  ( myt2.getCommandedState() == Turnout.CLOSED),"Turnout did not return to Closed" );
        JUnitUtil.waitFor(() ->  ( myt3.getCommandedState() == Turnout.INCONSISTENT),"Turnout did not return to Inconsistent" );
        JUnitUtil.waitFor(() ->  ( myt4.getCommandedState() == Turnout.UNKNOWN),"Turnout did not return to Unknown" );

    }

    @BeforeAll
    static public void startTests() {
        // this is to System.out because that's where the test output goes
        System.out.println("\njmri.jmrit.jython.SampleScriptTest starts, following output is from script tests");
    }

    @BeforeEach
    public void setUp(@TempDir java.nio.file.Path tempDir) throws java.io.IOException  {
        JUnitUtil.setUp();

        // it's not really understood why, but doing these inside of the
        // sample Python script doesn't always work; it's as if that
        // is working with a different InstanceManager. So we
        // include a comprehensive set here.
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager( new jmri.profile.NullProfile( tempDir.toFile()));

        JUnitUtil.initConfigureManager();
        JUnitUtil.initDefaultUserMessagePreferences();
        JUnitUtil.initDebugPowerManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initDebugThrottleManager();
    }

    @AfterEach
    public void tearDown() throws Exception {
        JUnitUtil.clearTurnoutThreads();
        JUnitUtil.clearRouteThreads();
        JUnitUtil.clearBlockBossLogicThreads();

        JUnitUtil.resetWindows(false, false);
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitAppender.suppressWarnMessageStartsWith("Turnout state file "); // Turnout state file '/tmp/junit15888088443980290405/TurnoutState.csv' does not exist
        JUnitUtil.tearDown();
    }

    @AfterAll
    static public void endTests() {
        // this is to System.out because that's where the test output goes
        System.out.println("jmri.jmrit.jython.SampleScriptTest ends, above output was from script tests");
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SampleScriptTest.class);

}
