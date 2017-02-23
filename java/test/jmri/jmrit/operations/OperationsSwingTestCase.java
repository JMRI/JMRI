//OperationsTestCase.java
package jmri.jmrit.operations;

import java.util.List;
import java.util.Locale;
import jmri.util.JUnitUtil;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JmriJFrame;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.extensions.jfcunit.finder.AbstractButtonFinder;
import junit.extensions.jfcunit.finder.DialogFinder;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
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

    @SuppressWarnings("unchecked")
    protected void pressDialogButton(JmriJFrame f, String buttonName) {
        //  (with JfcUnit, not pushing this off to another thread)                                                      
        // Locate resulting dialog box
        List<javax.swing.JDialog> dialogList = new DialogFinder(null).findAll(f);
        javax.swing.JDialog d = dialogList.get(0);
        // Find the button
        AbstractButtonFinder finder = new AbstractButtonFinder(buttonName);
        javax.swing.JButton button = (javax.swing.JButton) finder.find(d, 0);
        Assert.assertNotNull("button not found", button);
        // Click button
        enterClickAndLeave(button);
    }

    protected void enterClickAndLeave(JButton comp) {
        JButtonOperator jbo = new JButtonOperator(comp);
        jbo.clickMouse();
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
