package apps.startup;

import javax.swing.JFileChooser;

import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ScriptButtonPanelTest {

    @Test
    @DisabledIfHeadless
    public void testCTor() {
        JmriJFrame jf = new JmriJFrame("Script Button Panel Test");
        JFileChooser jfc = new JFileChooser();
        ScriptButtonPanel t = new ScriptButtonPanel(jfc,jf);
        Assertions.assertNotNull(t, "exists");
        JUnitUtil.dispose(jf);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ScriptButtonPanelTest.class);

}
