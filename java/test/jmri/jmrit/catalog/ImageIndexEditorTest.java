package jmri.jmrit.catalog;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
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
 * <p>
 * Description:
 *
 * @author pete cressman
 */
public class ImageIndexEditorTest extends jmri.util.SwingTestCase {

    public void testShow() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        ImageIndexEditor indexEditor = InstanceManager.getDefault(ImageIndexEditor.class);
        Assert.assertNotNull(JFrameOperator.waitJFrame(Bundle.getMessage("editIndexFrame"), true, true));
        Assert.assertFalse("Index not changed", indexEditor.isIndexChanged());

        jmri.util.ThreadingUtil.runOnGUIEventually(() -> {
            indexEditor.addNode();
        });
        flushAWT();
        java.awt.Container pane = findContainer(Bundle.getMessage("info"));
        Assert.assertNotNull("Select node prompt not found", pane);
        pressButton(pane, "OK");
        junit.extensions.jfcunit.TestHelper.disposeWindow(indexEditor, this);
    }

    /*
    public void testOpenDirectory() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        jmri.util.ThreadingUtil.runOnGUIEventually(() -> {
            InstanceManager.getDefault(DirectorySearcher.class).openDirectory();
        });
        java.awt.Container pane = findContainer(Bundle.getMessage("openDirMenu"));
        Assert.assertNotNull("FileChooser not found", pane);
        pressButton(pane, "Cancel");
    }
/*
    public void testPreviewDialog()  throws FileNotFoundException, IOException {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        long time = System.currentTimeMillis();
        System.out.println("Start testPreviewDialog: time = "+time+"ms");
        jmri.util.ThreadingUtil.runOnGUIEventually(() -> {
            InstanceManager.getDefault(DirectorySearcher.class).searchFS();
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
        flushAWT();
        pressButton(chooser, "Open");
        flushAWT();
        System.out.println("Mid testPreviewDialog: elapsed time = "+ (System.currentTimeMillis()-time)+"ms");

        // search a few directories
        int cnt = 0;
        while (cnt<1) {     // was 5.  not enough memory on Mac test machine?
            java.awt.Container pane = findContainer(Bundle.getMessage("previewDir"));
            Assert.assertNotNull("Preview directory not found", pane);
            pressButton(pane, Bundle.getMessage("ButtonKeepLooking"));
            cnt++;
            flushAWT();
        }
        System.out.println("Mid testPreviewDialog: elapsed time = "+ (System.currentTimeMillis()-time)+"ms");

        // cancel search of more directories
        java.awt.Container pane = findContainer(Bundle.getMessage("previewDir"));
        Assert.assertNotNull("Preview Cancel not found", pane);
        pressButton(pane, Bundle.getMessage("ButtonCancel"));
        flushAWT();

        // dismiss info dialog of count of number of icons found
        pane = findContainer(Bundle.getMessage("info"));
        Assert.assertNotNull("Preview dismiss not found", pane);
        pressButton(pane, "OK");
        System.out.println("End testPreviewDialog: elapsed time = "+ (System.currentTimeMillis()-time)+"ms");
    }
     */
    java.awt.Container findContainer(String title) {
        DialogFinder finder = new DialogFinder(title);
        JUnitUtil.waitFor(() -> {
            return (java.awt.Container) finder.find() != null;
        }, "Found dialog + \"title\"");
        java.awt.Container pane = (java.awt.Container) finder.find();
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
        super.setUp();
        JUnitUtil.setUp();
        JUnitUtil.initShutDownManager();
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ImageIndexEditorTest.class);
}
