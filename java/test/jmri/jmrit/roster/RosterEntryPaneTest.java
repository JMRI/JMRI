package jmri.jmrit.roster;

import junit.framework.*;

/**
 * Tests for the jmrit.roster.RosterEntryPane class.
 * @author	Bob Jacobsen     Copyright (C) 2001, 2002
 * @version	$Revision$
 */
public class RosterEntryPaneTest extends TestCase {

    // statics for test objects
    org.jdom.Element eOld = null;
    org.jdom.Element eNew = null;
    RosterEntry rOld = null;
    RosterEntry rNew = null;

    public void setUp() {
        // create Element
        eOld = new org.jdom.Element("locomotive")
            .setAttribute("id","id info")
            .setAttribute("fileName","file here")
            .setAttribute("roadNumber","431")
            .setAttribute("roadName","SP")
            .setAttribute("mfg","Athearn")
            .setAttribute("dccAddress","1234")
            .addContent(new org.jdom.Element("decoder")
                        .setAttribute("family","91")
                        .setAttribute("model","33")
                        )
            .addContent(new org.jdom.Element("locoaddress")
                        .addContent(new org.jdom.Element("number").addContent("1234"))
                        //As there is no throttle manager available all protocols default to dcc short
                        .addContent(new org.jdom.Element("protocol").addContent("dcc_short"))
                        
                        );
                        
        rOld = new RosterEntry(eOld){
                      protected void warnShortLong(String s){}
        };
        
        eNew = new org.jdom.Element("locomotive")
            .setAttribute("id","id info")
            .setAttribute("fileName","file here")
            .setAttribute("roadNumber","431")
            .setAttribute("roadName","SP")
            .setAttribute("mfg","Athearn")
            .addContent(new org.jdom.Element("decoder")
                        .setAttribute("family","91")
                        .setAttribute("model","33")
                        )

            ; // end create element

        rNew = new RosterEntry(eNew){
                       protected void warnShortLong(String s){}
        };
    }

    public void testCreate() {
        RosterEntryPane p = new RosterEntryPane(rOld);
        
        // copy to a new entry
        
        RosterEntry n = new RosterEntry(){
                            protected void warnShortLong(String s){}
        };
        p.update(n);
        
        // check for field text contents
        Assert.assertEquals("file name in pane", "file here", p.filename.getText());
        Assert.assertEquals("file name returned", null, n.getFileName());
        Assert.assertEquals("DCC Address ", "1234", n.getDccAddress());
        Assert.assertEquals("road name ", "SP", n.getRoadName());
        Assert.assertEquals("road number ", "431", n.getRoadNumber());
        Assert.assertEquals("manufacturer ", "Athearn", n.getMfg());
        Assert.assertEquals("model ", "33",n.getDecoderModel());
        Assert.assertEquals("family ", "91", n.getDecoderFamily());

    }

    public void testGuiChanged1() {
        RosterEntryPane p = new RosterEntryPane(rOld);
        
        // copy to a new entry
                
        // check for unchanged
        Assert.assertTrue("initially unchanged", !p.guiChanged(rOld));
        
        // change the roster road name entry and check
        rOld.setRoadName("changed value");
        Assert.assertTrue("detects change", p.guiChanged(rOld));

    }

    public void testGuiChanged2() {
        RosterEntryPane p = new RosterEntryPane(rOld);
        
        // copy to a new entry
                
        // check for unchanged
        Assert.assertTrue("initially unchanged", !p.guiChanged(rOld));
        
        // change the roster road name entry and check
        rOld.setDccAddress("4321");
        Assert.assertTrue("detects change", p.guiChanged(rOld));

    }

    public void testGuiChanged3() {

        RosterEntryPane p = new RosterEntryPane(rNew);
        // copy to a new entry
                
        // check for unchanged
        Assert.assertTrue("initially unchanged", !p.guiChanged(rNew));
        
        // change the roster address type entry and check
        rNew.setDccAddress("1234");
        Assert.assertTrue("detects change", p.guiChanged(rNew));

    }

    public void testGuiChanged4() {
        RosterEntryPane p = new RosterEntryPane(rNew);
        // copy to a new entry
                
        // check for unchanged
        Assert.assertTrue("initially unchanged", !p.guiChanged(rNew));
        
        // change the roster address type entry and check
        rNew.setDccAddress("4321");
        Assert.assertTrue("detects change", p.guiChanged(rNew));

    }

    public void testGuiChanged5() {
        RosterEntryPane p = new RosterEntryPane(rNew);
        // copy to a new entry
                
        // check for unchanged
        Assert.assertTrue("initially unchanged", !p.guiChanged(rNew));
        
        // change the roster address type entry and check
        rNew.setDccAddress("12");
        p.setDccAddressLong(false);
        Assert.assertTrue("detects change", p.guiChanged(rNew));

    }

    public void testNotDuplicate() {
        RosterEntryPane p = new RosterEntryPane(rNew);
        // reset Roster
        Roster.resetInstance();
        Assert.assertTrue(!p.checkDuplicate());
    }
    
    public void testIsDuplicate() {
        RosterEntryPane p = new RosterEntryPane(rNew);
        // reset Roster
        Roster.resetInstance();
        Roster.instance().addEntry(rNew);
        
        Assert.assertTrue(!p.checkDuplicate());
    }
    
    public void testRenamedDuplicate() {
        RosterEntryPane p = new RosterEntryPane(rOld);
        // reset Roster
        Roster.resetInstance();
        Roster.instance().addEntry(rNew);
        
        // reset entry
        p.id.setText("new id");
        p.update(rNew);
        
        Assert.assertTrue(p.checkDuplicate());
    }
    
    // from here down is testing infrastructure

    public RosterEntryPaneTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {RosterEntryPaneTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(RosterEntryPaneTest.class);
        return suite;
    }

}
