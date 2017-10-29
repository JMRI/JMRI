//CarsTableFrameTest.java
package jmri.jmrit.operations.rollingstock.cars;

import java.awt.GraphicsEnvironment;
import java.util.List;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsSwingTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the Operations CarsTableFrame class
 *
 * @author	Dan Boudreau Copyright (C) 2009
 */
public class CarsTableFrameTest extends OperationsSwingTestCase {

    @Test
    public void testCarsTableFrame() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // remove previous cars
        InstanceManager.getDefault(CarManager.class).dispose();
        InstanceManager.getDefault(CarRoads.class).dispose();

        // add Owner1 and Owner2
        CarOwners co = InstanceManager.getDefault(CarOwners.class);
        co.addName("Owner1");
        co.addName("Owner2");
        // add road names
        CarRoads cr = InstanceManager.getDefault(CarRoads.class);
        cr.addName("NH");
        cr.addName("UP");
        cr.addName("AA");
        cr.addName("SP");
        // add locations
        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
        Location westford = lManager.newLocation("Newer Westford");
        Track westfordYard = westford.addTrack("Yard", Track.YARD);
        westfordYard.setLength(300);
        Track westfordSiding = westford.addTrack("Siding", Track.SPUR);
        westfordSiding.setLength(300);
        Track westfordAble = westford.addTrack("Able", Track.SPUR);
        westfordAble.setLength(300);
        Location boxford = lManager.newLocation("Newer Boxford");
        Track boxfordYard = boxford.addTrack("Yard", Track.YARD);
        boxfordYard.setLength(300);
        Track boxfordJacobson = boxford.addTrack("Jacobson", Track.SPUR);
        boxfordJacobson.setLength(300);
        Track boxfordHood = boxford.addTrack("Hood", Track.SPUR);
        boxfordHood.setLength(300);

        // enable rfid field
        Setup.setRfidEnabled(true);

        CarsTableFrame ctf = new CarsTableFrame(true, null, null);
        // show all cars?
        Assert.assertTrue("show all cars", ctf.showAllCars);
        // table should be empty
        Assert.assertEquals("number of cars", "0", ctf.numCars.getText());

        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        // confirm no cars
        Assert.assertEquals("number of cars", 0, cManager.getNumEntries());
        // add 5 cars to table
        loadCars();

        Car c1 = cManager.getByRoadAndNumber("NH", "1");
        Assert.assertNotNull(c1);

        Assert.assertEquals("c1 location", Track.OKAY, c1.setLocation(westford, westfordYard));
        Assert.assertEquals("c1 destination", Track.OKAY, c1.setDestination(boxford, boxfordJacobson));

        Car c2 = cManager.getByRoadAndNumber("UP", "22");
        Assert.assertNotNull(c2);

        Car c3 = cManager.getByRoadAndNumber("AA", "3");
        Assert.assertNotNull(c3);

        Assert.assertEquals("c3 location", Track.OKAY, c3.setLocation(boxford, boxfordHood));
        Assert.assertEquals("c3 destination", Track.OKAY, c3.setDestination(boxford, boxfordYard));

        Car c4 = cManager.getByRoadAndNumber("SP", "2");
        Assert.assertNotNull(c4);

        Assert.assertEquals("c4 location", Track.OKAY, c4.setLocation(westford, westfordSiding));
        Assert.assertEquals("c4 destination", Track.OKAY, c4.setDestination(boxford, boxfordHood));

        Car c5 = cManager.getByRoadAndNumber("NH", "5");

        Assert.assertEquals("c5 location", Track.OKAY, c5.setLocation(westford, westfordAble));
        Assert.assertEquals("c5 destination", Track.OKAY, c5.setDestination(westford, westfordAble));

        Assert.assertEquals("number of cars", "5", ctf.numCars.getText());

        // default is sort by number
        List<Car> cars = ctf.carsTableModel.getSelectedCarList();
        Assert.assertEquals("1st car in sort by number list", c1.getId(), cars.get(0).getId());
        Assert.assertEquals("2nd car in sort by number list", c4.getId(), cars.get(1).getId());
        Assert.assertEquals("3rd car in sort by number list", c3.getId(), cars.get(2).getId());
        Assert.assertEquals("4th car in sort by number list", c5.getId(), cars.get(3).getId());
        Assert.assertEquals("5th car in sort by number list", c2.getId(), cars.get(4).getId());

        // now sort by built date
        enterClickAndLeave(ctf.sortByBuilt);
        cars = ctf.carsTableModel.getSelectedCarList();
        Assert.assertEquals("1st car in sort by built list", c5, cars.get(0));
        Assert.assertEquals("2nd car in sort by built list", c4, cars.get(1));
        Assert.assertEquals("3rd car in sort by built list", c2, cars.get(2));
        Assert.assertEquals("4th car in sort by built list", c3, cars.get(3));
        Assert.assertEquals("5th car in sort by built list", c1, cars.get(4));

        // now sort by color
        enterClickAndLeave(ctf.sortByColor);
        cars = ctf.carsTableModel.getSelectedCarList();
        Assert.assertEquals("1st car in sort by color list", c4.getId(), cars.get(0).getId());
        Assert.assertEquals("2nd car in sort by color list", c2.getId(), cars.get(1).getId());
        Assert.assertEquals("3rd car in sort by color list", c5.getId(), cars.get(2).getId());
        Assert.assertEquals("4th car in sort by color list", c1.getId(), cars.get(3).getId());
        Assert.assertEquals("5th car in sort by color list", c3.getId(), cars.get(4).getId());

        enterClickAndLeave(ctf.sortByDestination);
        cars = ctf.carsTableModel.getSelectedCarList();
        Assert.assertEquals("1st car in sort by destination list", c2, cars.get(0));
        Assert.assertEquals("2nd car in sort by destination list", c4, cars.get(1));
        Assert.assertEquals("3rd car in sort by destination list", c1, cars.get(2));
        Assert.assertEquals("4th car in sort by destination list", c3, cars.get(3));
        Assert.assertEquals("5th car in sort by destination list", c5, cars.get(4));

        enterClickAndLeave(ctf.sortByKernel);
        //TODO add kernels

        // now sort by load
        enterClickAndLeave(ctf.sortByLoad);
        cars = ctf.carsTableModel.getSelectedCarList();
        Assert.assertEquals("1st car in sort by load list", c2, cars.get(0));
        Assert.assertEquals("2nd car in sort by load list", c4, cars.get(1));
        Assert.assertEquals("3rd car in sort by load list", c1, cars.get(2));
        Assert.assertEquals("4th car in sort by load list", c3, cars.get(3));
        Assert.assertEquals("5th car in sort by load list", c5, cars.get(4));

        // now sort by location
        enterClickAndLeave(ctf.sortByLocation);
        cars = ctf.carsTableModel.getSelectedCarList();
        Assert.assertEquals("1st car in sort by location list", c2, cars.get(0));
        Assert.assertEquals("2nd car in sort by location list", c3, cars.get(1));
        Assert.assertEquals("3rd car in sort by location list", c5, cars.get(2));
        Assert.assertEquals("4th car in sort by location list", c4, cars.get(3));
        Assert.assertEquals("5th car in sort by location list", c1, cars.get(4));

        // now sort by moves
        enterClickAndLeave(ctf.sortByMoves);
        cars = ctf.carsTableModel.getSelectedCarList();
        Assert.assertEquals("1st car in sort by move list", c5, cars.get(0));
        Assert.assertEquals("2nd car in sort by move list", c4, cars.get(1));
        Assert.assertEquals("3rd car in sort by move list", c3, cars.get(2));
        Assert.assertEquals("4th car in sort by move list", c2, cars.get(3));
        Assert.assertEquals("5th car in sort by move list", c1, cars.get(4));

        // test sort by number again
        enterClickAndLeave(ctf.sortByNumber);
        cars = ctf.carsTableModel.getSelectedCarList();
        Assert.assertEquals("1st car in sort by number list 2", c1, cars.get(0));
        Assert.assertEquals("2nd car in sort by number list 2", c4, cars.get(1));
        Assert.assertEquals("3rd car in sort by number list 2", c3, cars.get(2));
        Assert.assertEquals("4th car in sort by number list 2", c5, cars.get(3));
        Assert.assertEquals("5th car in sort by number list 2", c2, cars.get(4));

        // test sort by owner
        //enterClickAndLeave(ctf.sortByOwner);
        ctf.sortByOwner.doClick();
        cars = ctf.carsTableModel.getSelectedCarList();
        Assert.assertEquals("1st car in sort by owner list", c4, cars.get(0));
        Assert.assertEquals("2nd car in sort by owner list", c3, cars.get(1));
        Assert.assertEquals("3rd car in sort by owner list", c2, cars.get(2));
        Assert.assertEquals("4th car in sort by owner list", c5, cars.get(3));
        Assert.assertEquals("5th car in sort by owner list", c1, cars.get(4));

        // test sort by rfid
        //enterClickAndLeave(ctf.sortByRfid);
        // use doClick() in case the radio button isn't visible due to scrollbars
        ctf.sortByRfid.doClick();
        cars = ctf.carsTableModel.getSelectedCarList();
        Assert.assertEquals("1st car in sort by rfid list", c5, cars.get(0));
        Assert.assertEquals("2nd car in sort by rfid list", c2, cars.get(1));
        Assert.assertEquals("3rd car in sort by rfid list", c1, cars.get(2));
        Assert.assertEquals("4th car in sort by rfid list", c4, cars.get(3));
        Assert.assertEquals("5th car in sort by rfid list", c3, cars.get(4));

        // test sort by road
        enterClickAndLeave(ctf.sortByRoad);
        cars = ctf.carsTableModel.getSelectedCarList();
        Assert.assertEquals("1st car in sort by road list", c3, cars.get(0));
        Assert.assertEquals("2nd car in sort by road list", c1, cars.get(1));
        Assert.assertEquals("3rd car in sort by road list", c5, cars.get(2));
        Assert.assertEquals("4th car in sort by road list", c4, cars.get(3));
        Assert.assertEquals("5th car in sort by road list", c2, cars.get(4));

        enterClickAndLeave(ctf.sortByTrain);
        //TODO add trains

        // test sort by type
        enterClickAndLeave(ctf.sortByType);
        cars = ctf.carsTableModel.getSelectedCarList();
        Assert.assertEquals("1st car in sort by type list", c2, cars.get(0));
        Assert.assertEquals("2nd car in sort by type list", c1, cars.get(1));
        Assert.assertEquals("3rd car in sort by type list", c5, cars.get(2));
        Assert.assertEquals("4th car in sort by type list", c3, cars.get(3));
        Assert.assertEquals("5th car in sort by type list", c4, cars.get(4));

        // test find text field
        ctf.findCarTextBox.setText("*2");
        enterClickAndLeave(ctf.findButton);
        // table is sorted by type, cars with number 2 are in the first and last rows
        Assert.assertEquals("find car by number 1st", 0, ctf.carsTable.getSelectedRow());
        enterClickAndLeave(ctf.findButton);
        Assert.assertEquals("find car by number 2nd", 4, ctf.carsTable.getSelectedRow());

        // create the CarEditFrame
        enterClickAndLeave(ctf.addButton);

        JUnitUtil.dispose(ctf);

    }

    private void loadCars() {
        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        // add 5 cars to table
        Car c1 = cManager.newCar("NH", "1");
        c1.setBuilt("2009");
        c1.setColor("Red");
        c1.setLength("40");
        c1.setLoadName("L");
        c1.setMoves(55);
        c1.setOwner("Owner2");
        // make sure the ID tags exist before we
        // try to add it to a car.
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("RFID 3");
        c1.setRfid("RFID 3");
        c1.setTypeName("Caboose");
        c1.setWeight("1.4");
        c1.setWeightTons("Tons of Weight");
        c1.setCaboose(true);
        c1.setComment("Test Car NH 1 Comment");

        Car c2 = cManager.newCar("UP", "22");
        c2.setBuilt("2004");
        c2.setColor("Blue");
        c2.setLength("50");
        c2.setLoadName("E");
        c2.setMoves(50);
        c2.setOwner("AT");
        // make sure the ID tags exist before we
        // try to add it to a car.
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("RFID 2");
        c2.setRfid("RFID 2");
        c2.setTypeName("Boxcar");

        Car c3 = cManager.newCar("AA", "3");
        c3.setBuilt("2006");
        c3.setColor("White");
        c3.setLength("30");
        c3.setLoadName("LA");
        c3.setMoves(40);
        c3.setOwner("AB");
        // make sure the ID tags exist before we
        // try to add it to a car.
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("RFID 5");
        c3.setRfid("RFID 5");
        c3.setTypeName("Gondola");

        Car c4 = cManager.newCar("SP", "2");
        c4.setBuilt("1990");
        c4.setColor("Black");
        c4.setLength("45");
        c4.setLoadName("EA");
        c4.setMoves(30);
        c4.setOwner("AAA");
        // make sure the ID tags exist before we
        // try to add it to a car.
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("RFID 4");
        c4.setRfid("RFID 4");
        c4.setTypeName("Tank Food");

        Car c5 = cManager.newCar("NH", "5");
        c5.setBuilt("1956");
        c5.setColor("Brown");
        c5.setLength("25");
        c5.setLoadName("LL");
        c5.setMoves(25);
        c5.setOwner("DAB");
        // make sure the ID tags exist before we
        // try to add it to a car.
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("RFID 1");
        c5.setRfid("RFID 1");
        c5.setTypeName("Coilcar");

    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
