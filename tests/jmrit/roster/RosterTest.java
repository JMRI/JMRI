package jmri.jmrit.roster;

import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.*;

/** 
 * RosterTest.java
 *
 * Description:	    tests for the jmrit.roster package & jmrit.roster.Roster class
 * @author			Bob Jacobsen
 * @version			
 */
public class RosterTest extends TestCase {

	public void testDirty() {
		Roster r = new Roster();
		Assert.assertEquals("new object ", false, r.isDirty());
		r.addEntry(null);
		Assert.assertEquals("after add ", true, r.isDirty());
	}
	
	public void testAdd() {
		Roster r = new Roster();
		Assert.assertEquals("empty length ", 0, r.numEntries());
		r.addEntry(new RosterEntry("file name Bob"));
		Assert.assertEquals("one item ", 1, r.numEntries());
	}

	public void testAddrSearch() {
		Roster r = new Roster();
		RosterEntry e = new RosterEntry("file name Bob");
		e.setRoadNumber("123");
		r.addEntry(e);
		Assert.assertEquals("search not OK ", false, r.checkEntry(0, null, "321", null, null, null, null));
		Assert.assertEquals("search OK ", true, r.checkEntry(0, null, "123", null, null, null, null));
	}
		
	public void testSearchList() {
		Roster r = new Roster();
		RosterEntry e;
		e = new RosterEntry("file name Bob");
		e.setRoadNumber("123");
		e.setRoadName("SP");
		r.addEntry(e);
		e = new RosterEntry("file name Bill");
		e.setRoadNumber("123");
		e.setRoadName("ATSF");
		e.setDecoderVersionID("81");
		e.setDecoderMfgID("33");
		r.addEntry(e);
		e = new RosterEntry("file name Ben");
		e.setRoadNumber("123");
		e.setRoadName("UP");
		r.addEntry(e);
		Assert.assertEquals("search for 0 ", 0, r.matchingList(null, "321", null, null, null, null).size());
		Assert.assertEquals("search for 1 ", 1, r.matchingList("UP", null,  null, null, null, null).size());
		Assert.assertEquals("search for 3 ", 3, r.matchingList(null, "123", null, null, null, null).size());
	}
	
	public void testBackupFile() throws Exception {
		// create a file in "temp"
		Roster.fileLocation = "temp";
		File f = new File("temp"+File.separator+"roster.xml");
		// remove it if its there
		f.delete();
		// load a new one
		String contents = "stuff"+"           ";
		PrintStream p = new PrintStream (new FileOutputStream(f));
		p.println(contents);
		
		// now do the backup
		Roster r = new Roster() {
				protected File backupFileName(String name)
						{ return new File("temp"+File.separator+"rosterBackupTest"); }
				};
		r.makeBackupFile();
		
		// and check
		InputStream in = new FileInputStream(new File("temp"+File.separator+"rosterBackupTest"));
		Assert.assertEquals("read 0 ", contents.charAt(0), in.read());
		Assert.assertEquals("read 1 ", contents.charAt(1), in.read());
		Assert.assertEquals("read 2 ", contents.charAt(2), in.read());
		Assert.assertEquals("read 3 ", contents.charAt(3), in.read());
	}

	public void testReadWrite() throws Exception {
		// store files in "temp"
		Roster.fileLocation = "temp";
		File f = new File("temp"+File.separator+"roster.xml");
		// remove existing roster if its there
		f.delete();

		// create a roster
		Roster r = new Roster();
		RosterEntry e;
		e = new RosterEntry("file name Bob");
		e.setRoadNumber("123");
		e.setRoadName("SP");
		r.addEntry(e);
		e = new RosterEntry("file name Bill");
		e.setRoadNumber("123");
		e.setRoadName("ATSF");
		e.setDecoderVersionID("81");
		e.setDecoderMfgID("33");
		r.addEntry(e);
		e = new RosterEntry("file name Ben");
		e.setRoadNumber("123");
		e.setRoadName("UP");
		r.addEntry(e);

		// write it
		r.writeFile("temp"+File.separator+"roster.xml");
		
		// create new roster & read
		Roster t = new Roster();
		t.readFile("temp"+File.separator+"roster.xml");
		System.out.println(" 0 "+t._list.get(0));
		System.out.println(" 1 "+t._list.get(1));
		System.out.println(" 2 "+t._list.get(2));
		
		// check contents
		Assert.assertEquals("search for 0 ", 0, t.matchingList(null, "321", null, null, null, null).size());
		Assert.assertEquals("search for 1 ", 1, t.matchingList("UP", null,  null, null, null, null).size());
		Assert.assertEquals("search for 3 ", 3, t.matchingList(null, "123", null, null, null, null).size());
	}

	
	// from here down is testing infrastructure
	
	public RosterTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {Roster.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(RosterTest.class);
		suite.addTest(jmri.jmrit.roster.IdentifyLocoTest.suite());
		suite.addTest(jmri.jmrit.roster.RosterEntryTest.suite());
		return suite;
	}
	
}
