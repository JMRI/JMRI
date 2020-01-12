package jmri.jmrit.picker;

import java.awt.GraphicsEnvironment;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.util.JUnitUtil;
import org.junit.*;
import org.netbeans.jemmy.operators.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Dave Sand Copyright (C) 2018
 */
public class PickSinglePanelTest {

    @Test
    public void testSinglePanel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PickListModel<Sensor> sensorModel = PickListModel.sensorPickModelInstance();
        PickSinglePanel<Sensor> sensorPanel = new PickSinglePanel<Sensor>(sensorModel);
        Assert.assertNotNull("exists", sensorPanel);

        jmri.util.JmriJFrame f = new jmri.util.JmriJFrame("Single Pick List");  // NOI18N
        f.setContentPane(sensorPanel);
        f.pack();
        f.setVisible(true);

        JFrameOperator jfo = new JFrameOperator("Single Pick List");
        Assert.assertNotNull(jfo);

        // Add an invalid name
        JTextFieldOperator jto = new JTextFieldOperator(jfo, 0);
        jto.enterText("QRS");
        Thread add1 = createModalDialogOperatorThread(Bundle.getMessage("WarningTitle"), Bundle.getMessage("ButtonOK"), "add1");  // NOI18N
        new JButtonOperator(jfo, "Add to Table").doClick();  // NOI18N
        JUnitUtil.waitFor(()->{return !(add1.isAlive());}, "add1 finished");  // NOI18N

        // Add a valid name
        jto = new JTextFieldOperator(jfo, 0);
        jto.enterText("IS999");
        jto = new JTextFieldOperator(jfo, 1);
        jto.enterText("User Name");
        new JButtonOperator(jfo, "Add to Table").doClick();  // NOI18N

        JTableOperator jtbo = new JTableOperator(jfo);
        jtbo.clickOnCell(0, 1);
        jmri.NamedBeanHandle nbh = sensorPanel.getSelectedBeanHandle();
        Assert.assertNotNull(nbh);

        // Switch to the signal head table
        f.remove(sensorPanel);
        PickListModel<SignalHead> signalHeadModel = PickListModel.signalHeadPickModelInstance();
        PickSinglePanel<SignalHead> signalHeadPanel = new PickSinglePanel<SignalHead>(signalHeadModel);
        f.setContentPane(signalHeadPanel);
        f.pack();

        //  Verify that the add fields are gone
        JLabelOperator jlo = new JLabelOperator(jfo, 1);
        Assert.assertTrue(jlo.getText().startsWith("Cannot add new items"));

        JUnitUtil.dispose(f);
    }

    Thread createModalDialogOperatorThread(String dialogTitle, String buttonText, String threadName) {
        Thread t = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(dialogTitle);
            JButtonOperator jbo = new JButtonOperator(jdo, buttonText);
            jbo.pushNoBlock();
        });
        t.setName(dialogTitle + " Close Dialog Thread: " + threadName);  // NOI18N
        t.start();
        return t;
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PickSinglePanelTest.class);
}
