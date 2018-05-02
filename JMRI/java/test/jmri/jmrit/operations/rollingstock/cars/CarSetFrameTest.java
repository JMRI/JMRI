//CarSetFrameTest.java
package jmri.jmrit.operations.rollingstock.cars;

import java.awt.GraphicsEnvironment;
import jmri.IdTagManager;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsSwingTestCase;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the Operations CarSetFrame class
 *
 * @author	Dan Boudreau Copyright (C) 2009
 */
public class CarSetFrameTest extends OperationsSwingTestCase {

    @Test
    public void testCarSetFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        loadCars();		// load cars
        CarSetFrame f = new CarSetFrame();
        f.setTitle("Test Car Set Frame");
        f.initComponents();
        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c3 = cManager.getByRoadAndNumber("AA", "3");
        f.loadCar(c3);

        JUnitUtil.dispose(f);
    }

    private void loadCars() {
        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        // remove previous cars
        cManager.dispose();

        // register the road names used
        CarRoads cr = InstanceManager.getDefault(CarRoads.class);
        cr.addName("UP");
        cr.addName("SP");
        cr.addName("NH");
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
        InstanceManager.getDefault(IdTagManager.class).provideIdTag("RFID 3");
        c1.setRfid("RFID 3");
        c1.setTypeName(Bundle.getMessage("Caboose"));
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
        InstanceManager.getDefault(IdTagManager.class).provideIdTag("RFID 2");
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
        InstanceManager.getDefault(IdTagManager.class).provideIdTag("RFID 5");
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
        InstanceManager.getDefault(IdTagManager.class).provideIdTag("RFID 4");
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
        InstanceManager.getDefault(IdTagManager.class).provideIdTag("RFID 1");
        c5.setRfid("RFID 1");
        c5.setTypeName("Coilcar");

    }

    @Override
    @After
    public void setUp() throws Exception {
        super.setUp();
        JUnitUtil.initIdTagManager();
    }

    @Override
    @Before
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
