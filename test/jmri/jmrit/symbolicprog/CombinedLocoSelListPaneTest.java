
package jmri.jmrit.symbolicprog;

import junit.framework.*;
import javax.swing.*;
import javax.swing.event.*;

// Revision: $Revision: 1.1 $

public class CombinedLocoSelListPaneTest extends TestCase {

  public CombinedLocoSelListPaneTest(String s) {
    super(s);
  }

  public void testIsDecoderSelected() {
    JLabel val1=  new JLabel();
    CombinedLocoSelListPane combinedlocosellistpane = new CombinedLocoSelListPane(val1);
    Assert.assertEquals("initial state", false, combinedlocosellistpane.isDecoderSelected());
    combinedlocosellistpane.mDecoderList.setSelectedIndex(1);
    Assert.assertEquals("after update", true, combinedlocosellistpane.isDecoderSelected());
  }

  public void testResetDecoderSelection() {
    JLabel val1=  new JLabel();
    CombinedLocoSelListPane combinedlocosellistpane = new CombinedLocoSelListPane(val1);
    combinedlocosellistpane.mDecoderList.setSelectedIndex(1);
    Assert.assertEquals("after update", true, combinedlocosellistpane.isDecoderSelected());
    combinedlocosellistpane.resetDecoderSelection();
    Assert.assertEquals("state after reset", false, combinedlocosellistpane.isDecoderSelected());
  }
  public void testSelectedDecoderType() {
    JLabel val1=  null;
    CombinedLocoSelListPane combinedlocosellistpane = new CombinedLocoSelListPane(val1);
    combinedlocosellistpane.mDecoderList.setSelectedIndex(1);
    String stringRet = combinedlocosellistpane.selectedDecoderType();
    Assert.assertEquals("selected item", "NMRA standard register definitions", stringRet);
  }

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {VariableTableModelTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(CombinedLocoSelListPaneTest.class);
		return suite;
	}
    // The minimal setup for log4J
    apps.tests.Log4JFixture log4jfixtureInst = new apps.tests.Log4JFixture(this);
    protected void setUp() { log4jfixtureInst.setUp(); }
    protected void tearDown() { log4jfixtureInst.tearDown(); }
}
