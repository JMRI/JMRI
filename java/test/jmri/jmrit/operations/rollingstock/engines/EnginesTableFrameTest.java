package jmri.jmrit.operations.rollingstock.engines;

import java.awt.GraphicsEnvironment;
import java.util.List;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.CarOwners;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the Operations EnginesTableFrame class
 *
 * @author	Dan Boudreau Copyright (C) 2010
 *
 */
public class EnginesTableFrameTest extends OperationsTestCase {

    @Test
    public void testenginesTableFrame() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // enable rfid field
        Setup.setRfidEnabled(true);

        EnginesTableFrame etf = new EnginesTableFrame();
        Assert.assertEquals("number of Engines 1", "5", etf.numEngines.getText());

        EngineManager eManager = InstanceManager.getDefault(EngineManager.class);
        // 5 Engines to check
        Engine e1 = eManager.getByRoadAndNumber("NH", "1");
        Engine e2 = eManager.getByRoadAndNumber("UP", "2");
        Engine e3 = eManager.getByRoadAndNumber("AA", "3");
        Engine e4 = eManager.getByRoadAndNumber("SP", "2");
        Engine e5 = eManager.getByRoadAndNumber("NH", "5");

        // default is sort by number
        List<Engine> Engines = etf.enginesModel.getSelectedEngineList();
        Assert.assertEquals("1st Engine in sort by number list", e1.getId(), Engines.get(0).getId());
        Assert.assertEquals("2nd Engine in sort by number list", e4.getId(), Engines.get(1).getId());
        Assert.assertEquals("3rd Engine in sort by number list", e2.getId(), Engines.get(2).getId());
        Assert.assertEquals("4th Engine in sort by number list", e3.getId(), Engines.get(3).getId());
        Assert.assertEquals("5th Engine in sort by number list", e5.getId(), Engines.get(4).getId());

        // now sort by built date
        JemmyUtil.enterClickAndLeave(etf.sortByBuilt);
        Engines = etf.enginesModel.getSelectedEngineList();
        Assert.assertEquals("1st Engine in sort by built list", e5, Engines.get(0));
        Assert.assertEquals("2nd Engine in sort by built list", e4, Engines.get(1));
        Assert.assertEquals("3rd Engine in sort by built list", e2, Engines.get(2));
        Assert.assertEquals("4th Engine in sort by built list", e3, Engines.get(3));
        Assert.assertEquals("5th Engine in sort by built list", e1, Engines.get(4));

        JemmyUtil.enterClickAndLeave(etf.sortByDestination);
        Engines = etf.enginesModel.getSelectedEngineList();
        Assert.assertEquals("1st Engine in sort by destination list", e2, Engines.get(0));
        Assert.assertEquals("2nd Engine in sort by destination list", e4, Engines.get(1));
        Assert.assertEquals("3rd Engine in sort by destination list", e1, Engines.get(2));
        Assert.assertEquals("4th Engine in sort by destination list", e3, Engines.get(3));
        Assert.assertEquals("5th Engine in sort by destination list", e5, Engines.get(4));

        // now sort by location
        JemmyUtil.enterClickAndLeave(etf.sortByLocation);
        Engines = etf.enginesModel.getSelectedEngineList();
        Assert.assertEquals("1st Engine in sort by location list", e2, Engines.get(0));
        Assert.assertEquals("2nd Engine in sort by location list", e3, Engines.get(1));
        Assert.assertEquals("3rd Engine in sort by location list", e5, Engines.get(2));
        Assert.assertEquals("4th Engine in sort by location list", e4, Engines.get(3));
        Assert.assertEquals("5th Engine in sort by location list", e1, Engines.get(4));

        // now sort by moves
        JemmyUtil.enterClickAndLeave(etf.sortByMoves);
        Engines = etf.enginesModel.getSelectedEngineList();
        Assert.assertEquals("1st Engine in sort by move list", e5, Engines.get(0));
        Assert.assertEquals("2nd Engine in sort by move list", e4, Engines.get(1));
        Assert.assertEquals("3rd Engine in sort by move list", e3, Engines.get(2));
        Assert.assertEquals("4th Engine in sort by move list", e2, Engines.get(3));
        Assert.assertEquals("5th Engine in sort by move list", e1, Engines.get(4));

        // test sort by number again
        JemmyUtil.enterClickAndLeave(etf.sortByNumber);
        Engines = etf.enginesModel.getSelectedEngineList();
        Assert.assertEquals("1st Engine in sort by number list 2", e1, Engines.get(0));
        Assert.assertEquals("2nd Engine in sort by number list 2", e4, Engines.get(1));
        Assert.assertEquals("3rd Engine in sort by number list 2", e2, Engines.get(2));
        Assert.assertEquals("4th Engine in sort by number list 2", e3, Engines.get(3));
        Assert.assertEquals("5th Engine in sort by number list 2", e5, Engines.get(4));

        // test sort by owner
        JemmyUtil.enterClickAndLeave(etf.sortByOwner);
        Engines = etf.enginesModel.getSelectedEngineList();
        Assert.assertEquals("1st Engine in sort by owner list", e4, Engines.get(0));
        Assert.assertEquals("2nd Engine in sort by owner list", e3, Engines.get(1));
        Assert.assertEquals("3rd Engine in sort by owner list", e2, Engines.get(2));
        Assert.assertEquals("4th Engine in sort by owner list", e5, Engines.get(3));
        Assert.assertEquals("5th Engine in sort by owner list", e1, Engines.get(4));

        // test sort by rfid
        JemmyUtil.enterClickAndLeave(etf.sortByRfid);
        Engines = etf.enginesModel.getSelectedEngineList();
        Assert.assertEquals("1st Engine in sort by rfid list", e5, Engines.get(0));
        Assert.assertEquals("2nd Engine in sort by rfid list", e2, Engines.get(1));
        Assert.assertEquals("3rd Engine in sort by rfid list", e1, Engines.get(2));
        Assert.assertEquals("4th Engine in sort by rfid list", e4, Engines.get(3));
        Assert.assertEquals("5th Engine in sort by rfid list", e3, Engines.get(4));

        // test sort by road
        JemmyUtil.enterClickAndLeave(etf.sortByRoad);
        Engines = etf.enginesModel.getSelectedEngineList();
        Assert.assertEquals("1st Engine in sort by road list", e3, Engines.get(0));
        Assert.assertEquals("2nd Engine in sort by road list", e1, Engines.get(1));
        Assert.assertEquals("3rd Engine in sort by road list", e5, Engines.get(2));
        Assert.assertEquals("4th Engine in sort by road list", e4, Engines.get(3));
        Assert.assertEquals("5th Engine in sort by road list", e2, Engines.get(4));

        JemmyUtil.enterClickAndLeave(etf.sortByTrain);
        //TODO add trains

        JemmyUtil.enterClickAndLeave(etf.sortByConsist);
        //TODO add consists

        // test sort by model
        JemmyUtil.enterClickAndLeave(etf.sortByModel);
        Engines = etf.enginesModel.getSelectedEngineList();
        Assert.assertEquals("1st Engine in sort by model list", e2, Engines.get(0));
        Assert.assertEquals("2nd Engine in sort by model list", e4, Engines.get(1));
        Assert.assertEquals("3rd Engine in sort by model list", e1, Engines.get(2));
        Assert.assertEquals("4th Engine in sort by model list", e5, Engines.get(3));
        Assert.assertEquals("5th Engine in sort by model list", e3, Engines.get(4));

        // test find text field
        etf.findEngineTextBox.setText("2");
        
        Thread find = new Thread(new Runnable() {
            @Override
            public void run() {
                JemmyUtil.enterClickAndLeave(etf.findButton);
            }
        });
        find.setName("Find Engine"); // NOI18N
        find.start();
        
        jmri.util.JUnitUtil.waitFor(() -> {
            return find.getState().equals(Thread.State.TERMINATED);
        }, "wait to complete");   
        
        // table is sorted by model, Engines with number 2 are in the first and 2nd rows
        Assert.assertEquals("find Engine by number 1st", 0, etf.enginesTable.getSelectedRow());
        JemmyUtil.enterClickAndLeave(etf.findButton);
        Assert.assertEquals("find Engine by number 2nd", 1, etf.enginesTable.getSelectedRow());

        // create the EngineEditFrame
        JemmyUtil.enterClickAndLeave(etf.addButton);

        JUnitUtil.dispose(etf);
    }

    // Ensure minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        super.setUp();

        loadEngines();
    }

    private void loadEngines() {

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
        Location westford = lManager.newLocation("Westford");
        Track westfordYard = westford.addTrack("Yard", Track.YARD);
        westfordYard.setLength(300);
        Track westfordSiding = westford.addTrack("Siding", Track.SPUR);
        westfordSiding.setLength(300);
        Track westfordAble = westford.addTrack("Able", Track.SPUR);
        westfordAble.setLength(300);
        Location boxford = lManager.newLocation("Boxford");
        Track boxfordYard = boxford.addTrack("Yard", Track.YARD);
        boxfordYard.setLength(300);
        Track boxfordJacobson = boxford.addTrack("Jacobson", Track.SPUR);
        boxfordJacobson.setLength(300);
        Track boxfordHood = boxford.addTrack("Hood", Track.SPUR);
        boxfordHood.setLength(300);

        EngineManager eManager = InstanceManager.getDefault(EngineManager.class);
        // add 5 Engines to table
        Engine e1 = eManager.newRS("NH", "1");
        e1.setModel("RS1");
        e1.setBuilt("2009");
        e1.setMoves(55);
        e1.setOwner("Owner2");
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("RFID 3");
        e1.setRfid("RFID 3");
        e1.setWeightTons("Tons of Weight");
        e1.setComment("Test Engine NH 1 Comment");
        Assert.assertEquals("e1 location", Track.OKAY, e1.setLocation(westford, westfordYard));
        Assert.assertEquals("e1 destination", Track.OKAY, e1.setDestination(boxford, boxfordJacobson));

        Engine e2 = eManager.newRS("UP", "2");
        e2.setModel("FT");
        e2.setBuilt("2004");
        e2.setMoves(50);
        e2.setOwner("AT");
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("RFID 2");
        e2.setRfid("RFID 2");

        Engine e3 = eManager.newRS("AA", "3");
        e3.setModel("SW8");
        e3.setBuilt("2006");
        e3.setMoves(40);
        e3.setOwner("AB");
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("RFID 5");
        e3.setRfid("RFID 5");
        Assert.assertEquals("e3 location", Track.OKAY, e3.setLocation(boxford, boxfordHood));
        Assert.assertEquals("e3 destination", Track.OKAY, e3.setDestination(boxford, boxfordYard));

        Engine e4 = eManager.newRS("SP", "2");
        e4.setModel("GP35");
        e4.setBuilt("1990");
        e4.setMoves(30);
        e4.setOwner("AAA");
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("RFID 4");
        e4.setRfid("RFID 4");
        Assert.assertEquals("e4 location", Track.OKAY, e4.setLocation(westford, westfordSiding));
        Assert.assertEquals("e4 destination", Track.OKAY, e4.setDestination(boxford, boxfordHood));

        Engine e5 = eManager.newRS("NH", "5");
        e5.setModel("SW1200");
        e5.setBuilt("1956");
        e5.setMoves(25);
        e5.setOwner("DAB");
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("RFID 1");
        e5.setRfid("RFID 1");
        Assert.assertEquals("e5 location", Track.OKAY, e5.setLocation(westford, westfordAble));
        Assert.assertEquals("e5 destination", Track.OKAY, e5.setDestination(westford, westfordAble));
    }
}
