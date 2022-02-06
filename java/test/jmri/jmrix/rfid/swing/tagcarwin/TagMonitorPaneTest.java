package jmri.jmrix.rfid.swing.tagcarwin;

import jmri.util.JUnitUtil;
import jmri.util.swing.JmriPanel;
import org.junit.jupiter.api.BeforeEach;

public class TagMonitorPaneTest extends jmri.util.swing.JmriPanelTest {
    protected JmriPanel pane = null;

    public TagMonitorPaneTest() {
        super();
        this.title = "RFID Device Monitor Tags by Car";
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        panel = pane = new TagMonitorPane();
        helpTarget = "package.jmri.jmrix.rfid.swing.tagcarwin.TagMonitorPane";
    }


}
