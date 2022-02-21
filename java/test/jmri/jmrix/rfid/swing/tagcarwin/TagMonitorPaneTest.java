package jmri.jmrix.rfid.swing.tagcarwin;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.BeforeEach;

public class TagMonitorPaneTest extends jmri.util.swing.JmriPanelTest {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        title = "RFID Device Monitor Tags by Car";
        panel = new TagMonitorPane();
        helpTarget = "package.jmri.jmrix.rfid.swing.tagcarwin.TagMonitorPane";
    }

}
