package jmri.jmrit.operations.rollingstock.engines;

import jmri.jmrit.operations.OperationsTestCase;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the Operations RollingStock Engine class Last manually
 * cross-checked on 20090131
 *
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
    public void testEngineModelsCreate() {
        EngineModels em1 = new EngineModels();
        Assert.assertNotNull("exists", em1);
    }

    // test EngineModels public constants
    public void testEngineModelsConstants() {
        EngineModels em1 = new EngineModels();

        Assert.assertNotNull("exists", em1);
        Assert.assertEquals("EngineModels ENGINEMODELS_CHANGED_PROPERTY", "EngineModels", EngineModels.ENGINEMODELS_CHANGED_PROPERTY);
    }

    // test EngineModels Names
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
    public void testEngineModelsDefaults() throws Exception {

        EngineModels.instance().addName("E8");
        Assert.assertEquals("EngineModels Default Model E8", true, EngineModels.instance().containsName("E8"));
        Assert.assertEquals("EngineModels Default Horse E8", "2250", EngineModels.instance().getModelHorsepower("E8"));
        Assert.assertEquals("EngineModels Default Length E8", "70", EngineModels.instance().getModelLength("E8"));
        Assert.assertEquals("EngineModels Default Type E8", "Diesel", EngineModels.instance().getModelType("E8"));

        EngineModels.instance().addName("FT");
        Assert.assertEquals("EngineModels Default Model FT", true, EngineModels.instance().containsName("FT"));
        Assert.assertEquals("EngineModels Default Horse FT", "1350", EngineModels.instance().getModelHorsepower("FT"));
        Assert.assertEquals("EngineModels Default Length FT", "50", EngineModels.instance().getModelLength("FT"));
        Assert.assertEquals("EngineModels Default Type FT", "Diesel", EngineModels.instance().getModelType("FT"));

        EngineModels.instance().addName("F3");
        Assert.assertEquals("EngineModels Default Model F3", true, EngineModels.instance().containsName("F3"));
        Assert.assertEquals("EngineModels Default Horse F3", "1500", EngineModels.instance().getModelHorsepower("F3"));
        Assert.assertEquals("EngineModels Default Length F3", "50", EngineModels.instance().getModelLength("F3"));
        Assert.assertEquals("EngineModels Default Type F3", "Diesel", EngineModels.instance().getModelType("F3"));

        EngineModels.instance().addName("F7");
        Assert.assertEquals("EngineModels Default Model F7", true, EngineModels.instance().containsName("F7"));
        Assert.assertEquals("EngineModels Default Horse F7", "1500", EngineModels.instance().getModelHorsepower("F7"));
        Assert.assertEquals("EngineModels Default Length F7", "50", EngineModels.instance().getModelLength("F7"));
        Assert.assertEquals("EngineModels Default Type F7", "Diesel", EngineModels.instance().getModelType("F7"));

        EngineModels.instance().addName("F9");
        Assert.assertEquals("EngineModels Default Model F9", true, EngineModels.instance().containsName("F9"));
        Assert.assertEquals("EngineModels Default Horse F9", "1750", EngineModels.instance().getModelHorsepower("F9"));
        Assert.assertEquals("EngineModels Default Length F9", "50", EngineModels.instance().getModelLength("F9"));
        Assert.assertEquals("EngineModels Default Type F9", "Diesel", EngineModels.instance().getModelType("F9"));

        EngineModels.instance().addName("GG1");
        Assert.assertEquals("EngineModels Default Model GG1", true, EngineModels.instance().containsName("GG1"));
        Assert.assertEquals("EngineModels Default Horse GG1", "4620", EngineModels.instance().getModelHorsepower("GG1"));
        Assert.assertEquals("EngineModels Default Length GG1", "80", EngineModels.instance().getModelLength("GG1"));
        Assert.assertEquals("EngineModels Default Type GG1", "Electric", EngineModels.instance().getModelType("GG1"));

        EngineModels.instance().addName("GP20");
        Assert.assertEquals("EngineModels Default Model GP20", true, EngineModels.instance().containsName("GP20"));
        Assert.assertEquals("EngineModels Default Horse GP20", "2000", EngineModels.instance().getModelHorsepower("GP20"));
        Assert.assertEquals("EngineModels Default Length GP20", "56", EngineModels.instance().getModelLength("GP20"));
        Assert.assertEquals("EngineModels Default Type GP20", "Diesel", EngineModels.instance().getModelType("GP20"));

        EngineModels.instance().addName("GP30");
        Assert.assertEquals("EngineModels Default Model GP30", true, EngineModels.instance().containsName("GP30"));
        Assert.assertEquals("EngineModels Default Horse GP30", "2250", EngineModels.instance().getModelHorsepower("GP30"));
        Assert.assertEquals("EngineModels Default Length GP30", "56", EngineModels.instance().getModelLength("GP30"));
        Assert.assertEquals("EngineModels Default Type GP30", "Diesel", EngineModels.instance().getModelType("GP30"));

        EngineModels.instance().addName("GP35");
        Assert.assertEquals("EngineModels Default Model GP35", true, EngineModels.instance().containsName("GP35"));
        Assert.assertEquals("EngineModels Default Horse GP35", "2500", EngineModels.instance().getModelHorsepower("GP35"));
        Assert.assertEquals("EngineModels Default Length GP35", "56", EngineModels.instance().getModelLength("GP35"));
        Assert.assertEquals("EngineModels Default Type GP35", "Diesel", EngineModels.instance().getModelType("GP35"));

        EngineModels.instance().addName("GP38");
        Assert.assertEquals("EngineModels Default Model GP38", true, EngineModels.instance().containsName("GP38"));
        Assert.assertEquals("EngineModels Default Horse GP38", "2000", EngineModels.instance().getModelHorsepower("GP38"));
        Assert.assertEquals("EngineModels Default Length GP38", "59", EngineModels.instance().getModelLength("GP38"));
        Assert.assertEquals("EngineModels Default Type GP38", "Diesel", EngineModels.instance().getModelType("GP38"));

        EngineModels.instance().addName("GP40");
        Assert.assertEquals("EngineModels Default Model GP40", true, EngineModels.instance().containsName("GP40"));
        Assert.assertEquals("EngineModels Default Horse GP40", "3000", EngineModels.instance().getModelHorsepower("GP40"));
        Assert.assertEquals("EngineModels Default Length GP40", "59", EngineModels.instance().getModelLength("GP40"));
        Assert.assertEquals("EngineModels Default Type GP40", "Diesel", EngineModels.instance().getModelType("GP40"));

        EngineModels.instance().addName("GTEL");
        Assert.assertEquals("EngineModels Default Model GTEL", true, EngineModels.instance().containsName("GTEL"));
        Assert.assertEquals("EngineModels Default Horse GTEL", "4500", EngineModels.instance().getModelHorsepower("GTEL"));
        Assert.assertEquals("EngineModels Default Length GTEL", "80", EngineModels.instance().getModelLength("GTEL"));
        Assert.assertEquals("EngineModels Default Type GTEL", "Gas Turbine", EngineModels.instance().getModelType("GTEL"));

        EngineModels.instance().addName("RS1");
        Assert.assertEquals("EngineModels Default Model RS1", true, EngineModels.instance().containsName("RS1"));
        Assert.assertEquals("EngineModels Default Horse RS1", "1000", EngineModels.instance().getModelHorsepower("RS1"));
        Assert.assertEquals("EngineModels Default Length RS1", "51", EngineModels.instance().getModelLength("RS1"));
        Assert.assertEquals("EngineModels Default Type RS1", "Diesel", EngineModels.instance().getModelType("RS1"));

        EngineModels.instance().addName("RS2");
        Assert.assertEquals("EngineModels Default Model RS2", true, EngineModels.instance().containsName("RS2"));
        Assert.assertEquals("EngineModels Default Horse RS2", "1500", EngineModels.instance().getModelHorsepower("RS2"));
        Assert.assertEquals("EngineModels Default Length RS2", "52", EngineModels.instance().getModelLength("RS2"));
        Assert.assertEquals("EngineModels Default Type RS2", "Diesel", EngineModels.instance().getModelType("RS2"));

        EngineModels.instance().addName("RS3");
        Assert.assertEquals("EngineModels Default Model RS3", true, EngineModels.instance().containsName("RS3"));
        Assert.assertEquals("EngineModels Default Horse RS3", "1600", EngineModels.instance().getModelHorsepower("RS3"));
        Assert.assertEquals("EngineModels Default Length RS3", "51", EngineModels.instance().getModelLength("RS3"));
        Assert.assertEquals("EngineModels Default Type RS3", "Diesel", EngineModels.instance().getModelType("RS3"));

        EngineModels.instance().addName("RS11");
        Assert.assertEquals("EngineModels Default Model RS11", true, EngineModels.instance().containsName("RS11"));
        Assert.assertEquals("EngineModels Default Horse RS11", "1800", EngineModels.instance().getModelHorsepower("RS11"));
        Assert.assertEquals("EngineModels Default Length RS11", "53", EngineModels.instance().getModelLength("RS11"));
        Assert.assertEquals("EngineModels Default Type RS11", "Diesel", EngineModels.instance().getModelType("RS11"));

        EngineModels.instance().addName("RS18");
        Assert.assertEquals("EngineModels Default Model RS18", true, EngineModels.instance().containsName("RS18"));
        Assert.assertEquals("EngineModels Default Horse RS18", "1800", EngineModels.instance().getModelHorsepower("RS18"));
        Assert.assertEquals("EngineModels Default Length RS18", "52", EngineModels.instance().getModelLength("RS18"));
        Assert.assertEquals("EngineModels Default Type RS18", "Diesel", EngineModels.instance().getModelType("RS18"));

        EngineModels.instance().addName("RS27");
        Assert.assertEquals("EngineModels Default Model RS27", true, EngineModels.instance().containsName("RS27"));
        Assert.assertEquals("EngineModels Default Horse RS27", "2400", EngineModels.instance().getModelHorsepower("RS27"));
        Assert.assertEquals("EngineModels Default Length RS27", "57", EngineModels.instance().getModelLength("RS27"));
        Assert.assertEquals("EngineModels Default Type RS27", "Diesel", EngineModels.instance().getModelType("RS27"));

        EngineModels.instance().addName("RSD4");
        Assert.assertEquals("EngineModels Default Model RSD4", true, EngineModels.instance().containsName("RSD4"));
        Assert.assertEquals("EngineModels Default Horse RSD4", "1600", EngineModels.instance().getModelHorsepower("RSD4"));
        Assert.assertEquals("EngineModels Default Length RSD4", "52", EngineModels.instance().getModelLength("RSD4"));
        Assert.assertEquals("EngineModels Default Type RSD4", "Diesel", EngineModels.instance().getModelType("RSD4"));

        EngineModels.instance().addName("Shay");
        Assert.assertEquals("EngineModels Default Model Shay", true, EngineModels.instance().containsName("Shay"));
        Assert.assertEquals("EngineModels Default Horse Shay", "70", EngineModels.instance().getModelHorsepower("Shay"));
        Assert.assertEquals("EngineModels Default Length Shay", "50", EngineModels.instance().getModelLength("Shay"));
        Assert.assertEquals("EngineModels Default Type Shay", "Steam", EngineModels.instance().getModelType("Shay"));

        EngineModels.instance().addName("SD26");
        Assert.assertEquals("EngineModels Default Model SD26", true, EngineModels.instance().containsName("SD26"));
        Assert.assertEquals("EngineModels Default Horse SD26", "2650", EngineModels.instance().getModelHorsepower("SD26"));
        Assert.assertEquals("EngineModels Default Length SD26", "61", EngineModels.instance().getModelLength("SD26"));
        Assert.assertEquals("EngineModels Default Type SD26", "Diesel", EngineModels.instance().getModelType("SD26"));

        EngineModels.instance().addName("SD45");
        Assert.assertEquals("EngineModels Default Model SD45", true, EngineModels.instance().containsName("SD45"));
        Assert.assertEquals("EngineModels Default Horse SD45", "3600", EngineModels.instance().getModelHorsepower("SD45"));
        Assert.assertEquals("EngineModels Default Length SD45", "66", EngineModels.instance().getModelLength("SD45"));
        Assert.assertEquals("EngineModels Default Type SD45", "Diesel", EngineModels.instance().getModelType("SD45"));

        EngineModels.instance().addName("SW1200");
        Assert.assertEquals("EngineModels Default Model SW1200", true, EngineModels.instance().containsName("SW1200"));
        Assert.assertEquals("EngineModels Default Horse SW1200", "1200", EngineModels.instance().getModelHorsepower("SW1200"));
        Assert.assertEquals("EngineModels Default Length SW1200", "45", EngineModels.instance().getModelLength("SW1200"));
        Assert.assertEquals("EngineModels Default Type SW1200", "Diesel", EngineModels.instance().getModelType("SW1200"));

        EngineModels.instance().addName("SW1500");
        Assert.assertEquals("EngineModels Default Model SW1500", true, EngineModels.instance().containsName("SW1500"));
        Assert.assertEquals("EngineModels Default Horse SW1500", "1500", EngineModels.instance().getModelHorsepower("SW1500"));
        Assert.assertEquals("EngineModels Default Length SW1500", "45", EngineModels.instance().getModelLength("SW1500"));
        Assert.assertEquals("EngineModels Default Type SW1500", "Diesel", EngineModels.instance().getModelType("SW1500"));

        EngineModels.instance().addName("SW8");
        Assert.assertEquals("EngineModels Default Model SW8", true, EngineModels.instance().containsName("SW8"));
        Assert.assertEquals("EngineModels Default Horse SW8", "800", EngineModels.instance().getModelHorsepower("SW8"));
        Assert.assertEquals("EngineModels Default Length SW8", "44", EngineModels.instance().getModelLength("SW8"));
        Assert.assertEquals("EngineModels Default Type SW8", "Diesel", EngineModels.instance().getModelType("SW8"));

        EngineModels.instance().addName("TRAINMASTER");
        Assert.assertEquals("EngineModels Default Model TRAINMASTER", true, EngineModels.instance().containsName("TRAINMASTER"));
        Assert.assertEquals("EngineModels Default Horse TRAINMASTER", "2400", EngineModels.instance().getModelHorsepower("TRAINMASTER"));
        Assert.assertEquals("EngineModels Default Length TRAINMASTER", "66", EngineModels.instance().getModelLength("TRAINMASTER"));
        Assert.assertEquals("EngineModels Default Type TRAINMASTER", "Diesel", EngineModels.instance().getModelType("TRAINMASTER"));

        EngineModels.instance().addName("U28B");
        Assert.assertEquals("EngineModels Default Model U28B", true, EngineModels.instance().containsName("U28B"));
        Assert.assertEquals("EngineModels Default Horse U28B", "2800", EngineModels.instance().getModelHorsepower("U28B"));
        Assert.assertEquals("EngineModels Default Length U28B", "60", EngineModels.instance().getModelLength("U28B"));
        Assert.assertEquals("EngineModels Default Type U28B", "Diesel", EngineModels.instance().getModelType("U28B"));
    }

    // from here down is testing infrastructure
    // Ensure minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public EngineModelsTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", EngineModelsTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(EngineModelsTest.class);
        return suite;
    }

    @Override
    protected void tearDown() throws Exception {
       super.tearDown();
    }


}
