package jmri.jmrit.blockboss;

import java.awt.GraphicsEnvironment;

import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.implementation.VirtualSignalHead;

import jmri.util.JUnitUtil;

import org.junit.Assume;
import org.junit.jupiter.api.*;
import org.netbeans.jemmy.operators.*;

/**
 * Test the 4 SSL types.
 * @author Paul Bender Copyright (C) 2017
 * @author Dave Sand Copyright (C) 2021
 */
public class BlockBossFrameTest {

    private BlockBossFrame frame;
    private SignalHeadManager signalHeadManager;

    @Test
    public void testSingle() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        frame.setVisible(true);
        JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("Simple_Signal_Logic"));  // NOI18N
        new JComboBoxOperator(jfo, 0).selectItem(1);
        new JComboBoxOperator(jfo, 1).selectItem(1);
        new JButtonOperator(jfo, Bundle.getMessage("ButtonApply")).doClick();
    }

    @Test
    public void testTrailMain() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        frame.setVisible(true);
        JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("Simple_Signal_Logic"));  // NOI18N
        new JRadioButtonOperator(jfo, 1).doClick();
        new JComboBoxOperator(jfo, 0).selectItem(1);
        new JComboBoxOperator(jfo, 1).selectItem(1);
        new JButtonOperator(jfo, Bundle.getMessage("ButtonApply")).doClick();
    }

    @Test
    public void testTrailDiv() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        frame.setVisible(true);
        JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("Simple_Signal_Logic"));  // NOI18N
        new JRadioButtonOperator(jfo, 2).doClick();
        new JComboBoxOperator(jfo, 0).selectItem(1);
        new JComboBoxOperator(jfo, 1).selectItem(1);
        new JButtonOperator(jfo, Bundle.getMessage("ButtonApply")).doClick();
    }

    @Test
    public void testFacing() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        frame.setVisible(true);
        JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("Simple_Signal_Logic"));  // NOI18N
        new JRadioButtonOperator(jfo, 3).doClick();
        new JComboBoxOperator(jfo, 0).selectItem(1);
        new JComboBoxOperator(jfo, 1).selectItem(1);
        new JButtonOperator(jfo, Bundle.getMessage("ButtonApply")).doClick();
    }

    @Test
    public void testDelete() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        frame.setVisible(true);
        JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("Simple_Signal_Logic"));  // NOI18N
        new JRadioButtonOperator(jfo, 0).doClick();
        new JComboBoxOperator(jfo, 0).selectItem(1);
        new JComboBoxOperator(jfo, 1).selectItem(1);
        new JButtonOperator(jfo, Bundle.getMessage("ButtonDelete")).doClick();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalSignalHeadManager();
        signalHeadManager = InstanceManager.getDefault(SignalHeadManager.class);
        signalHeadManager.register(new VirtualSignalHead("IH1","signal head 1"));
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS1");
        if (!GraphicsEnvironment.isHeadless()) {
            frame = new BlockBossFrame();
        }
    }

    @AfterEach
    public void tearDown() {
        if (!GraphicsEnvironment.isHeadless()) {
            JUnitUtil.dispose(frame);
            frame = null;
        }
        JUnitUtil.clearBlockBossLogic();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(BlockBossFrameTest.class);
}
