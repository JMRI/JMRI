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
		Assert.assertEquals("DCC Address ", null, r.getDccAddress());
		Assert.assertEquals("road name ", null, r.getRoadName());
		Assert.assertEquals("road number ", null, r.getRoadNumber());
		Assert.assertEquals("manufacturer ", null, r.getMfg());
		Assert.assertEquals("mfg ID ", null, r.getDecoderMfgID());
		Assert.assertEquals("version ID ", null, r.getDecoderVersionID());
	}

	public void testPartialLoad() {
		// create Element
		org.jdom.Namespace ns = org.jdom.Namespace.getNamespace("roster", "");
		org.jdom.Element e = new org.jdom.Element("locomotive", ns)
									.addAttribute("fileName","file here")
									.addAttribute("roadNumber","431")
									.addAttribute("roadName","SP")
									.addAttribute("mfg","Athearn")
									.addAttribute("address","1234")
				; // end create element
				
		RosterEntry r = new RosterEntry(e, ns);
		// check
		Assert.assertEquals("file name ", "file here", r.getFileName());
		Assert.assertEquals("DCC Address ", "1234", r.getDccAddress());
		Assert.assertEquals("road name ", "SP", r.getRoadName());
		Assert.assertEquals("road number ", "431", r.getRoadNumber());
		Assert.assertEquals("manufacturer ", "Athearn", r.getMfg());
		Assert.assertEquals("mfg ID ", null, r.getDecoderMfgID());
		Assert.assertEquals("mfg ID ", null, r.getDecoderVersionID());
	}

	public void testEmptyLoad() {
		// create Element
		org.jdom.Namespace ns = org.jdom.Namespace.getNamespace("roster", "");
		org.jdom.Element e = new org.jdom.Element("locomotive", ns)
									.addAttribute("fileName","file here")
				; // end create element
				
		RosterEntry r = new RosterEntry(e, ns);
		// check
		Assert.assertEquals("file name ", "file here", r.getFileName());
		Assert.assertEquals("DCC Address ", null, r.getDccAddress());
		Assert.assertEquals("road name ", null, r.getRoadName());
		Assert.assertEquals("road number ", null, r.getRoadNumber());
		Assert.assertEquals("manufacturer ", null, r.getMfg());
		Assert.assertEquals("mfg ID ", null, r.getDecoderMfgID());
		Assert.assertEquals("version ID ", null, r.getDecoderVersionID());
	}
	
	public void testFullLoad() {
		// create Element
		org.jdom.Namespace ns = org.jdom.Namespace.getNamespace("roster", "");
		org.jdom.Element e = new org.jdom.Element("locomotive", ns)
									.addAttribute("fileName","file here")
									.addAttribute("roadNumber","431")
									.addAttribute("roadName","SP")
									.addAttribute("mfg","Athearn")
									.addAttribute("address","1234")
									.addContent(new org.jdom.Element("decoder", ns)
										.addAttribute("versionID","91")
										.addAttribute("mfgID","33")
												)
				; // end create element
				
		RosterEntry r = new RosterEntry(e, ns);
		// check
		Assert.assertEquals("file name ", "file here", r.getFileName());
		Assert.assertEquals("DCC Address ", "1234", r.getDccAddress());
		Assert.assertEquals("road name ", "SP", r.getRoadName());
		Assert.assertEquals("road number ", "431", r.getRoadNumber());
		Assert.assertEquals("manufacturer ", "Athearn", r.getMfg());
		Assert.assertEquals("mfg ID ", "33", r.getDecoderMfgID());
		Assert.assertEquals("version ID ", "91", r.getDecoderVersionID());
	}

	public void testStore() {
		// create Element
		org.jdom.Namespace ns = org.jdom.Namespace.getNamespace("roster", "");
		org.jdom.Element e = new org.jdom.Element("locomotive", ns)
									.addAttribute("fileName","file here")
									.addAttribute("roadNumber","431")
									.addAttribute("roadName","SP")
									.addAttribute("mfg","Athearn")
									.addAttribute("address","1234")
									.addContent(new org.jdom.Element("decoder", ns)
										.addAttribute("versionID","91")
										.addAttribute("mfgID","33")
												)
				; // end create element
				
		RosterEntry r = new RosterEntry(e, ns);
		org.jdom.Element o = r.store(ns);
		// check
		Assert.assertEquals("XML Element ", e.toString(), o.toString());
		Assert.assertEquals("versionID ","91", o.getChild("decoder",ns).getAttribute("mfgID").getValue());
		Assert.assertEquals("mfgID ","33", o.getChild("decoder",ns).getAttribute("versionID").getValue());
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
