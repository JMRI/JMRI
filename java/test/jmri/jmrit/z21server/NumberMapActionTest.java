package jmri.jmrit.z21server;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Test simple functioning of NumberMapAction
 *
 * @author Eckart Meyer (C) 2025
 */
public class NumberMapActionTest {

    @Test
    @DisabledIfHeadless
    public void testCtor() {

        NumberMapAction t = new NumberMapAction();
        assertNotNull(t, "exists");
        assertTrue( t.isEnabled());

        ThreadingUtil.runOnGUI(() -> t.actionPerformed(null));
        JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("TitleNumberMapFrame"));
        assertNotNull(jfo);

        JUnitUtil.dispose(jfo.getWindow());
        jfo.waitClosed();

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
