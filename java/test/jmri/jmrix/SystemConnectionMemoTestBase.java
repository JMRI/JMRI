package jmri.jmrix;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

import java.util.Arrays;
import java.util.Comparator;
import java.util.ResourceBundle;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.SystemConnectionMemo;
import jmri.util.startup.StartupActionFactory;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Abstract base class for SystemConnectionMemo objects.
 *
 * @author Paul Bender Copyright (C) 2017
 * @param <M> the supported memo class
 */
abstract public class SystemConnectionMemoTestBase<M extends DefaultSystemConnectionMemo> {

    protected M scm = null;

    public void getTest(Class<?> m) {
        if (scm.provides(m)) {
            // if the manager reports providing the class, make sure it exists.
            Assert.assertNotNull("Provides Class " + m.getName(), scm.get(m));
        } else {
            Assert.assertNull("Provides Class " + m.getName(), scm.get(m));
        }
    }

    @Test
    public void testGetActionFactory() {
        assumeThat(scm.getActionModelResourceBundle()).as("provides ResourceBundle").isNotNull();
        StartupActionFactory f = scm.getActionFactory();
        assertThat(f).as("provides StartupActionFactory").isNotNull();
        Arrays.stream(f.getActionClasses()).forEach(a -> {
            assertThat(f.getTitle(a)).as("has title for %s", a).isNotNull();
            assertThatCode(() -> a.getDeclaredConstructor().newInstance()).doesNotThrowAnyException();
        });
    }

    @Test
    public void getPowerManager() {
        getTest(jmri.PowerManager.class);
    }

    @Test
    public void getTurnoutManager() {
        getTest(jmri.TurnoutManager.class);
    }

    @Test
    public void getThrottleManager() {
        getTest(jmri.ThrottleManager.class);
    }

    @Test
    public void getSensorManager() {
        getTest(jmri.SensorManager.class);
    }

    @Test
    public void getLightManager() {
        getTest(jmri.LightManager.class);
    }

    @Test
    public void getReporterManager() {
        getTest(jmri.ReporterManager.class);
    }

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", scm);
    }

    @Test
    public void testProvidesConsistManager() {
        getTest(jmri.ReporterManager.class);
    }

    @Test
    public void testGetAndSetPrefix() {
        scm.setSystemPrefix("A2");
        Assert.assertEquals("System Prefix after set", "A2", scm.getSystemPrefix());
    }

    @Test
    public void testMultipleMemosSamePrefix() {
        SystemConnectionMemo m = new DefaultSystemConnectionMemo("t", "test") {
            @Override
            protected ResourceBundle getActionModelResourceBundle() {
                return null;
            }

            @Override
            public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
                return null;
            }
        };
        Assert.assertEquals("t", m.getSystemPrefix());
        m.register();
        Assert.assertTrue(InstanceManager.getList(SystemConnectionMemo.class).contains(m));
        Assert.assertFalse(scm.setSystemPrefix("t"));
        Assert.assertTrue(scm.setSystemPrefix("t2"));
        Assert.assertEquals("t2", scm.getSystemPrefix());
    }

    @Test
    public void testGetAndSetOutputInterval() {
        scm.setOutputInterval(50);
        Assert.assertEquals("Output Interval after set", 50, scm.getOutputInterval());
    }

    @BeforeEach
    abstract public void setUp();

    @AfterEach
    abstract public void tearDown();

}
