package jmri.jmrit.operations.rollingstock.cars;

import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.trains.Train;

/**
 * Tests for the Operations RollingStock Cars CarManager class Last manually
 * cross-checked on 20090131
 * <p>
 * Still to do: Everything
 *
 * @author Bob Coleman Copyright (C) 2008, 2009
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
        initializeTest();

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
        initializeTest();

        CarManager manager = InstanceManager.getDefault(CarManager.class);

        c1.setBuilt("06-66"); // this becomes 1966
        c2.setBuilt("01-09"); // this becomes 1909
        c3.setBuilt("100"); // this stays at 100
        c4.setBuilt("10"); // this becomes 1910
        c5.setBuilt("1000");
        c6.setBuilt("1956");

        // now get cars by built
        List<Car> carList = manager.getByBuiltList();
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
        initializeTest();

        CarManager manager = InstanceManager.getDefault(CarManager.class);

        c1.setMoves(2);
        c2.setMoves(44);
        c3.setMoves(99999);
        c4.setMoves(33);
        c5.setMoves(4);
        c6.setMoves(9999);

        // now get cars by moves
        List<Car> carList = manager.getByMovesList();
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
        initializeTest();

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
        initializeTest();

        CarManager manager = InstanceManager.getDefault(CarManager.class);

        c1.setColor("RED");
        c2.setColor("BLUE");
        c3.setColor("YELLOW");
        c4.setColor("BLACK");
        c5.setColor("ROSE");
        c6.setColor("TUSCAN");
        // now get cars by color
        List<Car> carList = manager.getByColorList();
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
        initializeTest();

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
        initializeTest();

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
        initializeTest();

        CarManager manager = InstanceManager.getDefault(CarManager.class);

        // set car weight so there won't be an exception when setting car in a
        // kernel
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
        List<Car> carList = manager.getByKernelList();
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
        initializeTest();

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
        initializeTest();

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
        initializeTest();

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

        // now get cars by train
        List<Car> carList = manager.getByTrainList();
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
        initializeTest();

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
        List<Car> carList = manager.getByTrainList(t1);
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
        initializeTest();

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
    public void testListCarsByTrainDestination2() {
        initializeTest();

        CarManager manager = InstanceManager.getDefault(CarManager.class);
        Route r = new Route("id", "Test");
        r.addLocation(l1);
        RouteLocation rl = r.addLocation(l2); // default train direction is
                                              // north
        r.addLocation(l3);

        Train t1;

        t1 = new Train("id1", "F");
        t1.setRoute(r);

        c4.setCaboose(false);
        c5.setFred(false);
        c6.setCaboose(false);

        c1.setTrain(t1);
        c2.setTrain(t1);
        c3.setTrain(t1);
        c4.setTrain(t1);
        c5.setTrain(t1);
        c6.setTrain(t1);

        c1.setRouteDestination(rl);
        c2.setRouteDestination(rl);
        c3.setRouteDestination(rl);
        c4.setRouteDestination(rl);
        c5.setRouteDestination(rl);
        c6.setRouteDestination(rl);

        l2.addTrack("C", Track.SPUR);

        c1.setDestinationTrack(l2.getTrackByName("B", null));
        c2.setDestinationTrack(l2.getTrackByName("A", null));
        c3.setDestinationTrack(l2.getTrackByName("A", null));
        c4.setDestinationTrack(l2.getTrackByName("C", null));
        c5.setDestinationTrack(l2.getTrackByName("B", null));
        c6.setDestinationTrack(l2.getTrackByName("A", null));

        // normal sort is by spur name, "A" before "B"
        List<Car> carList = manager.getByTrainDestinationList(t1);
        Assert.assertEquals("Number of Cars in t1 by dest", 6, carList.size());
        Assert.assertEquals("1st car in list by t1 by dest", c6, carList.get(0));
        Assert.assertEquals("2nd car in list by t1 by dest", c2, carList.get(1));
        Assert.assertEquals("3rd car in list by t1 by dest", c3, carList.get(2));
        Assert.assertEquals("4th car in list by t1 by dest", c5, carList.get(3));
        Assert.assertEquals("5th car in list by t1 by dest", c1, carList.get(4));
        Assert.assertEquals("6th car in list by t1 by dest", c4, carList.get(5));

        // change track block order, "B" before "A"
        l2.getTrackByName("B", null).setBlockingOrder(1);
        l2.getTrackByName("A", null).setBlockingOrder(2);
        l2.getTrackByName("C", null).setBlockingOrder(3);

        // default train direction is north
        carList = manager.getByTrainDestinationList(t1);
        Assert.assertEquals("Number of Cars in t1 by dest", 6, carList.size());
        Assert.assertEquals("1st car in list by t1 by dest", c5, carList.get(0));
        Assert.assertEquals("2nd car in list by t1 by dest", c1, carList.get(1));
        Assert.assertEquals("3rd car in list by t1 by dest", c6, carList.get(2));
        Assert.assertEquals("4th car in list by t1 by dest", c2, carList.get(3));
        Assert.assertEquals("5th car in list by t1 by dest", c3, carList.get(4));
        Assert.assertEquals("6th car in list by t1 by dest", c4, carList.get(5));

        // change train direction, reverses sort
        rl.setTrainDirection(RouteLocation.SOUTH);

        carList = manager.getByTrainDestinationList(t1);
        Assert.assertEquals("Number of Cars in t1 by dest", 6, carList.size());
        Assert.assertEquals("1st car in list by t1 by dest", c4, carList.get(0));
        Assert.assertEquals("2nd car in list by t1 by dest", c6, carList.get(1));
        Assert.assertEquals("3rd car in list by t1 by dest", c2, carList.get(2));
        Assert.assertEquals("4th car in list by t1 by dest", c3, carList.get(3));
        Assert.assertEquals("5th car in list by t1 by dest", c5, carList.get(4));
        Assert.assertEquals("6th car in list by t1 by dest", c1, carList.get(5));

        // change train direction to West same as North
        rl.setTrainDirection(RouteLocation.WEST);
        carList = manager.getByTrainDestinationList(t1);
        Assert.assertEquals("Number of Cars in t1 by dest", 6, carList.size());
        Assert.assertEquals("1st car in list by t1 by dest", c5, carList.get(0));
        Assert.assertEquals("2nd car in list by t1 by dest", c1, carList.get(1));
        Assert.assertEquals("3rd car in list by t1 by dest", c6, carList.get(2));
        Assert.assertEquals("4th car in list by t1 by dest", c2, carList.get(3));
        Assert.assertEquals("5th car in list by t1 by dest", c3, carList.get(4));
        Assert.assertEquals("6th car in list by t1 by dest", c4, carList.get(5));

        // change train direction, reverses sort
        rl.setTrainDirection(RouteLocation.EAST);

        carList = manager.getByTrainDestinationList(t1);
        Assert.assertEquals("Number of Cars in t1 by dest", 6, carList.size());
        Assert.assertEquals("1st car in list by t1 by dest", c4, carList.get(0));
        Assert.assertEquals("2nd car in list by t1 by dest", c6, carList.get(1));
        Assert.assertEquals("3rd car in list by t1 by dest", c2, carList.get(2));
        Assert.assertEquals("4th car in list by t1 by dest", c3, carList.get(3));
        Assert.assertEquals("5th car in list by t1 by dest", c5, carList.get(4));
        Assert.assertEquals("6th car in list by t1 by dest", c1, carList.get(5));

        // Sort depends on train direction
        rl.setTrainDirection(RouteLocation.NORTH);
        c5.setRouteDestination(null);

        carList = manager.getByTrainDestinationList(t1);
        Assert.assertEquals("Number of Cars in t1 by dest", 6, carList.size());
        Assert.assertEquals("1st car in list by t1 by dest", c1, carList.get(0));
        Assert.assertEquals("2nd car in list by t1 by dest", c6, carList.get(1));
        Assert.assertEquals("3rd car in list by t1 by dest", c2, carList.get(2));
        Assert.assertEquals("4th car in list by t1 by dest", c3, carList.get(3));
        Assert.assertEquals("5th car in list by t1 by dest", c5, carList.get(4));
        Assert.assertEquals("6th car in list by t1 by dest", c4, carList.get(5));

        // sort needs a destination track
        c5.setDestinationTrack(null);

        carList = manager.getByTrainDestinationList(t1);
        Assert.assertEquals("Number of Cars in t1 by dest", 6, carList.size());
        Assert.assertEquals("1st car in list by t1 by dest", c5, carList.get(0));
        Assert.assertEquals("2nd car in list by t1 by dest", c1, carList.get(1));
        Assert.assertEquals("3rd car in list by t1 by dest", c6, carList.get(2));
        Assert.assertEquals("4th car in list by t1 by dest", c2, carList.get(3));
        Assert.assertEquals("5th car in list by t1 by dest", c3, carList.get(4));
        Assert.assertEquals("6th car in list by t1 by dest", c4, carList.get(5));
    }

    @Test
    public void testListCarsByTrainDestinationKernel() {
        initializeTest();

        CarManager manager = InstanceManager.getDefault(CarManager.class);
        Route r = new Route("id", "Test");
        r.addLocation(l1);
        r.addLocation(l2);
        r.addLocation(l3);

        Train t1 = new Train("id1", "F");
        t1.setRoute(r);

        Kernel k = InstanceManager.getDefault(KernelManager.class).newKernel("specialK");

        c4.setKernel(k); // make caboose lead car
        c2.setKernel(k);
        c3.setKernel(k);
        c1.setKernel(k);
        c5.setKernel(k);
        c6.setKernel(k);

        c1.setBlocking(6);
        c2.setBlocking(0);
        c3.setBlocking(2);
        c4.setBlocking(3);
        c5.setBlocking(1);
        c6.setBlocking(4);

        c1.setTrain(t1);
        c2.setTrain(t1);
        c3.setTrain(t1);
        c4.setTrain(t1);
        c5.setTrain(t1);
        c6.setTrain(t1);

        // now get cars by specific train
        List<Car> carList = manager.getByTrainDestinationList(t1);
        Assert.assertEquals("Number of Cars in t1 by dest", 6, carList.size());
        Assert.assertEquals("1st car in list by t1 by dest", c2, carList.get(0));
        Assert.assertEquals("2nd car in list by t1 by dest", c5, carList.get(1));
        Assert.assertEquals("3rd car in list by t1 by dest", c3, carList.get(2));
        Assert.assertEquals("4th car in list by t1 by dest", c4, carList.get(3));
        Assert.assertEquals("5th car in list by t1 by dest", c6, carList.get(4));
        Assert.assertEquals("6th car in list by t1 by dest", c1, carList.get(5));
    }

    @Test
    public void testListCarsByTrainDestinationPassenger() {
        initializeTest();

        CarManager manager = InstanceManager.getDefault(CarManager.class);
        Route r = new Route("id", "Test");
        r.addLocation(l1);
        r.addLocation(l2);
        r.addLocation(l3);

        Train t1 = new Train("id1", "F");
        t1.setRoute(r);

        c1.setPassenger(true);
        c2.setPassenger(true);
        c3.setPassenger(true);

        c1.setBlocking(6);
        c2.setBlocking(0);
        c3.setBlocking(2);
        c4.setBlocking(3); // caboose
        c5.setBlocking(1); // FRED
        c6.setBlocking(4); // caboose

        c1.setTrain(t1);
        c2.setTrain(t1);
        c3.setTrain(t1);
        c4.setTrain(t1);
        c5.setTrain(t1);
        c6.setTrain(t1);

        // now get cars by specific train
        List<Car> carList = manager.getByTrainDestinationList(t1);
        Assert.assertEquals("Number of Cars in t1 by dest", 6, carList.size());
        Assert.assertEquals("1st car in list by t1 by dest", c2, carList.get(0));
        Assert.assertEquals("2nd car in list by t1 by dest", c3, carList.get(1));
        Assert.assertEquals("3rd car in list by t1 by dest", c1, carList.get(2));
        Assert.assertEquals("4th car in list by t1 by dest", c5, carList.get(3));
        Assert.assertEquals("5th car in list by t1 by dest", c6, carList.get(4));
        Assert.assertEquals("6th car in list by t1 by dest", c4, carList.get(5));
    }

    @Test
    public void testListCarsAvailableByTrainNoRoute() {
        initializeTest();

        Train t1 = new Train("id1", "F");

        c1.setTrain(t1);
        c2.setTrain(t1);
        c3.setTrain(t1);

        CarManager manager = InstanceManager.getDefault(CarManager.class);
        List<Car> carList = manager.getAvailableTrainList(t1);
        Assert.assertEquals("Number of Cars available for t1", 0, carList.size());

        // no locations in the route
        Route r = new Route("id", "Test");
        t1.setRoute(r);

        carList = manager.getAvailableTrainList(t1);
        Assert.assertEquals("Number of Cars available for t1", 0, carList.size());
    }

    @Test
    public void testListCarsAvailableByTrain() {
        initializeTest();

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
        // note that c5 isn't available since it is located at the end of the
        // train's route

        // release cars from trains
        c2.setTrain(null);
        c4.setTrain(null);
        c6.setTrain(null); // c6 is located at the end of the route, therefore
                           // not available

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
    public void testListCarsAvailableByTrainCarNoLocation() {
        initializeTest();

        CarManager manager = InstanceManager.getDefault(CarManager.class);
        Route r = new Route("id", "Test");
        r.addLocation(l1);
        r.addLocation(l2);
        r.addLocation(l3);

        Train t1 = new Train("id1", "F");
        t1.setRoute(r);

        c1.setTrain(t1);
        c2.setTrain(t1);
        c3.setTrain(t1);
        c4.setTrain(t1);
        c5.setTrain(t1);
        c6.setTrain(t1);

        c3.setLocation(null, null);

        // how many cars available?
        List<Car> carList = manager.getAvailableTrainList(t1);
        Assert.assertEquals("Number of Cars available for t1", 5, carList.size());
        Assert.assertEquals("1st car in list available for t1", c1, carList.get(0));
    }

    /**
     * Cars at the last location are not normally included in car list if pick up is
     * disabled
     */
    @Test
    public void testListCarsAvailableByTrain2() {
        initializeTest();

        CarManager manager = InstanceManager.getDefault(CarManager.class);
        Route r = new Route("id", "Test");
        r.addLocation(l1);
        r.addLocation(l2);
        RouteLocation last = r.addLocation(l3);
        last.setPickUpAllowed(false); // no pulls

        Train t1 = new Train("id1", "F");
        t1.setRoute(r);

        c1.setTrain(t1);
        c2.setTrain(t1);
        c3.setTrain(t1);
        c4.setTrain(t1);
        c5.setTrain(t1);
        c6.setTrain(t1);

        // how many cars available?
        List<Car> carList = manager.getAvailableTrainList(t1);
        Assert.assertEquals("Number of Cars available for t1", 4, carList.size());
        Assert.assertEquals("1st car in list available for t1", c1, carList.get(0));

        // visit last location twice
        RouteLocation last2 = r.addLocation(l3);
        last2.setPickUpAllowed(false); // no pulls

        carList = manager.getAvailableTrainList(t1);
        Assert.assertEquals("Number of Cars available for t1", 6, carList.size());
    }

    @Test
    public void testListCarsAvailableByTrainStaging() {
        initializeTest();

        CarManager manager = InstanceManager.getDefault(CarManager.class);
        Route r = new Route("id", "Test");
        r.addLocation(l1);
        r.addLocation(l2);
        r.addLocation(l3);

        // make last staging
        l3.changeTrackType(Track.STAGING);

        Train t1 = new Train("id1", "F");
        t1.setRoute(r);

        c1.setTrain(t1);
        c2.setTrain(t1);
        c3.setTrain(t1);
        c4.setTrain(t1);
        c5.setTrain(t1);
        c6.setTrain(t1);

        // how many cars available?
        List<Car> carList = manager.getAvailableTrainList(t1);
        Assert.assertEquals("Number of Cars available for t1", 4, carList.size());
        Assert.assertEquals("1st car in list available for t1", c1, carList.get(0));
    }

    @Test
    public void testListCarsByPriority() {
        initializeTest();

        CarManager manager = InstanceManager.getDefault(CarManager.class);
        Route r = new Route("id", "Test");
        r.addLocation(l1);
        r.addLocation(l2);
        r.addLocation(l3);

        Train t1 = new Train("id1", "F");
        t1.setRoute(r);

        c1.setTrain(t1);
        c2.setTrain(t1);
        c3.setTrain(t1); // load name = "Tools"
        c4.setTrain(t1); // load name = "Fuel"
        c5.setTrain(t1);
        c6.setTrain(t1);

        CarLoads cl = InstanceManager.getDefault(CarLoads.class);
        cl.addName("Boxcar", "Tools");
        cl.setPriority("Boxcar", "Tools", CarLoad.PRIORITY_HIGH);
        cl.addName("Boxcar", "Fuel");
        cl.setPriority("Boxcar", "Fuel", CarLoad.PRIORITY_MEDIUM);

        // 1st car in list should be the car with the high priority load
        List<Car> carList = manager.getAvailableTrainList(t1);
        Assert.assertEquals("Number of Cars available for t1", 6, carList.size());
        Assert.assertEquals("1st car in list available for t1", c3, carList.get(0));
        Assert.assertEquals("2nd car in list available for t1", c4, carList.get(1));
    }

    @Test
    public void testListCarsByNumber() {
        initializeTest();

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
        initializeTest();

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
    public void testGetCarByTypeAndRoad() {
        initializeTest();

        CarManager manager = InstanceManager.getDefault(CarManager.class);

        // find car by type and road
        Assert.assertEquals("find car by type and road", c5, manager.getByTypeAndRoad("Boxcar", "PC"));
    }

    @Test
    public void testListCarsByRfid() {
        initializeTest();

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
        initializeTest();

        CarManager manager = InstanceManager.getDefault(CarManager.class);

        // find car by RFID
        Assert.assertEquals("find c1 by rfid", c1, manager.getByRfid("IDSQ1"));
        Assert.assertEquals("find c2 by rfid", c2, manager.getByRfid("ID1Ab"));
        Assert.assertEquals("find c3 by rfid", c3, manager.getByRfid("IDAse"));
        Assert.assertEquals("find c4 by rfid", c4, manager.getByRfid("IDasd"));
        Assert.assertEquals("find c5 by rfid", c5, manager.getByRfid("ID93F"));
        Assert.assertEquals("find c6 by rfid", c6, manager.getByRfid("IDB12"));
    }

    @Test
    public void testListCarsByType() {
        initializeTest();

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
        initializeTest();

        CarManager manager = InstanceManager.getDefault(CarManager.class);

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
        List<Car> carList = manager.getByLastDateList();
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
        initializeTest();

        CarManager manager = InstanceManager.getDefault(CarManager.class);

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
        List<Car> carList = manager.getByLastDateList(manager.getByIdList());
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
        initializeTest();

        CarManager manager = InstanceManager.getDefault(CarManager.class);

        // check caboose roads
        List<String> cabooseRoads = manager.getCabooseRoadNames();
        Assert.assertEquals("Number of cabooses", 2, cabooseRoads.size());
        Assert.assertEquals("1st road", "AA", cabooseRoads.get(0));
        Assert.assertEquals("2nd road", "CP", cabooseRoads.get(1));
    }

    @Test
    public void testListFREDRoads() {
        initializeTest();

        CarManager manager = InstanceManager.getDefault(CarManager.class);

        // check FRED roads
        List<String> fredRoads = manager.getFredRoadNames();
        Assert.assertEquals("Number of FRED road names", 1, fredRoads.size());
        Assert.assertEquals("1st road", "PC", fredRoads.get(0));

        c3.setFred(true); // road CP
        c4.setFred(true); // road CP, caboose with FRED
        fredRoads = manager.getFredRoadNames();
        Assert.assertEquals("Number of FRED road names", 2, fredRoads.size());
    }

    @Test
    public void testListCarsAtLocation() {
        initializeTest();

        CarManager manager = InstanceManager.getDefault(CarManager.class);
        List<Car> carList = manager.getList(l1);
        Assert.assertEquals("Number of Cars at location", 2, carList.size());
        Assert.assertTrue("c1 in car list at location", carList.contains(c1));
        Assert.assertTrue("c2 in car list at location", carList.contains(c2));
        Assert.assertFalse("c3 not in car list at location", carList.contains(c3));
    }

    @Test
    public void testListCarsOnTrack() {
        initializeTest();

        CarManager manager = InstanceManager.getDefault(CarManager.class);
        Track l1t1 = l1.getTrackByName("A", Track.SPUR);
        List<Car> carList = manager.getList(l1t1);
        Assert.assertEquals("Number of Cars on track", 1, carList.size());
        Assert.assertTrue("c1 in car list on track", carList.contains(c1));
        Assert.assertFalse("c2 not in car list on track", carList.contains(c2));
        Assert.assertFalse("c3 not in car list on track", carList.contains(c3));
    }

    @Test
    public void testReplaceCarLoad() {
        initializeTest();

        Assert.assertEquals("Nuts", c1.getLoadName());
        Assert.assertEquals("Screws", c2.getLoadName());
        Assert.assertEquals("Tools", c3.getLoadName());
        Assert.assertEquals("Fuel", c4.getLoadName());
        Assert.assertEquals("Bags", c5.getLoadName());
        Assert.assertEquals("Nails", c6.getLoadName());

        c2.setLoadName("Tools");
        c2.setTypeName("boxcar"); // don't change this car's load

        c4.setReturnWhenEmptyLoadName("Tools");
        c6.setReturnWhenLoadedLoadName("Tools");

        CarManager manager = InstanceManager.getDefault(CarManager.class);
        manager.replaceLoad("Boxcar", "Tools", "Nuts");

        Assert.assertEquals("Nuts", c1.getLoadName());
        Assert.assertEquals("Tools", c2.getLoadName());
        Assert.assertEquals("Nuts", c3.getLoadName());
        Assert.assertEquals("Fuel", c4.getLoadName());
        Assert.assertEquals("Bags", c5.getLoadName());
        Assert.assertEquals("Nails", c6.getLoadName());

        Assert.assertEquals("Nuts", c4.getReturnWhenEmptyLoadName());
        Assert.assertEquals("Nuts", c6.getReturnWhenLoadedLoadName());

        // now change load to default empty
        manager.replaceLoad("Boxcar", "Nuts", null);

        Assert.assertEquals("E", c1.getLoadName());
        Assert.assertEquals("Tools", c2.getLoadName());
        Assert.assertEquals("E", c3.getLoadName());
        Assert.assertEquals("Fuel", c4.getLoadName());
        Assert.assertEquals("Bags", c5.getLoadName());
        Assert.assertEquals("Nails", c6.getLoadName());

        Assert.assertEquals("E", c4.getReturnWhenEmptyLoadName());
        Assert.assertEquals("L", c6.getReturnWhenLoadedLoadName());
    }

    @Test
    public void testMiaCars() {
        initializeTest();

        CarManager manager = InstanceManager.getDefault(CarManager.class);
        List<Car> carList = manager.getCarsLocationUnknown();
        Assert.assertEquals("no cars mia", 0, carList.size());

        c3.setLocationUnknown(true);
        c4.setLocationUnknown(true);

        carList = manager.getCarsLocationUnknown();
        Assert.assertEquals("cars mia", 2, carList.size());
    }

    private void initializeTest() {
        InstanceManager.getDefault(CarManager.class).dispose();

        CarManager manager = InstanceManager.getDefault(CarManager.class);

        c1 = manager.newRS("CP", "1");
        c2 = manager.newRS("ACL", "3");
        c3 = manager.newRS("CP", "3");
        c4 = manager.newRS("CP", "3-1");
        c5 = manager.newRS("PC", "2");
        c6 = manager.newRS("AA", "1");

        // setup the cars
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
