package jmri.jmrit.catalog;

import java.awt.Container;
import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Ignore;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.JFrameOperator;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * ImageIndexEditorTest
 *
 * @author pete cressman
 */
public class ImageIndexEditorTest extends jmri.util.SwingTestCase {

    public void testShow() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ImageIndexEditor indexEditor = InstanceManager.getDefault(ImageIndexEditor.class);
        Assert.assertNotNull(JFrameOperator.waitJFrame(Bundle.getMessage("editIndexFrame"), true, true));

        jmri.util.ThreadingUtil.runOnGUIEventually(() -> {
            indexEditor.addNode(null);
        });
        new QueueTool().waitEmpty();
        Container pane = JUnitUtil.findContainer(Bundle.getMessage("QuestionTitle"));
        Assert.assertNotNull("Select node prompt not found", pane);
        JUnitUtil.pressButton(pane, Bundle.getMessage("ButtonOK"));
        new JFrameOperator(indexEditor).dispose();
    }

    @Ignore("Commented out in JUnit 3")
    public void testOpenDirectory() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.util.ThreadingUtil.runOnGUIEventually(() -> {
            InstanceManager.getDefault(DirectorySearcher.class).openDirectory();
        });
        Container pane = JUnitUtil.findContainer(Bundle.getMessage("openDirMenu"));
        Assert.assertNotNull("FileChooser not found", pane);
        JUnitUtil.pressButton(pane, "Cancel");
    }

    /*
    @Ignore("Commented out in JUnit 3")
    public void testPreviewDialog()  throws FileNotFoundException, IOException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        long time = System.currentTimeMillis();
        System.out.println("Start testPreviewDialog: time = "+time+"ms");
        jmri.util.ThreadingUtil.runOnGUIEventually(() -> {
            InstanceManager.getDefault(DirectorySearcher.class).searchFS();
        });
        JFileChooser chooser = JFileChooserOperator.waitJFileChooser();
        Assert.assertNotNull(" JFileChooser not found", chooser);
        File file = FileUtil.getFile(FileUtil.getAbsoluteFilename("program:resources/icons"));
        Assert.assertTrue(file.getPath()+" File does not exist", file.exists());
        new QueueTool().waitEmpty();
        jmri.util.ThreadingUtil.runOnGUIEventually(() -> {
            chooser.setCurrentDirectory(file);
        });
        new QueueTool().waitEmpty();
        JUnitUtil.pressButton(chooser, "Open");
        new QueueTool().waitEmpty();
        System.out.println("Mid testPreviewDialog: elapsed time = "+ (System.currentTimeMillis()-time)+"ms");

        // search a few directories
        int cnt = 0;
        while (cnt<1) {     // was 5.  not enough memory on Mac test machine?
            Container pane = JUnitUtil.findContainer(Bundle.getMessage("previewDir"));
            Assert.assertNotNull("Preview directory not found", pane);
            JUnitUtil.pressButton(pane, Bundle.getMessage("ButtonKeepLooking"));
            cnt++;
            flushAWT();
        }
        System.out.println("Mid testPreviewDialog: elapsed time = "+ (System.currentTimeMillis()-time)+"ms");

        // cancel search of more directories
        Container pane = JUnitUtil.findContainer(Bundle.getMessage("previewDir"));
        Assert.assertNotNull("Preview Cancel not found", pane);
        JUnitUtil.pressButton(pane, Bundle.getMessage("ButtonCancel"));
        new QueueTool().waitEmpty();

        // dismiss info dialog of count of number of icons found
        pane = JUnitUtil.findContainer(Bundle.getMessage("info"));
        Assert.assertNotNull("Preview dismiss not found", pane);
        JUnitUtil.pressButton(pane, Bundle.getMessage("ButtonOK"));
        System.out.println("End testPreviewDialog: elapsed time = "+ (System.currentTimeMillis()-time)+"ms");
    }
    */

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
        JUnitUtil.resetProfileManager();
        JUnitUtil.initShutDownManager();
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ImageIndexEditorTest.class);

}
