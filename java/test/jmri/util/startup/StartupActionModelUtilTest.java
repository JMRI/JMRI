package jmri.util.startup;

import java.util.Arrays;
import java.util.ResourceBundle;
import jmri.util.JUnitUtil;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Paul Bender Copyright 2017
 * @author Randall Wood Copyright 2020
 */
public class StartupActionModelUtilTest {

    @Test
    public void testCTor() {
        StartupActionModelUtil t = new StartupActionModelUtil();
        assertThat(t).as("exists").isNotNull();
    }

    /**
     * Test that no factory listed by the service loader throws an exception
     * getting it's listed class and that no title is null, empty, or just
     * whitespace.
     */
    @Test
    public void testStartupActionFactoryNoErrors() {
        StartupActionModelUtil t = new StartupActionModelUtil();
        Arrays.stream(t.getClasses())
                .forEach(clazz -> assertThat(t.getActionName(clazz)).isNotBlank());

        // suppress deprecation warning that's expected
        jmri.util.JUnitAppender.assertWarnMessageStartsWith("apps.startup.StartupActionFactoryScaffold is deprecated, please remove references to it");
    }

    /**
     * Test that a factory exists for every class named in
     * apps.ActionListBundle.
     */
    @Test
    public void testActionListBundleIsImplemented() {
        testNamedBundleIsImplemented("apps.ActionListBundle");
    }

    /**
     * Test that a factory exists for every class named in
     * apps.ActionListCoreBundle.
     */
    @Test
    public void testActionListCoreBundleIsImplemented() {
        testNamedBundleIsImplemented("apps.ActionListCoreBundle");
    }

    private void testNamedBundleIsImplemented(String name) {
        StartupActionModelUtil t = new StartupActionModelUtil();
        ResourceBundle rb = ResourceBundle.getBundle(name);
        rb.keySet().stream().filter(key -> !key.isEmpty()).forEach(key -> {
            assertThat(t.getActionName(key) != null || t.getOverride(key) != null).as(key).isTrue();
        });

        // suppress deprecation warning that's expected
        jmri.util.JUnitAppender.assertWarnMessageStartsWith("apps.startup.StartupActionFactoryScaffold is deprecated, please remove references to it");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        // NOTICE: do not clear any registered ShutDownTasks in this method. The
        // registration of a ShutDownTask in these tests are a sign that a class
        // is registering a ShutDownTask in a static initializer of that class
        // and that *must* be fixed, since this test is testing that optionally
        // instanciated classes can be presented to the user to allow the user to
        // choose to instanciate them, and such a class must not be registering
        // a ShutDownTaks unless instanciated
        JUnitUtil.tearDown();
    }

}
