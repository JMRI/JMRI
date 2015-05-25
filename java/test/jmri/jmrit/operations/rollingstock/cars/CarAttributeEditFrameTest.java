//CarAttributeEditFrameTest.java
package jmri.jmrit.operations.rollingstock.cars;

import java.io.File;
import java.util.List;
import java.util.Locale;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.rollingstock.engines.EngineManagerXml;
import jmri.jmrit.operations.routes.RouteManagerXml;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.trains.TrainManagerXml;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.extensions.jfcunit.finder.AbstractButtonFinder;
import junit.extensions.jfcunit.finder.DialogFinder;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import jmri.util.JUnitUtil;

/**
 * Tests for the Operations CarAttributeEditFrame class
 *
 * @author	Dan Boudreau Copyright (C) 2009
 * @version $Revision$
 */
public class CarAttributeEditFrameTest extends jmri.util.SwingTestCase {

    public void testCarAttributeEditFrameColor() {
        CarAttributeEditFrame f = new CarAttributeEditFrame();
        f.initComponents(CarEditFrame.COLOR);
        f.addTextBox.setText("Pink");
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addButton));
        // new color should appear at start of list
        Assert.assertEquals("new color", "Pink", f.comboBox.getItemAt(0));

        // test replace
        f.comboBox.setSelectedItem("Pink");
        f.addTextBox.setText("Pinker");
        // push replace button
        getHelper().enterClickAndLeave(new MouseEventData(this, f.replaceButton));
        // need to also push the "Yes" button in the dialog window
        pressDialogButton(f, "Yes");
        // did the replace work?
        Assert.assertEquals("replaced Pink with Pinker", "Pinker", f.comboBox.getItemAt(0));

        getHelper().enterClickAndLeave(new MouseEventData(this, f.deleteButton));
        // black is the first default color
        Assert.assertEquals("old color", "Black", f.comboBox.getItemAt(0));

        f.dispose();
    }

    public void testCarAttributeEditFrameKernel() {
        // remove all kernels
        CarManager cm = CarManager.instance();
        List<String> kList = cm.getKernelNameList();
        for (int i = 0; i < kList.size(); i++) {
            cm.deleteKernel(kList.get(i));
        }
        // create TwoCars kernel
        cm.newKernel("TwoCars");

        CarAttributeEditFrame f = new CarAttributeEditFrame();
        f.initComponents(CarEditFrame.KERNEL);
        // confirm that space and TwoCar kernel exists
        Assert.assertEquals("space 1", "", f.comboBox.getItemAt(0));
        Assert.assertEquals("previous kernel 1", "TwoCars", f.comboBox.getItemAt(1));

        f.addTextBox.setText("TestKernel");
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addButton));
        // new kernel should appear at start of list after blank
        Assert.assertEquals("new kernel", "TestKernel", f.comboBox.getItemAt(1));

        // test replace
        f.comboBox.setSelectedItem("TestKernel");
        f.addTextBox.setText("TestKernel2");
        // push replace button
        getHelper().enterClickAndLeave(new MouseEventData(this, f.replaceButton));
        // need to also push the "Yes" button in the dialog window
        pressDialogButton(f, "Yes");
        // did the replace work?
        Assert.assertEquals("replaced TestKernel with TestKernel2", "TestKernel2", f.comboBox.getItemAt(1));

        // now try and delete
        f.comboBox.setSelectedItem("TestKernel2");
        getHelper().enterClickAndLeave(new MouseEventData(this, f.deleteButton));
        // blank is the first default kernel
        Assert.assertEquals("space 2", "", f.comboBox.getItemAt(0));
        Assert.assertEquals("previous kernel 2", "TwoCars", f.comboBox.getItemAt(1));

        f.dispose();
    }

    public void testCarAttributeEditFrame2() {
        CarAttributeEditFrame f = new CarAttributeEditFrame();
        f.initComponents(CarEditFrame.LENGTH);
        f.dispose();
        f = new CarAttributeEditFrame();
        f.initComponents(CarEditFrame.OWNER);
        f.dispose();
        f = new CarAttributeEditFrame();
        f.initComponents(CarEditFrame.ROAD);
        f.dispose();
        f = new CarAttributeEditFrame();
        f.initComponents(CarEditFrame.TYPE);
        f.dispose();
    }

    private void loadCars() {
        CarManager cManager = CarManager.instance();
        // remove previous cars
        cManager.dispose();
        // add 5 cars to table
        Car c1 = cManager.newCar("NH", "1");
        c1.setBuilt("2009");
        c1.setColor("Red");
        c1.setLength("40");
        c1.setLoadName("L");
        c1.setMoves(55);
        c1.setOwner("Owner2");
        // make sure the ID tags exist before we
        // try to add it to a car.
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("RFID 3");
        c1.setRfid("RFID 3");
        c1.setTypeName("Caboose");
        c1.setWeight("1.4");
        c1.setWeightTons("Tons of Weight");
        c1.setCaboose(true);
        c1.setComment("Test Car NH 1 Comment");

        Car c2 = cManager.newCar("UP", "2");
        c2.setBuilt("2004");
        c2.setColor("Blue");
        c2.setLength("50");
        c2.setLoadName("E");
        c2.setMoves(50);
        c2.setOwner("AT");
        // make sure the ID tags exist before we
        // try to add it to a car.
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("RFID 2");
        c2.setRfid("RFID 2");
        c2.setTypeName("Boxcar");

        Car c3 = cManager.newCar("AA", "3");
        c3.setBuilt("2006");
        c3.setColor("White");
        c3.setLength("30");
        c3.setLoadName("LA");
        c3.setMoves(40);
        c3.setOwner("AB");
        // make sure the ID tags exist before we
        // try to add it to a car.
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("RFID 5");
        c3.setRfid("RFID 5");
        c3.setTypeName("Gondola");

        Car c4 = cManager.newCar("SP", "2");
        c4.setBuilt("1990");
        c4.setColor("Black");
        c4.setLength("45");
        c4.setLoadName("EA");
        c4.setMoves(30);
        c4.setOwner("AAA");
        // make sure the ID tags exist before we
        // try to add it to a car.
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("RFID 4");
        c4.setRfid("RFID 4");
        c4.setTypeName("Tank Food");

        Car c5 = cManager.newCar("NH", "5");
        c5.setBuilt("1956");
        c5.setColor("Brown");
        c5.setLength("25");
        c5.setLoadName("LL");
        c5.setMoves(25);
        c5.setOwner("DAB");
        // make sure the ID tags exist before we
        // try to add it to a car.
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("RFID 1");
        c5.setRfid("RFID 1");
        c5.setTypeName("Coilcar");

    }

    @SuppressWarnings("unchecked")
    private void pressDialogButton(OperationsFrame f, String buttonName) {
        //  (with JfcUnit, not pushing this off to another thread)			                                            
        // Locate resulting dialog box
        List<javax.swing.JDialog> dialogList = new DialogFinder(null).findAll(f);
        javax.swing.JDialog d = dialogList.get(0);
        // Find the button
        AbstractButtonFinder finder = new AbstractButtonFinder(buttonName);
        javax.swing.JButton button = (javax.swing.JButton) finder.find(d, 0);
        Assert.assertNotNull("button not found", button);
        // Click button
        getHelper().enterClickAndLeave(new MouseEventData(this, button));
    }

    // Ensure minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initIdTagManager();
        apps.tests.Log4JFixture.setUp();

        // set the locale to US English
        Locale.setDefault(Locale.ENGLISH);

        // Repoint OperationsSetupXml to JUnitTest subdirectory
        OperationsSetupXml.setOperationsDirectoryName("operations" + File.separator + "JUnitTest");
        // Change file names to ...Test.xml
        OperationsSetupXml.instance().setOperationsFileName("OperationsJUnitTest.xml");
        RouteManagerXml.instance().setOperationsFileName("OperationsJUnitTestRouteRoster.xml");
        EngineManagerXml.instance().setOperationsFileName("OperationsJUnitTestEngineRoster.xml");
        CarManagerXml.instance().setOperationsFileName("OperationsJUnitTestCarRoster.xml");
        LocationManagerXml.instance().setOperationsFileName("OperationsJUnitTestLocationRoster.xml");
        TrainManagerXml.instance().setOperationsFileName("OperationsJUnitTestTrainRoster.xml");

        CarColors.instance().dispose();	// reset colors
        CarTypes.instance().dispose();

    }

    public CarAttributeEditFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", CarAttributeEditFrameTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(CarAttributeEditFrameTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void tearDown() throws Exception {
        apps.tests.Log4JFixture.tearDown();
        super.tearDown();
    }
}
