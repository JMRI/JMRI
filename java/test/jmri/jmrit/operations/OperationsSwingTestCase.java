//OperationsTestCase.java
package jmri.jmrit.operations;

import java.util.List;
import java.util.Locale;
import jmri.util.JUnitUtil;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JmriJFrame;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.util.NameComponentChooser;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import org.junit.Before;
import org.junit.After;
import org.junit.Assert;

/**
 * Common setup and tear down for operation tests.
 *
 * @author	Dan Boudreau Copyright (C) 2015
 * @author  Paul Bender Copyright (C) 2016
 * 
 */
public class OperationsSwingTestCase {

    protected void pressDialogButton(JmriJFrame f, String buttonName) {
        JFrameOperator jfo = new JFrameOperator(f);
        JDialogOperator jdo = new JDialogOperator(jfo,1); // wait for the first dialog.
        NameComponentChooser bChooser = new NameComponentChooser(buttonName);
        //JButtonOperator jbo = new JButtonOperator(jdo,buttonName);
        JButtonOperator jbo = new JButtonOperator(jdo,bChooser);
        // Click button
        jbo.push();
    }

    protected void pressDialogButton(JmriJFrame f,String dialogTitle, String buttonName) {
        JFrameOperator jfo = new JFrameOperator(f);
        JDialogOperator jdo = new JDialogOperator(jfo,dialogTitle); // wait for the first dialog.
        JButtonOperator jbo = new JButtonOperator(jdo,buttonName);
        // Click button
        jbo.push();
    }

    protected void enterClickAndLeave(JButton comp) {
        JButtonOperator jbo = new JButtonOperator(comp);
        jbo.push();
        //jmri.util.JUnitUtil.releaseThread(comp.getTreeLock()); // compensate for race between GUI and test thread
    }
  
    protected void enterClickAndLeave(JCheckBox comp) {
        JCheckBoxOperator jbo = new JCheckBoxOperator(comp);
        jbo.clickMouse();
        //jmri.util.JUnitUtil.releaseThread(comp.getTreeLock()); // compensate for race between GUI and test thread
    }

    protected void enterClickAndLeave(JRadioButton comp) {
        JRadioButtonOperator jbo = new JRadioButtonOperator(comp);
        jbo.clickMouse();
        //jmri.util.JUnitUtil.releaseThread(comp.getTreeLock()); // compensate for race between GUI and test thread
    }

    @Before
    public void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();

        // set the locale to US English
        Locale.setDefault(Locale.ENGLISH);
        
        // Set things up outside of operations
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initIdTagManager();
        JUnitUtil.initShutDownManager();

        JUnitOperationsUtil.resetOperationsManager();

    }
    
    @After
    public void tearDown() throws Exception {
        // restore locale
        Locale.setDefault(Locale.getDefault());
        JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }
}
