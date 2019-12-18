package jmri.jmrit.operations.rollingstock.cars;

import java.io.IOException;
import java.util.List;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import org.jdom2.JDOMException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the Operations RollingStock Cars XML class Last manually
 * cross-checked on 20090131
 *
 * @author	Bob Coleman Copyright (C) 2008, 2009
 */
public class XmlTest extends OperationsTestCase {

    /**
     * Test Xml create and read support. Originally written as two separate
     * tests, now combined into one as of 8/29/2013.
     *
     * @throws JDOMException exception
     * @throws IOException exception
     */
    @Test
    public void testXMLCreate() throws JDOMException, IOException {

        // confirm that file name has been modified for testing
        Assert.assertEquals("OperationsJUnitTestCarRoster.xml", InstanceManager.getDefault(CarManagerXml.class).getOperationsFileName());

        // confirm proper defaults
        Assert.assertEquals("Default car empty", "E", InstanceManager.getDefault(CarLoads.class).getDefaultEmptyName());
        Assert.assertEquals("Default car load", "L", InstanceManager.getDefault(CarLoads.class).getDefaultLoadName());

        CarManager manager = InstanceManager.getDefault(CarManager.class);
        List<Car> tempcarList = manager.getByIdList();

        Assert.assertEquals("Starting Number of Cars", 0, tempcarList.size());
        Car c1 = manager.newRS("CP", "Test Number 1");
        Car c2 = manager.newRS("ACL", "Test Number 2");
        Car c3 = manager.newRS("CP", "Test Number 3");

        // modify car attributes
        c1.setBuilt("5619");
        c1.setCaboose(false);
        c1.setColor("black");
        c1.setComment("no comment");
        c1.setLength("04");
        c1.setLoadName("FULL");
        c1.setMoves(1);
        c1.setNumber("X Test Number c1");
        c1.setOutOfService(false);
        // make sure the ID tags exist before we
        // try to add it to a car.
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("norfidc1");
        c1.setRfid("norfidc1");
        c1.setRoadName("OLDRoad");
        c1.setTypeName("noCaboose");
        c1.setWait(6);
        c1.setWeight("54");
        c1.setWeightTons("001");

        c2.setBuilt("1234");
        c2.setFred(true);
        c2.setColor("red");
        c2.setComment("c2 comment");
        c2.setLength("77");
        c2.setLoadName("c2 Load");
        c2.setMoves(10000);
        c2.setNumber("X Test Number c2");
        c2.setOutOfService(true);
        // make sure the ID tags exist before we
        // try to add it to a car.
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("rfidc2");
        c2.setRfid("rfidc2");
        c2.setRoadName("c2 Road");
        c2.setTypeName("c2 Boxcar");
        c2.setWait(61);
        c2.setWeight("33");
        c2.setWeightTons("798");

        c3.setBuilt("234");
        c3.setCaboose(true);
        c3.setColor("green");
        c3.setComment("c3 comment");
        c3.setLength("453");
        c3.setLoadName("c3 Load");
        c3.setMoves(243);
        c3.setNumber("X Test Number c3");
        c3.setOutOfService(false);
        // make sure the ID tags exist before we
        // try to add it to a car.
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("rfidc3");
        c3.setRfid("rfidc3");
        c3.setRoadName("c3 Road");
        c3.setTypeName("c3 Boxcar");
        c3.setWait(0);
        c3.setWeight("345");
        c3.setWeightTons("1798");

        tempcarList = manager.getByIdList();
        Assert.assertEquals("New Number of Cars", 3, tempcarList.size());

        InstanceManager.getDefault(CarManagerXml.class).writeOperationsFile();

        // Add some more cars and write file again
        // so we can test the backup facility
        Car c4 = manager.newRS("PC", "Test Number 4");
        Car c5 = manager.newRS("BM", "Test Number 5");
        Car c6 = manager.newRS("SP", "Test Number 6");

        Assert.assertNotNull("car c4 exists", c4);
        Assert.assertNotNull("car c5 exists", c5);
        Assert.assertNotNull("car c6 exists", c6);

        // modify car attributes
        c1.setBuilt("1956");
        c1.setCaboose(true);
        c1.setColor("white");
        c1.setComment("c1 comment");
        c1.setLength("40");
        c1.setLoadName("Empty");
        c1.setMoves(3);
        c1.setNumber("New Test Number c1");
        c1.setOutOfService(true);
        // make sure the ID tags exist before we
        // try to add it to a car.
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("rfidc1");
        c1.setRfid("rfidc1");
        c1.setRoadName("newRoad");
        c1.setTypeName("bigCaboose");
        c1.setWait(5);
        c1.setWeight("45");
        c1.setWeightTons("100");

        c5.setBuilt("2010");
        c5.setCaboose(false);
        c5.setColor("blue");
        c5.setComment("c5 comment");
        c5.setLength("44");
        c5.setLoadName("Full");
        c5.setMoves(5);
        c5.setNumber("New Test Number c5");
        c5.setOutOfService(true);
        // make sure the ID tags exist before we
        // try to add it to a car.
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("rfidc5");
        c5.setRfid("rfidc5");
        c5.setRoadName("c5Road");
        c5.setTypeName("smallCaboose");
        c5.setWait(55);
        c5.setWeight("66");
        c5.setWeightTons("77");

        tempcarList = manager.getByIdList();
        Assert.assertEquals("New Number of Cars", 6, tempcarList.size());

        InstanceManager.getDefault(CarManagerXml.class).writeOperationsFile();
//	}
//
//	/**
//	 * Test reading xml car file
//	 * @throws JDOMException
//	 * @throws IOException
//	 */
//	public void testXMLRead() throws JDOMException, IOException{
//		CarManager manager = InstanceManager.getDefault(CarManager.class);
        manager.dispose();
        manager = InstanceManager.getDefault(CarManager.class);
        tempcarList = manager.getByIdList();
        Assert.assertEquals("Starting Number of Cars", 0, tempcarList.size());

        InstanceManager.getDefault(CarManagerXml.class).readFile(InstanceManager.getDefault(CarManagerXml.class).getDefaultOperationsFilename());

        tempcarList = manager.getByIdList();
        Assert.assertEquals("Number of Cars", 6, tempcarList.size());

        // verify cars can be gotten with current roads and numbers
        c1 = manager.getByRoadAndNumber("newRoad", "New Test Number c1");
        c2 = manager.getByRoadAndNumber("c2 Road", "X Test Number c2");
        c3 = manager.getByRoadAndNumber("c3 Road", "X Test Number c3");
        c4 = manager.getByRoadAndNumber("PC", "Test Number 4");
        c5 = manager.getByRoadAndNumber("c5Road", "New Test Number c5");
        c6 = manager.getByRoadAndNumber("SP", "Test Number 6");

        Assert.assertNotNull("car c1 exists", c1);
        Assert.assertNotNull("car c2 exists", c2);
        Assert.assertNotNull("car c3 exists", c3);
        Assert.assertNotNull("car c4 exists", c4);
        Assert.assertNotNull("car c5 exists", c5);
        Assert.assertNotNull("car c6 exists", c6);

        Assert.assertEquals("car c1 built date", "1956", c1.getBuilt());
        Assert.assertEquals("car c1 caboose", true, c1.isCaboose());
        Assert.assertEquals("car c1 color", "white", c1.getColor());
        Assert.assertEquals("car c1 comment", "c1 comment", c1.getComment());
        Assert.assertEquals("car c1 length", "40", c1.getLength());
        Assert.assertEquals("car c1 load", "Empty", c1.getLoadName());
        Assert.assertEquals("car c1 moves", 3, c1.getMoves());
        Assert.assertEquals("car c1 number", "New Test Number c1", c1.getNumber());
        Assert.assertEquals("car c1 out of service", true, c1.isOutOfService());
        Assert.assertEquals("car c1 rfid", "rfidc1", c1.getRfid());
        Assert.assertEquals("car c1 road", "newRoad", c1.getRoadName());
        Assert.assertEquals("car c1 type", "bigCaboose", c1.getTypeName());
        Assert.assertEquals("car c1 wait", 5, c1.getWait());
        Assert.assertEquals("car c1 weight", "45", c1.getWeight());
        Assert.assertEquals("car c1 weight tons", "100", c1.getWeightTons());

        Assert.assertEquals("car c2 built date", "1234", c2.getBuilt());
        Assert.assertEquals("car c2 caboose", false, c2.isCaboose());
        Assert.assertEquals("car c2 fred", true, c2.hasFred());
        Assert.assertEquals("car c2 color", "red", c2.getColor());
        Assert.assertEquals("car c2 comment", "c2 comment", c2.getComment());
        Assert.assertEquals("car c2 length", "77", c2.getLength());
        Assert.assertEquals("car c2 load", "c2 Load", c2.getLoadName());
        Assert.assertEquals("car c2 moves", 10000, c2.getMoves());
        Assert.assertEquals("car c2 number", "X Test Number c2", c2.getNumber());
        Assert.assertEquals("car c2 out of service", true, c2.isOutOfService());
        Assert.assertEquals("car c2 rfid", "rfidc2", c2.getRfid());
        Assert.assertEquals("car c2 road", "c2 Road", c2.getRoadName());
        Assert.assertEquals("car c2 type", "c2 Boxcar", c2.getTypeName());
        Assert.assertEquals("car c2 wait", 61, c2.getWait());
        Assert.assertEquals("car c2 weight", "33", c2.getWeight());
        Assert.assertEquals("car c2 weight tons", "798", c2.getWeightTons());

        Assert.assertEquals("car c3 built date", "234", c3.getBuilt());
        Assert.assertEquals("car c3 caboose", true, c3.isCaboose());
        Assert.assertEquals("car c3 fred", false, c3.hasFred());
        Assert.assertEquals("car c3 color", "green", c3.getColor());
        Assert.assertEquals("car c3 comment", "c3 comment", c3.getComment());
        Assert.assertEquals("car c3 length", "453", c3.getLength());
        Assert.assertEquals("car c3 load", "c3 Load", c3.getLoadName());
        Assert.assertEquals("car c3 moves", 243, c3.getMoves());
        Assert.assertEquals("car c3 number", "X Test Number c3", c3.getNumber());
        Assert.assertEquals("car c3 out of service", false, c3.isOutOfService());
        Assert.assertEquals("car c3 rfid", "rfidc3", c3.getRfid());
        Assert.assertEquals("car c3 road", "c3 Road", c3.getRoadName());
        Assert.assertEquals("car c3 type", "c3 Boxcar", c3.getTypeName());
        Assert.assertEquals("car c3 wait", 0, c3.getWait());
        Assert.assertEquals("car c3 weight", "345", c3.getWeight());
        Assert.assertEquals("car c3 weight tons", "1798", c3.getWeightTons());

        // c4 and c6 use defaults for most of their attributes.
        Assert.assertEquals("car c4 built date", "", c4.getBuilt());
        Assert.assertEquals("car c4 caboose", false, c4.isCaboose());
        Assert.assertEquals("car c4 fred", false, c4.hasFred());
        Assert.assertEquals("car c4 color", "", c4.getColor());
        Assert.assertEquals("car c4 comment", "", c4.getComment());
        Assert.assertEquals("car c4 length", "0", c4.getLength());
        Assert.assertEquals("car c4 load", "E", c4.getLoadName());
        Assert.assertEquals("car c4 moves", 0, c4.getMoves());
        Assert.assertEquals("car c4 number", "Test Number 4", c4.getNumber());
        Assert.assertEquals("car c4 out of service", false, c4.isOutOfService());
        Assert.assertEquals("car c4 rfid", "", c4.getRfid());
        Assert.assertEquals("car c4 road", "PC", c4.getRoadName());
        Assert.assertEquals("car c4 type", "", c4.getTypeName());
        Assert.assertEquals("car c4 wait", 0, c4.getWait());
        Assert.assertEquals("car c4 weight", "0", c4.getWeight());
        Assert.assertEquals("car c4 weight tons", "0", c4.getWeightTons());

        Assert.assertEquals("car c5 built date", "2010", c5.getBuilt());
        Assert.assertEquals("car c5 caboose", false, c5.isCaboose());
        Assert.assertEquals("car c5 color", "blue", c5.getColor());
        Assert.assertEquals("car c5 comment", "c5 comment", c5.getComment());
        Assert.assertEquals("car c5 length", "44", c5.getLength());
        Assert.assertEquals("car c5 load", "Full", c5.getLoadName());
        Assert.assertEquals("car c5 moves", 5, c5.getMoves());
        Assert.assertEquals("car c5 number", "New Test Number c5", c5.getNumber());
        Assert.assertEquals("car c5 out of service", true, c5.isOutOfService());
        Assert.assertEquals("car c5 rfid", "rfidc5", c5.getRfid());
        Assert.assertEquals("car c5 road", "c5Road", c5.getRoadName());
        Assert.assertEquals("car c5 type", "smallCaboose", c5.getTypeName());
        Assert.assertEquals("car c5 wait", 55, c5.getWait());
        Assert.assertEquals("car c5 weight", "66", c5.getWeight());
        Assert.assertEquals("car c5 weight tons", "77", c5.getWeightTons());

        Assert.assertEquals("car c6 built date", "", c6.getBuilt());
        Assert.assertEquals("car c6 caboose", false, c6.isCaboose());
        Assert.assertEquals("car c6 fred", false, c6.hasFred());
        Assert.assertEquals("car c6 color", "", c6.getColor());
        Assert.assertEquals("car c6 comment", "", c6.getComment());
        Assert.assertEquals("car c6 length", "0", c6.getLength());
        Assert.assertEquals("car c6 load", "E", c6.getLoadName());
        Assert.assertEquals("car c6 moves", 0, c6.getMoves());
        Assert.assertEquals("car c6 number", "Test Number 6", c6.getNumber());
        Assert.assertEquals("car c6 out of service", false, c6.isOutOfService());
        Assert.assertEquals("car c6 rfid", "", c6.getRfid());
        Assert.assertEquals("car c6 road", "SP", c6.getRoadName());
        Assert.assertEquals("car c6 type", "", c6.getTypeName());
        Assert.assertEquals("car c6 wait", 0, c6.getWait());
        Assert.assertEquals("car c6 weight", "0", c6.getWeight());
        Assert.assertEquals("car c6 weight tons", "0", c6.getWeightTons());

        // Now test back up file
        manager.dispose();
        manager = InstanceManager.getDefault(CarManager.class);
        tempcarList = manager.getByIdList();
        Assert.assertEquals("Starting Number of Cars", 0, tempcarList.size());

        // change default file name to backup
        InstanceManager.getDefault(CarManagerXml.class).setOperationsFileName("OperationsJUnitTestCarRoster.xml.bak");

        InstanceManager.getDefault(CarManagerXml.class).readFile(InstanceManager.getDefault(CarManagerXml.class).getDefaultOperationsFilename());

        tempcarList = manager.getByIdList();
        Assert.assertEquals("Number of Cars", 3, tempcarList.size());

        // verify cars can be gotten with current roads and numbers
        c1 = manager.getByRoadAndNumber("OLDRoad", "X Test Number c1");
        c2 = manager.getByRoadAndNumber("c2 Road", "X Test Number c2");
        c3 = manager.getByRoadAndNumber("c3 Road", "X Test Number c3");
        c4 = manager.getByRoadAndNumber("PC", "Test Number 4");
        c5 = manager.getByRoadAndNumber("c5Road", "New Test Number c5");
        c6 = manager.getByRoadAndNumber("SP", "Test Number 6");

        Assert.assertNotNull("car c1 exists", c1);
        Assert.assertNotNull("car c2 exists", c2);
        Assert.assertNotNull("car c3 exists", c3);
        Assert.assertNull("car c4 does not exist", c4);
        Assert.assertNull("car c5 does not exist", c5);
        Assert.assertNull("car c6 does not exist", c6);

        Assert.assertEquals("car c1 built date", "5619", c1.getBuilt());
        Assert.assertEquals("car c1 caboose", false, c1.isCaboose());
        Assert.assertEquals("car c1 color", "black", c1.getColor());
        Assert.assertEquals("car c1 comment", "no comment", c1.getComment());
        Assert.assertEquals("car c1 length", "04", c1.getLength());
        Assert.assertEquals("car c1 load", "FULL", c1.getLoadName());
        Assert.assertEquals("car c1 moves", 1, c1.getMoves());
        Assert.assertEquals("car c1 number", "X Test Number c1", c1.getNumber());
        Assert.assertEquals("car c1 out of service", false, c1.isOutOfService());
        Assert.assertEquals("car c1 rfid", "norfidc1", c1.getRfid());
        Assert.assertEquals("car c1 road", "OLDRoad", c1.getRoadName());
        Assert.assertEquals("car c1 type", "noCaboose", c1.getTypeName());
        Assert.assertEquals("car c1 wait", 6, c1.getWait());
        Assert.assertEquals("car c1 weight", "54", c1.getWeight());
        Assert.assertEquals("car c1 weight tons", "001", c1.getWeightTons());

        Assert.assertEquals("car c2 built date", "1234", c2.getBuilt());
        Assert.assertEquals("car c2 caboose", false, c2.isCaboose());
        Assert.assertEquals("car c2 fred", true, c2.hasFred());
        Assert.assertEquals("car c2 color", "red", c2.getColor());
        Assert.assertEquals("car c2 comment", "c2 comment", c2.getComment());
        Assert.assertEquals("car c2 length", "77", c2.getLength());
        Assert.assertEquals("car c2 load", "c2 Load", c2.getLoadName());
        Assert.assertEquals("car c2 moves", 10000, c2.getMoves());
        Assert.assertEquals("car c2 number", "X Test Number c2", c2.getNumber());
        Assert.assertEquals("car c2 out of service", true, c2.isOutOfService());
        Assert.assertEquals("car c2 rfid", "rfidc2", c2.getRfid());
        Assert.assertEquals("car c2 road", "c2 Road", c2.getRoadName());
        Assert.assertEquals("car c2 type", "c2 Boxcar", c2.getTypeName());
        Assert.assertEquals("car c2 wait", 61, c2.getWait());
        Assert.assertEquals("car c2 weight", "33", c2.getWeight());
        Assert.assertEquals("car c2 weight tons", "798", c2.getWeightTons());

        Assert.assertEquals("car c3 built date", "234", c3.getBuilt());
        Assert.assertEquals("car c3 caboose", true, c3.isCaboose());
        Assert.assertEquals("car c3 fred", false, c3.hasFred());
        Assert.assertEquals("car c3 color", "green", c3.getColor());
        Assert.assertEquals("car c3 comment", "c3 comment", c3.getComment());
        Assert.assertEquals("car c3 length", "453", c3.getLength());
        Assert.assertEquals("car c3 load", "c3 Load", c3.getLoadName());
        Assert.assertEquals("car c3 moves", 243, c3.getMoves());
        Assert.assertEquals("car c3 number", "X Test Number c3", c3.getNumber());
        Assert.assertEquals("car c3 out of service", false, c3.isOutOfService());
        Assert.assertEquals("car c3 rfid", "rfidc3", c3.getRfid());
        Assert.assertEquals("car c3 road", "c3 Road", c3.getRoadName());
        Assert.assertEquals("car c3 type", "c3 Boxcar", c3.getTypeName());
        Assert.assertEquals("car c3 wait", 0, c3.getWait());
        Assert.assertEquals("car c3 weight", "345", c3.getWeight());
        Assert.assertEquals("car c3 weight tons", "1798", c3.getWeightTons());
    }

    // TODO: Add tests for location
    // TODO: Add tests for track location
    // TODO: Add tests for destination
    // TODO: Add tests for track destination
    // TODO: Add tests for train
    // TODO: Add tests for route location
    // TODO: Add tests for route track location
    // TODO: Add tests for route destination
    // TODO: Add tests for route track destination
    // TODO: Add test for import
    // TODO: Add test to create xml file
    // TODO: Add test to read xml file
}
