package jmri.jmrit.catalog;

import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jmri.ShutDownManager;
import jmri.ShutDownTask;
import jmri.implementation.swing.SwingShutDownTask;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.JFrameOperator;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;

import static org.assertj.core.api.Assertions.assertThat;

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
        //verify there is a shutdown task in the shutdown manager
        ShutDownManager sdm = InstanceManager.getDefault(ShutDownManager.class);
        List<ShutDownTask> tasks = sdm.tasks().stream().collect(ArrayList::new,ArrayList::add,ArrayList::addAll);
        List<SwingShutDownTask> swingTasks = tasks.stream().filter(t -> t instanceof SwingShutDownTask).map(t-> { return (SwingShutDownTask) t;}).collect(Collectors.toList());
        assertThat(swingTasks).isNotEmpty();
        // remove all the tasks from the shutdown manager
        tasks.forEach(sdm::deregister);
    }

    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ImageIndexEditorTest.class);

}
