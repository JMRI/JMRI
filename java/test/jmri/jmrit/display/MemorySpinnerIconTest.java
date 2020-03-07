package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import org.junit.*;

/**
 * MemorySpinnerIconTest.java
 * <p>
 * Description:
 *
 * @author	Bob Jacobsen Copyright 2009
 */
public class MemorySpinnerIconTest extends PositionableJPanelTest {

    MemorySpinnerIcon tos1 = null;
    MemorySpinnerIcon tos2 = null;
    MemorySpinnerIcon tos3 = null;
    MemorySpinnerIcon toi1 = null;
    MemorySpinnerIcon toi2 = null;
    MemorySpinnerIcon toi3 = null;

    @Test
    @Override
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("exists",p);
    }

    @Override
    @Test
    public void testShow() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JmriJFrame jf = new JmriJFrame();
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        tos1 = new MemorySpinnerIcon(editor);
        jf.getContentPane().add(tos1);
        tos2 = new MemorySpinnerIcon(editor);
        jf.getContentPane().add(tos2);
        toi1 = new MemorySpinnerIcon(editor);
        jf.getContentPane().add(toi1);
        toi2 = new MemorySpinnerIcon(editor);
        jf.getContentPane().add(toi2);

        InstanceManager.getDefault().clearAll();
        JUnitUtil.initDefaultUserMessagePreferences();

        tos1.setMemory("IM1");
        tos2.setMemory("IM1");
        Memory im1 = InstanceManager.getDefault(MemoryManager.class).getMemory("IM1");
        Assert.assertNotNull(im1);
        im1.setValue("4");

        toi1.setMemory("IM2");
        toi2.setMemory("IM2");
        Memory im2 = InstanceManager.getDefault(MemoryManager.class).getMemory("IM2");
        Assert.assertNotNull(im2);
        im2.setValue(10);

        tos3 = new MemorySpinnerIcon(editor);
        jf.getContentPane().add(tos3);
        toi3 = new MemorySpinnerIcon(editor);
        jf.getContentPane().add(toi3);
        tos3.setMemory("IM1");
        toi3.setMemory("IM2");
        im1.setValue(11.58F);
        im2.setValue(0.89);
        tos1.setMemory("IM1");
        Assert.assertEquals("Spinner 1", "12", tos1.getValue());
        tos2.setMemory("IM2");
        Assert.assertEquals("Spinner 2", "12", tos1.getValue());

        jf.pack();
        jf.setVisible(true);

        if (!System.getProperty("jmri.demo", "false").equals("false")) {
            jf.setVisible(false);
            jf.dispose();
        }
    }

    @Override
    @Before
    public void setUp() {
        super.setUp();
        jmri.InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);
        if (!GraphicsEnvironment.isHeadless()) {
            editor = new jmri.jmrit.display.panelEditor.PanelEditor("Test MemorySpinnerIcon Panel");
            p=tos1 = new MemorySpinnerIcon(editor);
            tos1.setMemory("IM1");
        }
    }

    @Override
    @After
    public void tearDown() {
        tos1 = null;
        tos2 = null;
        tos3 = null;
        toi1 = null;
        toi2 = null;
        toi3 = null;
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TurnoutIconTest.class);
}
