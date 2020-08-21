package jmri.jmrit.catalog;

import java.awt.Container;
import java.awt.GraphicsEnvironment;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DirectorySearcherTest.class);

}
