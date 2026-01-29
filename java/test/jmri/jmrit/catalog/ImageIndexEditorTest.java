package jmri.jmrit.catalog;

import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jmri.InstanceManager;
import jmri.ShutDownManager;
import jmri.implementation.swing.SwingShutDownTask;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.JFrameOperator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * ImageIndexEditorTest
 *
 * @author pete cressman
 */
public class ImageIndexEditorTest {

    @Test
    @DisabledIfHeadless
    public void testShow() {

        ImageIndexEditor indexEditor = InstanceManager.getDefault(ImageIndexEditor.class);
        assertNotNull(JFrameOperator.waitJFrame(Bundle.getMessage("editIndexFrame"), true, true));

        jmri.util.ThreadingUtil.runOnGUIEventually(() -> {
            indexEditor.addNode(null);
        });
        new QueueTool().waitEmpty();
        Container pane = JUnitUtil.findContainer(Bundle.getMessage("QuestionTitle"));
        assertNotNull( pane, "Select node prompt not found");
        JUnitUtil.pressButton(pane, Bundle.getMessage("ButtonOK"));
        new JFrameOperator(indexEditor).dispose();
        //verify there is a shutdown task in the shutdown manager
        ShutDownManager sdm = InstanceManager.getDefault(ShutDownManager.class);
        var tasks = sdm.getCallables().stream().collect(ArrayList::new,ArrayList::add,ArrayList::addAll);
        List<SwingShutDownTask> swingTasks = tasks.stream().filter(t -> t instanceof SwingShutDownTask).map(t-> { return (SwingShutDownTask) t;}).collect(Collectors.toList());
        assertThat(swingTasks).isNotEmpty();
        // remove all the tasks from the shutdown manager
        JUnitUtil.clearShutDownManager();
    }

    @BeforeEach
    public void setUp() {
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
