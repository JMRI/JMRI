package jmri.jmrit.roster;

import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/** 
 * RosterEntryTest.java
 *
 * Description:	    tests for the jmrit.roster.RosterEntry class
 * @author			Bob Jacobsen
 * @version			
 */
public class RosterEntryTest extends TestCase {

	public void testCreate() {
		RosterEntry r = new RosterEntry("file here");
		Assert.assertEquals("file name ", "file here", r.getFileName());
		Assert.assertEquals("DCC Address ", "", r.getDccAddress());
		Assert.assertEquals("road name ", "", r.getRoadName());
		Assert.assertEquals("road number ", "", r.getRoadNumber());
		Assert.assertEquals("manufacturer ", "", r.getMfg());
		Assert.assertEquals("model ", "", r.getDecoderModel());
		Assert.assertEquals("family ", "", r.getDecoderFamily());
	}

	public void testPartialLoad() {
		// create Element
		org.jdom.Namespace ns = org.jdom.Namespace.getNamespace("roster", "");
		org.jdom.Element e = new org.jdom.Element("locomotive", ns)
									.addAttribute("id","our id")
									.addAttribute("fileName","file here")
									.addAttribute("roadNumber","431")
									.addAttribute("roadName","SP")
									.addAttribute("mfg","Athearn")
									.addAttribute("dccAddress","1234")
				; // end create element
				
		RosterEntry r = new RosterEntry(e, ns);
		// check
		Assert.assertEquals("file name ", "file here", r.getFileName());
		Assert.assertEquals("DCC Address ", "1234", r.getDccAddress());
		Assert.assertEquals("road name ", "SP", r.getRoadName());
		Assert.assertEquals("road number ", "431", r.getRoadNumber());
		Assert.assertEquals("manufacturer ", "Athearn", r.getMfg());
		Assert.assertEquals("model ", "", r.getDecoderModel());
		Assert.assertEquals("family ", "", r.getDecoderFamily());
	}

	public void testEmptyLoad() {
		// create Element
		org.jdom.Namespace ns = org.jdom.Namespace.getNamespace("roster", "");
		org.jdom.Element e = new org.jdom.Element("locomotive", ns)
									.addAttribute("id","our id")
									.addAttribute("fileName","file here")
				; // end create element
				
		RosterEntry r = new RosterEntry(e, ns);
		// check
		Assert.assertEquals("file name ", "file here", r.getFileName());
		Assert.assertEquals("DCC Address ", "", r.getDccAddress());
		Assert.assertEquals("road name ", "", r.getRoadName());
		Assert.assertEquals("road number ", "", r.getRoadNumber());
		Assert.assertEquals("manufacturer ", "", r.getMfg());
		Assert.assertEquals("model ", "", r.getDecoderModel());
		Assert.assertEquals("family ", "", r.getDecoderFamily());
	}
	
	public void testFullLoad() {
		// create Element
		org.jdom.Namespace ns = org.jdom.Namespace.getNamespace("roster", "");
		org.jdom.Element e = new org.jdom.Element("locomotive", ns)
									.addAttribute("id","our id")
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
				
		RosterEntry r = new RosterEntry(e, ns);
		// check
		Assert.assertEquals("file name ", "file here", r.getFileName());
		Assert.assertEquals("DCC Address ", "1234", r.getDccAddress());
		Assert.assertEquals("road name ", "SP", r.getRoadName());
		Assert.assertEquals("road number ", "431", r.getRoadNumber());
		Assert.assertEquals("manufacturer ", "Athearn", r.getMfg());
		Assert.assertEquals("model ", "33", r.getDecoderModel());
		Assert.assertEquals("family ", "91", r.getDecoderFamily());
	}

	public void testStore() {
		// create Element
		org.jdom.Namespace ns = org.jdom.Namespace.getNamespace("roster", "");
		org.jdom.Element e = new org.jdom.Element("locomotive", ns)
									.addAttribute("id","our id")
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
				
		RosterEntry r = new RosterEntry(e, ns);
		org.jdom.Element o = r.store(ns);
		// check
		Assert.assertEquals("XML Element ", e.toString(), o.toString());
		Assert.assertEquals("family ","91", o.getChild("decoder",ns).getAttribute("family").getValue());
		Assert.assertEquals("model ","33", o.getChild("decoder",ns).getAttribute("model").getValue());
	}

	// from here down is testing infrastructure
	
	public RosterEntryTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {RosterEntry.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(RosterEntryTest.class);
		return suite;
	}
	
}
