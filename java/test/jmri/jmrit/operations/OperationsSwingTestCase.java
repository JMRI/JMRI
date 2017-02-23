//OperationsTestCase.java
package jmri.jmrit.operations;

import java.io.File;
import java.awt.Component;
import java.util.List;
import java.util.Locale;
import jmri.jmrit.operations.automation.AutomationManager;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.locations.schedules.ScheduleManager;
import jmri.jmrit.operations.rollingstock.RollingStockLogger;
import jmri.jmrit.operations.rollingstock.cars.CarColors;
import jmri.jmrit.operations.rollingstock.cars.CarLengths;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarManagerXml;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.engines.EngineLengths;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.rollingstock.engines.EngineManagerXml;
import jmri.jmrit.operations.rollingstock.engines.EngineModels;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.routes.RouteManagerXml;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.TrainManagerXml;
import jmri.util.FileUtil;
import jmri.util.JUnitUtil;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JmriJFrame;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.extensions.jfcunit.finder.AbstractButtonFinder;
import junit.extensions.jfcunit.finder.DialogFinder;
import org.junit.Assert;

/**
 * Common setup and tear down for operation tests.
 *
 * @author	Dan Boudreau Copyright (C) 2015
 * 
 */
public class OperationsSwingTestCase extends jmri.util.SwingTestCase {

    public OperationsSwingTestCase(String s) {
        super(s);
    }
    
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

    protected void enterClickAndLeave(Component comp) {
        getHelper().enterClickAndLeave(new MouseEventData(this, comp,1000l));
        jmri.util.JUnitUtil.releaseThread(comp.getTreeLock()); // compensate for race between GUI and test thread
    }
  

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
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
    
    @Override
    protected void tearDown() throws Exception {
        // restore locale
        Locale.setDefault(Locale.getDefault());
        JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
        super.tearDown();
    }
}
