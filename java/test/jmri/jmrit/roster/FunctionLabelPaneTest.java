package jmri.jmrit.roster;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmrit.roster.FunctionLabelPane class.
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 */
public class FunctionLabelPaneTest {

    // statics for test objects
    org.jdom2.Element eOld = null;
    org.jdom2.Element eNew = null;
    RosterEntry rOld = null;
    RosterEntry rNew = null;

    @Test
    public void testShow() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        FunctionLabelPane p = new FunctionLabelPane(rOld);

        jmri.util.JmriJFrame j = new jmri.util.JmriJFrame("FunctionLabelPaneTest");
        j.add(p);
        j.pack();
        j.setVisible(true);

        // Now close
        JUnitUtil.dispose(j);

    }

    @Test
    public void testGuiChanged1() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        FunctionLabelPane p = new FunctionLabelPane(rOld);

        // copy to a new entry
        // check for unchanged
        Assert.assertTrue("initially unchanged", !p.guiChanged(rOld));

        // change the entry and check
        rOld.setFunctionLabel(14, "changed value");
        Assert.assertTrue("detects change", p.guiChanged(rOld));

    }

    @Test
    public void testGuiChanged2() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        FunctionLabelPane p = new FunctionLabelPane(rOld);

        // copy to a new entry
        // check for unchanged
        Assert.assertTrue("initially unchanged", !p.guiChanged(rOld));

        // change the roster road name entry and check
        rOld.setFunctionLockable(14, false);
        Assert.assertTrue("detects change", p.guiChanged(rOld));

    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();

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
            @Override
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
            @Override
            protected void warnShortLong(String s) {
            }
        };
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
