
package jmri.jmrit.symbolicprog;

import javax.swing.*;

import jmri.managers.DefaultProgrammerManager;
import junit.framework.*;

// Revision: $Revision$

public class CombinedLocoSelListPaneTest extends TestCase {

  public CombinedLocoSelListPaneTest(String s) {
    super(s);
  }

  public void testIsDecoderSelected() {
    jmri.InstanceManager.setProgrammerManager(new DefaultProgrammerManager(new jmri.progdebugger.ProgDebugger()));
    JLabel val1=  new JLabel();
    // ensure a valid DecoderIndexFile
    jmri.jmrit.decoderdefn.DecoderIndexFile.resetInstance();
    CombinedLocoSelListPane combinedlocosellistpane = new CombinedLocoSelListPane(val1);
    Assert.assertEquals("initial state", false, combinedlocosellistpane.isDecoderSelected());
    combinedlocosellistpane.mDecoderList.setSelectedIndex(1);
    Assert.assertEquals("after update", true, combinedlocosellistpane.isDecoderSelected());
  }

  public void testSelectedDecoderType() {
    JLabel val1=  new JLabel();
    jmri.InstanceManager.setProgrammerManager(new DefaultProgrammerManager(new jmri.progdebugger.ProgDebugger()));
    // ensure a valid DecoderIndexFile
    jmri.jmrit.decoderdefn.DecoderIndexFile.resetInstance();

    CombinedLocoSelListPane combinedlocosellistpane = new CombinedLocoSelListPane(val1);
    combinedlocosellistpane.mDecoderList.setSelectedIndex(2);
    Assert.assertEquals("after update", true, combinedlocosellistpane.isDecoderSelected());
    String stringRet = combinedlocosellistpane.selectedDecoderType();
    Assert.assertEquals("selected item", "NMRA standard register definitions (NMRA standard register definitions)",
                    stringRet);
  }

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", CombinedLocoSelListPaneTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(CombinedLocoSelListPaneTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() throws Exception { 
        super.setUp();
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
