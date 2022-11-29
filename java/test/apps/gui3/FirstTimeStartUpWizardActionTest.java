package apps.gui3;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class FirstTimeStartUpWizardActionTest {

    @Test
    public void testCTor() {
        FirstTimeStartUpWizardAction t = new FirstTimeStartUpWizardAction("test CTor");
        Assertions.assertNotNull(t, "exists");
        t.dispose();
    }

    @Test
    public void testNotPartOfAPanel() {
        FirstTimeStartUpWizardAction t = new FirstTimeStartUpWizardAction("test NotPartOfAPanel", (new jmri.util.JmriJFrame("testNotPartOfAPanel Frame")));
        Exception ex = Assertions.assertThrows(IllegalArgumentException.class, () -> { t.makePanel(); });
        Assertions.assertNotNull(ex);
        t.dispose();
    }

    @Test
    public void testFirstTimeStartUpWizardAction3PartCTor() {
        FirstTimeStartUpWizardAction t = new FirstTimeStartUpWizardAction("test FirstTimeStartUpWizardAction3PartCTor", null, null);
        Assertions.assertNotNull(t, "exists");
        t.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(FirstTimeStartUpWizardActionTest.class);
}
