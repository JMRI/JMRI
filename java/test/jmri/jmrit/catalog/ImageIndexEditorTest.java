package jmri.jmrit.catalog;

import java.awt.GraphicsEnvironment;
import java.awt.event.WindowListener;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.util.JUnitUtil;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.extensions.jfcunit.finder.AbstractButtonFinder;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.Assert;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * LinkingLabelTest.java
 *
 * Description:
 *
 * @author  Bob Jacobsen
 */
public class ImageIndexEditorTest extends jmri.util.SwingTestCase {

    ControlPanelEditor _cpe;

    public void testShow() {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't Assume in TestCase
        }
        ImageIndexEditor indexEditor = ImageIndexEditor.instance(_cpe);
        Assert.assertNotNull(JFrameOperator.waitJFrame(Bundle.getMessage("editIndexFrame"), true, true));
        Assert.assertFalse("Index not changed",ImageIndexEditor.isIndexChanged());
/* Cannot get rid of FileChooser        
        DirectorySearcher.instance().openDirectory();        
        DialogFinder finder = new DialogFinder(Bundle.getMessage("openDirMenu"));
        JUnitUtil.waitFor(() -> {
            return (java.awt.Container)finder.find()!=null;
        }, "Found dialog + \"title\"");
        java.awt.Container pane = (java.awt.Container)finder.find();
        Assert.assertNotNull("FileChooser not found", pane);
        ((javax.swing.JFileChooser)pane).cancelSelection();
        */
    }
    
    private javax.swing.AbstractButton pressButton(java.awt.Container frame, String text) {
        AbstractButtonFinder buttonFinder = new AbstractButtonFinder(text);
        javax.swing.AbstractButton button = (javax.swing.AbstractButton) buttonFinder.find(frame, 0);
        Assert.assertNotNull(text+" Button not found", button);
        getHelper().enterClickAndLeave(new MouseEventData(this, button));
        flushAWT();
        return button;
    }

    // from here down is testing infrastructure
    public ImageIndexEditorTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", ImageIndexEditorTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ImageIndexEditorTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initShutDownManager();

        if (!GraphicsEnvironment.isHeadless()) {
            _cpe = new ControlPanelEditor("ControlPanelEditorTestPanel");
            Assert.assertNotNull(JFrameOperator.waitJFrame("ControlPanelEditorTestPanel", true, true));
            Assert.assertNotNull(_cpe.getTargetPanel());
        }
    }

    @Override
    protected void tearDown() {
        if (_cpe != null) {
            // now close panel window
            java.awt.event.WindowListener[] listeners = _cpe.getTargetFrame().getWindowListeners();
            for (WindowListener listener : listeners) {
                _cpe.getTargetFrame().removeWindowListener(listener);
            }
            junit.extensions.jfcunit.TestHelper.disposeWindow(_cpe.getTargetFrame(), this);

            _cpe = null;
        }
        apps.tests.Log4JFixture.tearDown();
    }

    // static private Logger log = LoggerFactory.getLogger(ImageIndexEditorTest.class.getName());
}

