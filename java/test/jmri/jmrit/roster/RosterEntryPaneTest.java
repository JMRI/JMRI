package jmri.jmrit.roster;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the jmrit.roster.RosterEntryPane class.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2002
 */
public class RosterEntryPaneTest {

    // statics for test objects
    org.jdom2.Element eOld = null;
    org.jdom2.Element eNew = null;
    RosterEntry rOld = null;
    RosterEntry rNew = null;

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
                )
                .addContent(new org.jdom2.Element("locoaddress")
                        .addContent(new org.jdom2.Element("number").addContent("1234"))
                        //As there is no throttle manager available all protocols default to dcc short
                        .addContent(new org.jdom2.Element("protocol").addContent("dcc_short"))
                );

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

    @Test
    public void testCreate() {
        RosterEntryPane p = new RosterEntryPane(rOld);

        // copy to a new entry
        RosterEntry n = new RosterEntry() {
            @Override
            protected void warnShortLong(String s) {
            }
        };
        p.update(n);

        // check for field text contents
        Assert.assertEquals("file name returned", null, n.getFileName());
        Assert.assertEquals("DCC Address ", "1234", n.getDccAddress());
        Assert.assertEquals("road name ", "SP", n.getRoadName());
        Assert.assertEquals("road number ", "431", n.getRoadNumber());
        Assert.assertEquals("manufacturer ", "Athearn", n.getMfg());
        Assert.assertEquals("model ", "33", n.getDecoderModel());
        Assert.assertEquals("family ", "91", n.getDecoderFamily());

    }

    @Test
    public void testGuiChanged1() {
        RosterEntryPane p = new RosterEntryPane(rOld);

        // copy to a new entry
        // check for unchanged
        Assert.assertTrue("initially unchanged", !p.guiChanged(rOld));

        // change the roster road name entry and check
        rOld.setRoadName("changed value");
        Assert.assertTrue("detects change", p.guiChanged(rOld));

    }

    @Test
    public void testGuiChanged2() {
        RosterEntryPane p = new RosterEntryPane(rOld);

        // copy to a new entry
        // check for unchanged
        Assert.assertTrue("initially unchanged", !p.guiChanged(rOld));

        // change the roster road name entry and check
        rOld.setDccAddress("4321");
        Assert.assertTrue("detects change", p.guiChanged(rOld));

    }

    @Test
    public void testGuiChanged3() {

        RosterEntryPane p = new RosterEntryPane(rNew);
        // copy to a new entry

        // check for unchanged
        Assert.assertTrue("initially unchanged", !p.guiChanged(rNew));

        // change the roster address type entry and check
        rNew.setDccAddress("1234");
        Assert.assertTrue("detects change", p.guiChanged(rNew));

    }

    @Test
    public void testGuiChanged4() {
        RosterEntryPane p = new RosterEntryPane(rNew);
        // copy to a new entry

        // check for unchanged
        Assert.assertTrue("initially unchanged", !p.guiChanged(rNew));

        // change the roster address type entry and check
        rNew.setDccAddress("4321");
        Assert.assertTrue("detects change", p.guiChanged(rNew));

    }

    @Test
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

    @Test
    public void testNotDuplicate() {
        RosterEntryPane p = new RosterEntryPane(rNew);
        // reset Roster
        InstanceManager.reset(Roster.class);
        InstanceManager.setDefault(Roster.class, new Roster(null));
        Assert.assertTrue(!p.checkDuplicate());
    }

    @Test
    public void testIsDuplicate() {
        RosterEntryPane p = new RosterEntryPane(rNew);
        // reset Roster
        InstanceManager.reset(Roster.class);
        InstanceManager.setDefault(Roster.class, new Roster(null));
        Roster.getDefault().addEntry(rNew);

        Assert.assertTrue(!p.checkDuplicate());
    }

    @Test
    public void testRenamedDuplicate() {
        RosterEntryPane p = new RosterEntryPane(rOld);
        // reset Roster
        InstanceManager.reset(Roster.class);
        InstanceManager.setDefault(Roster.class, new Roster(null));
        Roster.getDefault().addEntry(rNew);

        // reset entry
        p.id.setText("new id");
        p.update(rNew);

        Assert.assertTrue(p.checkDuplicate());
    }

}
