package jmri.jmrit.operations.rollingstock.cars;

import java.awt.GraphicsEnvironment;
import java.util.List;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTableOperator;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.locations.schedules.ScheduleManager;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

/**
 * Tests for the Operations CarsTableFrame class
 *
 * @author Dan Boudreau Copyright (C) 2009
 */
public class CarsTableFrameTest extends OperationsTestCase {

    @Test
    public void testCarsTableFrame() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

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
        Track westfordSpur = westford.addTrack("Spur", Track.SPUR);
        westfordSpur.setLength(300);
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
        Setup.setValueEnabled(true);

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
        c1.setFinalDestination(westford);
        c1.setReturnWhenEmptyDestination(boxford);
        c1.setReturnWhenLoadedDestination(boxford);

        Car c2 = cManager.getByRoadAndNumber("UP", "22");
        Assert.assertNotNull(c2);
        c2.setFinalDestination(boxford);

        Car c3 = cManager.getByRoadAndNumber("AA", "3");
        Assert.assertNotNull(c3);

        Assert.assertEquals("c3 location", Track.OKAY, c3.setLocation(boxford, boxfordHood));
        Assert.assertEquals("c3 destination", Track.OKAY, c3.setDestination(boxford, boxfordYard));
        c3.setFinalDestination(westford);
        c3.setReturnWhenEmptyDestination(westford);
        c3.setReturnWhenLoadedDestination(boxford);
        c3.setReturnWhenLoadedDestTrack(boxfordHood);

        Car c4 = cManager.getByRoadAndNumber("SP", "2");
        Assert.assertNotNull(c4);

        Assert.assertEquals("c4 location", Track.OKAY, c4.setLocation(westford, westfordSpur));
        Assert.assertEquals("c4 destination", Track.OKAY, c4.setDestination(boxford, boxfordHood));
        c4.setFinalDestination(boxford);
        c4.setReturnWhenEmptyDestination(boxford);
        c4.setReturnWhenEmptyDestTrack(boxfordHood);
        c4.setReturnWhenLoadedDestination(westford);
        c4.setReturnWhenLoadedDestTrack(westfordSpur);

        Car c5 = cManager.getByRoadAndNumber("NH", "5");

        Assert.assertEquals("c5 location", Track.OKAY, c5.setLocation(westford, westfordAble));
        Assert.assertEquals("c5 destination", Track.OKAY, c5.setDestination(westford, westfordAble));
        c5.setReturnWhenEmptyDestination(westford);
        c5.setReturnWhenEmptyDestTrack(westfordSpur);

        Assert.assertEquals("number of cars", "5", ctf.numCars.getText());

        // default is sort by number
        List<Car> cars = ctf.carsTableModel.getSelectedCarList();
        Assert.assertEquals("1st car in sort by number list", c1.getId(), cars.get(0).getId());
        Assert.assertEquals("2nd car in sort by number list", c4.getId(), cars.get(1).getId());
        Assert.assertEquals("3rd car in sort by number list", c3.getId(), cars.get(2).getId());
        Assert.assertEquals("4th car in sort by number list", c5.getId(), cars.get(3).getId());
        Assert.assertEquals("5th car in sort by number list", c2.getId(), cars.get(4).getId());

        // now sort by built date
        JemmyUtil.enterClickAndLeave(ctf.sortByBuilt);
        cars = ctf.carsTableModel.getSelectedCarList();
        Assert.assertEquals("1st car in sort by built list", c5, cars.get(0));
        Assert.assertEquals("2nd car in sort by built list", c4, cars.get(1));
        Assert.assertEquals("3rd car in sort by built list", c2, cars.get(2));
        Assert.assertEquals("4th car in sort by built list", c3, cars.get(3));
        Assert.assertEquals("5th car in sort by built list", c1, cars.get(4));

        // now sort by color
        JemmyUtil.enterClickAndLeave(ctf.sortByColor);
        cars = ctf.carsTableModel.getSelectedCarList();
        Assert.assertEquals("1st car in sort by color list", c4.getId(), cars.get(0).getId());
        Assert.assertEquals("2nd car in sort by color list", c2.getId(), cars.get(1).getId());
        Assert.assertEquals("3rd car in sort by color list", c5.getId(), cars.get(2).getId());
        Assert.assertEquals("4th car in sort by color list", c1.getId(), cars.get(3).getId());
        Assert.assertEquals("5th car in sort by color list", c3.getId(), cars.get(4).getId());

        JemmyUtil.enterClickAndLeave(ctf.sortByDestination);
        cars = ctf.carsTableModel.getSelectedCarList();
        Assert.assertEquals("1st car in sort by destination list", c2, cars.get(0));
        Assert.assertEquals("2nd car in sort by destination list", c4, cars.get(1));
        Assert.assertEquals("3rd car in sort by destination list", c1, cars.get(2));
        Assert.assertEquals("4th car in sort by destination list", c3, cars.get(3));
        Assert.assertEquals("5th car in sort by destination list", c5, cars.get(4));

        JemmyUtil.enterClickAndLeave(ctf.sortByKernel);
        //TODO add kernels

        // now sort by load
        JemmyUtil.enterClickAndLeave(ctf.sortByLoad);
        cars = ctf.carsTableModel.getSelectedCarList();
        Assert.assertEquals("1st car in sort by load list", c2, cars.get(0));
        Assert.assertEquals("2nd car in sort by load list", c4, cars.get(1));
        Assert.assertEquals("3rd car in sort by load list", c1, cars.get(2));
        Assert.assertEquals("4th car in sort by load list", c3, cars.get(3));
        Assert.assertEquals("5th car in sort by load list", c5, cars.get(4));

        // now sort by location
        JemmyUtil.enterClickAndLeave(ctf.sortByLocation);
        cars = ctf.carsTableModel.getSelectedCarList();
        Assert.assertEquals("1st car in sort by location list", c2, cars.get(0));
        Assert.assertEquals("2nd car in sort by location list", c3, cars.get(1));
        Assert.assertEquals("3rd car in sort by location list", c5, cars.get(2));
        Assert.assertEquals("4th car in sort by location list", c4, cars.get(3));
        Assert.assertEquals("5th car in sort by location list", c1, cars.get(4));

        // now sort by moves
        JemmyUtil.enterClickAndLeave(ctf.sortByMoves);
        cars = ctf.carsTableModel.getSelectedCarList();
        Assert.assertEquals("1st car in sort by move list", c5, cars.get(0));
        Assert.assertEquals("2nd car in sort by move list", c4, cars.get(1));
        Assert.assertEquals("3rd car in sort by move list", c3, cars.get(2));
        Assert.assertEquals("4th car in sort by move list", c2, cars.get(3));
        Assert.assertEquals("5th car in sort by move list", c1, cars.get(4));

        // test sort by number again
        JemmyUtil.enterClickAndLeave(ctf.sortByNumber);
        cars = ctf.carsTableModel.getSelectedCarList();
        Assert.assertEquals("1st car in sort by number list 2", c1, cars.get(0));
        Assert.assertEquals("2nd car in sort by number list 2", c4, cars.get(1));
        Assert.assertEquals("3rd car in sort by number list 2", c3, cars.get(2));
        Assert.assertEquals("4th car in sort by number list 2", c5, cars.get(3));
        Assert.assertEquals("5th car in sort by number list 2", c2, cars.get(4));

        // test sort by owner
        //JemmyUtil.enterClickAndLeave(ctf.sortByOwner);
        // use doClick() in case the radio button isn't visible due to scrollbars
        ctf.sortByOwner.doClick();
        cars = ctf.carsTableModel.getSelectedCarList();
        Assert.assertEquals("1st car in sort by owner list", c4, cars.get(0));
        Assert.assertEquals("2nd car in sort by owner list", c3, cars.get(1));
        Assert.assertEquals("3rd car in sort by owner list", c2, cars.get(2));
        Assert.assertEquals("4th car in sort by owner list", c5, cars.get(3));
        Assert.assertEquals("5th car in sort by owner list", c1, cars.get(4));

        // test sort by rfid
        //JemmyUtil.enterClickAndLeave(ctf.sortByRfid);
        // use doClick() in case the radio button isn't visible due to scrollbars
        ctf.sortByRfid.doClick();
        cars = ctf.carsTableModel.getSelectedCarList();
        Assert.assertEquals("1st car in sort by rfid list", c5, cars.get(0));
        Assert.assertEquals("2nd car in sort by rfid list", c2, cars.get(1));
        Assert.assertEquals("3rd car in sort by rfid list", c1, cars.get(2));
        Assert.assertEquals("4th car in sort by rfid list", c4, cars.get(3));
        Assert.assertEquals("5th car in sort by rfid list", c3, cars.get(4));

        // test sort by road
        JemmyUtil.enterClickAndLeave(ctf.sortByRoad);
        cars = ctf.carsTableModel.getSelectedCarList();
        Assert.assertEquals("1st car in sort by road list", c3, cars.get(0));
        Assert.assertEquals("2nd car in sort by road list", c1, cars.get(1));
        Assert.assertEquals("3rd car in sort by road list", c5, cars.get(2));
        Assert.assertEquals("4th car in sort by road list", c4, cars.get(3));
        Assert.assertEquals("5th car in sort by road list", c2, cars.get(4));

        JemmyUtil.enterClickAndLeave(ctf.sortByTrain);
        //TODO add trains
        
        JemmyUtil.enterClickAndLeave(ctf.sortByRwe);
        cars = ctf.carsTableModel.getSelectedCarList();
        Assert.assertEquals("1st car in sort by FD list", c2, cars.get(0));
        Assert.assertEquals("2nd car in sort by FD list", c1, cars.get(1));
        Assert.assertEquals("3rd car in sort by FD list", c4, cars.get(2));
        Assert.assertEquals("4th car in sort by FD list", c3, cars.get(3));
        Assert.assertEquals("5th car in sort by FD list", c5, cars.get(4));
        
        JemmyUtil.enterClickAndLeave(ctf.sortByRwl);
        cars = ctf.carsTableModel.getSelectedCarList();
        Assert.assertEquals("1st car in sort by FD list", c2, cars.get(0));
        Assert.assertEquals("2nd car in sort by FD list", c5, cars.get(1));
        Assert.assertEquals("3rd car in sort by FD list", c1, cars.get(2));
        Assert.assertEquals("4th car in sort by FD list", c3, cars.get(3));
        Assert.assertEquals("5th car in sort by FD list", c4, cars.get(4));
        
        JemmyUtil.enterClickAndLeave(ctf.sortByFinalDestination);
        cars = ctf.carsTableModel.getSelectedCarList();
        Assert.assertEquals("1st car in sort by FD list", c5, cars.get(0));
        Assert.assertEquals("2nd car in sort by FD list", c2, cars.get(1));
        Assert.assertEquals("3rd car in sort by FD list", c4, cars.get(2));
        Assert.assertEquals("4th car in sort by FD list", c1, cars.get(3));
        Assert.assertEquals("5th car in sort by FD list", c3, cars.get(4));
        
        JemmyUtil.enterClickAndLeave(ctf.sortByValue);
        //TODO add values
        
        JemmyUtil.enterClickAndLeave(ctf.sortByWait);
        //TODO add wait
        
        JemmyUtil.enterClickAndLeave(ctf.sortByPickup);
        //TODO add pickup
        
        JemmyUtil.enterClickAndLeave(ctf.sortByLast);
        //TODO add last moved date

        // test sort by type
        JemmyUtil.enterClickAndLeave(ctf.sortByType);
        cars = ctf.carsTableModel.getSelectedCarList();
        Assert.assertEquals("1st car in sort by type list", c2, cars.get(0));
        Assert.assertEquals("2nd car in sort by type list", c1, cars.get(1));
        Assert.assertEquals("3rd car in sort by type list", c5, cars.get(2));
        Assert.assertEquals("4th car in sort by type list", c3, cars.get(3));
        Assert.assertEquals("5th car in sort by type list", c4, cars.get(4));

        // test find text field
        ctf.findCarTextBox.setText("*2");
        JemmyUtil.enterClickAndLeave(ctf.findButton);
        // table is sorted by type, cars with number 2 are in the first and last rows
        Assert.assertEquals("find car by number 1st", 0, ctf.carsTable.getSelectedRow());
        JemmyUtil.enterClickAndLeave(ctf.findButton);
        Assert.assertEquals("find car by number 2nd", 4, ctf.carsTable.getSelectedRow());

        // create the CarEditFrame
        JemmyUtil.enterClickAndLeave(ctf.addButton);

        JUnitUtil.dispose(ctf);
    }
    
    @Test
    public void carsTableEditButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        loadCars();
        CarsTableFrame ctf = new CarsTableFrame(true, null, null);
        
        JFrameOperator jfo = new JFrameOperator(ctf);
        JTableOperator tbl = new JTableOperator(jfo);
        tbl.clickOnCell(0, tbl.findColumn(Bundle.getMessage("ButtonEdit"))); // Edit button
        
        // for test coverage
        tbl.clickOnCell(0, tbl.findColumn(Bundle.getMessage("ButtonEdit"))); // Edit button
        
        JFrameOperator jfoc = new JFrameOperator(Bundle.getMessage("TitleCarEdit"));
        JButtonOperator jbo = new JButtonOperator(jfoc, Bundle.getMessage("ButtonDelete"));
        jbo.doClick();

        jfo.dispose(); // also clears the edit car window
    }
    
    @Test
    public void carsTableSetButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        loadCars();
        CarsTableFrame ctf = new CarsTableFrame(true, null, null);
        
        JFrameOperator jfo = new JFrameOperator(ctf);
        JTableOperator tbl = new JTableOperator(jfo);
        tbl.clickOnCell(0, tbl.findColumn(Bundle.getMessage("ButtonSet"))); // Set button
        
        // for test coverage
        tbl.clickOnCell(0, tbl.findColumn(Bundle.getMessage("ButtonSet"))); // Set button
        
        JFrameOperator jfoc = new JFrameOperator(Bundle.getMessage("TitleCarSet"));
        JButtonOperator jbo = new JButtonOperator(jfoc, Bundle.getMessage("ButtonSave"));
        jbo.doClick();

        jfo.dispose(); // also clears the set car window
    }
    
    @Test
    public void carsTableMoves() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        loadCars();
        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c1 = cManager.getByRoadAndNumber("NH", "1");
        Assert.assertEquals("Moves", 55, c1.getMoves());
        
        CarsTableFrame ctf = new CarsTableFrame(true, null, null);
        
        JFrameOperator jfo = new JFrameOperator(ctf);
        JTableOperator tbl = new JTableOperator(jfo);
        tbl.setValueAt(5, 0, tbl.findColumn(Bundle.getMessage("Moves"))); // Moves
        Assert.assertEquals("Moves", 5, c1.getMoves());

        jfo.dispose(); // also clears the set car window
    }
    
    @Test
    public void carsTableWait() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        loadCars();
        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c1 = cManager.getByRoadAndNumber("NH", "1");
        Assert.assertEquals("Wait", 0, c1.getWait());
        c1.setWait(-1); // force to be 1st car in wait sort
        
        // wait only appears if there are schedules
        ScheduleManager sm = InstanceManager.getDefault(ScheduleManager.class);
        sm.newSchedule("Test Schedule");
        
        CarsTableFrame ctf = new CarsTableFrame(true, null, null);
        
        JFrameOperator jfo = new JFrameOperator(ctf);
        JRadioButtonOperator jbo = new JRadioButtonOperator(jfo, Bundle.getMessage("Wait"));
        jbo.doClick();
        JTableOperator tbl = new JTableOperator(jfo);
        tbl.setValueAt(5, 0, tbl.findColumn(Bundle.getMessage("Wait"))); // Moves
        Assert.assertEquals("Wait", 5, c1.getWait());

        jfo.dispose(); // also clears the set car window
    }
    
    @Test
    public void carsTableSelectCheckbox() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        loadCars();
        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c1 = cManager.getByRoadAndNumber("NH", "1");
        CarsTableFrame ctf = new CarsTableFrame(true, null, null);
        
        Assert.assertFalse("Default not selected", c1.isSelected());
        JFrameOperator jfo = new JFrameOperator(ctf);
        JTableOperator tbl = new JTableOperator(jfo);
        tbl.clickOnCell(0, tbl.findColumn(Bundle.getMessage("ButtonSelect"))); // Select button
        Assert.assertTrue("Selected", c1.isSelected());
        
        tbl.clickOnCell(0, tbl.findColumn(Bundle.getMessage("ButtonSelect"))); // Select button
        Assert.assertFalse("Not selected", c1.isSelected());

        jfo.dispose();
    }
    
    private void loadCars() {
        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        // add 5 cars to table
        CarOwners co = InstanceManager.getDefault(CarOwners.class);
        co.addName("Owner2");
            
        Car c1 = JUnitOperationsUtil.createAndPlaceCar("NH", "1", Bundle.getMessage("Caboose"), "40", "Owner2", "2009", null, 55);
        c1.setColor("Red");
        c1.setLoadName("L");
        // make sure the ID tags exist before we
        // try to add it to a car.
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("RFID 3");
        c1.setRfid("RFID 3");
        c1.setWeight("1.4");
        c1.setWeightTons("Tons of Weight");
        c1.setCaboose(true);
        c1.setComment("Test Car NH 1 Comment");

        Car c2 = cManager.newRS("UP", "22");
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

        Car c3 = cManager.newRS("AA", "3");
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

        Car c4 = cManager.newRS("SP", "2");
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

        Car c5 = cManager.newRS("NH", "5");
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
    @BeforeEach
    public void setUp() {
        super.setUp();
        // add type names
        CarTypes ct = InstanceManager.getDefault(CarTypes.class);
        ct.addName("Gondola");
        ct.addName("Boxcar");
        ct.addName(Bundle.getMessage("Caboose"));
        ct.addName("Tank Food");
        ct.addName("Coilcar");
    }
}
