package jmri.jmrit.roster.swing;

import java.awt.GraphicsEnvironment;

import jmri.*;
import jmri.jmrit.roster.*;
import jmri.util.*;
import jmri.util.junit.rules.RetryRule;

import org.junit.*;

/**
 * Test simple functioning of RosterFrame
 *
 * @author	Paul Bender Copyright (C) 2015, 2016
 */
public class RosterFrameTest {

    @Rule
    public RetryRule retryRule = new RetryRule(3);  // allow 3 retries

    @Rule // This test class was periodically stalling and causing the CI run to time out. Limit its duration.
    public org.junit.rules.Timeout globalTimeout = org.junit.rules.Timeout.seconds(20);

    private RosterFrame frame = null;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("exists", frame);
    }

    @Test
    public void testIdentifyEnabled() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        frame.setVisible(true);
        RosterFrameScaffold operator = new RosterFrameScaffold(frame.getTitle());
        Assert.assertTrue("Identify Button Enabled", operator.isIdentifyButtonEnabled());
    }

    @Test
    public void testIdentify3NotPresent() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        frame.pack();
        frame.setVisible(true);
        RosterFrameScaffold operator = new RosterFrameScaffold(frame.getTitle());
        
        // set some CV values
        jmri.progdebugger.ProgDebugger prog = (jmri.progdebugger.ProgDebugger)InstanceManager.getDefault(GlobalProgrammerManager.class).getGlobalProgrammer();
        prog.resetCv(1, 3);
        prog.resetCv(29, 0);
                
        operator.pushIdentifyButton();
        // and wait for message that will come at end because nothing is found
        JUnitUtil.waitFor(() ->{
                return JUnitAppender.checkForMessage("Read address 3, but no such loco in roster") != null;
            }, "error message at end");
            
    }
    
    @Test
    public void testIdentify3Present() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // add entry to Roster
        Roster roster = Roster.getDefault();
        RosterEntry re1 = new RosterEntry();
        re1.setId("1st entry");
        re1.setDccAddress("3");
        roster.addEntry(re1);
        RosterEntry re2 = new RosterEntry();
        re2.setId("2nd entry");
        re2.setDccAddress("4");
        roster.addEntry(re2);
        RosterEntry re3 = new RosterEntry();
        re3.setId("3rd entry");
        re3.setDccAddress("5");
        roster.addEntry(re3);
        
        frame.setVisible(true);
        frame.pack();
        RosterFrameScaffold operator = new RosterFrameScaffold(frame.getTitle());
        
        // set some CV values
        jmri.progdebugger.ProgDebugger prog = (jmri.progdebugger.ProgDebugger)InstanceManager.getDefault(GlobalProgrammerManager.class).getGlobalProgrammer();
        prog.resetCv(1, 3);
        prog.resetCv(29, 0);
                
        operator.pushIdentifyButton();
        
        JUnitUtil.waitFor(() ->{
            return frame.getSelectedRosterEntries().length == 1;
        }, "selection complete");
        RosterEntry[] selected = frame.getSelectedRosterEntries();
        Assert.assertEquals("selected ", 1, selected.length);
        Assert.assertEquals("selected ", re1, selected[0]);
        
    }

    @Test
    public void testIdentify3WithDecoderTypeMismatch() {
        // match on address if unique, even if decoder type not right
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // add entry to Roster
        Roster roster = Roster.getDefault();
        RosterEntry re1 = new RosterEntry();
        re1.setId("1st entry is 3; mismatched types accepted");
        re1.setDccAddress("3");
        re1.setDecoderModel("Four Function Dual Mode");  // CV8 = 127 Atlas, CV7 = 46
        re1.setDecoderFamily("Four Function Dual Mode"); 
        roster.addEntry(re1);
        RosterEntry re2 = new RosterEntry();
        re2.setId("2nd entry");
        re2.setDccAddress("4");
        roster.addEntry(re2);
        RosterEntry re3 = new RosterEntry();
        re3.setId("3rd entry");
        re3.setDccAddress("5");
        roster.addEntry(re3);
        
        frame.setVisible(true);
        frame.pack();
        RosterFrameScaffold operator = new RosterFrameScaffold(frame.getTitle());
        
        // set some CV values
        jmri.progdebugger.ProgDebugger prog = (jmri.progdebugger.ProgDebugger)InstanceManager.getDefault(GlobalProgrammerManager.class).getGlobalProgrammer();
        prog.resetCv(1, 3);
        prog.resetCv(29, 0);
        prog.resetCv(7, 45); // Dual Mode (not Four Function Dual Mode)
        prog.resetCv(8, 127); // Atlas
                
        operator.pushIdentifyButton();
        // and wait for message that will come at end:
        //JUnitUtil.waitFor(() ->{
        //        return JUnitAppender.checkForMessage("Read address 3, but no such loco in roster") != null;
        //    }, "error message at end");

        JUnitUtil.waitFor(() ->{
                return frame.getSelectedRosterEntries().length == 1;
            }, "selection complete");
        RosterEntry[] selected = frame.getSelectedRosterEntries();
        Assert.assertEquals("selected ", re1, selected[0]);
	    Assert.assertTrue("entry selected",frame.checkIfEntrySelected());
        
    }

    @Test
    @Ignore("RosterFrame doesn't do multiple selection properly yet")
    public void testIdentify3Multiple() {
    
        // this is a test of what happens when multiples are selectable
        
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // add entry to Roster
        Roster roster = Roster.getDefault();
        RosterEntry re1 = new RosterEntry();
        re1.setId("1st entry is 3");
        re1.setDccAddress("3");
        roster.addEntry(re1);
        RosterEntry re2 = new RosterEntry();
        re2.setId("2nd entry is not 3");
        re2.setDccAddress("4");
        roster.addEntry(re2);
        RosterEntry re3 = new RosterEntry();
        re3.setId("3rd entry is 3");
        re3.setDccAddress("3");
        roster.addEntry(re3);
        
        frame.setVisible(true);
        frame.pack();
        RosterFrameScaffold operator = new RosterFrameScaffold(frame.getTitle());
        
        // set some CV values
        jmri.progdebugger.ProgDebugger prog = (jmri.progdebugger.ProgDebugger)InstanceManager.getDefault(GlobalProgrammerManager.class).getGlobalProgrammer();
        prog.resetCv(1, 3);
        prog.resetCv(29, 0);
                 
        operator.pushIdentifyButton();

        // right now, nothing is ever selected, because multiple selection 
        // is not working.  See @Ignore above

        JUnitUtil.waitFor(() ->{
                return frame.getSelectedRosterEntries().length == 2;
            }, "selection complete");
        RosterEntry[] selected = frame.getSelectedRosterEntries();
        Assert.assertEquals("selected ", re1, selected[0]);
        Assert.assertEquals("selected ", re3, selected[1]);
	    Assert.assertTrue("entry selected",frame.checkIfEntrySelected());
                
    }

    @Test
    public void testIdentify3ViaDecoderId() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // add entry to Roster
        Roster roster = Roster.getDefault();
        RosterEntry re1 = new RosterEntry();
        re1.setId("1st entry is 3 Four Function Dual Mode");
        re1.setDccAddress("3");
        re1.setDecoderModel("Four Function Dual Mode");  // CV8 = 127 Atlas, CV7 = 46
        re1.setDecoderFamily("Four Function Dual Mode"); 
        roster.addEntry(re1);
        RosterEntry re2 = new RosterEntry();
        re2.setId("2nd entry in not 3");
        re2.setDccAddress("4");
        roster.addEntry(re2);
        RosterEntry re3 = new RosterEntry();
        re3.setId("3rd entry is 3 Dual Mode");
        re3.setDccAddress("3");
        re3.setDecoderModel("Dual Mode");  // CV8 = 127 Atlas, CV7 = 45
        re3.setDecoderFamily("Dual Mode"); 
        roster.addEntry(re3);
        
        frame.setVisible(true);
        frame.pack();
        RosterFrameScaffold operator = new RosterFrameScaffold(frame.getTitle());
        
        // set some CV values
        jmri.progdebugger.ProgDebugger prog = (jmri.progdebugger.ProgDebugger)InstanceManager.getDefault(GlobalProgrammerManager.class).getGlobalProgrammer();
        prog.resetCv(1, 3);
        prog.resetCv(29, 0);
        prog.resetCv(7, 45); // Dual Mode (not Four Function Dual Mode)
        prog.resetCv(8, 127); // Atlas
                
        operator.pushIdentifyButton();
        
        JUnitUtil.waitFor(() ->{
                return frame.getSelectedRosterEntries().length == 1;
            }, "selection complete");
        RosterEntry[] selected = frame.getSelectedRosterEntries();
        Assert.assertEquals("selected ", 1, selected.length);
        Assert.assertEquals("selected ", re3, selected[0]);  // 2nd address=3 selected by decoder match
	    Assert.assertTrue("entry selected",frame.checkIfEntrySelected());
        
    }

    @Test
    @Ignore("does not find and close dialog as expected")
    public void testCheckIfEntrySelected() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        frame.setVisible(true);
        RosterFrameScaffold operator = new RosterFrameScaffold(frame.getTitle());
        Thread t = new Thread(() -> {
	        jmri.util.swing.JemmyUtil.confirmJOptionPane(operator,"Message","","OK");
        });
        t.setName("Error Dialog Close Thread");
        t.start();
        // the return true case happens in the identify methods above, so
	    // we only check the return false case here.
	    Assert.assertFalse("entry not selected",frame.checkIfEntrySelected());
    }

    @Test
    public void testGetandSetAllowQuit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        frame.setVisible(true);
	    frame.allowQuit(false);
	    Assert.assertFalse("Quit Not Allowed",frame.isAllowQuit());
	    frame.allowQuit(true);
	    Assert.assertTrue("Quit Allowed",frame.isAllowQuit());
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initDefaultUserMessagePreferences();
        JUnitUtil.initGuiLafPreferencesManager();
        jmri.InstanceManager.setDefault(jmri.jmrix.ConnectionConfigManager.class, new jmri.jmrix.ConnectionConfigManager());
        jmri.InstanceManager.setDefault(jmri.jmrit.symbolicprog.ProgrammerConfigManager.class, new jmri.jmrit.symbolicprog.ProgrammerConfigManager());
        JUnitUtil.initDebugProgrammerManager();
        JUnitUtil.initRosterConfigManager();
        Roster.getDefault(); // ensure exists
        if(!GraphicsEnvironment.isHeadless()){
           frame = new RosterFrame();
	    }
    }

    @After
    public void tearDown() {
	    if(frame!=null) {
           JUnitUtil.dispose(frame);
	    }
        JUnitUtil.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RosterFrameTest.class);
}
