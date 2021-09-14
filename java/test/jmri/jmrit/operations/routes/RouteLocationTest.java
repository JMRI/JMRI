package jmri.jmrit.operations.routes;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.setup.Setup;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class RouteLocationTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Location l = new Location("Test id", "Test Name");
        RouteLocation t = new RouteLocation("Test", l);
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testDepartureTime() {
        Location l = new Location("Test id", "Test Name");
        RouteLocation rl = new RouteLocation("Test", l);
        rl.setDepartureTime("2", "5");
        Assert.assertEquals("Hour", "02", rl.getDepartureTimeHour());
        Assert.assertEquals("Minute", "05", rl.getDepartureTimeMinute());
        Assert.assertEquals("Time", "02:05", rl.getFormatedDepartureTime());
        
        // Change to AM / PM format
        Setup.set12hrFormatEnabled(true);
        Assert.assertEquals("12HR Time", "2:05 AM", rl.getFormatedDepartureTime());
        rl.setDepartureTime("0", "09");
        Assert.assertEquals("12HR Time AM", "12:09 AM", rl.getFormatedDepartureTime());
        // test PM
        rl.setDepartureTime("13", "0");
        Assert.assertEquals("12HR Time PM", "1:00 PM", rl.getFormatedDepartureTime()); 
        rl.setDepartureTime("23", "59");
        Assert.assertEquals("12HR Time PM", "11:59 PM", rl.getFormatedDepartureTime());  
    }
}
