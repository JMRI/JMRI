package jmri.jmrit.throttle;

import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Test simple functioning of ThrottlesPreferencesWindow
 *
 * @author Lionel Jeanson
 */
public class ThrottlesPreferencesWindowTest {

    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    @Test
    public void testCtor() {
        try {
            ThrottlesPreferencesWindow w = new ThrottlesPreferencesWindow("ThrottlesPreferencesWindowTest");
            Assertions.assertNotNull(w, "exists");
            w.pack();
            ThreadingUtil.runOnGUI(() -> {
                w.setVisible(true);
            });

            JFrameOperator jfo = new JFrameOperator(w.getTitle());
            Assertions.assertNotNull(jfo);
            new JButtonOperator(jfo,Bundle.getMessage("ButtonCancel")).doClick();
            jfo.waitClosed();
            
        } catch (IndexOutOfBoundsException e) {
            Assertions.fail("IndexOutOfBoundsException\n",e);
        }
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();

    }
}

