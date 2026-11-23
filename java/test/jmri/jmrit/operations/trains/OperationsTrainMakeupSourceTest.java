package jmri.jmrit.operations.trains;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import jmri.InstanceManager;
import jmri.TrainMakeupSource;
import jmri.TrainMakeupSource.TrainMakeup;
import jmri.TrainRecord;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.JUnitOperationsUtil;

import org.junit.jupiter.api.Test;

/**
 * Operations' trains as a source of what trains are made up of.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class OperationsTrainMakeupSourceTest extends OperationsTestCase {

    @Test
    public void testRegisteredByTrainManager() {
        InstanceManager.getDefault(TrainManager.class).initialize();
        List<TrainMakeupSource> sources = InstanceManager.getList(TrainMakeupSource.class);
        assertTrue(sources.stream().anyMatch(s -> s instanceof OperationsTrainMakeupSource));
        sources.forEach(s -> {
            if (s instanceof OperationsTrainMakeupSource) {
                ((OperationsTrainMakeupSource) s).dispose();
            }
        });
    }

    @Test
    public void testMakeup() {
        JUnitOperationsUtil.initOperationsData();
        OperationsTrainMakeupSource source = new OperationsTrainMakeupSource();
        Train train1 = InstanceManager.getDefault(TrainManager.class).getTrainById("1");
        assertNull(source.getMakeup(train1.getName()), "not built");
        assertNull(source.getMakeup("No such train"));

        assertFalse(source.isWatching(), "nothing watched until something listens");
        List<String> told = new ArrayList<>();
        java.beans.PropertyChangeListener l = e -> told.add((String) e.getNewValue());
        source.addPropertyChangeListener(TrainMakeupSource.PROPERTY_MAKEUP, l);
        assertTrue(source.isWatching());
        assertTrue(train1.build());
        assertTrue(told.contains(train1.getName()), "told it was built");

        TrainMakeup m = java.util.Objects.requireNonNull(source.getMakeup(train1.getName()));
        List<String> stops = new ArrayList<>();
        train1.getRoute().getLocationsBySequenceList().forEach(rl -> stops.add(rl.getName()));
        String previous = null;
        for (TrainRecord.TrainComponent c : m.getTrainComponents()) {
            assertNotNull(c.getLength(), c.getName());
            assertTrue(stops.contains(c.getName()), "named after the stop it is set out at: " + c.getName());
            assertNotEquals(previous, c.getName(), "a component ends where the stop changes");
            previous = c.getName();
        }
        // the whole train's length in Operations' units, as millimetres on the model
        double expected = train1.getTrainLength() * (Setup.METER.equals(Setup.getLengthUnit()) ? 1000.0 : 304.8)
                / Setup.getScaleRatio();
        Double length = java.util.Objects.requireNonNull(m.getLength());
        assertEquals(expected, length, 0.5);
        assertEquals(2, m.getWork().size(), "two stops ahead");
        assertEquals("North Industries", m.getWork().get(0).getLocation());
        assertEquals(1, m.getWork().get(0).getSetOuts());
        assertEquals(1, m.getWork().get(0).getPickUps());

        // moved on to its next stop: set out one, picked up one
        told.clear();
        train1.move();
        assertTrue(told.contains(train1.getName()), "told it moved on");
        TrainMakeup moved = java.util.Objects.requireNonNull(source.getMakeup(train1.getName()));
        double expectedThere = train1.getTrainLength()
                * (Setup.METER.equals(Setup.getLengthUnit()) ? 1000.0 : 304.8) / Setup.getScaleRatio();
        assertEquals(expectedThere, java.util.Objects.requireNonNull(moved.getLength()), 0.5);
        assertEquals(1, moved.getWork().size(), "one stop ahead");

        // a listener for every property starts and stops the watching too
        java.beans.PropertyChangeListener any = e -> {
        };
        source.removePropertyChangeListener(TrainMakeupSource.PROPERTY_MAKEUP, l);
        assertFalse(source.isWatching());
        source.addPropertyChangeListener(any);
        assertTrue(source.isWatching());
        source.removePropertyChangeListener(any);
        assertFalse(source.isWatching());
        source.addPropertyChangeListener(TrainMakeupSource.PROPERTY_MAKEUP, l);

        int listeners = train1.getPropertyChangeListeners().length;
        source.removePropertyChangeListener(TrainMakeupSource.PROPERTY_MAKEUP, l);
        assertFalse(source.isWatching(), "stops when nothing listens");
        assertEquals(listeners - 1, train1.getPropertyChangeListeners().length);
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

    @Test
    public void testComponentsDividedWhereTheStopChanges() {
        Route route = new Route("R1", "Route");
        RouteLocation yard = route.addLocation(new Location("L1", "Yard"));
        RouteLocation mill = route.addLocation(new Location("L2", "Mill"));
        Engine loco = new Engine("SP", "1");
        Car c1 = new Car("SP", "11");
        Car c2 = new Car("SP", "12");
        Car c3 = new Car("SP", "13");
        Car caboose = new Car("SP", "14");
        loco.setLength("50");
        c1.setLength("40");
        c2.setLength("40");
        c3.setLength("36");
        caboose.setLength("30");
        loco.setRouteDestination(yard); // the engine goes on to the end
        c1.setRouteDestination(mill);
        c2.setRouteDestination(mill);
        c3.setRouteDestination(yard);
        caboose.setRouteDestination(yard);

        List<TrainRecord.TrainComponent> components = OperationsTrainMakeupSource.components(
                List.of(loco, c1, c2, c3, caboose));
        // each length with its couplers, in scale feet, as millimetres on the model
        double perFoot = 304.8 / Setup.getScaleRatio();
        int couplers = jmri.jmrit.operations.rollingstock.RollingStock.COUPLERS;
        assertEquals(3, components.size(), "the engine, the rake for the Mill, and what goes on behind it");
        assertEquals("Yard", components.get(0).getName());
        assertEquals((50 + couplers) * perFoot,
                java.util.Objects.requireNonNull(components.get(0).getLength()), 0.001);
        assertEquals("Mill", components.get(1).getName());
        assertEquals((80 + 2 * couplers) * perFoot,
                java.util.Objects.requireNonNull(components.get(1).getLength()), 0.001);
        assertEquals("Yard", components.get(2).getName());
        assertEquals((66 + 2 * couplers) * perFoot,
                java.util.Objects.requireNonNull(components.get(2).getLength()), 0.001);
    }

    @Test
    public void testMillimetres() {
        assertNull(OperationsTrainMakeupSource.millimetres(0));
        Double mm = java.util.Objects.requireNonNull(OperationsTrainMakeupSource.millimetres(40));
        assertEquals(40 * 304.8 / Setup.getScaleRatio(), mm, 0.001, "40 scale feet");
    }
}
