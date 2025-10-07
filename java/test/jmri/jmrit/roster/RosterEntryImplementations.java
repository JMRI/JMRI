package jmri.jmrit.roster;

import org.jdom2.Element;

/**
 * Static Roster Entries for use in Testing.
 * Implementations originally from jmri.jmrit.roster.swing.RosterTableModelTest
 * @author Bob Jacobsen Copyright (C) 2009
 * @author Steve Young Copyright (C) 2022
 */
public class RosterEntryImplementations {

    // class only provides static methods
    private RosterEntryImplementations(){}

    public static RosterEntry id1() {
        Element e = new Element("locomotive")
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
        return new NoWarnRosterEntry(e);
    }

    public static RosterEntry id2() {
        Element e = new Element("locomotive")
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
        return new NoWarnRosterEntry(e);
    }

    public static RosterEntry id3() {
        Element e = new Element("locomotive")
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
        return new NoWarnRosterEntry(e);
    }

    public static RosterEntry id4() {
        Element e = new Element("locomotive")
            .setAttribute("id", "id 4")
            .setAttribute("fileName", "file here")
            .setAttribute("roadNumber", "9000")
            .setAttribute("roadName", "CNR")
            .setAttribute("mfg", "Athearn")
            .addContent(new org.jdom2.Element("decoder")
                    .setAttribute("family", "91")
                    .setAttribute("model", "35")
            )
            .addContent(new org.jdom2.Element("locoaddress")
                    .addContent(new org.jdom2.Element("dcclocoaddress")
                            .setAttribute("number", "9000")
                            .setAttribute("longaddress", "yes")
                    )
            ); // end create element
        return new NoWarnRosterEntry(e);
    }

    public static class NoWarnRosterEntry extends RosterEntry {

        public NoWarnRosterEntry( Element e){
            super(e);
        }

        @Override
        protected void warnShortLong(String s) {
        }

        @Override
        public void updateFile() {
        }

    }

}
