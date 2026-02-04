package apps.jmrit.log;

import jmri.util.JUnitUtil;

import org.apache.logging.log4j.LogManager;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.*;
import org.slf4j.LoggerFactory;

/**
 * Tests for Log4JTreePane.
 *
 * @author Bob Jacobsen Copyright 2003, 2010
 * @author Steve Young Copyright(C) 2023
 */
@jmri.util.junit.annotations.DisabledIfHeadless
public class Log4JTreePaneTest extends jmri.util.swing.JmriPanelTest {

    @Test
    public void testShow() {
        LoggerFactory.getLogger("jmri.jmrix");
        LoggerFactory.getLogger("apps.foo");
        LoggerFactory.getLogger("jmri.util");

        jmri.util.ThreadingUtil.runOnGUI( () ->
            new jmri.util.swing.JmriNamedPaneAction("Log4J Tree",
                new jmri.util.swing.sdi.JmriJFrameInterface(),
                "apps.jmrit.log.Log4JTreePane").actionPerformed(null));
        
        JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("MenuItemLogTreeAction"));
        Assertions.assertNotNull(jfo);

        JUnitUtil.dispose( jfo.getWindow() );
        jfo.waitClosed();
        
    }

    @Test
    public void testChangeALoggingLevel(){
        String testLoggerName = "apps.jmrit.log.Log4JTreePaneTest.testChangeALoggingLevel";

        jmri.util.ThreadingUtil.runOnGUI( () ->
            new jmri.util.swing.JmriNamedPaneAction("Log4J Tree",
                new jmri.util.swing.sdi.JmriJFrameInterface(),
                "apps.jmrit.log.Log4JTreePane").actionPerformed(null));

        JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("MenuItemLogTreeAction"));
        Assertions.assertNotNull(jfo);

        // enter logger name
        JTextFieldOperator tfo = new JTextFieldOperator(jfo,0);
        tfo.clearText();
        tfo.enterText(testLoggerName);
        JComboBoxOperator logLevelSelect = new JComboBoxOperator(jfo, 1);
        logLevelSelect.setSelectedItem(org.apache.logging.log4j.Level.TRACE);
        new JButtonOperator(jfo, Bundle.getMessage("ButtonEditLoggingLevel")).doClick();

        JTextAreaOperator jtfo = new JTextAreaOperator(jfo, 0);
        JUnitUtil.waitFor(() -> jtfo.getText().contains(testLoggerName) , "Test found in list");
        JUnitUtil.waitFor(() -> LogManager.getLogger(testLoggerName).isTraceEnabled() , "log level changed to trace");
        Assertions.assertTrue(LogManager.getLogger(testLoggerName).isTraceEnabled());

        new JComboBoxOperator(jfo, 0).setSelectedItem(testLoggerName);
        logLevelSelect.setSelectedItem(org.apache.logging.log4j.Level.ERROR);
        new JButtonOperator(jfo, Bundle.getMessage("ButtonEditLoggingLevel")).doClick();
        JUnitUtil.waitFor(() -> !LogManager.getLogger(testLoggerName).isTraceEnabled() , "log level changed to error");
        Assertions.assertFalse(LogManager.getLogger(testLoggerName).isTraceEnabled());

        JUnitUtil.dispose( jfo.getWindow() );
        jfo.waitClosed();

    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();

        panel = new Log4JTreePane();
        title=Bundle.getMessage("MenuItemLogTreeAction");
        helpTarget="package.apps.jmrit.log.Log4JTreePane";
    }

    @AfterEach
    @Override
    public void tearDown() {
        panel = null;
        title = null;
        helpTarget = null;

        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

}
