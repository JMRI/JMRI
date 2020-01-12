package jmri.jmrit.operations.rollingstock.engines;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
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
public class EngineModelsTest extends OperationsTestCase {

    // test EngineModels Class
    // test EngineModels creation
    @Test
    public void testEngineModelsCreate() {
        EngineModels em1 = new EngineModels();
        Assert.assertNotNull("exists", em1);
    }

    // test EngineModels public constants
    @Test
    public void testEngineModelsConstants() {
        EngineModels em1 = new EngineModels();

        Assert.assertNotNull("exists", em1);
        Assert.assertEquals("EngineModels ENGINEMODELS_CHANGED_PROPERTY", "EngineModels", EngineModels.ENGINEMODELS_CHANGED_PROPERTY);
    }

    // test EngineModels Names
    @Test
    public void testEngineModelsNames() {
        EngineModels em1 = new EngineModels();

        Assert.assertEquals("EngineModels Null Names", false, em1.containsName("TESTENGINEMODELNAME1"));

        em1.addName("TESTENGINEMODELNAME1");
        Assert.assertEquals("EngineModels add Name1", true, em1.containsName("TESTENGINEMODELNAME1"));

        em1.addName("TESTENGINEMODELNAME2");
        Assert.assertEquals("EngineModels add Name2", true, em1.containsName("TESTENGINEMODELNAME2"));

        em1.deleteName("TESTENGINEMODELNAME2");
        Assert.assertEquals("EngineModels delete Name2", false, em1.containsName("TESTENGINEMODELNAME2"));

        em1.deleteName("TESTENGINEMODELNAME1");
        Assert.assertEquals("EngineModels delete Name1", false, em1.containsName("TESTENGINEMODELNAME1"));
    }

    // test EngineModels Attributes
    @Test
    public void testEngineModelsAttributes() {
        EngineModels em1 = new EngineModels();

        em1.setModelHorsepower("TESTENGINEMODELNAME1", "3800");
        Assert.assertEquals("EngineModels HorsePower1", "3800", em1.getModelHorsepower("TESTENGINEMODELNAME1"));

        em1.setModelHorsepower("TESTENGINEMODELNAME2", "2400");
        Assert.assertEquals("EngineModels HorsePower2+1", "3800", em1.getModelHorsepower("TESTENGINEMODELNAME1"));
        Assert.assertEquals("EngineModels HorsePower2", "2400", em1.getModelHorsepower("TESTENGINEMODELNAME2"));

        em1.setModelLength("TESTENGINEMODELNAME1", "60");
        Assert.assertEquals("EngineModels Length1", "60", em1.getModelLength("TESTENGINEMODELNAME1"));

        em1.setModelLength("TESTENGINEMODELNAME2", "50");
        Assert.assertEquals("EngineModels Length2+1", "60", em1.getModelLength("TESTENGINEMODELNAME1"));
        Assert.assertEquals("EngineModels Length2", "50", em1.getModelLength("TESTENGINEMODELNAME2"));

        em1.setModelType("TESTENGINEMODELNAME1", "TESTDiesel");
        Assert.assertEquals("EngineModels Type1", "TESTDiesel", em1.getModelType("TESTENGINEMODELNAME1"));

        em1.setModelType("TESTENGINEMODELNAME2", "TESTSteam");
        Assert.assertEquals("EngineModels Type2+1", "TESTDiesel", em1.getModelType("TESTENGINEMODELNAME1"));
        Assert.assertEquals("EngineModels Type2", "TESTSteam", em1.getModelType("TESTENGINEMODELNAME2"));
        Assert.assertEquals("EngineModels Type+Length2+1", "60", em1.getModelLength("TESTENGINEMODELNAME1"));
        Assert.assertEquals("EngineModels Type+Length2", "50", em1.getModelLength("TESTENGINEMODELNAME2"));
        Assert.assertEquals("EngineModels Type+HorsePower2+1", "3800", em1.getModelHorsepower("TESTENGINEMODELNAME1"));
        Assert.assertEquals("EngineModels Type+HorsePower2", "2400", em1.getModelHorsepower("TESTENGINEMODELNAME2"));
    }

    // test EngineModels Defaults
    @Test
    public void testEngineModelsDefaults() throws Exception {

        InstanceManager.getDefault(EngineModels.class).addName("E8");
        Assert.assertEquals("EngineModels Default Model E8", true, InstanceManager.getDefault(EngineModels.class).containsName("E8"));
        Assert.assertEquals("EngineModels Default Horse E8", "2250", InstanceManager.getDefault(EngineModels.class).getModelHorsepower("E8"));
        Assert.assertEquals("EngineModels Default Length E8", "70", InstanceManager.getDefault(EngineModels.class).getModelLength("E8"));
        Assert.assertEquals("EngineModels Default Type E8", "Diesel", InstanceManager.getDefault(EngineModels.class).getModelType("E8"));

        InstanceManager.getDefault(EngineModels.class).addName("FT");
        Assert.assertEquals("EngineModels Default Model FT", true, InstanceManager.getDefault(EngineModels.class).containsName("FT"));
        Assert.assertEquals("EngineModels Default Horse FT", "1350", InstanceManager.getDefault(EngineModels.class).getModelHorsepower("FT"));
        Assert.assertEquals("EngineModels Default Length FT", "50", InstanceManager.getDefault(EngineModels.class).getModelLength("FT"));
        Assert.assertEquals("EngineModels Default Type FT", "Diesel", InstanceManager.getDefault(EngineModels.class).getModelType("FT"));

        InstanceManager.getDefault(EngineModels.class).addName("F3");
        Assert.assertEquals("EngineModels Default Model F3", true, InstanceManager.getDefault(EngineModels.class).containsName("F3"));
        Assert.assertEquals("EngineModels Default Horse F3", "1500", InstanceManager.getDefault(EngineModels.class).getModelHorsepower("F3"));
        Assert.assertEquals("EngineModels Default Length F3", "50", InstanceManager.getDefault(EngineModels.class).getModelLength("F3"));
        Assert.assertEquals("EngineModels Default Type F3", "Diesel", InstanceManager.getDefault(EngineModels.class).getModelType("F3"));

        InstanceManager.getDefault(EngineModels.class).addName("F7");
        Assert.assertEquals("EngineModels Default Model F7", true, InstanceManager.getDefault(EngineModels.class).containsName("F7"));
        Assert.assertEquals("EngineModels Default Horse F7", "1500", InstanceManager.getDefault(EngineModels.class).getModelHorsepower("F7"));
        Assert.assertEquals("EngineModels Default Length F7", "50", InstanceManager.getDefault(EngineModels.class).getModelLength("F7"));
        Assert.assertEquals("EngineModels Default Type F7", "Diesel", InstanceManager.getDefault(EngineModels.class).getModelType("F7"));

        InstanceManager.getDefault(EngineModels.class).addName("F9");
        Assert.assertEquals("EngineModels Default Model F9", true, InstanceManager.getDefault(EngineModels.class).containsName("F9"));
        Assert.assertEquals("EngineModels Default Horse F9", "1750", InstanceManager.getDefault(EngineModels.class).getModelHorsepower("F9"));
        Assert.assertEquals("EngineModels Default Length F9", "50", InstanceManager.getDefault(EngineModels.class).getModelLength("F9"));
        Assert.assertEquals("EngineModels Default Type F9", "Diesel", InstanceManager.getDefault(EngineModels.class).getModelType("F9"));

        InstanceManager.getDefault(EngineModels.class).addName("GG1");
        Assert.assertEquals("EngineModels Default Model GG1", true, InstanceManager.getDefault(EngineModels.class).containsName("GG1"));
        Assert.assertEquals("EngineModels Default Horse GG1", "4620", InstanceManager.getDefault(EngineModels.class).getModelHorsepower("GG1"));
        Assert.assertEquals("EngineModels Default Length GG1", "80", InstanceManager.getDefault(EngineModels.class).getModelLength("GG1"));
        Assert.assertEquals("EngineModels Default Type GG1", "Electric", InstanceManager.getDefault(EngineModels.class).getModelType("GG1"));

        InstanceManager.getDefault(EngineModels.class).addName("GP20");
        Assert.assertEquals("EngineModels Default Model GP20", true, InstanceManager.getDefault(EngineModels.class).containsName("GP20"));
        Assert.assertEquals("EngineModels Default Horse GP20", "2000", InstanceManager.getDefault(EngineModels.class).getModelHorsepower("GP20"));
        Assert.assertEquals("EngineModels Default Length GP20", "56", InstanceManager.getDefault(EngineModels.class).getModelLength("GP20"));
        Assert.assertEquals("EngineModels Default Type GP20", "Diesel", InstanceManager.getDefault(EngineModels.class).getModelType("GP20"));

        InstanceManager.getDefault(EngineModels.class).addName("GP30");
        Assert.assertEquals("EngineModels Default Model GP30", true, InstanceManager.getDefault(EngineModels.class).containsName("GP30"));
        Assert.assertEquals("EngineModels Default Horse GP30", "2250", InstanceManager.getDefault(EngineModels.class).getModelHorsepower("GP30"));
        Assert.assertEquals("EngineModels Default Length GP30", "56", InstanceManager.getDefault(EngineModels.class).getModelLength("GP30"));
        Assert.assertEquals("EngineModels Default Type GP30", "Diesel", InstanceManager.getDefault(EngineModels.class).getModelType("GP30"));

        InstanceManager.getDefault(EngineModels.class).addName("GP35");
        Assert.assertEquals("EngineModels Default Model GP35", true, InstanceManager.getDefault(EngineModels.class).containsName("GP35"));
        Assert.assertEquals("EngineModels Default Horse GP35", "2500", InstanceManager.getDefault(EngineModels.class).getModelHorsepower("GP35"));
        Assert.assertEquals("EngineModels Default Length GP35", "56", InstanceManager.getDefault(EngineModels.class).getModelLength("GP35"));
        Assert.assertEquals("EngineModels Default Type GP35", "Diesel", InstanceManager.getDefault(EngineModels.class).getModelType("GP35"));

        InstanceManager.getDefault(EngineModels.class).addName("GP38");
        Assert.assertEquals("EngineModels Default Model GP38", true, InstanceManager.getDefault(EngineModels.class).containsName("GP38"));
        Assert.assertEquals("EngineModels Default Horse GP38", "2000", InstanceManager.getDefault(EngineModels.class).getModelHorsepower("GP38"));
        Assert.assertEquals("EngineModels Default Length GP38", "59", InstanceManager.getDefault(EngineModels.class).getModelLength("GP38"));
        Assert.assertEquals("EngineModels Default Type GP38", "Diesel", InstanceManager.getDefault(EngineModels.class).getModelType("GP38"));

        InstanceManager.getDefault(EngineModels.class).addName("GP40");
        Assert.assertEquals("EngineModels Default Model GP40", true, InstanceManager.getDefault(EngineModels.class).containsName("GP40"));
        Assert.assertEquals("EngineModels Default Horse GP40", "3000", InstanceManager.getDefault(EngineModels.class).getModelHorsepower("GP40"));
        Assert.assertEquals("EngineModels Default Length GP40", "59", InstanceManager.getDefault(EngineModels.class).getModelLength("GP40"));
        Assert.assertEquals("EngineModels Default Type GP40", "Diesel", InstanceManager.getDefault(EngineModels.class).getModelType("GP40"));

        InstanceManager.getDefault(EngineModels.class).addName("GTEL");
        Assert.assertEquals("EngineModels Default Model GTEL", true, InstanceManager.getDefault(EngineModels.class).containsName("GTEL"));
        Assert.assertEquals("EngineModels Default Horse GTEL", "4500", InstanceManager.getDefault(EngineModels.class).getModelHorsepower("GTEL"));
        Assert.assertEquals("EngineModels Default Length GTEL", "80", InstanceManager.getDefault(EngineModels.class).getModelLength("GTEL"));
        Assert.assertEquals("EngineModels Default Type GTEL", "Gas Turbine", InstanceManager.getDefault(EngineModels.class).getModelType("GTEL"));

        InstanceManager.getDefault(EngineModels.class).addName("RS1");
        Assert.assertEquals("EngineModels Default Model RS1", true, InstanceManager.getDefault(EngineModels.class).containsName("RS1"));
        Assert.assertEquals("EngineModels Default Horse RS1", "1000", InstanceManager.getDefault(EngineModels.class).getModelHorsepower("RS1"));
        Assert.assertEquals("EngineModels Default Length RS1", "51", InstanceManager.getDefault(EngineModels.class).getModelLength("RS1"));
        Assert.assertEquals("EngineModels Default Type RS1", "Diesel", InstanceManager.getDefault(EngineModels.class).getModelType("RS1"));

        InstanceManager.getDefault(EngineModels.class).addName("RS2");
        Assert.assertEquals("EngineModels Default Model RS2", true, InstanceManager.getDefault(EngineModels.class).containsName("RS2"));
        Assert.assertEquals("EngineModels Default Horse RS2", "1500", InstanceManager.getDefault(EngineModels.class).getModelHorsepower("RS2"));
        Assert.assertEquals("EngineModels Default Length RS2", "52", InstanceManager.getDefault(EngineModels.class).getModelLength("RS2"));
        Assert.assertEquals("EngineModels Default Type RS2", "Diesel", InstanceManager.getDefault(EngineModels.class).getModelType("RS2"));

        InstanceManager.getDefault(EngineModels.class).addName("RS3");
        Assert.assertEquals("EngineModels Default Model RS3", true, InstanceManager.getDefault(EngineModels.class).containsName("RS3"));
        Assert.assertEquals("EngineModels Default Horse RS3", "1600", InstanceManager.getDefault(EngineModels.class).getModelHorsepower("RS3"));
        Assert.assertEquals("EngineModels Default Length RS3", "51", InstanceManager.getDefault(EngineModels.class).getModelLength("RS3"));
        Assert.assertEquals("EngineModels Default Type RS3", "Diesel", InstanceManager.getDefault(EngineModels.class).getModelType("RS3"));

        InstanceManager.getDefault(EngineModels.class).addName("RS11");
        Assert.assertEquals("EngineModels Default Model RS11", true, InstanceManager.getDefault(EngineModels.class).containsName("RS11"));
        Assert.assertEquals("EngineModels Default Horse RS11", "1800", InstanceManager.getDefault(EngineModels.class).getModelHorsepower("RS11"));
        Assert.assertEquals("EngineModels Default Length RS11", "53", InstanceManager.getDefault(EngineModels.class).getModelLength("RS11"));
        Assert.assertEquals("EngineModels Default Type RS11", "Diesel", InstanceManager.getDefault(EngineModels.class).getModelType("RS11"));

        InstanceManager.getDefault(EngineModels.class).addName("RS18");
        Assert.assertEquals("EngineModels Default Model RS18", true, InstanceManager.getDefault(EngineModels.class).containsName("RS18"));
        Assert.assertEquals("EngineModels Default Horse RS18", "1800", InstanceManager.getDefault(EngineModels.class).getModelHorsepower("RS18"));
        Assert.assertEquals("EngineModels Default Length RS18", "52", InstanceManager.getDefault(EngineModels.class).getModelLength("RS18"));
        Assert.assertEquals("EngineModels Default Type RS18", "Diesel", InstanceManager.getDefault(EngineModels.class).getModelType("RS18"));

        InstanceManager.getDefault(EngineModels.class).addName("RS27");
        Assert.assertEquals("EngineModels Default Model RS27", true, InstanceManager.getDefault(EngineModels.class).containsName("RS27"));
        Assert.assertEquals("EngineModels Default Horse RS27", "2400", InstanceManager.getDefault(EngineModels.class).getModelHorsepower("RS27"));
        Assert.assertEquals("EngineModels Default Length RS27", "57", InstanceManager.getDefault(EngineModels.class).getModelLength("RS27"));
        Assert.assertEquals("EngineModels Default Type RS27", "Diesel", InstanceManager.getDefault(EngineModels.class).getModelType("RS27"));

        InstanceManager.getDefault(EngineModels.class).addName("RSD4");
        Assert.assertEquals("EngineModels Default Model RSD4", true, InstanceManager.getDefault(EngineModels.class).containsName("RSD4"));
        Assert.assertEquals("EngineModels Default Horse RSD4", "1600", InstanceManager.getDefault(EngineModels.class).getModelHorsepower("RSD4"));
        Assert.assertEquals("EngineModels Default Length RSD4", "52", InstanceManager.getDefault(EngineModels.class).getModelLength("RSD4"));
        Assert.assertEquals("EngineModels Default Type RSD4", "Diesel", InstanceManager.getDefault(EngineModels.class).getModelType("RSD4"));

        InstanceManager.getDefault(EngineModels.class).addName("Shay");
        Assert.assertEquals("EngineModels Default Model Shay", true, InstanceManager.getDefault(EngineModels.class).containsName("Shay"));
        Assert.assertEquals("EngineModels Default Horse Shay", "70", InstanceManager.getDefault(EngineModels.class).getModelHorsepower("Shay"));
        Assert.assertEquals("EngineModels Default Length Shay", "50", InstanceManager.getDefault(EngineModels.class).getModelLength("Shay"));
        Assert.assertEquals("EngineModels Default Type Shay", "Steam", InstanceManager.getDefault(EngineModels.class).getModelType("Shay"));

        InstanceManager.getDefault(EngineModels.class).addName("SD26");
        Assert.assertEquals("EngineModels Default Model SD26", true, InstanceManager.getDefault(EngineModels.class).containsName("SD26"));
        Assert.assertEquals("EngineModels Default Horse SD26", "2650", InstanceManager.getDefault(EngineModels.class).getModelHorsepower("SD26"));
        Assert.assertEquals("EngineModels Default Length SD26", "61", InstanceManager.getDefault(EngineModels.class).getModelLength("SD26"));
        Assert.assertEquals("EngineModels Default Type SD26", "Diesel", InstanceManager.getDefault(EngineModels.class).getModelType("SD26"));

        InstanceManager.getDefault(EngineModels.class).addName("SD45");
        Assert.assertEquals("EngineModels Default Model SD45", true, InstanceManager.getDefault(EngineModels.class).containsName("SD45"));
        Assert.assertEquals("EngineModels Default Horse SD45", "3600", InstanceManager.getDefault(EngineModels.class).getModelHorsepower("SD45"));
        Assert.assertEquals("EngineModels Default Length SD45", "66", InstanceManager.getDefault(EngineModels.class).getModelLength("SD45"));
        Assert.assertEquals("EngineModels Default Type SD45", "Diesel", InstanceManager.getDefault(EngineModels.class).getModelType("SD45"));

        InstanceManager.getDefault(EngineModels.class).addName("SW1200");
        Assert.assertEquals("EngineModels Default Model SW1200", true, InstanceManager.getDefault(EngineModels.class).containsName("SW1200"));
        Assert.assertEquals("EngineModels Default Horse SW1200", "1200", InstanceManager.getDefault(EngineModels.class).getModelHorsepower("SW1200"));
        Assert.assertEquals("EngineModels Default Length SW1200", "45", InstanceManager.getDefault(EngineModels.class).getModelLength("SW1200"));
        Assert.assertEquals("EngineModels Default Type SW1200", "Diesel", InstanceManager.getDefault(EngineModels.class).getModelType("SW1200"));

        InstanceManager.getDefault(EngineModels.class).addName("SW1500");
        Assert.assertEquals("EngineModels Default Model SW1500", true, InstanceManager.getDefault(EngineModels.class).containsName("SW1500"));
        Assert.assertEquals("EngineModels Default Horse SW1500", "1500", InstanceManager.getDefault(EngineModels.class).getModelHorsepower("SW1500"));
        Assert.assertEquals("EngineModels Default Length SW1500", "45", InstanceManager.getDefault(EngineModels.class).getModelLength("SW1500"));
        Assert.assertEquals("EngineModels Default Type SW1500", "Diesel", InstanceManager.getDefault(EngineModels.class).getModelType("SW1500"));

        InstanceManager.getDefault(EngineModels.class).addName("SW8");
        Assert.assertEquals("EngineModels Default Model SW8", true, InstanceManager.getDefault(EngineModels.class).containsName("SW8"));
        Assert.assertEquals("EngineModels Default Horse SW8", "800", InstanceManager.getDefault(EngineModels.class).getModelHorsepower("SW8"));
        Assert.assertEquals("EngineModels Default Length SW8", "44", InstanceManager.getDefault(EngineModels.class).getModelLength("SW8"));
        Assert.assertEquals("EngineModels Default Type SW8", "Diesel", InstanceManager.getDefault(EngineModels.class).getModelType("SW8"));

        InstanceManager.getDefault(EngineModels.class).addName("TRAINMASTER");
        Assert.assertEquals("EngineModels Default Model TRAINMASTER", true, InstanceManager.getDefault(EngineModels.class).containsName("TRAINMASTER"));
        Assert.assertEquals("EngineModels Default Horse TRAINMASTER", "2400", InstanceManager.getDefault(EngineModels.class).getModelHorsepower("TRAINMASTER"));
        Assert.assertEquals("EngineModels Default Length TRAINMASTER", "66", InstanceManager.getDefault(EngineModels.class).getModelLength("TRAINMASTER"));
        Assert.assertEquals("EngineModels Default Type TRAINMASTER", "Diesel", InstanceManager.getDefault(EngineModels.class).getModelType("TRAINMASTER"));

        InstanceManager.getDefault(EngineModels.class).addName("U28B");
        Assert.assertEquals("EngineModels Default Model U28B", true, InstanceManager.getDefault(EngineModels.class).containsName("U28B"));
        Assert.assertEquals("EngineModels Default Horse U28B", "2800", InstanceManager.getDefault(EngineModels.class).getModelHorsepower("U28B"));
        Assert.assertEquals("EngineModels Default Length U28B", "60", InstanceManager.getDefault(EngineModels.class).getModelLength("U28B"));
        Assert.assertEquals("EngineModels Default Type U28B", "Diesel", InstanceManager.getDefault(EngineModels.class).getModelType("U28B"));
    }
}
