package jmri.jmrit.roster;

import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/** 
 * RosterEntryPaneTest.java
 *
 * Description:	    tests for the jmrit.roster.RosterEntryPane class
 * @author			Bob Jacobsen
 * @version			
 */
public class RosterEntryPaneTest extends TestCase {

	// statics for test objects
	static org.jdom.Namespace ns = null;
	org.jdom.Element e = null;
	RosterEntry r = null;
	
	public void setUp() {
		// create Element
		ns = org.jdom.Namespace.getNamespace("roster", "");
		e = new org.jdom.Element("locomotive", ns)
									.addAttribute("id","id info")
									.addAttribute("fileName","file here")
									.addAttribute("roadNumber","431")
									.addAttribute("roadName","SP")
									.addAttribute("mfg","Athearn")
									.addAttribute("dccAddress","1234")
									.addContent(new org.jdom.Element("decoder", ns)
										.addAttribute("family","91")
										.addAttribute("model","33")
												)
				; // end create element
				
		r = new RosterEntry(e, ns);
	}

	public void testCreate() {
		RosterEntryPane p = new RosterEntryPane(r);
		// check for field text contents
		Assert.assertEquals("file name ", "file here", p.filename.getText());
		Assert.assertEquals("DCC Address ", "1234", p.dccAddress.getText());
		Assert.assertEquals("road name ", "SP", p.roadName.getText());
		Assert.assertEquals("road number ", "431", p.roadNumber.getText());
		Assert.assertEquals("manufacturer ", "Athearn", p.mfg.getText());
		Assert.assertEquals("model ", "33",p.decoderModel.getText());
		Assert.assertEquals("family ", "91", p.decoderFamily.getText());

	}

	// from here down is testing infrastructure
	
	public RosterEntryPaneTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {RosterEntryPane.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(RosterEntryPaneTest.class);
		return suite;
	}
	
}
