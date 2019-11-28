package jmri.jmrit.catalog;

import java.awt.Container;
import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;
import org.netbeans.jemmy.QueueTool;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;

/**
 *
 * @author pete cressman
 * @author Paul Bender Copyright (C) 2017	
 */
public class DirectorySearcherTest {

    @Test
    public void testInstance() {
        DirectorySearcher t = DirectorySearcher.instance();
        Assert.assertNotNull("exists",t);
        t.close();
    }

    @Test
    public void testOpenDirectory() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.util.ThreadingUtil.runOnGUIEventually(() -> {
            InstanceManager.getDefault(DirectorySearcher.class).openDirectory();
        });
        Container pane = JUnitUtil.findContainer(Bundle.getMessage("openDirMenu"));
        Assert.assertNotNull("FileChooser not found", pane);
        JUnitUtil.pressButton(pane, "Cancel");
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

    // private final static Logger log = LoggerFactory.getLogger(DirectorySearcherTest.class);

}
