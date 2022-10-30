package jmri;

import javax.annotation.Nonnull;

import jmri.util.JUnitUtil;
import jmri.util.PreferNumericComparator;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test default method
 * @author Bob Jacobsen Copyright (C) 2022
 */
public class NameIncrementingManagerTest {

    private static class Testable implements NameIncrementingManager {
        @Override
        public boolean allowMultipleAdditions(@Nonnull String systemName) {
            return true;
        }
    }

    @Test
    public void testSimpleSensor() throws JmriException {
        var testManager = new Testable();

        Sensor start = new jmri.implementation.AbstractSensor("IS12") {

            @Override
            public void requestUpdateFromLayout() {
                // nothing to do
            }

            @Override
            public int compareSystemNameSuffix(@Nonnull String suffix1, @Nonnull String suffix2, NamedBean n) {
                return (new PreferNumericComparator()).compare(suffix1, suffix2);
            }
        };

        String next = testManager.getNextValidSystemName(start);
        Assert.assertEquals("IS13", next);
    }

    @Test
    public void testSensorThrows() throws JmriException {
        var testManager = new Testable();

        Sensor start = new jmri.implementation.AbstractSensor("ISFOO") {

            @Override
            public void requestUpdateFromLayout() {
                // nothing to do
            }

            @Override
            public int compareSystemNameSuffix(@Nonnull String suffix1, @Nonnull String suffix2, NamedBean n) {
                return (new PreferNumericComparator()).compare(suffix1, suffix2);
            }
        };

        JmriException assertThrows = Assertions.assertThrows(JmriException.class, () -> testManager.getNextValidSystemName(start));
        Assertions.assertEquals("No existing number found when incrementing ISFOO", assertThrows.getMessage());

    }

    @Test
    public void testSimpleTurnout() throws JmriException {
        var testManager = new Testable();

        Turnout start = new jmri.implementation.AbstractTurnout("IT12") {
            @Override
            protected void forwardCommandChangeToLayout(int s) {
                // nothing to do
            }

            @Override
            protected void turnoutPushbuttonLockout(boolean b) {
                // nothing to do
            }

            @Override
            public int compareSystemNameSuffix(@Nonnull String suffix1, @Nonnull String suffix2, NamedBean n) {
                return (new PreferNumericComparator()).compare(suffix1, suffix2);
            }

            @Override
            public boolean isCanFollow() {
                return true;
            }

            @Override
            public int getNumberControlBits() {
                return 2;
            }

        };

        String next = testManager.getNextValidSystemName(start);
        Assert.assertEquals("IT14", next);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NameIncrementingManagerTest.class);
}
