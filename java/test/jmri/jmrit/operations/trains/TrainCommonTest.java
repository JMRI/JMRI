package jmri.jmrit.operations.trains;

import java.awt.Color;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.JUnitOperationsUtil;

/**
 * Tests for the TrainCommon class
 *
 * @author Paul Bender Copyright (C) 2015
 */
public class TrainCommonTest extends OperationsTestCase {

    @Test
    public void testGetDate_DateArgument() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        String date = TrainCommon.getDate(calendar.getTime());
        Assert.assertNotNull("Date String", date);
    }

    @Test
    public void testGetDate_BooleanArgument() {
        String date = TrainCommon.getDate(false);
        Assert.assertNotNull("Date String", date);
    }

    @Test
    public void testGetTextColorString() {
        String testString = TrainCommon.formatColorString("Test Color Text", Color.YELLOW);
        Assert.assertEquals("text", "Test Color Text", TrainCommon.getTextColorString(testString));
    }

    @Test
    public void testGetTextColor() {
        String testString = TrainCommon.formatColorString("Test Color Text", Color.YELLOW);
        Assert.assertEquals("text", Color.YELLOW, TrainCommon.getTextColor(testString));
    }
    
    @Test
    public void testSetoutUtilityCarsLocal() {
        JUnitOperationsUtil.initOperationsData();
        CarManager cmanager = InstanceManager.getDefault(CarManager.class);
        
        List<Car> cars = cmanager.getList();
        Assert.assertEquals("Confirm list size", 9, cars.size());
        
        for (Car car : cars) {
            car.setUtility(true);
        }
        
        String[] format = Setup.getLocalSwitchListMessageFormat();
        format[3] = " "; //remove length argument from format
        Setup.setLocalSwitchListMessageFormat(format);
        Setup.setSwitchListFormatSameAsManifest(false);
        
        TrainCommon tc = new TrainCommon();
        // select car type "Boxcar"
        String s = tc.setoutUtilityCars(cars, cars.get(0), TrainCommon.LOCAL, TrainCommon.IS_MANIFEST);
        Assert.assertEquals("Confirm text", " 4     Boxcar 40'  E from North End 2 to  ", s);
        
        s = tc.setoutUtilityCars(cars, cars.get(0), TrainCommon.LOCAL, TrainCommon.IS_MANIFEST);
        Assert.assertNull("should be null", s);
        
        // select car type "Caboose"
        s = tc.setoutUtilityCars(cars, cars.get(4), TrainCommon.LOCAL, !TrainCommon.IS_MANIFEST);
        Assert.assertEquals("Confirm text", " 2     Caboose  from North End 1 to  Test Car CP C10099 Comment", s);
        
        s = tc.setoutUtilityCars(cars, cars.get(4), TrainCommon.LOCAL, !TrainCommon.IS_MANIFEST);
        Assert.assertNull("should be null", s);
    }
    
    @Test
    public void testSetoutUtilityCars() {
        JUnitOperationsUtil.initOperationsData();
        CarManager cmanager = InstanceManager.getDefault(CarManager.class);
        
        List<Car> cars = cmanager.getList();
        Assert.assertEquals("Confirm list size", 9, cars.size());
        
        for (Car car : cars) {
            car.setUtility(true);
        }
        
        String[] format = Setup.getDropSwitchListMessageFormat();
        format[3] = " "; //remove length argument from format
        Setup.setDropSwitchListMessageFormat(format);
        Setup.setSwitchListFormatSameAsManifest(false);
        
        TrainCommon tc = new TrainCommon();
        // select car type "Boxcar"
        String s = tc.setoutUtilityCars(cars, cars.get(0), !TrainCommon.LOCAL, TrainCommon.IS_MANIFEST);
        Assert.assertEquals("Confirm text", " 4     Boxcar 40'  E to   ", s);
        
        s = tc.setoutUtilityCars(cars, cars.get(0), !TrainCommon.LOCAL, TrainCommon.IS_MANIFEST);
        Assert.assertNull("should be null", s);
        
        // select car type "Caboose"
        s = tc.setoutUtilityCars(cars, cars.get(4), !TrainCommon.LOCAL, !TrainCommon.IS_MANIFEST);
        Assert.assertEquals("Confirm text", " 2     Caboose  to  Test Car CP C10099 Comment ", s);
        
        s = tc.setoutUtilityCars(cars, cars.get(4), !TrainCommon.LOCAL, !TrainCommon.IS_MANIFEST);
        Assert.assertNull("should be null", s);
    }
}
