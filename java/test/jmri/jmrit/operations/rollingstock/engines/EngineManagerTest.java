package jmri.jmrit.operations.rollingstock.engines;

import java.util.List;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.trains.Train;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the Operations RollingStock Engine class Last manually
 * cross-checked on 20090131
 * <p>
 * Still to do: Engine: Destination Engine: Verify everything else EngineTypes:
 * get/set Names lists EngineModels: get/set Names lists EngineLengths:
 * Everything Consist: Everything Import: Everything EngineManager: Engine
 * register/deregister EngineManager: Consists
 *
 * @author	Bob Coleman Copyright (C) 2008, 2009
 */
public class EngineManagerTest extends OperationsTestCase {

    private Engine e1;
    private Engine e2;
    private Engine e3;
    private Engine e4;
    private Engine e5;
    private Engine e6;
    private Location l1;
    private Location l2;
    private Location l3;

    @Test
    public void testCTor() {
        EngineManager manager = InstanceManager.getDefault(EngineManager.class);
        Assert.assertNotNull("Manager Creation", manager);
    }

    @Test
    public void testAddEngines() {
        EngineManager manager = InstanceManager.getDefault(EngineManager.class);
        List<Engine> engineList = manager.getByIdList();

        Assert.assertEquals("Starting Number of Engines", 0, engineList.size());
        e1 = manager.newRS("CP", "1");
        e2 = manager.newRS("ACL", "3");
        e3 = manager.newRS("CP", "3");
        e4 = manager.newRS("CP", "3-1");
        e5 = manager.newRS("PC", "2");
        e6 = manager.newRS("AA", "1");
        engineList = manager.getByIdList();
        Assert.assertEquals("Finishing Number of Engines", 6, engineList.size());
        manager.dispose();
        engineList = manager.getByIdList();
        Assert.assertEquals("After dispose Number of Engines", 0, engineList.size());
    }

    @Test
    public void testListEnginesById() {
        resetEngineManager();

        EngineManager manager = InstanceManager.getDefault(EngineManager.class);

        // now get engines by id
        List<Engine> engineList = manager.getByIdList();
        Assert.assertEquals("Number of Engines by id", 6, engineList.size());
        Assert.assertEquals("1st engine in list by id", e6, engineList.get(0));
        Assert.assertEquals("2nd engine in list by id", e2, engineList.get(1));
        Assert.assertEquals("3rd engine in list by id", e1, engineList.get(2));
        Assert.assertEquals("4th engine in list by id", e3, engineList.get(3));
        Assert.assertEquals("5th engine in list by id", e4, engineList.get(4));
        Assert.assertEquals("6th engine in list by id", e5, engineList.get(5));
    }

    @Test
    public void testListEnginesByBuildDate() {
        resetEngineManager();

        EngineManager manager = InstanceManager.getDefault(EngineManager.class);

        //setup the engines
        e1.setBuilt("2016");
        e2.setBuilt("1212");
        e3.setBuilt("100"); // this stays 100
        e4.setBuilt("10"); // this becomes 1910
        e5.setBuilt("07-55"); // this becomes 1955
        e6.setBuilt("1956");

        // now get engines by built
        List<Engine> engineList = manager.getByBuiltList();
        Assert.assertEquals("Number of Engines by built", 6, engineList.size());
        Assert.assertEquals("1st engine in list by built", e3, engineList.get(0));
        Assert.assertEquals("2nd engine in list by built", e2, engineList.get(1));
        Assert.assertEquals("3rd engine in list by built", e4, engineList.get(2));
        Assert.assertEquals("4th engine in list by built", e5, engineList.get(3));
        Assert.assertEquals("5th engine in list by built", e6, engineList.get(4));
        Assert.assertEquals("6th engine in list by built", e1, engineList.get(5));
    }

    @Test
    public void testListEnginesByMoves() {
        resetEngineManager();

        EngineManager manager = InstanceManager.getDefault(EngineManager.class);

        //setup the engines
        e1.setMoves(2);
        e2.setMoves(44);
        e3.setMoves(99999);
        e4.setMoves(33);
        e5.setMoves(4);
        e6.setMoves(9999);

        // now get engines by moves
        List<Engine> engineList = manager.getByMovesList();
        Assert.assertEquals("Number of Engines by move", 6, engineList.size());
        Assert.assertEquals("1st engine in list by move", e1, engineList.get(0));
        Assert.assertEquals("2nd engine in list by move", e5, engineList.get(1));
        Assert.assertEquals("3rd engine in list by move", e4, engineList.get(2));
        Assert.assertEquals("4th engine in list by move", e2, engineList.get(3));
        Assert.assertEquals("5th engine in list by move", e6, engineList.get(4));
        Assert.assertEquals("6th engine in list by move", e3, engineList.get(5));
    }

    @Test
    public void testListEnginesByOwner() {
        resetEngineManager();

        EngineManager manager = InstanceManager.getDefault(EngineManager.class);

        //setup the engines
        e1.setOwner("LAST");
        e2.setOwner("FOOL");
        e3.setOwner("AAA");
        e4.setOwner("DAD");
        e5.setOwner("DAB");
        e6.setOwner("BOB");

        // now get engines by owner
        List<Engine> engineList = manager.getByOwnerList();
        Assert.assertEquals("Number of Engines by owner", 6, engineList.size());
        Assert.assertEquals("1st engine in list by owner", e3, engineList.get(0));
        Assert.assertEquals("2nd engine in list by owner", e6, engineList.get(1));
        Assert.assertEquals("3rd engine in list by owner", e5, engineList.get(2));
        Assert.assertEquals("4th engine in list by owner", e4, engineList.get(3));
        Assert.assertEquals("5th engine in list by owner", e2, engineList.get(4));
        Assert.assertEquals("6th engine in list by owner", e1, engineList.get(5));
    }

    @Test
    public void testListEnginesByRoadName() {
        resetEngineManager();

        EngineManager manager = InstanceManager.getDefault(EngineManager.class);

        // now get engines by road name
        List<Engine> engineList = manager.getByRoadNameList();
        Assert.assertEquals("Number of Engines by road name", 6, engineList.size());
        Assert.assertEquals("1st engine in list by road name", e6, engineList.get(0));
        Assert.assertEquals("2nd engine in list by road name", e2, engineList.get(1));
        Assert.assertEquals("3rd engine in list by road name", e1, engineList.get(2));
        Assert.assertEquals("4th engine in list by road name", e3, engineList.get(3));
        Assert.assertEquals("5th engine in list by road name", e4, engineList.get(4));
        Assert.assertEquals("6th engine in list by road name", e5, engineList.get(5));
    }

    @Test
    public void testListEnginesByConsist() {
        resetEngineManager();

        EngineManager manager = InstanceManager.getDefault(EngineManager.class);

        //setup the engines
        e1.setConsist(new Consist("F"));
        e2.setConsist(new Consist("D"));
        e3.setConsist(new Consist("B"));
        e4.setConsist(new Consist("A"));
        e5.setConsist(new Consist("C"));
        e6.setConsist(new Consist("E"));

        // now get engines by consist
        List<Engine> engineList = manager.getByConsistList();
        Assert.assertEquals("Number of Engines by consist", 6, engineList.size());
        Assert.assertEquals("1st engine in list by consist", e4, engineList.get(0));
        Assert.assertEquals("2nd engine in list by consist", e3, engineList.get(1));
        Assert.assertEquals("3rd engine in list by consist", e5, engineList.get(2));
        Assert.assertEquals("4th engine in list by consist", e2, engineList.get(3));
        Assert.assertEquals("5th engine in list by consist", e6, engineList.get(4));
        Assert.assertEquals("6th engine in list by consist", e1, engineList.get(5));
    }

    @Test
    public void testListEnginesByLocation() {
        resetEngineManager();

        EngineManager manager = InstanceManager.getDefault(EngineManager.class);

        // now get engines by location
        List<Engine> engineList = manager.getByLocationList();
        Assert.assertEquals("Number of Engines by location", 6, engineList.size());
        Assert.assertEquals("1st engine in list by location", e6, engineList.get(0));
        Assert.assertEquals("2nd engine in list by location", e5, engineList.get(1));
        Assert.assertEquals("3rd engine in list by location", e1, engineList.get(2));
        Assert.assertEquals("4th engine in list by location", e2, engineList.get(3));
        Assert.assertEquals("5th engine in list by location", e4, engineList.get(4));
        Assert.assertEquals("6th engine in list by location", e3, engineList.get(5));
    }

    @Test
    public void testListEnginesByDestination() {
        resetEngineManager();

        EngineManager manager = InstanceManager.getDefault(EngineManager.class);

        // now get engines by destination
        List<Engine> engineList = manager.getByDestinationList();
        Assert.assertEquals("Number of Engines by destination", 6, engineList.size());
        Assert.assertEquals("1st engine in list by destination", e2, engineList.get(0));
        Assert.assertEquals("2nd engine in list by destination", e1, engineList.get(1));
        Assert.assertEquals("3rd engine in list by destination", e5, engineList.get(2));
        Assert.assertEquals("4th engine in list by destination", e6, engineList.get(3));
        Assert.assertEquals("5th engine in list by destination", e3, engineList.get(4));
        Assert.assertEquals("6th engine in list by destination", e4, engineList.get(5));
    }

    @Test
    public void testListEnginesByTrain() {
        resetEngineManager();

        EngineManager manager = InstanceManager.getDefault(EngineManager.class);

        Route r = new Route("id", "Test");
        r.addLocation(l1);
        r.addLocation(l2);
        r.addLocation(l3);

        Train t1 = new Train("id1", "F");
        t1.setRoute(r);
        Train t3 = new Train("id3", "E");
        t3.setRoute(r);

        //setup the engines
        e1.setTrain(t1);
        e2.setTrain(t3);
        e3.setTrain(t3);
        e4.setTrain(new Train("id4", "B"));
        e5.setTrain(t3);
        e6.setTrain(new Train("id6", "A"));

        // now get engines by train
        List<Engine> engineList = manager.getByTrainList();
        Assert.assertEquals("Number of Engines by train", 6, engineList.size());
        Assert.assertEquals("1st engine in list by train", e6, engineList.get(0));
        Assert.assertEquals("2nd engine in list by train", e4, engineList.get(1));
        Assert.assertEquals("3rd engine in list by train", e5, engineList.get(2));
        Assert.assertEquals("4th engine in list by train", e2, engineList.get(3));
        Assert.assertEquals("5th engine in list by train", e3, engineList.get(4));
        Assert.assertEquals("6th engine in list by train", e1, engineList.get(5));
    }

    @Test
    public void testListEnginesBySpecifiedTrain() {
        resetEngineManager();

        EngineManager manager = InstanceManager.getDefault(EngineManager.class);

        Route r = new Route("id", "Test");
        r.addLocation(l1);
        r.addLocation(l2);
        r.addLocation(l3);

        Train t1 = new Train("id1", "F");
        t1.setRoute(r);
        Train t3 = new Train("id3", "E");
        t3.setRoute(r);

        //setup the engines
        e1.setTrain(t1);
        e2.setTrain(t3);
        e3.setTrain(t3);
        e4.setTrain(new Train("id4", "B"));
        e5.setTrain(t3);
        e6.setTrain(new Train("id6", "A"));

        // now get engines by specific train
        List<Engine> engineList = manager.getByTrainBlockingList(t1);
        Assert.assertEquals("Number of Engines in t1", 1, engineList.size());
        Assert.assertEquals("1st engine in list by t1", e1, engineList.get(0));
        engineList = manager.getByTrainBlockingList(t3);
        Assert.assertEquals("Number of Engines in t3", 3, engineList.size());
        Assert.assertEquals("1st engine in list by t3", e5, engineList.get(0));
        Assert.assertEquals("2nd engine in list by t3", e2, engineList.get(1));
        Assert.assertEquals("3rd engine in list by t3", e3, engineList.get(2));

    }

    @Test
    public void testListAvaialbleEngines() {
        resetEngineManager();

        EngineManager manager = InstanceManager.getDefault(EngineManager.class);

        Route r = new Route("id", "Test");
        r.addLocation(l1);
        r.addLocation(l2);
        r.addLocation(l3);

        Train t1 = new Train("id1", "F");
        t1.setRoute(r);
        Train t3 = new Train("id3", "E");
        t3.setRoute(r);

        //setup the engines
        e1.setTrain(t1);
        e2.setTrain(t3);
        e3.setTrain(t3);
        e4.setTrain(new Train("id4", "B"));
        e5.setTrain(t3);
        e6.setTrain(new Train("id6", "A"));

        // now get engines by specific train
        // how many engines available?
        List<Engine> engineList = manager.getAvailableTrainList(t1);
        Assert.assertEquals("Number of Engines available for t1", 1, engineList.size());
        Assert.assertEquals("1st engine in list available for t1", e1, engineList.get(0));

        engineList = manager.getAvailableTrainList(t3);
        Assert.assertEquals("Number of Engines available for t3", 3, engineList.size());
        Assert.assertEquals("1st engine in list available for t3", e5, engineList.get(0));
        Assert.assertEquals("2nd engine in list available for t3", e2, engineList.get(1));
        Assert.assertEquals("3rd engine in list available for t3", e3, engineList.get(2));
    }

    @Test
    public void testAvailableAfterReleaseFromTrain() {
        resetEngineManager();

        EngineManager manager = InstanceManager.getDefault(EngineManager.class);

        Route r = new Route("id", "Test");
        r.addLocation(l1);
        r.addLocation(l2);
        r.addLocation(l3);

        Train t1 = new Train("id1", "F");
        t1.setRoute(r);
        Train t3 = new Train("id3", "E");
        t3.setRoute(r);

        //setup the engines
        e1.setTrain(t1);
        e2.setTrain(t3);
        e3.setTrain(t3);
        e4.setTrain(new Train("id4", "B"));
        e5.setTrain(t3);
        e6.setTrain(new Train("id6", "A"));

        // release engines from trains
        e2.setTrain(null);
        e4.setTrain(null);	// e4 is located in the middle of the route, therefore not available
        e6.setTrain(null);	// e6 is located at the end of the route, therefore not available

        // there should be more engines now
        List<Engine> engineList = manager.getAvailableTrainList(t1);
        Assert.assertEquals("Number of Engines available t1 after release", 4, engineList.size());
        // should be sorted by moves
        Assert.assertEquals("1st engine in list available for t1", e1, engineList.get(0));
        Assert.assertEquals("2nd engine in list available for t1", e4, engineList.get(1));

        engineList = manager.getAvailableTrainList(t3);
        Assert.assertEquals("Number of Engines available for t3 after release", 5, engineList.size());
        Assert.assertEquals("1st engine in list available for t3", e5, engineList.get(0));

    }

    @Test
    public void testListEnginesByNumber() {
        resetEngineManager();

        EngineManager manager = InstanceManager.getDefault(EngineManager.class);

        // now get engines by road number
        List<Engine> engineList = manager.getByNumberList();
        Assert.assertEquals("Number of Engines by number", 6, engineList.size());
        Assert.assertEquals("1st engine in list by number", e6, engineList.get(0));
        Assert.assertEquals("2nd engine in list by number", e1, engineList.get(1));
        Assert.assertEquals("3rd engine in list by number", e5, engineList.get(2));
        Assert.assertEquals("4th engine in list by number", e2, engineList.get(3));
        Assert.assertEquals("5th engine in list by number", e3, engineList.get(4));
        Assert.assertEquals("6th engine in list by number", e4, engineList.get(5));
    }

    @Test
    public void testFindEnginesByRoadNameAndNumber() {
        resetEngineManager();

        EngineManager manager = InstanceManager.getDefault(EngineManager.class);

        // find engine by road and number
        Assert.assertEquals("find e1 by road and number", e1, manager.getByRoadAndNumber("CP", "1"));
        Assert.assertEquals("find e2 by road and number", e2, manager.getByRoadAndNumber("ACL", "3"));
        Assert.assertEquals("find e3 by road and number", e3, manager.getByRoadAndNumber("CP", "3"));
        Assert.assertEquals("find e4 by road and number", e4, manager.getByRoadAndNumber("CP", "3-1"));
        Assert.assertEquals("find e5 by road and number", e5, manager.getByRoadAndNumber("PC", "2"));
        Assert.assertEquals("find e6 by road and number", e6, manager.getByRoadAndNumber("AA", "1"));
    }

    @Test
    public void testListEnginesByRfid() {
        resetEngineManager();

        EngineManager manager = InstanceManager.getDefault(EngineManager.class);

        // make sure the ID tags exist before we
        // try to add it to an engine.
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("SQ1");
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("1Ab");
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("Ase");
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("asd");
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("93F");
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("B12");

        //setup the engines
        e1.setRfid("SQ1");
        e2.setRfid("1Ab");
        e3.setRfid("Ase");
        e4.setRfid("asd");
        e5.setRfid("93F");
        e6.setRfid("B12");

        // now get engines by RFID
        List<Engine> engineList = manager.getByRfidList();
        Assert.assertEquals("Number of Engines by rfid", 6, engineList.size());
        Assert.assertEquals("1st engine in list by rfid", e2, engineList.get(0));
        Assert.assertEquals("2nd engine in list by rfid", e5, engineList.get(1));
        Assert.assertEquals("3rd engine in list by rfid", e4, engineList.get(2));
        Assert.assertEquals("4th engine in list by rfid", e3, engineList.get(3));
        Assert.assertEquals("5th engine in list by rfid", e6, engineList.get(4));
        Assert.assertEquals("6th engine in list by rfid", e1, engineList.get(5));
    }

    @Test
    public void testFindEnginesByRfid() {
        resetEngineManager();

        EngineManager manager = InstanceManager.getDefault(EngineManager.class);

        // make sure the ID tags exist before we
        // try to add it to an engine.
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("SQ1");
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("1Ab");
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("Ase");
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("asd");
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("93F");
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("B12");

        //setup the engines
        e1.setRfid("SQ1");
        e2.setRfid("1Ab");
        e3.setRfid("Ase");
        e4.setRfid("asd");
        e5.setRfid("93F");
        e6.setRfid("B12");

        // find engine by RFID
        Assert.assertEquals("find e1 by rfid", e1, manager.getByRfid("SQ1"));
        Assert.assertEquals("find e2 by rfid", e2, manager.getByRfid("1Ab"));
        Assert.assertEquals("find e3 by rfid", e3, manager.getByRfid("Ase"));
        Assert.assertEquals("find e4 by rfid", e4, manager.getByRfid("asd"));
        Assert.assertEquals("find e5 by rfid", e5, manager.getByRfid("93F"));
        Assert.assertEquals("find e6 by rfid", e6, manager.getByRfid("B12"));

    }

    @Test
    public void testListEnginesByType() {
        resetEngineManager();

        EngineManager manager = InstanceManager.getDefault(EngineManager.class);
        // now get engines by model
        List<Engine> engineList = manager.getByModelList();
        Assert.assertEquals("Number of Engines by type", 6, engineList.size());
        Assert.assertEquals("1st engine in list by type", e3, engineList.get(0));
        Assert.assertEquals("2nd engine in list by type", e4, engineList.get(1));
        Assert.assertEquals("3rd engine in list by type", e5, engineList.get(2));
        Assert.assertEquals("4th engine in list by type", e2, engineList.get(3));
        Assert.assertEquals("5th engine in list by type", e6, engineList.get(4));
        Assert.assertEquals("6th engine in list by type", e1, engineList.get(5));
    }

    @Test
    public void testListEnginesByLastMovedDate() {
        resetEngineManager();

        EngineManager manager = InstanceManager.getDefault(EngineManager.class);

        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.util.Date start = cal.getTime(); // save to rest time, to avoid
        // test probelms if run near
        // midnight.
        java.util.Date time = cal.getTime();
        e1.setLastDate(time); // right now

        cal.setTime(start);
        cal.add(java.util.Calendar.HOUR_OF_DAY, -1);
        time = cal.getTime();
        e2.setLastDate(time); // one hour ago

        cal.setTime(start);
        cal.add(java.util.Calendar.HOUR_OF_DAY, 1);
        time = cal.getTime();
        e3.setLastDate(time); // one hour from now

        cal.setTime(start);
        cal.set(java.util.Calendar.DAY_OF_MONTH, -1);
        time = cal.getTime();
        e4.setLastDate(time); // one day ago.

        cal.setTime(start);
        cal.add(java.util.Calendar.DAY_OF_MONTH, 1);
        time = cal.getTime();
        e5.setLastDate(time); // one day in the future now.

        cal.setTime(start);
        cal.add(java.util.Calendar.YEAR, -1);
        time = cal.getTime();
        e6.setLastDate(time); // one year ago.

        // now get engines by last move date.
        List<Engine> engineList = manager.getByLastDateList();
        Assert.assertEquals("Number of Engines by last move date", 6, engineList.size());
        Assert.assertEquals("1st engine in list by move date", e6, engineList.get(0));
        Assert.assertEquals("2nd engine in list by move date", e4, engineList.get(1));
        Assert.assertEquals("3rd engine in list by move date", e2, engineList.get(2));
        Assert.assertEquals("4th engine in list by move date", e1, engineList.get(3));
        Assert.assertEquals("5th engine in list by move date", e3, engineList.get(4));
        Assert.assertEquals("6th engine in list by move date", e5, engineList.get(5));
    }

    @Test
    public void testSortListedEnginesByLastMovedDate() {
        resetEngineManager();

        EngineManager manager = InstanceManager.getDefault(EngineManager.class);

        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.util.Date start = cal.getTime(); // save to rest time, to avoid
        // test probelms if run near
        // midnight.
        java.util.Date time = cal.getTime();
        e1.setLastDate(time); // right now
        cal.setTime(start);
        cal.add(java.util.Calendar.HOUR_OF_DAY, -1);
        time = cal.getTime();
        e2.setLastDate(time); // one hour ago

        cal.setTime(start);
        cal.add(java.util.Calendar.HOUR_OF_DAY, 1);
        time = cal.getTime();
        e3.setLastDate(time); // one hour from now

        cal.setTime(start);
        cal.set(java.util.Calendar.DAY_OF_MONTH, -1);
        time = cal.getTime();
        e4.setLastDate(time); // one day ago.

        cal.setTime(start);
        cal.add(java.util.Calendar.DAY_OF_MONTH, 1);
        time = cal.getTime();
        e5.setLastDate(time); // one day in the future now.

        cal.setTime(start);
        cal.add(java.util.Calendar.YEAR, -1);

        time = cal.getTime();
        e6.setLastDate(time); // one year ago.

        // now get engines by last move date.
        List<Engine> engineList = manager.getByLastDateList(manager.getByIdList());
        Assert.assertEquals("Number of Engines by last move date", 6, engineList.size());
        Assert.assertEquals("1st engine in list by move date", e6, engineList.get(0));
        Assert.assertEquals("2nd engine in list by move date", e4, engineList.get(1));
        Assert.assertEquals("3rd engine in list by move date", e2, engineList.get(2));
        Assert.assertEquals("4th engine in list by move date", e1, engineList.get(3));
        Assert.assertEquals("5th engine in list by move date", e3, engineList.get(4));
        Assert.assertEquals("6th engine in list by move date", e5, engineList.get(5));
    }

    @Test
    public void testListEnginesAtLocation() {
        resetEngineManager();

        EngineManager manager = InstanceManager.getDefault(EngineManager.class);
        List<Engine> engineList = manager.getList(l1);
        Assert.assertEquals("Number of Engines at location", 2, engineList.size());
        Assert.assertTrue("e1 in engine list at location", engineList.contains(e1));
        Assert.assertTrue("e2 in engine list at location", engineList.contains(e2));
        Assert.assertFalse("e3 not in engine list at location", engineList.contains(e3));
    }

    @Test
    public void testListEnginesOnTrack() {
        resetEngineManager();

        EngineManager manager = InstanceManager.getDefault(EngineManager.class);
        Track l1t1 = l1.getTrackByName("A", Track.SPUR);
        List<Engine> engineList = manager.getList(l1t1);
        Assert.assertEquals("Number of Engines on track", 1, engineList.size());
        Assert.assertTrue("e1 in engine list on track", engineList.contains(e1));
        Assert.assertFalse("e2 not in engine list on track", engineList.contains(e2));
        Assert.assertFalse("e3 not in engine list on track", engineList.contains(e3));
    }

    private void resetEngineManager() {
        InstanceManager.getDefault(EngineManager.class).dispose();

        EngineManager manager = InstanceManager.getDefault(EngineManager.class);

        e1 = manager.newRS("CP", "1");
        e2 = manager.newRS("ACL", "3");
        e3 = manager.newRS("CP", "3");
        e4 = manager.newRS("CP", "3-1");
        e5 = manager.newRS("PC", "2");
        e6 = manager.newRS("AA", "1");

        e1.setModel("GP356");
        e2.setModel("GP354");
        e3.setModel("GP351");
        e4.setModel("GP352");
        e5.setModel("GP353");
        e6.setModel("GP355");

        e1.setTypeName("Diesel");
        e2.setTypeName("Diesel");
        e3.setTypeName("Diesel");
        e4.setTypeName("Diesel");
        e5.setTypeName("Diesel");
        e6.setTypeName("Diesel");

        e1.setLength("13");
        e2.setLength("9");
        e3.setLength("12");
        e4.setLength("10");
        e5.setLength("11");
        e6.setLength("14");

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

        l1.addTypeName("Diesel");
        l2.addTypeName("Diesel");
        l3.addTypeName("Diesel");
        l1t1.addTypeName("Diesel");
        l1t2.addTypeName("Diesel");
        l2t1.addTypeName("Diesel");
        l2t2.addTypeName("Diesel");
        l3t1.addTypeName("Diesel");
        l3t2.addTypeName("Diesel");

        EngineTypes et = InstanceManager.getDefault(EngineTypes.class);
        et.addName("Diesel");

        // place engines on tracks
        e1.setLocation(l1, l1t1);
        e2.setLocation(l1, l1t2);
        e3.setLocation(l2, l2t1);
        e4.setLocation(l2, l2t2);
        e5.setLocation(l3, l3t1);
        e6.setLocation(l3, l3t2);

        // set engine destinations
        e1.setDestination(l3, l3t1);
        e2.setDestination(l3, l3t2);
        e3.setDestination(l2, l2t2);
        e4.setDestination(l2, l2t1);
        e5.setDestination(l1, l1t1);
        e6.setDestination(l1, l1t2);

        e1.setMoves(2);
        e2.setMoves(44);
        e3.setMoves(99999);
        e4.setMoves(33);
        e5.setMoves(4);
        e6.setMoves(9999);

    }
}
