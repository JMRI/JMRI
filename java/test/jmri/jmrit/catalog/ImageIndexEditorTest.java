package jmri.jmrit.catalog;

import java.awt.GraphicsEnvironment;
import java.awt.event.WindowListener;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.util.JUnitUtil;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.extensions.jfcunit.finder.AbstractButtonFinder;
import junit.extensions.jfcunit.finder.DialogFinder;
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
        /* Next call opens JOptionPane.  Modal dialog blocks following code to find and press buttons.
        indexEditor.addNode();
        java.awt.Container pane = findContainer("selectAddNode");
        Assert.assertNotNull("Select node prompt not found", pane);
        pressButton(pane, "OK");
        // This opens FileChooser  - also modal      
        DirectorySearcher.instance().openDirectory();
        pane = findContainer("openDirMenu");
        Assert.assertNotNull("FileChooser not found", pane);
        ((javax.swing.JFileChooser)pane).cancelSelection();
        pressButton(pane, "Cancel");
        */
        
        /* This doesn't work either
        Thread t = new ModalFinder("selectAddNode", "OK");
        t.start();
        indexEditor.addNode();
        t = new ModalFinder("openDirMenu", "Cancel");
        t.start();
        DirectorySearcher.instance().openDirectory();
        */
    }
    
    java.awt.Container findContainer(String title) {
        DialogFinder finder = new DialogFinder(Bundle.getMessage(title));
        JUnitUtil.waitFor(() -> {
            return (java.awt.Container)finder.find()!=null;
        }, "Found dialog + \"title\"");
        return (java.awt.Container)finder.find();
        
    }
    
    javax.swing.AbstractButton pressButton(java.awt.Container frame, String text) {
        AbstractButtonFinder buttonFinder = new AbstractButtonFinder(text);
        javax.swing.AbstractButton button = (javax.swing.AbstractButton) buttonFinder.find(frame, 0);
        Assert.assertNotNull(text+" Button not found", button);
        getHelper().enterClickAndLeave(new MouseEventData(this, button));
        flushAWT();
        return button;
    }
    
    class ModalFinder extends Thread implements Runnable {
        String frameTitle;
        String buttonName;
        
        ModalFinder(String ft, String bn) {
            frameTitle = ft;
            buttonName = bn;
        }
        public void run() {
            System.out.println("ModalFinder("+frameTitle+", "+buttonName+")");
            int waitTime=0;
            System.out.println("ModalFinder at DialogFinder");
            DialogFinder finder = new DialogFinder(Bundle.getMessage(frameTitle));
            java.awt.Component pane = finder.find();
            System.out.println("ModalFinder start DialogFinder loop");
            while (waitTime < 1000 && pane==null) {
                try {
                    pane = finder.find();
                    sleep(200);
                    waitTime += 200;
                    System.out.println("ModalFinder in DialogFinder loop waitTime= "+waitTime);
                } catch (InterruptedException e) {
                    Assert.fail("InterruptedException");
                }            
            }
            Assert.assertNotNull("Modal dialog \""+frameTitle+"\" not found", pane);
            waitTime=0;
            AbstractButtonFinder buttonFinder = new AbstractButtonFinder(buttonName);
            javax.swing.AbstractButton button = (javax.swing.AbstractButton)buttonFinder.find((java.awt.Container)pane, 0);
            flushAWT();
            while (waitTime < 1000 && button==null) {
                try {
                    button = (javax.swing.AbstractButton) buttonFinder.find((java.awt.Container)pane, 0);
                    sleep(200);
                    waitTime += 200;
                } catch (InterruptedException e) {
                    Assert.fail("InterruptedException");
                }            
            }
            Assert.assertNotNull(buttonName+" Button not found", button);
            getHelper().enterClickAndLeave(new MouseEventData(null, button));
        }
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

    // private final static Logger log = LoggerFactory.getLogger(ImageIndexEditorTest.class.getName());
}

