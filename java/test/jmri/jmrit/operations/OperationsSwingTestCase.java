//OperationsTestCase.java
package jmri.jmrit.operations;

import java.io.File;
import java.util.List;
import java.util.Locale;
import jmri.jmrit.operations.automation.AutomationManager;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.locations.ScheduleManager;
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
import jmri.util.JmriJFrame;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.extensions.jfcunit.finder.AbstractButtonFinder;
import junit.extensions.jfcunit.finder.DialogFinder;
import org.junit.Assert;

/**
 * Common setup and tear down for operation tests.
 *
 * @author	Dan Boudreau Copyright (C) 2015
 * @version $Revision: 28746 $
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
        getHelper().enterClickAndLeave(new MouseEventData(this, button));
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

        // set the file location to temp (in the root of the build directory).
        OperationsSetupXml.setFileLocation("temp" + File.separator);
        
        // Repoint OperationsSetupXml to JUnitTest subdirectory
        String tempstring = OperationsSetupXml.getOperationsDirectoryName();
        if (!tempstring.contains(File.separator + "JUnitTest")) {
            OperationsSetupXml.setOperationsDirectoryName("operations" + File.separator + "JUnitTest");
        }
        // Change file names to ...Test.xml
        OperationsSetupXml.instance().setOperationsFileName("OperationsJUnitTest.xml");
        RouteManagerXml.instance().setOperationsFileName("OperationsJUnitTestRouteRoster.xml");
        EngineManagerXml.instance().setOperationsFileName("OperationsJUnitTestEngineRoster.xml");
        CarManagerXml.instance().setOperationsFileName("OperationsJUnitTestCarRoster.xml");
        LocationManagerXml.instance().setOperationsFileName("OperationsJUnitTestLocationRoster.xml");
        TrainManagerXml.instance().setOperationsFileName("OperationsJUnitTestTrainRoster.xml");

        FileUtil.createDirectory(OperationsXml.getFileLocation() + OperationsSetupXml.getOperationsDirectoryName());

        // delete files
        File file = new File(RouteManagerXml.instance().getDefaultOperationsFilename());
        file.delete();
        file = new File(EngineManagerXml.instance().getDefaultOperationsFilename());
        file.delete();
        file = new File(CarManagerXml.instance().getDefaultOperationsFilename());
        file.delete();
        file = new File(LocationManagerXml.instance().getDefaultOperationsFilename());
        file.delete();
        file = new File(TrainManagerXml.instance().getDefaultOperationsFilename());
        file.delete();
        file = new File(OperationsSetupXml.instance().getDefaultOperationsFilename());
        file.delete();
      
        TrainManager.instance().dispose();
        LocationManager.instance().dispose();
        RouteManager.instance().dispose();
        ScheduleManager.instance().dispose();
        CarTypes.instance().dispose();
        CarColors.instance().dispose();
        CarLengths.instance().dispose();
        CarLoads.instance().dispose();
        CarRoads.instance().dispose();
        CarManager.instance().dispose();
        AutomationManager.instance().dispose();
        
        // delete file and log directory before testing
        file = new File(RollingStockLogger.instance().getFullLoggerFileName());
        file.delete();
        File dir = new File(RollingStockLogger.instance().getDirectoryName());
        dir.delete();

        RollingStockLogger.instance().dispose();
 
        // dispose of the manager first, because otherwise
        // the models go away.
        EngineManager.instance().dispose();
        EngineModels.instance().dispose();
        EngineLengths.instance().dispose();
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
