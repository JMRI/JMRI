package jmri.jmrit.throttle;

import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Test simple functioning of ThrottlesPreferencesWindow
 *
 * @author Lionel Jeanson
 */
@DisabledIfHeadless
public class ThrottlesPreferencesWindowTest {

    @Test
    public void testCtor() {

        ThrottlesPreferencesWindow w = new ThrottlesPreferencesWindow("ThrottlesPreferencesWindowTest");
        Assertions.assertNotNull(w, "exists");
        ThreadingUtil.runOnGUI(() -> {
            w.pack();
            w.setVisible(true);
        });

        JFrameOperator jfo = new JFrameOperator(w.getTitle());
        Assertions.assertNotNull(jfo);
        new JButtonOperator(jfo,Bundle.getMessage("ButtonCancel")).doClick();
        jfo.waitClosed();
        JUnitUtil.dispose(jfo.getWindow());
            
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

