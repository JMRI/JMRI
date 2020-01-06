package jmri.jmrit.catalog;

import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFileChooser;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import jmri.InstanceManager;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;
import jmri.util.JUnitUtil;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.JFileChooserOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author pete cressman
 * @author Paul Bender Copyright (C) 2017	
 */
public class PreviewDialogTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JmriJFrame jf = new JmriJFrame("PreviewDialog test frame");
        // the second paramter is a key for the bundle
        PreviewDialog t = new PreviewDialog(jf,"catalogs",folder.getRoot(),new String[0]);
        Assert.assertNotNull("exists",t);
        t.dispose();
        jf.dispose();
    }

    @Test
    public void testPreviewDialog()  throws FileNotFoundException, IOException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        long time = System.currentTimeMillis();
        log.debug("Start testPreviewDialog: time = {}ms",time);
        jmri.util.ThreadingUtil.runOnGUIEventually(() -> {
            InstanceManager.getDefault(DirectorySearcher.class).searchFS();
        });
        JFileChooser chooser = JFileChooserOperator.waitJFileChooser();
        Assert.assertNotNull(" JFileChooser not found", chooser);

        File dir = FileUtil.getFile(FileUtil.getAbsoluteFilename("program:resources/icons"));
        Assert.assertTrue(dir.getPath()+" Test directory does not exist", dir.exists());
        new QueueTool().waitEmpty();
        jmri.util.ThreadingUtil.runOnGUIEventually(() -> {
            chooser.setCurrentDirectory(dir);
        });
        new QueueTool().waitEmpty();

        File file = FileUtil.getFile(FileUtil.getAbsoluteFilename("program:resources/icons/misc"));
        Assert.assertTrue(file.getPath()+" Test file does not exist", file.exists());
        new QueueTool().waitEmpty();
        jmri.util.ThreadingUtil.runOnGUIEventually(() -> {
            chooser.setSelectedFile(file);
        });
        new QueueTool().waitEmpty();

        // instead of locating the activate button, which can change, via
        //   JUnitUtil.pressButton(chooser, "Choose");
        // we directly fire the dialog
        jmri.util.ThreadingUtil.runOnGUIEventually(() -> {
            chooser.approveSelection();
        });
        new QueueTool().waitEmpty();
        log.debug("Mid testPreviewDialog: elapsed time = {}ms",(System.currentTimeMillis()-time));

        // search a few directories
        int cnt = 0;
        while (cnt<1) {     // was 5.  not enough memory on Mac test machine?
            Container pane = JUnitUtil.findContainer(Bundle.getMessage("previewDir"));
            Assert.assertNotNull("Preview directory not found", pane);
            JUnitUtil.pressButton(pane, Bundle.getMessage("ButtonKeepLooking"));
            cnt++;
            new QueueTool().waitEmpty();
        }
        log.debug("Mid testPreviewDialog: elapsed time = {}ms",(System.currentTimeMillis()-time));

        // cancel search of more directories
        Container pane = JUnitUtil.findContainer(Bundle.getMessage("previewDir"));
        Assert.assertNotNull("Preview Cancel not found", pane);
        JUnitUtil.pressButton(pane, Bundle.getMessage("ButtonCancel"));
        new QueueTool().waitEmpty();

        // dismiss info dialog of count of number of icons found
        pane = JUnitUtil.findContainer("Message");
        Assert.assertNotNull("Preview dismiss not found", pane);
        JUnitUtil.pressButton(pane, Bundle.getMessage("ButtonOK"));
        log.debug("End testPreviewDialog: elapsed time = {}ms",(System.currentTimeMillis()-time));
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(PreviewDialogTest.class.getName());

}
