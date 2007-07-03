package jmri.jmrit.display;

import jmri.jmrit.XmlFile;

import java.io.File;
import java.io.FileWriter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * PanelEditorTest.java
 *
 * Description:
 * @author			Bob Jacobsen
 * @version			$Revision: 1.3 $
 */
public class PanelEditorTest extends TestCase {

    TurnoutIcon to = null;

	public void testShow() throws java.io.IOException {
	    // create a test file
        XmlFile.ensurePrefsPresent("prefs");
        XmlFile.ensurePrefsPresent("prefs"+File.separator+"temp");
	    File f = new File(XmlFile.prefsDir()+File.separator+"temp"+File.separator+"PanelEditorTest1.xml");
	    FileWriter fw = new FileWriter(f);
	    fw.write(layerTestData);
	    fw.close();
	    
	    // load and display
	    (new jmri.configurexml.ConfigXmlManager()).load(f);
	}


	// from here down is testing infrastructure

	public PanelEditorTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", PanelEditorTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(PanelEditorTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

String layerTestData = 
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
"<!DOCTYPE layout-config SYSTEM \"layout-config.dtd\">"+
"<layout-config>"+
"  <paneleditor class=\"jmri.jmrit.display.configurexml.PanelEditorXml\" name=\"Layer Name Test\" x=\"20\" y=\"22\" height=\"226\" width=\"204\" editable=\"no\" positionable=\"yes\" controlling=\"yes\" hide=\"yes\" panelmenu=\"no\">"+
"    <positionablelabel forcecontroloff=\"false\" fixed=\"false\" showtooltip=\"true\" class=\"jmri.jmrit.display.configurexml.PositionableLabelXml\" x=\"40\" y=\"130\" level=\"5\" text=\"Checks should be\" size=\"13\" style=\"0\" />"+
"    <positionablelabel forcecontroloff=\"false\" fixed=\"false\" showtooltip=\"true\" class=\"jmri.jmrit.display.configurexml.PositionableLabelXml\" x=\"51\" y=\"149\" level=\"5\" text=\"in front of Xs\" size=\"13\" style=\"0\" />"+
"    <turnouticon turnout=\"IT1\" x=\"45\" y=\"55\" level=\"10\" closed=\"resource:resources/icons/misc/Checkmark-green.gif\" thrown=\"resource:resources/icons/misc/Checkmark-green.gif\" unknown=\"resource:resources/icons/misc/Checkmark-green.gif\" inconsistent=\"resource:resources/icons/misc/Checkmark-green.gif\" rotate=\"0\" forcecontroloff=\"false\" class=\"jmri.jmrit.display.configurexml.TurnoutIconXml\" />"+
"    <sensoricon sensor=\"IS1\" x=\"46\" y=\"53\" level=\"7\" active=\"resource:resources/icons/misc/X-red.gif\" inactive=\"resource:resources/icons/misc/X-red.gif\" unknown=\"resource:resources/icons/misc/X-red.gif\" inconsistent=\"resource:resources/icons/misc/X-red.gif\" rotate=\"0\" forcecontroloff=\"false\" momentary=\"false\" class=\"jmri.jmrit.display.configurexml.SensorIconXml\" />"+
"    <turnouticon turnout=\"IT1\" x=\"136\" y=\"53\" level=\"7\" closed=\"resource:resources/icons/misc/X-red.gif\" thrown=\"resource:resources/icons/misc/X-red.gif\" unknown=\"resource:resources/icons/misc/X-red.gif\" inconsistent=\"resource:resources/icons/misc/X-red.gif\" rotate=\"0\" forcecontroloff=\"false\" class=\"jmri.jmrit.display.configurexml.TurnoutIconXml\" />"+
"    <sensoricon sensor=\"IS1\" x=\"138\" y=\"54\" level=\"10\" active=\"resource:resources/icons/misc/Checkmark-green.gif\" inactive=\"resource:resources/icons/misc/Checkmark-green.gif\" unknown=\"resource:resources/icons/misc/Checkmark-green.gif\" inconsistent=\"resource:resources/icons/misc/Checkmark-green.gif\" rotate=\"0\" forcecontroloff=\"false\" momentary=\"false\" class=\"jmri.jmrit.display.configurexml.SensorIconXml\" />"+
"  </paneleditor>"+
"</layout-config>";



	// static private org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(TurnoutIconTest.class.getName());

}
