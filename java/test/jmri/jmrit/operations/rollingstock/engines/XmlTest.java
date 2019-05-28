package jmri.jmrit.operations.rollingstock.engines;

import java.io.IOException;
import java.util.List;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import org.jdom2.JDOMException;
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
public class XmlTest extends OperationsTestCase {

    // test Xml create support
    @Test
    public void testXMLCreate() throws JDOMException, IOException {

        // confirm that file name has been modified for testing
        Assert.assertEquals("OperationsJUnitTestEngineRoster.xml", InstanceManager.getDefault(EngineManagerXml.class).getOperationsFileName());

        EngineManager manager = InstanceManager.getDefault(EngineManager.class);
        List<Engine> tempengineList = manager.getByIdList();

        Assert.assertEquals("Starting Number of Engines", 0, tempengineList.size());
        Engine e1 = manager.newRS("CP", "Test Number 1");
        Engine e2 = manager.newRS("ACL", "Test Number 2");
        Engine e3 = manager.newRS("CP", "Test Number 3");

        // modify engine attributes
        e1.setBuilt("5619");
        e1.setColor("black");
        e1.setComment("no comment");
        e1.setModel("e1 X model");
        e1.setLength("04");
        e1.setHp("e1 hp");
        e1.setMoves(1);
        e1.setNumber("X Test Number e1");
        e1.setOutOfService(false);
        // make sure the ID tags exist before we
        // try to add it to a engine.
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("norfide1");
        e1.setRfid("norfide1");
        e1.setRoadName("OLDRoad");
        e1.setTypeName("e1 X type");
        e1.setWeight("54");
        e1.setWeightTons("001");

        e2.setBuilt("1234");
        e2.setColor("red");
        e2.setComment("e2 comment");
        e2.setModel("e2 model");
        e2.setLength("77");
        e2.setHp("e2 hp");
        e2.setMoves(10000);
        e2.setNumber("X Test Number e2");
        e2.setOutOfService(true);
        // make sure the ID tags exist before we
        // try to add it to a engine.
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("rfide2");
        e2.setRfid("rfide2");
        e2.setRoadName("e2 Road");
        e2.setTypeName("e2 type");
        e2.setWeight("33");
        e2.setWeightTons("798");

        e3.setBuilt("234");
        e3.setColor("green");
        e3.setComment("e3 comment");
        e3.setModel("e3 model");
        e3.setLength("453");
        e3.setHp("e3 hp");
        e3.setMoves(243);
        e3.setNumber("X Test Number e3");
        e3.setOutOfService(false);
        // make sure the ID tags exist before we
        // try to add it to a engine.
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("rfide3");
        e3.setRfid("rfide3");
        e3.setRoadName("e3 Road");
        e3.setTypeName("e3 type");
        e3.setWeight("345");
        e3.setWeightTons("1798");

        tempengineList = manager.getByIdList();
        Assert.assertEquals("New Number of Engines", 3, tempengineList.size());

        InstanceManager.getDefault(EngineManagerXml.class).writeOperationsFile();

        // Add some more engines and write file again
        // so we can test the backup facility
        Engine e4 = manager.newRS("PC", "Test Number 4");
        Engine e5 = manager.newRS("BM", "Test Number 5");
        Engine e6 = manager.newRS("SP", "Test Number 6");

        Assert.assertNotNull("engine e4 exists", e4);
        Assert.assertNotNull("engine e5 exists", e5);
        Assert.assertNotNull("engine e6 exists", e6);

        // modify engine attributes
        e1.setBuilt("1956");
        e1.setColor("white");
        e1.setComment("e1 comment");
        e1.setModel("e1 model");
        e1.setLength("40");
        e1.setHp("e1 hp");
        e1.setMoves(3);
        e1.setNumber("New Test Number e1");
        e1.setOutOfService(true);
        // make sure the ID tags exist before we
        // try to add it to a engine.
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("rfide1");
        e1.setRfid("rfide1");
        e1.setRoadName("newRoad");
        e1.setTypeName("e1 type");
        e1.setWeight("45");
        e1.setWeightTons("100");

        e5.setBuilt("2010");
        e5.setColor("blue");
        e5.setComment("e5 comment");
        e5.setModel("e5 model");
        e5.setLength("44");
        e5.setHp("e5 hp");
        e5.setMoves(5);
        e5.setNumber("New Test Number e5");
        e5.setOutOfService(true);
        // make sure the ID tags exist before we
        // try to add it to a engine.
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("rfide5");
        e5.setRfid("rfide5");
        e5.setRoadName("e5Road");
        e5.setTypeName("e5 type");
        e5.setWeight("66");
        e5.setWeightTons("77");

        tempengineList = manager.getByIdList();
        Assert.assertEquals("New Number of Engines", 6, tempengineList.size());

        InstanceManager.getDefault(EngineManagerXml.class).writeOperationsFile();

        // now reset everything using dispose
        manager.dispose();
        InstanceManager.getDefault(EngineModels.class).dispose();

        manager = InstanceManager.getDefault(EngineManager.class);
        tempengineList = manager.getByIdList();
        Assert.assertEquals("Starting Number of Engines", 0, tempengineList.size());

        // confirm that engine models has been reset by dispose
        Assert.assertEquals("e1 model type", null, InstanceManager.getDefault(EngineModels.class).getModelType("e1 model"));
        Assert.assertEquals("e1 model length", null, InstanceManager.getDefault(EngineModels.class).getModelLength("e1 model"));
        Assert.assertEquals("e1 model Weight Tons", null, InstanceManager.getDefault(EngineModels.class).getModelWeight("e1 model"));
        Assert.assertEquals("e1 model hp", null, InstanceManager.getDefault(EngineModels.class).getModelHorsepower("e1 model"));

        InstanceManager.getDefault(EngineManagerXml.class).readFile(InstanceManager.getDefault(EngineManagerXml.class).getDefaultOperationsFilename());

        tempengineList = manager.getByIdList();
        Assert.assertEquals("Number of Engines", 6, tempengineList.size());

        // confirm that engine models was loaded
        Assert.assertEquals("e1 model type", "e1 type", InstanceManager.getDefault(EngineModels.class).getModelType("e1 model"));
        Assert.assertEquals("e1 model length", "40", InstanceManager.getDefault(EngineModels.class).getModelLength("e1 model"));
        Assert.assertEquals("e1 model Weight Tons", "100", InstanceManager.getDefault(EngineModels.class).getModelWeight("e1 model"));
        Assert.assertEquals("e1 model hp", "e1 hp", InstanceManager.getDefault(EngineModels.class).getModelHorsepower("e1 model"));

        // verify engines can be gotten using current road and number
        e1 = manager.getByRoadAndNumber("newRoad", "New Test Number e1");
        e2 = manager.getByRoadAndNumber("e2 Road", "X Test Number e2");
        e3 = manager.getByRoadAndNumber("e3 Road", "X Test Number e3");
        e4 = manager.getByRoadAndNumber("PC", "Test Number 4");
        e5 = manager.getByRoadAndNumber("e5Road", "New Test Number e5");
        e6 = manager.getByRoadAndNumber("SP", "Test Number 6");

        Assert.assertNotNull("engine e1 exists", e1);
        Assert.assertNotNull("engine e2 exists", e2);
        Assert.assertNotNull("engine e3 exists", e3);
        Assert.assertNotNull("engine e4 exists", e4);
        Assert.assertNotNull("engine e5 exists", e5);
        Assert.assertNotNull("engine e6 exists", e6);

        Assert.assertEquals("engine e1 built date", "1956", e1.getBuilt());
        Assert.assertEquals("engine e1 color", "white", e1.getColor());
        Assert.assertEquals("engine e1 comment", "e1 comment", e1.getComment());
        Assert.assertEquals("engine e1 length", "40", e1.getLength());
        Assert.assertEquals("engine e1 moves", 3, e1.getMoves());
        Assert.assertEquals("engine e1 number", "New Test Number e1", e1.getNumber());
        Assert.assertEquals("engine e1 out of service", true, e1.isOutOfService());
        Assert.assertEquals("engine e1 rfid", "rfide1", e1.getRfid());
        Assert.assertEquals("engine e1 road", "newRoad", e1.getRoadName());
        Assert.assertEquals("engine e1 type", "e1 type", e1.getTypeName());
        Assert.assertEquals("engine e1 weight", "45", e1.getWeight());
        Assert.assertEquals("engine e1 weight tons", "100", e1.getWeightTons());
        Assert.assertEquals("engine e1 hp", "e1 hp", e1.getHp());
        Assert.assertEquals("engine e1 model", "e1 model", e1.getModel());

        Assert.assertEquals("engine e2 built date", "1234", e2.getBuilt());
        Assert.assertEquals("engine e2 color", "red", e2.getColor());
        Assert.assertEquals("engine e2 comment", "e2 comment", e2.getComment());
        Assert.assertEquals("engine e2 length", "77", e2.getLength());
        Assert.assertEquals("engine e2 moves", 10000, e2.getMoves());
        Assert.assertEquals("engine e2 number", "X Test Number e2", e2.getNumber());
        Assert.assertEquals("engine e2 out of service", true, e2.isOutOfService());
        Assert.assertEquals("engine e2 rfid", "rfide2", e2.getRfid());
        Assert.assertEquals("engine e2 road", "e2 Road", e2.getRoadName());
        Assert.assertEquals("engine e2 type", "e2 type", e2.getTypeName());
        Assert.assertEquals("engine e2 weight", "33", e2.getWeight());
        Assert.assertEquals("engine e2 weight tons", "798", e2.getWeightTons());
        Assert.assertEquals("engine e2 hp", "e2 hp", e2.getHp());
        Assert.assertEquals("engine e2 model", "e2 model", e2.getModel());

        Assert.assertEquals("engine e3 built date", "234", e3.getBuilt());
        Assert.assertEquals("engine e3 color", "green", e3.getColor());
        Assert.assertEquals("engine e3 comment", "e3 comment", e3.getComment());
        Assert.assertEquals("engine e3 length", "453", e3.getLength());
        Assert.assertEquals("engine e3 moves", 243, e3.getMoves());
        Assert.assertEquals("engine e3 number", "X Test Number e3", e3.getNumber());
        Assert.assertEquals("engine e3 out of service", false, e3.isOutOfService());
        Assert.assertEquals("engine e3 rfid", "rfide3", e3.getRfid());
        Assert.assertEquals("engine e3 road", "e3 Road", e3.getRoadName());
        Assert.assertEquals("engine e3 type", "e3 type", e3.getTypeName());
        Assert.assertEquals("engine e3 weight", "345", e3.getWeight());
        Assert.assertEquals("engine e3 weight tons", "1798", e3.getWeightTons());
        Assert.assertEquals("engine e3 hp", "e3 hp", e3.getHp());
        Assert.assertEquals("engine e3 model", "e3 model", e3.getModel());

        // e4 and e6 use defaults for most of their attributes.
        Assert.assertEquals("engine e4 built date", "", e4.getBuilt());
        Assert.assertEquals("engine e4 color", "", e4.getColor());
        Assert.assertEquals("engine e4 comment", "", e4.getComment());
        Assert.assertEquals("engine e4 length", "0", e4.getLength());
        Assert.assertEquals("engine e4 moves", 0, e4.getMoves());
        Assert.assertEquals("engine e4 number", "Test Number 4", e4.getNumber());
        Assert.assertEquals("engine e4 out of service", false, e4.isOutOfService());
        Assert.assertEquals("engine e4 rfid", "", e4.getRfid());
        Assert.assertEquals("engine e4 road", "PC", e4.getRoadName());
        Assert.assertEquals("engine e4 type", "", e4.getTypeName());
        Assert.assertEquals("engine e4 weight", "0", e4.getWeight());
        Assert.assertEquals("engine e4 weight tons", "", e4.getWeightTons());
        Assert.assertEquals("engine e4 hp", "", e4.getHp());
        Assert.assertEquals("engine e4 model", "", e4.getModel());

        Assert.assertEquals("engine e5 built date", "2010", e5.getBuilt());
        Assert.assertEquals("engine e5 color", "blue", e5.getColor());
        Assert.assertEquals("engine e5 comment", "e5 comment", e5.getComment());
        Assert.assertEquals("engine e5 length", "44", e5.getLength());
        Assert.assertEquals("engine e5 moves", 5, e5.getMoves());
        Assert.assertEquals("engine e5 number", "New Test Number e5", e5.getNumber());
        Assert.assertEquals("engine e5 out of service", true, e5.isOutOfService());
        Assert.assertEquals("engine e5 rfid", "rfide5", e5.getRfid());
        Assert.assertEquals("engine e5 road", "e5Road", e5.getRoadName());
        Assert.assertEquals("engine e5 type", "e5 type", e5.getTypeName());
        Assert.assertEquals("engine e5 weight", "66", e5.getWeight());
        Assert.assertEquals("engine e5 weight tons", "77", e5.getWeightTons());
        Assert.assertEquals("engine e5 hp", "e5 hp", e5.getHp());
        Assert.assertEquals("engine e5 model", "e5 model", e5.getModel());

        Assert.assertEquals("engine e6 built date", "", e6.getBuilt());
        Assert.assertEquals("engine e6 color", "", e6.getColor());
        Assert.assertEquals("engine e6 comment", "", e6.getComment());
        Assert.assertEquals("engine e6 length", "0", e6.getLength());
        Assert.assertEquals("engine e6 moves", 0, e6.getMoves());
        Assert.assertEquals("engine e6 number", "Test Number 6", e6.getNumber());
        Assert.assertEquals("engine e6 out of service", false, e6.isOutOfService());
        Assert.assertEquals("engine e6 rfid", "", e6.getRfid());
        Assert.assertEquals("engine e6 road", "SP", e6.getRoadName());
        Assert.assertEquals("engine e6 type", "", e6.getTypeName());
        Assert.assertEquals("engine e6 weight", "0", e6.getWeight());
        Assert.assertEquals("engine e6 weight tons", "", e6.getWeightTons());
        Assert.assertEquals("engine e6 hp", "", e6.getHp());
        Assert.assertEquals("engine e6 model", "", e6.getModel());

        // now test backup file
        manager.dispose();
        manager = InstanceManager.getDefault(EngineManager.class);
        tempengineList = manager.getByIdList();
        Assert.assertEquals("Starting Number of Engines", 0, tempengineList.size());

        // confirm that engine models has been reset by dispose
        Assert.assertEquals("e1 model type", null, InstanceManager.getDefault(EngineModels.class).getModelType("e1 X model"));
        Assert.assertEquals("e1 model length", null, InstanceManager.getDefault(EngineModels.class).getModelLength("e1 X model"));
        Assert.assertEquals("e1 model Weight Tons", null, InstanceManager.getDefault(EngineModels.class).getModelWeight("e1 X model"));
        Assert.assertEquals("e1 model hp", null, InstanceManager.getDefault(EngineModels.class).getModelHorsepower("e1 X model"));

        // change default file name to backup
        InstanceManager.getDefault(EngineManagerXml.class).setOperationsFileName("OperationsJUnitTestEngineRoster.xml.bak");

        InstanceManager.getDefault(EngineManagerXml.class).readFile(InstanceManager.getDefault(EngineManagerXml.class).getDefaultOperationsFilename());

        tempengineList = manager.getByIdList();
        Assert.assertEquals("Number of Engines", 3, tempengineList.size());

        // confirm that engine models was loaded
        Assert.assertEquals("e1 model type", "e1 X type", InstanceManager.getDefault(EngineModels.class).getModelType("e1 X model"));
        Assert.assertEquals("e1 model length", "04", InstanceManager.getDefault(EngineModels.class).getModelLength("e1 X model"));
        Assert.assertEquals("e1 model Weight Tons", "001", InstanceManager.getDefault(EngineModels.class).getModelWeight("e1 X model"));
        Assert.assertEquals("e1 model hp", "e1 hp", InstanceManager.getDefault(EngineModels.class).getModelHorsepower("e1 X model"));

        // verify engines can be gotten using current road and number
        e1 = manager.getByRoadAndNumber("OLDRoad", "X Test Number e1");
        e2 = manager.getByRoadAndNumber("e2 Road", "X Test Number e2");
        e3 = manager.getByRoadAndNumber("e3 Road", "X Test Number e3");
        e4 = manager.getByRoadAndNumber("PC", "Test Number 4");
        e5 = manager.getByRoadAndNumber("e5Road", "New Test Number e5");
        e6 = manager.getByRoadAndNumber("SP", "Test Number 6");

        Assert.assertNotNull("engine e1 exists", e1);
        Assert.assertNotNull("engine e2 exists", e2);
        Assert.assertNotNull("engine e3 exists", e3);
        Assert.assertNull("engine e4 does not exist", e4);
        Assert.assertNull("engine e5 does not exist", e5);
        Assert.assertNull("engine e6 does not exist", e6);

        Assert.assertEquals("engine e1 built date", "5619", e1.getBuilt());
        Assert.assertEquals("engine e1 color", "black", e1.getColor());
        Assert.assertEquals("engine e1 comment", "no comment", e1.getComment());
        Assert.assertEquals("engine e1 length", "04", e1.getLength());
        Assert.assertEquals("engine e1 moves", 1, e1.getMoves());
        Assert.assertEquals("engine e1 number", "X Test Number e1", e1.getNumber());
        Assert.assertEquals("engine e1 out of service", false, e1.isOutOfService());
        Assert.assertEquals("engine e1 rfid", "norfide1", e1.getRfid());
        Assert.assertEquals("engine e1 road", "OLDRoad", e1.getRoadName());
        Assert.assertEquals("engine e1 type", "e1 X type", e1.getTypeName());
        Assert.assertEquals("engine e1 weight", "54", e1.getWeight());
        Assert.assertEquals("engine e1 weight tons", "001", e1.getWeightTons());
        Assert.assertEquals("engine e1 hp", "e1 hp", e1.getHp());
        Assert.assertEquals("engine e1 model", "e1 X model", e1.getModel());

        Assert.assertEquals("engine e2 built date", "1234", e2.getBuilt());
        Assert.assertEquals("engine e2 color", "red", e2.getColor());
        Assert.assertEquals("engine e2 comment", "e2 comment", e2.getComment());
        Assert.assertEquals("engine e2 length", "77", e2.getLength());
        Assert.assertEquals("engine e2 moves", 10000, e2.getMoves());
        Assert.assertEquals("engine e2 number", "X Test Number e2", e2.getNumber());
        Assert.assertEquals("engine e2 out of service", true, e2.isOutOfService());
        Assert.assertEquals("engine e2 rfid", "rfide2", e2.getRfid());
        Assert.assertEquals("engine e2 road", "e2 Road", e2.getRoadName());
        Assert.assertEquals("engine e2 type", "e2 type", e2.getTypeName());
        Assert.assertEquals("engine e2 weight", "33", e2.getWeight());
        Assert.assertEquals("engine e2 weight tons", "798", e2.getWeightTons());
        Assert.assertEquals("engine e2 hp", "e2 hp", e2.getHp());
        Assert.assertEquals("engine e2 model", "e2 model", e2.getModel());

        Assert.assertEquals("engine e3 built date", "234", e3.getBuilt());
        Assert.assertEquals("engine e3 color", "green", e3.getColor());
        Assert.assertEquals("engine e3 comment", "e3 comment", e3.getComment());
        Assert.assertEquals("engine e3 length", "453", e3.getLength());
        Assert.assertEquals("engine e3 moves", 243, e3.getMoves());
        Assert.assertEquals("engine e3 number", "X Test Number e3", e3.getNumber());
        Assert.assertEquals("engine e3 out of service", false, e3.isOutOfService());
        Assert.assertEquals("engine e3 rfid", "rfide3", e3.getRfid());
        Assert.assertEquals("engine e3 road", "e3 Road", e3.getRoadName());
        Assert.assertEquals("engine e3 type", "e3 type", e3.getTypeName());
        Assert.assertEquals("engine e3 weight", "345", e3.getWeight());
        Assert.assertEquals("engine e3 weight tons", "1798", e3.getWeightTons());
        Assert.assertEquals("engine e3 hp", "e3 hp", e3.getHp());
        Assert.assertEquals("engine e3 model", "e3 model", e3.getModel());

    }

    // TODO: Add test for import
}
