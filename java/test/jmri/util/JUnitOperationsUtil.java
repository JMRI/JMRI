package jmri.util;

import java.io.File;
import jmri.jmrit.operations.OperationsXml;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common utility methods for working with Opertations related JUnit tests.
 *
 * @author Paul Bender Copyright 2017
 * @since 2.7.1
 */
public class JUnitOperationsUtil {

    /**
     * Reset the OperationsManager and set the files location for 
     * operations file used durring tests.
     */
    public static void resetOperationsManager(){
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

    private final static Logger log = LoggerFactory.getLogger(JUnitOperationsUtil.class.getName());
}
