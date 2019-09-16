package jmri.jmrit.roster.swing;

import jmri.InstanceManager;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.JUnitUtil;
import org.jdom2.Element;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the roster.swing.RosterTableModel class.
 *
 * @author	Bob Jacobsen Copyright (C) 2009
 */
public class RosterTableModelTest {

    @Test
    public void testTableLength() throws Exception {
        RosterTableModel t = new RosterTableModel();

        Assert.assertEquals(NENTRIES, t.getRowCount());
    }

    @Test
    public void testTableWidth() throws Exception {
        RosterTableModel t = new RosterTableModel();

        // hard-coded value is number of columns expected
        Assert.assertEquals(t.getColumnCount(), t.getColumnCount());
    }

    @Test
    public void testColumnName() throws Exception {
        RosterTableModel t = new RosterTableModel();

        Assert.assertEquals("DCC Address", t.getColumnName(RosterTableModel.ADDRESSCOL));
    }

    @Test
    public void testGetValueAt() {
        RosterTableModel t = new RosterTableModel();

        Assert.assertEquals("id 1", t.getValueAt(0, RosterTableModel.IDCOL));
        Assert.assertEquals(12, t.getValueAt(0, RosterTableModel.ADDRESSCOL));
        Assert.assertEquals("33", t.getValueAt(0, RosterTableModel.DECODERCOL));

        Assert.assertEquals("id 2", t.getValueAt(1, RosterTableModel.IDCOL));
        Assert.assertEquals(13, t.getValueAt(1, RosterTableModel.ADDRESSCOL));
        Assert.assertEquals("34", t.getValueAt(1, RosterTableModel.DECODERCOL));

        Assert.assertEquals("id 3", t.getValueAt(2, RosterTableModel.IDCOL));
        Assert.assertEquals(14, t.getValueAt(2, RosterTableModel.ADDRESSCOL));
        Assert.assertEquals("35", t.getValueAt(2, RosterTableModel.DECODERCOL));
    }

    // create a standard test roster
    static int NENTRIES = 3;
    static int NKEYS = 4;

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();

        jmri.util.JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();

        // first entry
        Element e;
        RosterEntry r;

        e = new org.jdom2.Element("locomotive")
                .setAttribute("id", "id 1")
                .setAttribute("fileName", "file here")
                .setAttribute("roadNumber", "431")
                .setAttribute("roadName", "SP")
                .setAttribute("mfg", "Athearn")
                .setAttribute("dccAddress", "1234")
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

        r = new RosterEntry(e) {
            @Override
            protected void warnShortLong(String s) {
            }
        };
        Roster.getDefault().addEntry(r);
        r.putAttribute("key a", "value 1");

        e = new org.jdom2.Element("locomotive")
                .setAttribute("id", "id 2")
                .setAttribute("fileName", "file here")
                .setAttribute("roadNumber", "431")
                .setAttribute("roadName", "SP")
                .setAttribute("mfg", "Athearn")
                .addContent(new org.jdom2.Element("decoder")
                        .setAttribute("family", "91")
                        .setAttribute("model", "34")
                )
                .addContent(new org.jdom2.Element("locoaddress")
                        .addContent(new org.jdom2.Element("dcclocoaddress")
                                .setAttribute("number", "13")
                                .setAttribute("longaddress", "yes")
                        )
                ); // end create element

        r = new RosterEntry(e) {
            @Override
            protected void warnShortLong(String s) {
            }
        };
        Roster.getDefault().addEntry(r);
        r.putAttribute("key a", "value 11");
        r.putAttribute("key b", "value 12");
        r.putAttribute("key c", "value 13");
        r.putAttribute("key d", "value 14");

        e = new org.jdom2.Element("locomotive")
                .setAttribute("id", "id 3")
                .setAttribute("fileName", "file here")
                .setAttribute("roadNumber", "431")
                .setAttribute("roadName", "SP")
                .setAttribute("mfg", "Athearn")
                .addContent(new org.jdom2.Element("decoder")
                        .setAttribute("family", "91")
                        .setAttribute("model", "35")
                )
                .addContent(new org.jdom2.Element("locoaddress")
                        .addContent(new org.jdom2.Element("dcclocoaddress")
                                .setAttribute("number", "14")
                                .setAttribute("longaddress", "yes")
                        )
                ); // end create element

        r = new RosterEntry(e) {
            @Override
            protected void warnShortLong(String s) {
            }
        };
        Roster.getDefault().addEntry(r);

    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
