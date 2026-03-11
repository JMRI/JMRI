package jmri.jmrit.picker;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jmri.Sensor;
import jmri.SignalHead;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;
import org.netbeans.jemmy.operators.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Dave Sand Copyright (C) 2018
 */
public class PickSinglePanelTest {

    @Test
    @DisabledIfHeadless
    public void testSinglePanel() {
        PickListModel<Sensor> sensorModel = PickListModel.sensorPickModelInstance();
        PickSinglePanel<Sensor> sensorPanel = new PickSinglePanel<>(sensorModel);
        assertNotNull(sensorPanel, "exists");

        jmri.util.JmriJFrame f = new jmri.util.JmriJFrame("Single Pick List");  // NOI18N
        f.setContentPane(sensorPanel);
        f.pack();
        f.setVisible(true);

        JFrameOperator jfo = new JFrameOperator("Single Pick List");
        assertNotNull(jfo);

        // Add an invalid name
        JTextFieldOperator jto = new JTextFieldOperator(jfo, 0);
        jto.typeText("QRS");
        Thread add1 = JemmyUtil.createModalDialogOperatorThread(
            Bundle.getMessage("WarningTitle"), Bundle.getMessage("ButtonOK"));
        new JButtonOperator(jfo, "Add to Table").doClick();  // NOI18N
        JUnitUtil.waitFor(()->{return !(add1.isAlive());}, "add1 finished");  // NOI18N

        // Add a valid name
        jto = new JTextFieldOperator(jfo, 0);
        jto.clearText();
        jto.typeText("IS999");
        jto = new JTextFieldOperator(jfo, 1);
        jto.typeText("User Name");
        new JButtonOperator(jfo, "Add to Table").doClick();  // NOI18N

        JTableOperator jtbo = new JTableOperator(jfo);
        jtbo.clickOnCell(0, 1);
        jmri.NamedBeanHandle<Sensor> nbh = sensorPanel.getSelectedBeanHandle();
        assertNotNull(nbh);

        // Switch to the signal head table
        f.remove(sensorPanel);
        PickListModel<SignalHead> signalHeadModel = PickListModel.signalHeadPickModelInstance();
        PickSinglePanel<SignalHead> signalHeadPanel = new PickSinglePanel<>(signalHeadModel);
        f.setContentPane(signalHeadPanel);
        f.pack();

        //  Verify that the add fields are gone
        JLabelOperator jlo = new JLabelOperator(jfo, 1);
        Assertions.assertTrue(jlo.getText().startsWith("Cannot add new items"));

        JUnitUtil.dispose(f);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PickSinglePanelTest.class);
}
