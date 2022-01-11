package jmri.jmrit.jython;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

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
        File[] files = (new File("jython/test")).listFiles((File a, String b) -> b.endsWith(".py"));
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
            Assert.fail("ScriptException during test of " + file);
        } catch (java.io.IOException ex2) {
            log.error("IOException during test of {}", file, ex2);
            Assert.fail("IOException during test of " + file);
        }
    }

    @BeforeAll
    static public void startTests() {
        // this is to System.out because that's where the test output goes
        System.out.println("\njmri.jmrit.jython.SampleScriptTest starts, following output is from script tests");
    }

    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();

        // it's not really understood why, but doing these inside of the
        // sample Python script doesn't always work; it's as if that
        // is working with a different InstanceManager. So we
        // include a comprehensive set here.
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
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
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    @AfterAll
    static public void endTests() {
        // this is to System.out because that's where the test output goes
        System.out.println("jmri.jmrit.jython.SampleScriptTest ends, above output was from script tests");
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SampleScriptTest.class);

}
