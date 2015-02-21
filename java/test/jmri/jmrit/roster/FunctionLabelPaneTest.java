package jmri.jmrit.roster;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrit.roster.FunctionLabelPane class.
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 * @version	$Revision$
 */
public class FunctionLabelPaneTest extends TestCase {

    // statics for test objects
    org.jdom2.Element eOld = null;
    org.jdom2.Element eNew = null;
    RosterEntry rOld = null;
    RosterEntry rNew = null;

    public void setUp() {
        // log4J
        apps.tests.Log4JFixture.setUp();

        // create Element
        eOld = new org.jdom2.Element("locomotive")
                .setAttribute("id", "id info")
                .setAttribute("fileName", "file here")
                .setAttribute("roadNumber", "431")
                .setAttribute("roadName", "SP")
                .setAttribute("mfg", "Athearn")
                .setAttribute("dccAddress", "1234")
                .addContent(new org.jdom2.Element("decoder")
                        .setAttribute("family", "91")
                        .setAttribute("model", "33")
                ); // end create element

        rOld = new RosterEntry(eOld) {
            protected void warnShortLong(String s) {
            }
        };

        eNew = new org.jdom2.Element("locomotive")
                .setAttribute("id", "id info")
                .setAttribute("fileName", "file here")
                .setAttribute("roadNumber", "431")
                .setAttribute("roadName", "SP")
                .setAttribute("mfg", "Athearn")
                .addContent(new org.jdom2.Element("decoder")
                        .setAttribute("family", "91")
                        .setAttribute("model", "33")
                )
                .addContent(new org.jdom2.Element("locoaddress")
                        .addContent(new org.jdom2.Element("dcclocoaddress")
                                .setAttribute("number", "12")
                                .setAttribute("longaddress", "yes")
                        )
                ); // end create element

        rNew = new RosterEntry(eNew) {
            protected void warnShortLong(String s) {
            }
        };
    }

    public void testShow() {
        FunctionLabelPane p = new FunctionLabelPane(rOld);

        jmri.util.JmriJFrame j = new jmri.util.JmriJFrame("FunctionLabelPaneTest");
        j.add(p);
        j.pack();
        j.setVisible(true);

        // Now close
        j.dispose();

    }

    public void testGuiChanged1() {
        FunctionLabelPane p = new FunctionLabelPane(rOld);

        // copy to a new entry
        // check for unchanged
        Assert.assertTrue("initially unchanged", !p.guiChanged(rOld));

        // change the entry and check
        rOld.setFunctionLabel(14, "changed value");
        Assert.assertTrue("detects change", p.guiChanged(rOld));

    }

    public void testGuiChanged2() {
        FunctionLabelPane p = new FunctionLabelPane(rOld);

        // copy to a new entry
        // check for unchanged
        Assert.assertTrue("initially unchanged", !p.guiChanged(rOld));

        // change the roster road name entry and check
        rOld.setFunctionLockable(14, false);
        Assert.assertTrue("detects change", p.guiChanged(rOld));

    }

    // from here down is testing infrastructure
    public FunctionLabelPaneTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {FunctionLabelPaneTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(FunctionLabelPaneTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
}
