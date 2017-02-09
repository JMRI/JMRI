package jmri.jmrit.catalog;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.JFileChooser;
import jmri.util.FileUtil;
import jmri.util.JUnitUtil;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.extensions.jfcunit.finder.AbstractButtonFinder;
import junit.extensions.jfcunit.finder.ComponentFinder;
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
 * @author pete cressman
 */
public class ImageIndexEditorTest extends jmri.util.SwingTestCase {

    public void testShow() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        ImageIndexEditor indexEditor = ImageIndexEditor.instance(null);
        Assert.assertNotNull(JFrameOperator.waitJFrame(Bundle.getMessage("editIndexFrame"), true, true));
        Assert.assertFalse("Index not changed",ImageIndexEditor.isIndexChanged());
        
        jmri.util.ThreadingUtil.runOnGUIEventually(() -> {
            indexEditor.addNode();
        });
        flushAWT();
        java.awt.Container pane = findContainer(Bundle.getMessage("info"));
        Assert.assertNotNull("Select node prompt not found", pane);
        pressButton(pane, "OK");
        junit.extensions.jfcunit.TestHelper.disposeWindow(indexEditor,this);
    }

    public void testOpenDirectory() {
        jmri.util.ThreadingUtil.runOnGUIEventually(() -> {
            DirectorySearcher.instance().openDirectory();
        });
        java.awt.Container pane = findContainer(Bundle.getMessage("openDirMenu"));
        Assert.assertNotNull("FileChooser not found", pane);
        pressButton(pane, "Cancel");
    }
    
    public void testPreviewDialog()  throws FileNotFoundException, IOException {

        jmri.util.ThreadingUtil.runOnGUIEventually(() -> {
            DirectorySearcher.instance().searchFS();
        });
        ComponentFinder finder = new ComponentFinder(JFileChooser.class);
        JUnitUtil.waitFor(() -> {
            return (JFileChooser)finder.find()!=null;
        }, "Found JFileChooser \"searchFSMenu\"");
        JFileChooser chooser = (JFileChooser) finder.find();
        Assert.assertNotNull(" JFileChooser not found", chooser);
        File file = FileUtil.getFile(FileUtil.getAbsoluteFilename("program:resources/icons"));
        Assert.assertTrue(file.getPath()+" File does not exist", file.exists());
        flushAWT();
        jmri.util.ThreadingUtil.runOnGUIEventually(() -> {
            chooser.setCurrentDirectory(file);
        });
        pressButton(chooser, "Open");

        // search a few directories
        int cnt = 0;
        while (cnt<2) {     // was 5.  not enough memory on test machine
            java.awt.Container pane = findContainer(Bundle.getMessage("previewDir"));
            Assert.assertNotNull("Preview directory not found", pane);
            pressButton(pane, Bundle.getMessage("ButtonKeepLooking"));
            cnt++;
        }

        // cancel search of more directories
        java.awt.Container pane = findContainer(Bundle.getMessage("previewDir"));
        Assert.assertNotNull("Preview Cancel not found", pane);
        pressButton(pane, Bundle.getMessage("ButtonCancel"));

        // dismiss info dialog of count of number of icons found
        pane = findContainer(Bundle.getMessage("info"));
        Assert.assertNotNull("Preview dismiss not found", pane);
        pressButton(pane, "OK");
    }
    
    java.awt.Container findContainer(String title) {
        DialogFinder finder = new DialogFinder(title);
        JUnitUtil.waitFor(() -> {
            return (java.awt.Container)finder.find()!=null;
        }, "Found dialog + \"title\"");
        java.awt.Container pane = (java.awt.Container)finder.find();
        return pane;
        
    }
    private javax.swing.AbstractButton pressButton(java.awt.Container frame, String text) {
        AbstractButtonFinder buttonFinder = new AbstractButtonFinder(text);
        javax.swing.AbstractButton button = (javax.swing.AbstractButton) buttonFinder.find(frame, 0);
        Assert.assertNotNull(text + " Button not found", button);
        getHelper().enterClickAndLeave(new MouseEventData(this, button));
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
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initShutDownManager();
    }

    @Override
    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    // static private Logger log = LoggerFactory.getLogger(ImageIndexEditorTest.class.getName());
}

