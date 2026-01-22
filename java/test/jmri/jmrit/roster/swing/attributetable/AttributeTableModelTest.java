package jmri.jmrit.roster.swing.attributetable;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.JUnitUtil;

import org.jdom2.Element;

import org.junit.jupiter.api.*;

/**
 * Tests for the roster.swing.attributetable.AttributeTableModel class.
 *
 * @author Bob Jacobsen Copyright (C) 2009
 */
public class AttributeTableModelTest {

    @Test
    public void testTableLength() {
        AttributeTableModel t = new AttributeTableModel();

        assertEquals(NENTRIES, t.getRowCount());
    }

    @Test
    public void testTableWidth() {
        AttributeTableModel t = new AttributeTableModel();

        assertEquals(NKEYS, t.getColumnCount());
    }

    @Test
    public void testColumnName() {
        AttributeTableModel t = new AttributeTableModel();

        assertEquals("key b", t.getColumnName(1));
    }

    @Test
    public void testGetValueAt() {
        AttributeTableModel t = new AttributeTableModel();

        assertEquals("value 1", t.getValueAt(0, 0));
        assertEquals("", t.getValueAt(0, 1));
        assertEquals("", t.getValueAt(0, 2));
        assertEquals("", t.getValueAt(0, 3));
        assertEquals("value 11", t.getValueAt(1, 0));
        assertEquals("value 12", t.getValueAt(1, 1));
        assertEquals("value 13", t.getValueAt(1, 2));
        assertEquals("value 14", t.getValueAt(1, 3));
        assertEquals("", t.getValueAt(2, 0));
        assertEquals("", t.getValueAt(2, 1));
        assertEquals("", t.getValueAt(2, 2));
        assertEquals("", t.getValueAt(2, 3));
    }

    // create a standard test roster
    private final static int NENTRIES = 3;
    private final static int NKEYS = 4;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.resetProfileManager();
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

    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
