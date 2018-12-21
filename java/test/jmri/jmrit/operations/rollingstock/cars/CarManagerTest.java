package jmri.jmrit.operations.rollingstock.cars;

import java.util.List;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.trains.Train;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the Operations RollingStock Cars CarManager class Last manually
 * cross-checked on 20090131
 * <p>
 * Still to do: Everything
 *
 * @author	Bob Coleman Copyright (C) 2008, 2009
 */
public class CarManagerTest extends OperationsTestCase {

    private Car c1;
    private Car c2;
    private Car c3;
    private Car c4;
    private Car c5;
    private Car c6;
    private Location l1;
    private Location l2;
    private Location l3;

    @Test
    public void testCTor() {
        CarManager manager = InstanceManager.getDefault(CarManager.class);
        Assert.assertNotNull("Manager Creation", manager);
    }

    @Test
    public void testAddCars() {
        CarManager manager = InstanceManager.getDefault(CarManager.class);
        List<Car> carList = manager.getByIdList();

        Assert.assertEquals("Starting Number of Cars", 0, carList.size());
        c1 = manager.newRS("CP", "1");
        c2 = manager.newRS("ACL", "3");
        c3 = manager.newRS("CP", "3");
        c4 = manager.newRS("CP", "3-1");
        c5 = manager.newRS("PC", "2");
        c6 = manager.newRS("AA", "1");
        carList = manager.getByIdList();
        Assert.assertEquals("Finishing Number of Cars", 6, carList.size());
        manager.dispose();
        carList = manager.getByIdList();
        Assert.assertEquals("After dispose Number of Cars", 0, carList.size());
    }

    @Test
    public void testListCarsById() {
        resetCarManager();

        CarManager manager = InstanceManager.getDefault(CarManager.class);
        List<Car> carList = manager.getByIdList();

        // now get cars by id
        carList = manager.getByIdList();
        Assert.assertEquals("Number of Cars by id", 6, carList.size());
        Assert.assertEquals("1st car in list by id", c6, carList.get(0));
        Assert.assertEquals("2nd car in list by id", c2, carList.get(1));
        Assert.assertEquals("3rd car in list by id", c1, carList.get(2));
        Assert.assertEquals("4th car in list by id", c3, carList.get(3));
        Assert.assertEquals("5th car in list by id", c4, carList.get(4));
        Assert.assertEquals("6th car in list by id", c5, carList.get(5));
    }

    @Test
    public void testListCarsByBuildDate() {
        resetCarManager();

        CarManager manager = InstanceManager.getDefault(CarManager.class);
        List<Car> carList;

        c1.setBuilt("06-66"); // this becomes 1966
        c2.setBuilt("01-09"); // this becomes 1909
        c3.setBuilt("100"); // this stays at 100
        c4.setBuilt("10"); // this becomes 1910
        c5.setBuilt("1000");
        c6.setBuilt("1956");

        // now get cars by built
        carList = manager.getByBuiltList();
        Assert.assertEquals("Number of Cars by built", 6, carList.size());
        Assert.assertEquals("1st car in list by built", c3, carList.get(0));
        Assert.assertEquals("2nd car in list by built", c5, carList.get(1));
        Assert.assertEquals("3rd car in list by built", c2, carList.get(2));
        Assert.assertEquals("4th car in list by built", c4, carList.get(3));
        Assert.assertEquals("5th car in list by built", c6, carList.get(4));
        Assert.assertEquals("6th car in list by built", c1, carList.get(5));
    }

    @Test
    public void testListCarsByMoves() {
        resetCarManager();

        CarManager manager = InstanceManager.getDefault(CarManager.class);
        List<Car> carList;

        c1.setMoves(2);
        c2.setMoves(44);
        c3.setMoves(99999);
        c4.setMoves(33);
        c5.setMoves(4);
        c6.setMoves(9999);

        // now get cars by moves
        carList = manager.getByMovesList();
        Assert.assertEquals("Number of Cars by move", 6, carList.size());
        Assert.assertEquals("1st car in list by move", c1, carList.get(0));
        Assert.assertEquals("2nd car in list by move", c5, carList.get(1));
        Assert.assertEquals("3rd car in list by move", c4, carList.get(2));
        Assert.assertEquals("4th car in list by move", c2, carList.get(3));
        Assert.assertEquals("5th car in list by move", c6, carList.get(4));
        Assert.assertEquals("6th car in list by move", c3, carList.get(5));
    }

    @Test
    public void testListCarsByOwner() {
        resetCarManager();

        CarManager manager = InstanceManager.getDefault(CarManager.class);
        List<Car> carList = manager.getByIdList();
        // now get cars by owner
        carList = manager.getByOwnerList();
        Assert.assertEquals("Number of Cars by owner", 6, carList.size());
        Assert.assertEquals("1st car in list by owner", c3, carList.get(0));
        Assert.assertEquals("2nd car in list by owner", c6, carList.get(1));
        Assert.assertEquals("3rd car in list by owner", c5, carList.get(2));
        Assert.assertEquals("4th car in list by owner", c4, carList.get(3));
        Assert.assertEquals("5th car in list by owner", c2, carList.get(4));
        Assert.assertEquals("6th car in list by owner", c1, carList.get(5));
    }

    @Test
    public void testListCarsByColor() {
        resetCarManager();

        CarManager manager = InstanceManager.getDefault(CarManager.class);
        List<Car> carList;

        c1.setColor("RED");
        c2.setColor("BLUE");
        c3.setColor("YELLOW");
        c4.setColor("BLACK");
        c5.setColor("ROSE");
        c6.setColor("TUSCAN");
        // now get cars by color
        carList = manager.getByColorList();
        Assert.assertEquals("Number of Cars by color", 6, carList.size());
        Assert.assertEquals("1st car in list by color", c4, carList.get(0));
        Assert.assertEquals("2nd car in list by color", c2, carList.get(1));
        Assert.assertEquals("3rd car in list by color", c1, carList.get(2));
        Assert.assertEquals("4th car in list by color", c5, carList.get(3));
        Assert.assertEquals("5th car in list by color", c6, carList.get(4));
        Assert.assertEquals("6th car in list by color", c3, carList.get(5));
    }

    @Test
    public void testListCarsByRoadName() {
        resetCarManager();

        CarManager manager = InstanceManager.getDefault(CarManager.class);
        List<Car> carList = manager.getByIdList();
        // now get cars by road name
        carList = manager.getByRoadNameList();
        Assert.assertEquals("Number of Cars by road name", 6, carList.size());
        Assert.assertEquals("1st car in list by road name", c6, carList.get(0));
        Assert.assertEquals("2nd car in list by road name", c2, carList.get(1));
        Assert.assertEquals("3rd car in list by road name", c1, carList.get(2));
        Assert.assertEquals("4th car in list by road name", c3, carList.get(3));
        Assert.assertEquals("5th car in list by road name", c4, carList.get(4));
        Assert.assertEquals("6th car in list by road name", c5, carList.get(5));
    }

    @Test
    public void testListCarsByLoad() {
        resetCarManager();

        CarManager manager = InstanceManager.getDefault(CarManager.class);
        List<Car> carList = manager.getByIdList();
        // now get cars by load
        carList = manager.getByLoadList();
        Assert.assertEquals("Number of Cars by load", 6, carList.size());
        Assert.assertEquals("1st car in list by load", c5, carList.get(0));
        Assert.assertEquals("2nd car in list by load", c4, carList.get(1));
        Assert.assertEquals("3rd car in list by load", c6, carList.get(2));
        Assert.assertEquals("4th car in list by load", c1, carList.get(3));
        Assert.assertEquals("5th car in list by load", c2, carList.get(4));
        Assert.assertEquals("6th car in list by load", c3, carList.get(5));
    }

    @Test
    public void testListCarsByKernel() {
        resetCarManager();

        CarManager manager = InstanceManager.getDefault(CarManager.class);
        List<Car> carList;

        // set car weight so there won't be an exception when setting car in a kernel
        c1.setWeight("20");
        c2.setWeight("6");
        c3.setWeight("21");
        c4.setWeight("20");
        c5.setWeight("50");
        c6.setWeight("30");

        c1.setKernel(new Kernel("F"));
        c2.setKernel(new Kernel("D"));
        c3.setKernel(new Kernel("B"));
        c4.setKernel(new Kernel("A"));
        c5.setKernel(new Kernel("C"));
        c6.setKernel(new Kernel("E"));
        // now get cars by kernel
        carList = manager.getByKernelList();
        Assert.assertEquals("Number of Cars by kernel", 6, carList.size());
        Assert.assertEquals("1st car in list by kernel", c4, carList.get(0));
        Assert.assertEquals("2nd car in list by kernel", c3, carList.get(1));
        Assert.assertEquals("3rd car in list by kernel", c5, carList.get(2));
        Assert.assertEquals("4th car in list by kernel", c2, carList.get(3));
        Assert.assertEquals("5th car in list by kernel", c6, carList.get(4));
        Assert.assertEquals("6th car in list by kernel", c1, carList.get(5));
    }

    @Test
    public void testListCarsByLocation() {
        resetCarManager();

        CarManager manager = InstanceManager.getDefault(CarManager.class);
        // now get cars by location
        List<Car> carList = manager.getByLocationList();
        Assert.assertEquals("Number of Cars by location", 6, carList.size());
        Assert.assertEquals("1st car in list by location", c6, carList.get(0));
        Assert.assertEquals("2nd car in list by location", c5, carList.get(1));
        Assert.assertEquals("3rd car in list by location", c1, carList.get(2));
        Assert.assertEquals("4th car in list by location", c2, carList.get(3));
        Assert.assertEquals("5th car in list by location", c4, carList.get(4));
        Assert.assertEquals("6th car in list by location", c3, carList.get(5));
    }

    @Test
    public void testListCarsByDestination() {
        resetCarManager();

        CarManager manager = InstanceManager.getDefault(CarManager.class);
        List<Car> carList = manager.getByIdList();
        // now get cars by destination
        carList = manager.getByDestinationList();
        Assert.assertEquals("Number of Cars by destination", 6, carList.size());
        Assert.assertEquals("1st car in list by destination", c2, carList.get(0));
        Assert.assertEquals("2nd car in list by destination", c1, carList.get(1));
        Assert.assertEquals("3rd car in list by destination", c5, carList.get(2));
        Assert.assertEquals("4th car in list by destination", c6, carList.get(3));
        Assert.assertEquals("5th car in list by destination", c3, carList.get(4));
        Assert.assertEquals("6th car in list by destination", c4, carList.get(5));
    }

    @Test
    public void testListCarsByTrain() {
        resetCarManager();

        CarManager manager = InstanceManager.getDefault(CarManager.class);
        List<Car> carList;

        Route r = new Route("id", "Test");
        r.addLocation(l1);
        r.addLocation(l2);
        r.addLocation(l3);

        Train t1;
        Train t3;

        t1 = new Train("id1", "F");
        t1.setRoute(r);
        t3 = new Train("id3", "E");
        t3.setRoute(r);

        c1.setTrain(t1);
        c2.setTrain(t3);
        c3.setTrain(t3);
        c4.setTrain(new Train("id4", "B"));
        c5.setTrain(t3);
        c6.setTrain(new Train("id6", "A"));
        // now get cars by train
        carList = manager.getByTrainList();
        Assert.assertEquals("Number of Cars by train", 6, carList.size());
        Assert.assertEquals("1st car in list by train", c6, carList.get(0));
        Assert.assertEquals("2nd car in list by train", c4, carList.get(1));
        Assert.assertEquals("3rd car in list by train", c5, carList.get(2));
        Assert.assertEquals("4th car in list by train", c2, carList.get(3));
        Assert.assertEquals("5th car in list by train", c3, carList.get(4));
        Assert.assertEquals("6th car in list by train", c1, carList.get(5));
    }

    @Test
    public void testListCarsBySpecifiedTrain() {
        resetCarManager();

        CarManager manager = InstanceManager.getDefault(CarManager.class);
        List<Car> carList;
        Route r = new Route("id", "Test");
        r.addLocation(l1);
        r.addLocation(l2);
        r.addLocation(l3);

        Train t1;
        Train t3;

        t1 = new Train("id1", "F");
        t1.setRoute(r);
        t3 = new Train("id3", "E");
        t3.setRoute(r);

        c1.setTrain(t1);
        c2.setTrain(t3);
        c3.setTrain(t3);
        c4.setTrain(new Train("id4", "B"));
        c5.setTrain(t3);
        c6.setTrain(new Train("id6", "A"));
        // now get cars by specific train
        carList = manager.getByTrainList(t1);
        Assert.assertEquals("Number of Cars in t1", 1, carList.size());
        Assert.assertEquals("1st car in list by t1", c1, carList.get(0));
        carList = manager.getByTrainList(t3);
        Assert.assertEquals("Number of Cars in t3", 3, carList.size());
        Assert.assertEquals("1st car in list by t3", c5, carList.get(0));
        Assert.assertEquals("2nd car in list by t3", c2, carList.get(1));
        Assert.assertEquals("3rd car in list by t3", c3, carList.get(2));
    }

    @Test
    public void testListCarsByTrainDestination() {
        resetCarManager();

        CarManager manager = InstanceManager.getDefault(CarManager.class);
        Route r = new Route("id", "Test");
        r.addLocation(l1);
        r.addLocation(l2);
        r.addLocation(l3);

        Train t1;
        Train t3;

        t1 = new Train("id1", "F");
        t1.setRoute(r);
        t3 = new Train("id3", "E");
        t3.setRoute(r);

        c1.setTrain(t1);
        c2.setTrain(t3);
        c3.setTrain(t3);
        c4.setTrain(new Train("id4", "B"));
        c5.setTrain(t3);
        c6.setTrain(new Train("id6", "A"));
        // now get cars by specific train
        List<Car> carList = manager.getByTrainDestinationList(t1);
        Assert.assertEquals("Number of Cars in t1 by dest", 1, carList.size());
        Assert.assertEquals("1st car in list by t1 by dest", c1, carList.get(0));
        carList = manager.getByTrainDestinationList(t3);
        Assert.assertEquals("Number of Cars in t3 by dest", 3, carList.size());
        Assert.assertEquals("1st car in list by t3 by dest", c2, carList.get(0));
        Assert.assertEquals("2nd car in list by t3 by dest", c3, carList.get(1));
        Assert.assertEquals("3rd car in list by t3 by dest", c5, carList.get(2));

    }

    @Test
    public void testListCarsAvailableByTrain() {
        resetCarManager();

        CarManager manager = InstanceManager.getDefault(CarManager.class);
        Route r = new Route("id", "Test");
        r.addLocation(l1);
        r.addLocation(l2);
        RouteLocation last = r.addLocation(l3);

        Train t1;
        Train t3;

        t1 = new Train("id1", "F");
        t1.setRoute(r);
        t3 = new Train("id3", "E");
        t3.setRoute(r);

        c1.setTrain(t1);
        c2.setTrain(t3);
        c3.setTrain(t3);
        c4.setTrain(new Train("id4", "B"));
        c5.setTrain(t3);
        c6.setTrain(new Train("id6", "A"));

        // how many cars available?
        List<Car> carList = manager.getAvailableTrainList(t1);
        Assert.assertEquals("Number of Cars available for t1", 1, carList.size());
        Assert.assertEquals("1st car in list available for t1", c1, carList.get(0));

        carList = manager.getAvailableTrainList(t3);
        Assert.assertEquals("Number of Cars available for t3", 3, carList.size());
        Assert.assertEquals("1st car in list available for t3", c5, carList.get(0));
        Assert.assertEquals("2nd car in list available for t3", c2, carList.get(1));
        Assert.assertEquals("3rd car in list available for t3", c3, carList.get(2));

        // now don't allow pickups at the last location in the train's route
        last.setPickUpAllowed(false);
        carList = manager.getAvailableTrainList(t3);
        Assert.assertEquals("Number of Cars available for t3", 2, carList.size());
        Assert.assertEquals("1st car in list available for t3", c2, carList.get(0));
        Assert.assertEquals("2nd car in list available for t3", c3, carList.get(1));
        // note that c5 isn't available since it is located at the end of the train's route

        // release cars from trains
        c2.setTrain(null);
        c4.setTrain(null);
        c6.setTrain(null);	// c6 is located at the end of the route, therefore not available

        // there should be more cars now
        carList = manager.getAvailableTrainList(t1);
        Assert.assertEquals("Number of Cars available t1 after release", 3, carList.size());
        // should be sorted by moves
        Assert.assertEquals("1st car in list available for t1", c1, carList.get(0));
        Assert.assertEquals("2nd car in list available for t1", c4, carList.get(1));
        Assert.assertEquals("3rd car in list available for t1", c2, carList.get(2));

        carList = manager.getAvailableTrainList(t3);
        Assert.assertEquals("Number of Cars available for t3 after release", 3, carList.size());
        Assert.assertEquals("1st car in list available for t3", c4, carList.get(0));
        Assert.assertEquals("2nd car in list available for t3", c2, carList.get(1));
        Assert.assertEquals("3rd car in list available for t3", c3, carList.get(2));
    }

    @Test
    public void testListCarsByNumber() {
        resetCarManager();

        CarManager manager = InstanceManager.getDefault(CarManager.class);
        List<Car> carList = manager.getByIdList();
        // now get cars by road number
        carList = manager.getByNumberList();
        Assert.assertEquals("Number of Cars by number", 6, carList.size());
        Assert.assertEquals("1st car in list by number", c6, carList.get(0));
        Assert.assertEquals("2nd car in list by number", c1, carList.get(1));
        Assert.assertEquals("3rd car in list by number", c5, carList.get(2));
        Assert.assertEquals("4th car in list by number", c2, carList.get(3));
        Assert.assertEquals("5th car in list by number", c3, carList.get(4));
        Assert.assertEquals("6th car in list by number", c4, carList.get(5));
    }

    @Test
    public void testGetCarByRoadNumber() {
        resetCarManager();

        CarManager manager = InstanceManager.getDefault(CarManager.class);

        // find car by road and number
        Assert.assertEquals("find c1 by road and number", c1, manager.getByRoadAndNumber("CP", "1"));
        Assert.assertEquals("find c2 by road and number", c2, manager.getByRoadAndNumber("ACL", "3"));
        Assert.assertEquals("find c3 by road and number", c3, manager.getByRoadAndNumber("CP", "3"));
        Assert.assertEquals("find c4 by road and number", c4, manager.getByRoadAndNumber("CP", "3-1"));
        Assert.assertEquals("find c5 by road and number", c5, manager.getByRoadAndNumber("PC", "2"));
        Assert.assertEquals("find c6 by road and number", c6, manager.getByRoadAndNumber("AA", "1"));
    }

    @Test
    public void testListCarsByRfid() {
        resetCarManager();

        CarManager manager = InstanceManager.getDefault(CarManager.class);
        List<Car> carList = manager.getByIdList();
        // now get cars by RFID
        carList = manager.getByRfidList();
        Assert.assertEquals("Number of Cars by rfid", 6, carList.size());
        Assert.assertEquals("1st car in list by rfid", c2, carList.get(0));
        Assert.assertEquals("2nd car in list by rfid", c5, carList.get(1));
        Assert.assertEquals("3rd car in list by rfid", c4, carList.get(2));
        Assert.assertEquals("4th car in list by rfid", c3, carList.get(3));
        Assert.assertEquals("5th car in list by rfid", c6, carList.get(4));
        Assert.assertEquals("6th car in list by rfid", c1, carList.get(5));
    }

    @Test
    public void testGetCarByRfid() {
        resetCarManager();

        CarManager manager = InstanceManager.getDefault(CarManager.class);

        // find car by RFID
        Assert.assertEquals("find c1 by rfid", c1, manager.getByRfid("SQ1"));
        Assert.assertEquals("find c2 by rfid", c2, manager.getByRfid("1Ab"));
        Assert.assertEquals("find c3 by rfid", c3, manager.getByRfid("Ase"));
        Assert.assertEquals("find c4 by rfid", c4, manager.getByRfid("asd"));
        Assert.assertEquals("find c5 by rfid", c5, manager.getByRfid("93F"));
        Assert.assertEquals("find c6 by rfid", c6, manager.getByRfid("B12"));
    }

    @Test
    public void testListCarsByType() {
        resetCarManager();

        CarManager manager = InstanceManager.getDefault(CarManager.class);
        List<Car> carList = manager.getByIdList();
        // change car types so sort will work
        c1.setTypeName("F");
        c2.setTypeName("D");
        c3.setTypeName("A");
        c4.setTypeName("B");
        c5.setTypeName("C");
        c6.setTypeName("E");

        // now get cars by type
        carList = manager.getByTypeList();
        Assert.assertEquals("Number of Cars by type", 6, carList.size());
        Assert.assertEquals("1st car in list by type", c3, carList.get(0));
        Assert.assertEquals("2nd car in list by type", c4, carList.get(1));
        Assert.assertEquals("3rd car in list by type", c5, carList.get(2));
        Assert.assertEquals("4th car in list by type", c2, carList.get(3));
        Assert.assertEquals("5th car in list by type", c6, carList.get(4));
        Assert.assertEquals("6th car in list by type", c1, carList.get(5));
    }

    @Test
    public void testListCarsByLastMovedDate() {
        resetCarManager();

        CarManager manager = InstanceManager.getDefault(CarManager.class);
        List<Car> carList;

        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.util.Date start = cal.getTime(); // save to rest time, to avoid
        // test probelms if run near
        // midnight.
        java.util.Date time = cal.getTime();
        c1.setLastDate(time); // right now

        cal.setTime(start);
        cal.add(java.util.Calendar.HOUR_OF_DAY, -1);
        time = cal.getTime();
        c2.setLastDate(time); // one hour ago

        cal.setTime(start);
        cal.add(java.util.Calendar.HOUR_OF_DAY, 1);
        time = cal.getTime();
        c3.setLastDate(time); // one hour from now

        cal.setTime(start);
        cal.set(java.util.Calendar.DAY_OF_MONTH, -1);
        time = cal.getTime();
        c4.setLastDate(time); // one day ago.

        cal.setTime(start);
        cal.add(java.util.Calendar.DAY_OF_MONTH, 1);
        time = cal.getTime();
        c5.setLastDate(time); // one day in the future now.

        cal.setTime(start);
        cal.add(java.util.Calendar.YEAR, -1);
        time = cal.getTime();
        c6.setLastDate(time); // one year ago.

        // now get cars by last move date.
        carList = manager.getByLastDateList();
        Assert.assertEquals("Number of Cars by last move date", 6, carList.size());
        Assert.assertEquals("1st car in list by move date", c6, carList.get(0));
        Assert.assertEquals("2nd car in list by move date", c4, carList.get(1));
        Assert.assertEquals("3rd car in list by move date", c2, carList.get(2));
        Assert.assertEquals("4th car in list by move date", c1, carList.get(3));
        Assert.assertEquals("5th car in list by move date", c3, carList.get(4));
        Assert.assertEquals("6th car in list by move date", c5, carList.get(5));
    }

    @Test
    public void testSortListedCarsByLastMovedDate() {
        resetCarManager();

        CarManager manager = InstanceManager.getDefault(CarManager.class);
        List<Car> carList;

        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.util.Date start = cal.getTime(); // save to rest time, to avoid
        // test probelms if run near
        // midnight.
        java.util.Date time = cal.getTime();
        c1.setLastDate(time); // right now

        cal.setTime(start);
        cal.add(java.util.Calendar.HOUR_OF_DAY, -1);
        time = cal.getTime();
        c2.setLastDate(time); // one hour ago

        cal.setTime(start);
        cal.add(java.util.Calendar.HOUR_OF_DAY, 1);
        time = cal.getTime();
        c3.setLastDate(time); // one hour from now

        cal.setTime(start);
        cal.set(java.util.Calendar.DAY_OF_MONTH, -1);
        time = cal.getTime();
        c4.setLastDate(time); // one day ago.

        cal.setTime(start);
        cal.add(java.util.Calendar.DAY_OF_MONTH, 1);
        time = cal.getTime();
        c5.setLastDate(time); // one day in the future now.

        cal.setTime(start);
        cal.add(java.util.Calendar.YEAR, -1);
        time = cal.getTime();
        c6.setLastDate(time); // one year ago.

        // now get cars by last move date.
        carList = manager.getByLastDateList(manager.getByIdList());
        Assert.assertEquals("Number of Cars by last move date", 6, carList.size());
        Assert.assertEquals("1st car in list by move date", c6, carList.get(0));
        Assert.assertEquals("2nd car in list by move date", c4, carList.get(1));
        Assert.assertEquals("3rd car in list by move date", c2, carList.get(2));
        Assert.assertEquals("4th car in list by move date", c1, carList.get(3));
        Assert.assertEquals("5th car in list by move date", c3, carList.get(4));
        Assert.assertEquals("6th car in list by move date", c5, carList.get(5));
    }

    @Test
    public void testListCabooseRoads() {
        resetCarManager();

        CarManager manager = InstanceManager.getDefault(CarManager.class);

        // check caboose roads
        List<String> cabooseRoads = manager.getCabooseRoadNames();
        Assert.assertEquals("Number of cabooses", 2, cabooseRoads.size());
        Assert.assertEquals("1st road", "AA", cabooseRoads.get(0));
        Assert.assertEquals("2nd road", "CP", cabooseRoads.get(1));
    }

    @Test
    public void testListFREDRoads() {
        resetCarManager();

        CarManager manager = InstanceManager.getDefault(CarManager.class);

        // check FRED roads
        List<String> fredRoads = manager.getFredRoadNames();
        Assert.assertEquals("Number of FRED", 1, fredRoads.size());
        Assert.assertEquals("1st road", "PC", fredRoads.get(0));
    }

    @Test
    public void testListCarsAtLocation() {
        resetCarManager();

        CarManager manager = InstanceManager.getDefault(CarManager.class);
        List<Car> carList = manager.getList(l1);
        Assert.assertEquals("Number of Cars at location", 2, carList.size());
        Assert.assertTrue("c1 in car list at location", carList.contains(c1));
        Assert.assertTrue("c2 in car list at location", carList.contains(c2));
        Assert.assertFalse("c3 not in car list at location", carList.contains(c3));
    }

    @Test
    public void testListCarsOnTrack() {
        resetCarManager();

        CarManager manager = InstanceManager.getDefault(CarManager.class);
        Track l1t1 = l1.getTrackByName("A", Track.SPUR);
        List<Car> carList = manager.getList(l1t1);
        Assert.assertEquals("Number of Cars on track", 1, carList.size());
        Assert.assertTrue("c1 in car list on track", carList.contains(c1));
        Assert.assertFalse("c2 not in car list on track", carList.contains(c2));
        Assert.assertFalse("c3 not in car list on track", carList.contains(c3));
    }

    private void resetCarManager() {
        InstanceManager.getDefault(CarManager.class).dispose();

        CarManager manager = InstanceManager.getDefault(CarManager.class);

        c1 = manager.newRS("CP", "1");
        c2 = manager.newRS("ACL", "3");
        c3 = manager.newRS("CP", "3");
        c4 = manager.newRS("CP", "3-1");
        c5 = manager.newRS("PC", "2");
        c6 = manager.newRS("AA", "1");

        //setup the cars
        c1.setTypeName("Boxcar");
        c2.setTypeName("Boxcar");
        c3.setTypeName("Boxcar");
        c4.setTypeName("Boxcar");
        c5.setTypeName("Boxcar");
        c6.setTypeName("Boxcar");

        c1.setLength("13");
        c2.setLength("9");
        c3.setLength("12");
        c4.setLength("10");
        c5.setLength("11");
        c6.setLength("14");

        l1 = new Location("id1", "B");
        Track l1t1 = l1.addTrack("A", Track.SPUR);
        Track l1t2 = l1.addTrack("B", Track.SPUR);
        l2 = new Location("id2", "C");
        Track l2t1 = l2.addTrack("B", Track.SPUR);
        Track l2t2 = l2.addTrack("A", Track.SPUR);
        l3 = new Location("id3", "A");
        Track l3t1 = l3.addTrack("B", Track.SPUR);
        Track l3t2 = l3.addTrack("A", Track.SPUR);

        // add track lengths
        l1t1.setLength(100);
        l1t2.setLength(100);
        l2t1.setLength(100);
        l2t2.setLength(100);
        l3t1.setLength(100);
        l3t2.setLength(100);

        l1.addTypeName("Boxcar");
        l2.addTypeName("Boxcar");
        l3.addTypeName("Boxcar");
        l1t1.addTypeName("Boxcar");
        l1t2.addTypeName("Boxcar");
        l2t1.addTypeName("Boxcar");
        l2t2.addTypeName("Boxcar");
        l3t1.addTypeName("Boxcar");
        l3t2.addTypeName("Boxcar");

        CarTypes ct = InstanceManager.getDefault(CarTypes.class);
        ct.addName("Boxcar");

        // place cars on tracks
        c1.setLocation(l1, l1t1);
        c2.setLocation(l1, l1t2);
        c3.setLocation(l2, l2t1);
        c4.setLocation(l2, l2t2);
        c5.setLocation(l3, l3t1);
        c6.setLocation(l3, l3t2);

        // set car destinations
        c1.setDestination(l3, l3t1);
        c2.setDestination(l3, l3t2);
        c3.setDestination(l2, l2t2);
        c4.setDestination(l2, l2t1);
        c5.setDestination(l1, l1t1);
        c6.setDestination(l1, l1t2);

        c1.setMoves(2);
        c2.setMoves(44);
        c3.setMoves(99999);
        c4.setMoves(33);
        c5.setMoves(4);
        c6.setMoves(9999);

        // make sure the ID tags exist before we
        // try to add it to a car.
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("SQ1");
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("1Ab");
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("Ase");
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("asd");
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("93F");
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("B12");

        c1.setRfid("SQ1");
        c2.setRfid("1Ab");
        c3.setRfid("Ase");
        c4.setRfid("asd");
        c5.setRfid("93F");
        c6.setRfid("B12");

        c1.setLoadName("Nuts");
        c2.setLoadName("Screws");
        c3.setLoadName("Tools");
        c4.setLoadName("Fuel");
        c5.setLoadName("Bags");
        c6.setLoadName("Nails");

        c1.setOwner("LAST");
        c2.setOwner("FOOL");
        c3.setOwner("AAA");
        c4.setOwner("DAD");
        c5.setOwner("DAB");
        c6.setOwner("BOB");

        // make a couple of cabooses
        c4.setCaboose(true);
        c6.setCaboose(true);

        // car with FRED
        c5.setFred(true);

    }
}
