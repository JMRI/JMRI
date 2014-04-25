// OperationsEnginesTest.java

package jmri.jmrit.operations.rollingstock.engines;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.jdom.JDOMException;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.CarManagerXml;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteManagerXml;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManagerXml;

/**
 * Tests for the Operations RollingStock Engine class
 * Last manually cross-checked on 20090131
 * 
 * Still to do:
 *   Engine: Destination
 *   Engine: Verify everything else 
 *   EngineTypes: get/set Names lists 
 *   EngineModels: get/set Names lists
 *   EngineLengths: Everything
 *   Consist: Everything
 *   Import: Everything  
 *   EngineManager: Engine register/deregister
 *   EngineManager: Consists
 * 
 * @author	Bob Coleman Copyright (C) 2008, 2009
 * @version $Revision$
 */
public class OperationsEnginesTest extends TestCase {

	// test Engine Class
        // test Engine creation
	public void testCreate() {
		Engine e1 = new Engine("TESTROAD", "TESTNUMBER1");
		e1.setModel("TESTMODEL");
		e1.setLength("TESTLENGTH");

		Assert.assertEquals("Engine Road", "TESTROAD", e1.getRoadName());
		Assert.assertEquals("Engine Number", "TESTNUMBER1", e1.getNumber());
		Assert.assertEquals("Engine Model", "TESTMODEL", e1.getModel());
		Assert.assertEquals("Engine Length", "TESTLENGTH", e1.getLength());
	}


	// test EngineLengths Class
        // test EngineLengths creation
	public void testEngineLengthsCreate() {
		EngineLengths el1 = new EngineLengths();
		Assert.assertNotNull("exists", el1 );
	}

        // test EngineLengths public constants
	public void testEngineLengthsConstants() {
		EngineLengths el1 = new EngineLengths();
		
		Assert.assertNotNull("exists", el1 );
		Assert.assertEquals("EngineTypes ENGINELENGTHS_CHANGED_PROPERTY", "EngineLengths", EngineLengths.ENGINELENGTHS_CHANGED_PROPERTY);
	}

        // test EngineLengths Names
	public void testEngineLengthsNames() {
		EngineLengths el1 = new EngineLengths();

		Assert.assertEquals("EngineLengths Null Names", false, el1.containsName("TESTENGINELENGTHNAME1"));

                el1.addName("TESTENGINELENGTHNAME1");
                Assert.assertEquals("EngineLengths add Name1", true, el1.containsName("TESTENGINELENGTHNAME1"));

                el1.addName("TESTENGINELENGTHNAME2");
                Assert.assertEquals("EngineLengths add Name2", true, el1.containsName("TESTENGINELENGTHNAME2"));

                el1.deleteName("TESTENGINELENGTHNAME2");
                Assert.assertEquals("EngineLengths delete Name2", false, el1.containsName("TESTENGINELENGTHNAME2"));

                el1.deleteName("TESTENGINELENGTHNAME1");
                Assert.assertEquals("EngineLengths delete Name1", false, el1.containsName("TESTENGINELENGTHNAME1"));
	}

        
	// test EngineTypes Class
        // test EngineTypes creation
	public void testEngineTypesCreate() {
		EngineTypes et1 = new EngineTypes();
		Assert.assertNotNull("exists", et1 );
	}

        // test EngineTypes public constants
	public void testEngineTypesConstants() {
		EngineTypes et1 = new EngineTypes();

		Assert.assertNotNull("exists", et1 );
		Assert.assertEquals("EngineTypes ENGINETYPES_LENGTH_CHANGED_PROPERTY", "EngineTypesLength", EngineTypes.ENGINETYPES_LENGTH_CHANGED_PROPERTY);
		Assert.assertEquals("EngineTypes ENGINETYPES_NAME_CHANGED_PROPERTY", "EngineTypesName", EngineTypes.ENGINETYPES_NAME_CHANGED_PROPERTY);
	}

        // test EngineTypes Names
	public void testEngineTypesNames() {
		EngineTypes et1 = new EngineTypes();

		Assert.assertEquals("EngineTypes Null Names", false, et1.containsName("TESTENGINETYPENAME1"));

                et1.addName("TESTENGINETYPENAME1");
                Assert.assertEquals("EngineTypes add Name1", true, et1.containsName("TESTENGINETYPENAME1"));

                et1.addName("TESTENGINETYPENAME2");
                Assert.assertEquals("EngineTypes add Name2", true, et1.containsName("TESTENGINETYPENAME2"));

                et1.replaceName("TESTENGINETYPENAME1", "TESTENGINETYPENAME3");
                Assert.assertEquals("EngineTypes replace Name1", false, et1.containsName("TESTENGINETYPENAME1"));
                Assert.assertEquals("EngineTypes replace Name3", true, et1.containsName("TESTENGINETYPENAME3"));

                et1.deleteName("TESTENGINETYPENAME2");
                Assert.assertEquals("EngineTypes delete Name2", false, et1.containsName("TESTENGINETYPENAME2"));

                et1.deleteName("TESTENGINETYPENAME3");
                Assert.assertEquals("EngineTypes delete Name3", false, et1.containsName("TESTENGINETYPENAME3"));
	}

        
	// test EngineModels Class
        // test EngineModels creation
	public void testEngineModelsCreate() {
		EngineModels em1 = new EngineModels();
		Assert.assertNotNull("exists", em1 );
	}

        // test EngineModels public constants
	public void testEngineModelsConstants() {
		EngineModels em1 = new EngineModels();

		Assert.assertNotNull("exists", em1 );
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

	public void testConsist() {
		Consist c1 = new Consist("TESTCONSIST");
		Assert.assertEquals("Consist Name", "TESTCONSIST", c1.getName());

		Engine e1 = new Engine("TESTROAD", "TESTNUMBER1");
		e1.setModel("GP35");  //  e1.setLength("56");
		Engine e2 = new Engine("TESTROAD", "TESTNUMBER2");
		e2.setModel("GP40");  //  e2.setLength("59");
		Engine e3 = new Engine("TESTROAD", "TESTNUMBER3");
		e3.setModel("SW1500");  //  e3.setLength("45");
		Engine e4 = new Engine("TESTROAD", "TESTNUMBER4");
		e4.setModel("SW1500");  //  e3.setLength("45");

		Assert.assertEquals("Consist Initial Length", 0, c1.getTotalLength());
		Assert.assertFalse("Consist Lead Engine 0", c1.isLead(e1));

		c1.add(e1);
		Assert.assertEquals("Consist Engine 1 Length", 56+4, c1.getTotalLength());
		Assert.assertTrue("Consist Lead Engine 1", c1.isLead(e1));

		c1.add(e2);
		Assert.assertEquals("Consist Engine 2 Length", 56+4+59+4, c1.getTotalLength());
		Assert.assertTrue("Consist Lead Engine 1 after 2", c1.isLead(e1));

                c1.setLead(e2);
		Assert.assertFalse("Consist Lead Engine 1 after 2c", c1.isLead(e1));
		Assert.assertTrue("Consist Lead Engine 2 after 2c", c1.isLead(e2));
                
		c1.add(e3);
		Assert.assertEquals("Consist Engine 3 Length", 56+4+59+4+45+4, c1.getTotalLength());
		Assert.assertTrue("Consist Lead Engine 2 after 3", c1.isLead(e2));
		Assert.assertFalse("Consist Lead Engine 1 after 3", c1.isLead(e1));
		Assert.assertFalse("Consist Lead Engine 3 after 3", c1.isLead(e3));

                // Can't set lead engine if not part of consist
                c1.setLead(e4);
		Assert.assertTrue("Consist Lead Engine 2 after 4c", c1.isLead(e2));
		Assert.assertFalse("Consist Lead Engine 4 after 4c", c1.isLead(e4));
              	List<Engine> tempengines = new ArrayList<Engine>();
                tempengines = c1.getEngines();
		Assert.assertTrue("Consist Engine 2 after 4c", tempengines.contains(e2));
		Assert.assertFalse("Consist Engine 4 after 4c", tempengines.contains(e4));
                


		c1.delete(e2);
		Assert.assertEquals("Consist Engine Delete 2 Length", 56+4+45+4, c1.getTotalLength());

		c1.delete(e1);
		Assert.assertEquals("Consist Engine Delete 1 Length", 45+4, c1.getTotalLength());

		c1.delete(e3);
		Assert.assertEquals("Consist Engine Delete 3 Length", 0, c1.getTotalLength());

	}

	public void testEngineConsist() {
		Consist cold = new Consist("TESTCONSISTOLD");
		Assert.assertEquals("Consist Name old", "TESTCONSISTOLD", cold.getName());

		Consist cnew = new Consist("TESTCONSISTNEW");
		Assert.assertEquals("Consist Name new", "TESTCONSISTNEW", cnew.getName());

		Engine e1 = new Engine("TESTROAD", "TESTNUMBER1");
		e1.setModel("GP35");  //  e1.setLength("56");
		Engine e2 = new Engine("TESTROAD", "TESTNUMBER2");
		e2.setModel("GP40");  //  e2.setLength("59");
		Engine e3 = new Engine("TESTROAD", "TESTNUMBER3");
		e3.setModel("SW1500");  //  e3.setLength("45");
		Engine e4 = new Engine("TESTROAD", "TESTNUMBER4");
		e4.setModel("SW1500");  //  e3.setLength("45");

		//  All three engines start out in the old consist with engine 1 as the lead engine.
		e1.setConsist(cold);
		e2.setConsist(cold);
		e3.setConsist(cold);
		Assert.assertEquals("Consist Name for engine 1 before", "TESTCONSISTOLD", e1.getConsistName());
		Assert.assertEquals("Consist Name for engine 2 before", "TESTCONSISTOLD", e2.getConsistName());
		Assert.assertEquals("Consist Name for engine 3 before", "TESTCONSISTOLD", e3.getConsistName());
		Assert.assertEquals("Consist old length before", 56+4+59+4+45+4, cold.getTotalLength());
		Assert.assertEquals("Consist new length before", 0, cnew.getTotalLength());
		Assert.assertTrue("Consist old Lead is Engine 1 before", cold.isLead(e1));
		Assert.assertFalse("Consist old Lead is not Engine 2 before", cold.isLead(e2));
		Assert.assertFalse("Consist old Lead is not Engine 3 before", cold.isLead(e3));
		Assert.assertFalse("Consist new Lead is not Engine 1 before", cnew.isLead(e1));
		Assert.assertFalse("Consist new Lead is not Engine 2 before", cnew.isLead(e2));
		Assert.assertFalse("Consist new Lead is not Engine 3 before", cnew.isLead(e3));

		//  Move engine 1 to the new consist where it will be the lead engine.
		//  Engine 2 should now be the lead engine of the old consist.
		e1.setConsist(cnew);
		Assert.assertEquals("Consist Name for engine 1 after", "TESTCONSISTNEW", e1.getConsistName());
		Assert.assertEquals("Consist Name for engine 2 after", "TESTCONSISTOLD", e2.getConsistName());
		Assert.assertEquals("Consist Name for engine 3 after", "TESTCONSISTOLD", e3.getConsistName());
		Assert.assertEquals("Consist old length after", 59+4+45+4, cold.getTotalLength());
		Assert.assertEquals("Consist new length after", 56+4, cnew.getTotalLength());
		Assert.assertFalse("Consist old Lead is not Engine 1 after", cold.isLead(e1));
		Assert.assertTrue("Consist old Lead is Engine 2 after", cold.isLead(e2));
		Assert.assertFalse("Consist old Lead is not Engine 3 after", cold.isLead(e3));
		Assert.assertTrue("Consist new Lead is Engine 1 after", cnew.isLead(e1));
		Assert.assertFalse("Consist new Lead is not Engine 2 after", cnew.isLead(e2));
		Assert.assertFalse("Consist new Lead is not Engine 3 after", cnew.isLead(e3));

		//  Move engine 3 to the new consist.
		e3.setConsist(cnew);
		Assert.assertEquals("Consist Name for engine 1 after3", "TESTCONSISTNEW", e1.getConsistName());
		Assert.assertEquals("Consist Name for engine 2 after3", "TESTCONSISTOLD", e2.getConsistName());
		Assert.assertEquals("Consist Name for engine 3 after3", "TESTCONSISTNEW", e3.getConsistName());
		Assert.assertEquals("Consist old length after3", 59+4, cold.getTotalLength());
		Assert.assertEquals("Consist new length after3", 56+4+45+4, cnew.getTotalLength());
		Assert.assertFalse("Consist old Lead is not Engine 1 after3", cold.isLead(e1));
		Assert.assertTrue("Consist old Lead is Engine 2 after3", cold.isLead(e2));
		Assert.assertFalse("Consist old Lead is not Engine 3 after3", cold.isLead(e3));
		Assert.assertTrue("Consist new Lead is Engine 1 after3", cnew.isLead(e1));
		Assert.assertFalse("Consist new Lead is not Engine 2 after3", cnew.isLead(e2));
		Assert.assertFalse("Consist new Lead is not Engine 3 after3", cnew.isLead(e3));
	}

	public void testEngineManager(){
        EngineManager manager = EngineManager.instance();
        List<RollingStock> engineList = manager.getByIdList();

        Assert.assertEquals("Starting Number of Engines", 0, engineList.size());
        Engine e1 = manager.newEngine("CP", "1");
        Engine e2 = manager.newEngine("ACL", "3");
        Engine e3 = manager.newEngine("CP", "3");
        Engine e4 = manager.newEngine("CP", "3-1");
        Engine e5 = manager.newEngine("PC", "2");
        Engine e6 = manager.newEngine("AA", "1");
        
        //setup the engines
        e1.setBuilt("2800");
        e2.setBuilt("1212");
        e3.setBuilt("100");
        e4.setBuilt("10");
        e5.setBuilt("1000");
        e6.setBuilt("1956");
        
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
        
        Location l1 = new Location("id1", "B");
        Track l1t1 = l1.addTrack("A",Track.SPUR);
        Track l1t2 = l1.addTrack("B",Track.SPUR);
        Location l2 = new Location("id2", "C");
        Track l2t1 = l2.addTrack("B",Track.SPUR);
        Track l2t2 = l2.addTrack("A",Track.SPUR);
        Location l3 = new Location("id3", "A");
        Track l3t1 = l3.addTrack("B",Track.SPUR);
        Track l3t2 = l3.addTrack("A",Track.SPUR);

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
        
        EngineTypes et = EngineTypes.instance();
        et.addName("Diesel");
        
        // place engines on tracks
        Assert.assertEquals("place e1", Track.OKAY, e1.setLocation(l1, l1t1));
        Assert.assertEquals("place e2", Track.OKAY, e2.setLocation(l1, l1t2));
        Assert.assertEquals("place e3", Track.OKAY, e3.setLocation(l2, l2t1));
        Assert.assertEquals("place e4", Track.OKAY, e4.setLocation(l2, l2t2));
        Assert.assertEquals("place e5", Track.OKAY, e5.setLocation(l3, l3t1));
        Assert.assertEquals("place e6", Track.OKAY, e6.setLocation(l3, l3t2));

        // set engine destinations
        Assert.assertEquals("destination e1", Track.OKAY, e1.setDestination(l3, l3t1));
        Assert.assertEquals("destination e2", Track.OKAY, e2.setDestination(l3, l3t2));
        Assert.assertEquals("destination e3", Track.OKAY, e3.setDestination(l2, l2t2));
        Assert.assertEquals("destination e4", Track.OKAY, e4.setDestination(l2, l2t1));
        Assert.assertEquals("destination e5", Track.OKAY, e5.setDestination(l1, l1t1));
        Assert.assertEquals("destination e6", Track.OKAY, e6.setDestination(l1, l1t2));

        e1.setConsist(new Consist("F"));
        e2.setConsist(new Consist("D"));
        e3.setConsist(new Consist("B"));
        e4.setConsist(new Consist("A"));
        e5.setConsist(new Consist("C"));
        e6.setConsist(new Consist("E"));
        
        e1.setMoves(2);
        e2.setMoves(44);
        e3.setMoves(99999);
        e4.setMoves(33);
        e5.setMoves(4);
        e6.setMoves(9999);
        
        e1.setRfid("SQ1");
        e2.setRfid("1Ab");
        e3.setRfid("Ase");
        e4.setRfid("asd");
        e5.setRfid("93F");
        e6.setRfid("B12");

        e1.setOwner("LAST");
        e2.setOwner("FOOL");
        e3.setOwner("AAA");
        e4.setOwner("DAD");
        e5.setOwner("DAB");
        e6.setOwner("BOB");
        
        Route r = new Route("id","Test");
        r.addLocation(l1);
        r.addLocation(l2);
        r.addLocation(l3);
        
        Train t1 = new Train("id1", "F");
        t1.setRoute(r);
        Train t3 = new Train("id3", "E");
        t3.setRoute(r);
        
        e1.setTrain(t1);
        e2.setTrain(t3);
        e3.setTrain(t3);
        e4.setTrain(new Train("id4", "B"));
        e5.setTrain(t3);
        e6.setTrain(new Train("id6", "A"));

        // now get engines by id
        engineList = manager.getByIdList();
        Assert.assertEquals("Number of Engines by id", 6, engineList.size());
        Assert.assertEquals("1st engine in list by id", e6, engineList.get(0));
        Assert.assertEquals("2nd engine in list by id", e2, engineList.get(1));
        Assert.assertEquals("3rd engine in list by id", e1, engineList.get(2));
        Assert.assertEquals("4th engine in list by id", e3, engineList.get(3));
        Assert.assertEquals("5th engine in list by id", e4, engineList.get(4));
        Assert.assertEquals("6th engine in list by id", e5, engineList.get(5));
   
        // now get engines by built
        engineList = manager.getByBuiltList();
        Assert.assertEquals("Number of Engines by built", 6, engineList.size());
        Assert.assertEquals("1st engine in list by built", e4, engineList.get(0));
        Assert.assertEquals("2nd engine in list by built", e3, engineList.get(1));
        Assert.assertEquals("3rd engine in list by built", e5, engineList.get(2));
        Assert.assertEquals("4th engine in list by built", e2, engineList.get(3));
        Assert.assertEquals("5th engine in list by built", e6, engineList.get(4));
        Assert.assertEquals("6th engine in list by built", e1, engineList.get(5));
  
        // now get engines by moves
        engineList = manager.getByMovesList();
        Assert.assertEquals("Number of Engines by move", 6, engineList.size());
        Assert.assertEquals("1st engine in list by move", e1, engineList.get(0));
        Assert.assertEquals("2nd engine in list by move", e5, engineList.get(1));
        Assert.assertEquals("3rd engine in list by move", e4, engineList.get(2));
        Assert.assertEquals("4th engine in list by move", e2, engineList.get(3));
        Assert.assertEquals("5th engine in list by move", e6, engineList.get(4));
        Assert.assertEquals("6th engine in list by move", e3, engineList.get(5));
  
        // now get engines by owner
        engineList = manager.getByOwnerList();
        Assert.assertEquals("Number of Engines by owner", 6, engineList.size());
        Assert.assertEquals("1st engine in list by owner", e3, engineList.get(0));
        Assert.assertEquals("2nd engine in list by owner", e6, engineList.get(1));
        Assert.assertEquals("3rd engine in list by owner", e5, engineList.get(2));
        Assert.assertEquals("4th engine in list by owner", e4, engineList.get(3));
        Assert.assertEquals("5th engine in list by owner", e2, engineList.get(4));
        Assert.assertEquals("6th engine in list by owner", e1, engineList.get(5));
 
        // now get engines by road name
        engineList = manager.getByRoadNameList();
        Assert.assertEquals("Number of Engines by road name", 6, engineList.size());
        Assert.assertEquals("1st engine in list by road name", e6, engineList.get(0));
        Assert.assertEquals("2nd engine in list by road name", e2, engineList.get(1));
        Assert.assertEquals("3rd engine in list by road name", e1, engineList.get(2));
        Assert.assertEquals("4th engine in list by road name", e3, engineList.get(3));
        Assert.assertEquals("5th engine in list by road name", e4, engineList.get(4));
        Assert.assertEquals("6th engine in list by road name", e5, engineList.get(5));

        // now get engines by consist
        engineList = manager.getByConsistList();
        Assert.assertEquals("Number of Engines by consist", 6, engineList.size());
        Assert.assertEquals("1st engine in list by consist", e4, engineList.get(0));
        Assert.assertEquals("2nd engine in list by consist", e3, engineList.get(1));
        Assert.assertEquals("3rd engine in list by consist", e5, engineList.get(2));
        Assert.assertEquals("4th engine in list by consist", e2, engineList.get(3));
        Assert.assertEquals("5th engine in list by consist", e6, engineList.get(4));
        Assert.assertEquals("6th engine in list by consist", e1, engineList.get(5));

        // now get engines by location
        engineList = manager.getByLocationList();
        Assert.assertEquals("Number of Engines by location", 6, engineList.size());
        Assert.assertEquals("1st engine in list by location", e6, engineList.get(0));
        Assert.assertEquals("2nd engine in list by location", e5, engineList.get(1));
        Assert.assertEquals("3rd engine in list by location", e1, engineList.get(2));
        Assert.assertEquals("4th engine in list by location", e2, engineList.get(3));
        Assert.assertEquals("5th engine in list by location", e4, engineList.get(4));
        Assert.assertEquals("6th engine in list by location", e3, engineList.get(5));

        // now get engines by destination
        engineList = manager.getByDestinationList();
        Assert.assertEquals("Number of Engines by destination", 6, engineList.size());
        Assert.assertEquals("1st engine in list by destination", e2, engineList.get(0));
        Assert.assertEquals("2nd engine in list by destination", e1, engineList.get(1));
        Assert.assertEquals("3rd engine in list by destination", e5, engineList.get(2));
        Assert.assertEquals("4th engine in list by destination", e6, engineList.get(3));
        Assert.assertEquals("5th engine in list by destination", e3, engineList.get(4));
        Assert.assertEquals("6th engine in list by destination", e4, engineList.get(5));

        // now get engines by train
        engineList = manager.getByTrainList();
        Assert.assertEquals("Number of Engines by train", 6, engineList.size());
        Assert.assertEquals("1st engine in list by train", e6, engineList.get(0));
        Assert.assertEquals("2nd engine in list by train", e4, engineList.get(1));
        Assert.assertEquals("3rd engine in list by train", e5, engineList.get(2));
        Assert.assertEquals("4th engine in list by train", e2, engineList.get(3));
        Assert.assertEquals("5th engine in list by train", e3, engineList.get(4));
        Assert.assertEquals("6th engine in list by train", e1, engineList.get(5));

        // now get engines by specific train
        List<Engine> engineList2 = manager.getByTrainBlockingList(t1);
        Assert.assertEquals("Number of Engines in t1", 1, engineList2.size());
        Assert.assertEquals("1st engine in list by t1", e1, engineList2.get(0));
        engineList2 = manager.getByTrainBlockingList(t3);
        Assert.assertEquals("Number of Engines in t3", 3, engineList2.size());
        Assert.assertEquals("1st engine in list by t3", e5, engineList2.get(0));
        Assert.assertEquals("2nd engine in list by t3", e2, engineList2.get(1));
        Assert.assertEquals("3rd engine in list by t3", e3, engineList2.get(2));
                    
        // how many engines available?
        engineList2 = manager.getAvailableTrainList(t1);
        Assert.assertEquals("Number of Engines available for t1", 1, engineList2.size());
        Assert.assertEquals("1st engine in list available for t1", e1, engineList2.get(0));

        engineList2 = manager.getAvailableTrainList(t3);
        Assert.assertEquals("Number of Engines available for t3", 3, engineList2.size());
        Assert.assertEquals("1st engine in list available for t3", e5, engineList2.get(0));
        Assert.assertEquals("2nd engine in list available for t3", e2, engineList2.get(1));
        Assert.assertEquals("3rd engine in list available for t3", e3, engineList2.get(2));
        
        // release engines from trains
        e2.setTrain(null);
        e4.setTrain(null);	// e4 is located in the middle of the route, therefore not available
        e6.setTrain(null);	// e6 is located at the end of the route, therefore not available
        
        // there should be more engines now
        engineList2 = manager.getAvailableTrainList(t1);
        Assert.assertEquals("Number of Engines available t1 after release", 4, engineList2.size());
        // should be sorted by moves
        Assert.assertEquals("1st engine in list available for t1", e1, engineList2.get(0));
        Assert.assertEquals("2nd engine in list available for t1", e4, engineList2.get(1));

        engineList2 = manager.getAvailableTrainList(t3);
        Assert.assertEquals("Number of Engines available for t3 after release", 5, engineList2.size());
        Assert.assertEquals("1st engine in list available for t3", e5, engineList2.get(0));

        // now get engines by road number
        engineList = manager.getByNumberList();
        Assert.assertEquals("Number of Engines by number", 6, engineList.size());
        Assert.assertEquals("1st engine in list by number", e6, engineList.get(0));
        Assert.assertEquals("2nd engine in list by number", e1, engineList.get(1));
        Assert.assertEquals("3rd engine in list by number", e5, engineList.get(2));
        Assert.assertEquals("4th engine in list by number", e2, engineList.get(3));
        Assert.assertEquals("5th engine in list by number", e3, engineList.get(4));
        Assert.assertEquals("6th engine in list by number", e4, engineList.get(5));
        
        // find engine by road and number
        Assert.assertEquals("find e1 by road and number", e1, manager.getByRoadAndNumber("CP", "1"));
        Assert.assertEquals("find e2 by road and number", e2, manager.getByRoadAndNumber("ACL", "3"));
        Assert.assertEquals("find e3 by road and number", e3, manager.getByRoadAndNumber("CP", "3"));
        Assert.assertEquals("find e4 by road and number", e4, manager.getByRoadAndNumber("CP", "3-1"));
        Assert.assertEquals("find e5 by road and number", e5, manager.getByRoadAndNumber("PC", "2"));
        Assert.assertEquals("find e6 by road and number", e6, manager.getByRoadAndNumber("AA", "1"));

        // now get engines by RFID
        engineList = manager.getByRfidList();
        Assert.assertEquals("Number of Engines by rfid", 6, engineList.size());
        Assert.assertEquals("1st engine in list by rfid", e2, engineList.get(0));
        Assert.assertEquals("2nd engine in list by rfid", e5, engineList.get(1));
        Assert.assertEquals("3rd engine in list by rfid", e4, engineList.get(2));
        Assert.assertEquals("4th engine in list by rfid", e3, engineList.get(3));
        Assert.assertEquals("5th engine in list by rfid", e6, engineList.get(4));
        Assert.assertEquals("6th engine in list by rfid", e1, engineList.get(5));

        // find engine by RFID
        Assert.assertEquals("find e1 by rfid", e1, manager.getByRfid("SQ1"));
        Assert.assertEquals("find e2 by rfid", e2, manager.getByRfid("1Ab"));
        Assert.assertEquals("find e3 by rfid", e3, manager.getByRfid("Ase"));
        Assert.assertEquals("find e4 by rfid", e4, manager.getByRfid("asd"));
        Assert.assertEquals("find e5 by rfid", e5, manager.getByRfid("93F"));
        Assert.assertEquals("find e6 by rfid", e6, manager.getByRfid("B12"));
        
        // now get engines by model
        engineList = manager.getByModelList();
        Assert.assertEquals("Number of Engines by type", 6, engineList.size());
        Assert.assertEquals("1st engine in list by type", e3, engineList.get(0));
        Assert.assertEquals("2nd engine in list by type", e4, engineList.get(1));
        Assert.assertEquals("3rd engine in list by type", e5, engineList.get(2));
        Assert.assertEquals("4th engine in list by type", e2, engineList.get(3));
        Assert.assertEquals("5th engine in list by type", e6, engineList.get(4));
        Assert.assertEquals("6th engine in list by type", e1, engineList.get(5));
        
        manager.dispose();
        engineList = manager.getByIdList();
        Assert.assertEquals("After dispose Number of Engines", 0, engineList.size());		
	}

	// test Xml create support
	public void testXMLCreate() throws JDOMException, IOException{
		
		// confirm that file name has been modified for testing
		Assert.assertEquals("OperationsJUnitTestEngineRoster.xml", EngineManagerXml.instance().getOperationsFileName());

		EngineManager manager = EngineManager.instance();
		List<RollingStock> tempengineList = manager.getByIdList();

		Assert.assertEquals("Starting Number of Engines", 0, tempengineList.size());
		
		Engine e1 = manager.newEngine("CP", "Test Number 1");
		Engine e2 = manager.newEngine("ACL", "Test Number 2");
		Engine e3 = manager.newEngine("CP", "Test Number 3");

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
		e3.setRfid("rfide3");
		e3.setRoadName("e3 Road");
		e3.setTypeName("e3 type");
		e3.setWeight("345");
		e3.setWeightTons("1798");

		tempengineList = manager.getByIdList();
		Assert.assertEquals("New Number of Engines", 3, tempengineList.size());

		EngineManagerXml.instance().writeOperationsFile();

		// Add some more engines and write file again
		// so we can test the backup facility
		Engine e4 = manager.newEngine("PC", "Test Number 4");
		Engine e5 = manager.newEngine("BM", "Test Number 5");
		Engine e6 = manager.newEngine("SP", "Test Number 6");

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
		e5.setRfid("rfide5");
		e5.setRoadName("e5Road");
		e5.setTypeName("e5 type");
		e5.setWeight("66");
		e5.setWeightTons("77");      
		
		tempengineList = manager.getByIdList();
		Assert.assertEquals("New Number of Engines", 6, tempengineList.size());

		EngineManagerXml.instance().writeOperationsFile();
		
		// now reset everything using dispose
		manager.dispose();
	    EngineModels.instance().dispose();
	    
		manager = EngineManager.instance();
		tempengineList = manager.getByIdList();
		Assert.assertEquals("Starting Number of Engines", 0, tempengineList.size());

		// confirm that engine models has been reset by dispose
		Assert.assertEquals("e1 model type", null, EngineModels.instance().getModelType("e1 model"));
		Assert.assertEquals("e1 model length", null, EngineModels.instance().getModelLength("e1 model"));
		Assert.assertEquals("e1 model Weight Tons", null, EngineModels.instance().getModelWeight("e1 model"));
		Assert.assertEquals("e1 model hp", null, EngineModels.instance().getModelHorsepower("e1 model"));
		
		EngineManagerXml.instance().readFile(EngineManagerXml.instance().getDefaultOperationsFilename());	

		tempengineList = manager.getByIdList();
		Assert.assertEquals("Number of Engines", 6, tempengineList.size());
		
		// confirm that engine models was loaded	
		Assert.assertEquals("e1 model type", "e1 type", EngineModels.instance().getModelType("e1 model"));
		Assert.assertEquals("e1 model length", "40", EngineModels.instance().getModelLength("e1 model"));
		Assert.assertEquals("e1 model Weight Tons", "100", EngineModels.instance().getModelWeight("e1 model"));
		Assert.assertEquals("e1 model hp", "e1 hp", EngineModels.instance().getModelHorsepower("e1 model"));
		
		e1 = manager.getByRoadAndNumber("CP", "Test Number 1"); // must find engine by original id
		e2 = manager.getByRoadAndNumber("ACL", "Test Number 2"); // must find engine by original id
		e3 = manager.getByRoadAndNumber("CP", "Test Number 3"); // must find engine by original id
		e4 = manager.getByRoadAndNumber("PC", "Test Number 4"); // must find engine by original id 
		e5 = manager.getByRoadAndNumber("BM", "Test Number 5"); // must find engine by original id
		e6 = manager.getByRoadAndNumber("SP", "Test Number 6"); // must find engine by original id

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
		Assert.assertEquals("engine e4 length", "", e4.getLength());
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
		Assert.assertEquals("engine e6 length", "", e6.getLength());
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
		manager = EngineManager.instance();
		tempengineList = manager.getByIdList();
		Assert.assertEquals("Starting Number of Engines", 0, tempengineList.size());
		
		// confirm that engine models has been reset by dispose	
		Assert.assertEquals("e1 model type", null, EngineModels.instance().getModelType("e1 X model"));
		Assert.assertEquals("e1 model length", null, EngineModels.instance().getModelLength("e1 X model"));
		Assert.assertEquals("e1 model Weight Tons", null, EngineModels.instance().getModelWeight("e1 X model"));
		Assert.assertEquals("e1 model hp", null, EngineModels.instance().getModelHorsepower("e1 X model"));

		// change default file name to backup
		EngineManagerXml.instance().setOperationsFileName("OperationsJUnitTestEngineRoster.xml.bak");

		EngineManagerXml.instance().readFile(EngineManagerXml.instance().getDefaultOperationsFilename());	

		tempengineList = manager.getByIdList();
		Assert.assertEquals("Number of Engines", 3, tempengineList.size());
		
		// confirm that engine models was loaded	
		Assert.assertEquals("e1 model type", "e1 X type", EngineModels.instance().getModelType("e1 X model"));
		Assert.assertEquals("e1 model length", "04", EngineModels.instance().getModelLength("e1 X model"));
		Assert.assertEquals("e1 model Weight Tons", "001", EngineModels.instance().getModelWeight("e1 X model"));
		Assert.assertEquals("e1 model hp", "e1 hp", EngineModels.instance().getModelHorsepower("e1 X model"));

		e1 = manager.getByRoadAndNumber("CP", "Test Number 1"); // must find engine by original id
		e2 = manager.getByRoadAndNumber("ACL", "Test Number 2"); // must find engine by original id
		e3 = manager.getByRoadAndNumber("CP", "Test Number 3"); // must find engine by original id
		e4 = manager.getByRoadAndNumber("PC", "Test Number 4"); // must find engine by original id 
		e5 = manager.getByRoadAndNumber("BM", "Test Number 5"); // must find engine by original id
		e6 = manager.getByRoadAndNumber("SP", "Test Number 6"); // must find engine by original id

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

    // from here down is testing infrastructure

    // Ensure minimal setup for log4J
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        
		// set the locale to US English
		Locale.setDefault(Locale.ENGLISH);
        
		// Repoint OperationsSetupXml to JUnitTest subdirectory
		OperationsSetupXml.setOperationsDirectoryName("operations"+File.separator+"JUnitTest");
		// Change file names to ...Test.xml
		OperationsSetupXml.instance().setOperationsFileName("OperationsJUnitTest.xml"); 
		RouteManagerXml.instance().setOperationsFileName("OperationsJUnitTestRouteRoster.xml");
		CarManagerXml.instance().setOperationsFileName("OperationsJUnitTestCarRoster.xml");
		EngineManagerXml.instance().setOperationsFileName("OperationsJUnitTestEngineRoster.xml");
		LocationManagerXml.instance().setOperationsFileName("OperationsJUnitTestLocationRoster.xml");
		TrainManagerXml.instance().setOperationsFileName("OperationsJUnitTestTrainRoster.xml");

        // Need to clear out EngineManager global variables
        EngineManager manager = EngineManager.instance();
        List<String> tempconsistList = manager.getConsistNameList();
        for (int i = 0; i < tempconsistList.size(); i++) {
            String consistId = tempconsistList.get(i);
            manager.deleteConsist(consistId);
        }
        EngineModels.instance().dispose();
        EngineLengths.instance().dispose();
        manager.dispose();
    }

	public OperationsEnginesTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", OperationsEnginesTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(OperationsEnginesTest.class);
		return suite;
	}

    // The minimal setup for log4J
    @Override
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
