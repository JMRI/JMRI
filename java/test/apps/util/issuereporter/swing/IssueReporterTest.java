package apps.util.issuereporter.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Minimal test skeleton for IssueReporter class
 */
@jmri.util.junit.annotations.DisabledIfHeadless
public class IssueReporterTest {

    @Test
    public void testCtor(){
        IssueReporter t = new IssueReporter();
        Assertions.assertNotNull( t, "IssueReporter constructor");
        jmri.util.ThreadingUtil.runOnGUI( () -> t.setVisible(true));

        JFrameOperator jfo = new JFrameOperator(t.getTitle());
        Assertions.assertNotNull(jfo);

        JUnitUtil.dispose(t);
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

