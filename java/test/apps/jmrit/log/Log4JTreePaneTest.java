package apps.jmrit.log;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.*;
import org.slf4j.LoggerFactory;

/**
 * Tests for Log4JTreePane.
 *
 * @author Bob Jacobsen Copyright 2003, 2010
 * @author Steve Young Copyright(C) 2023
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class Log4JTreePaneTest extends jmri.util.swing.JmriPanelTest {

    @Test
    public void testShow() {
        LoggerFactory.getLogger("jmri.jmrix");
        LoggerFactory.getLogger("apps.foo");
        LoggerFactory.getLogger("jmri.util");

        new jmri.util.swing.JmriNamedPaneAction("Log4J Tree",
                new jmri.util.swing.sdi.JmriJFrameInterface(),
                "apps.jmrit.log.Log4JTreePane").actionPerformed(null);
        
        JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("MenuItemLogTreeAction"));
        Assertions.assertNotNull(jfo);

        jfo.requestClose();
        jfo.waitClosed();
        
    }

    @Test
    public void testButtons(){
        LoggerFactory.getLogger("apps.morefoo");
        LoggerFactory.getLogger("jmri.util");

        new jmri.util.swing.JmriNamedPaneAction("Log4J Tree",
                new jmri.util.swing.sdi.JmriJFrameInterface(),
                "apps.jmrit.log.Log4JTreePane").actionPerformed(null);

        JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("MenuItemLogTreeAction"));
        Assertions.assertNotNull(jfo);

        LoggerFactory.getLogger("apps.evenmorefoo");
        JButtonOperator refreshOp = new JButtonOperator(jfo, Bundle.getMessage("ButtonRefreshCategories"));
        refreshOp.doClick();

        JTextAreaOperator jtfo = new JTextAreaOperator(jfo, 0);
        Assertions.assertTrue(jtfo.getText().contains("apps.evenmorefoo"));

        JComboBoxOperator logLevelSelect = new JComboBoxOperator(jfo, 1);
        logLevelSelect.setSelectedItem(org.apache.logging.log4j.Level.TRACE);

        new JComboBoxOperator(jfo, 0).setSelectedItem("apps.evenmorefoo");
        JButtonOperator jbo = new JButtonOperator(jfo, Bundle.getMessage("ButtonEditLoggingLevel"));
        jbo.doClick();
        Assertions.assertTrue(LoggerFactory.getLogger("apps.evenmorefoo").isTraceEnabled());

        logLevelSelect.setSelectedItem(org.apache.logging.log4j.Level.ERROR);
        jbo.doClick();
        Assertions.assertFalse(LoggerFactory.getLogger("apps.evenmorefoo").isTraceEnabled());

        jfo.requestClose();
        jfo.waitClosed();

    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
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
