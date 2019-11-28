package jmri.jmrit.catalog;

import java.awt.Container;
import java.awt.GraphicsEnvironment;

import org.junit.*;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.JFrameOperator;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;

/**
 * ImageIndexEditorTest
 *
 * @author pete cressman
 */
public class ImageIndexEditorTest {

    @Test
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

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // should be converted to check of scheduled ShutDownActions
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ImageIndexEditorTest.class);

}
