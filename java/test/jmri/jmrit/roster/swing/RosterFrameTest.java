package jmri.jmrit.roster.swing;

import java.awt.GraphicsEnvironment;

import jmri.*;
import jmri.jmrit.roster.*;
import jmri.util.*;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of RosterFrame
 *
 * @author	Paul Bender Copyright (C) 2015,2016
 */
public class RosterFrameTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        RosterFrame frame = new RosterFrame();
        Assert.assertNotNull("exists", frame);
        JUnitUtil.dispose(frame);
    }

    @Test
    public void testIdentifyEnabled() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        RosterFrame frame = new RosterFrame();
        frame.setVisible(true);
        RosterFrameScaffold operator = new RosterFrameScaffold(frame.getTitle());
        Assert.assertTrue("Identify Button Enabled", operator.isIdentifyButtonEnabled());
        JUnitUtil.dispose(frame);
    }

    @Test
    public void testIdentify3NotPresent() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        RosterFrame frame = new RosterFrame();
        frame.pack();
        frame.setVisible(true);
        RosterFrameScaffold operator = new RosterFrameScaffold(frame.getTitle());
        
        // set some CV values
        jmri.progdebugger.ProgDebugger prog = (jmri.progdebugger.ProgDebugger)InstanceManager.getDefault(GlobalProgrammerManager.class).getGlobalProgrammer();
        prog.resetCv(1, 3);
        prog.resetCv(29, 0);
                
        operator.pushIdentifyButton();
        // and wait for message that will come at end:
        JUnitUtil.waitFor(() ->{
                return JUnitAppender.checkForMessage("Read address 3, but no such loco in roster") != null;
            }, "error message at end");
            
        // to leave visible
        JUnitUtil.waitFor(5000);
            
        JUnitUtil.dispose(frame);
    }

    @Test
    public void testIdentify3Present() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // add entry to Roster
        Roster roster = Roster.getDefault();
        RosterEntry re;
        re = new RosterEntry();
        re.setId("1st entry");
        re.setDccAddress("3");
        roster.addEntry(re);
        re = new RosterEntry();
        re.setId("2nd entry");
        re.setDccAddress("4");
        roster.addEntry(re);
        re = new RosterEntry();
        re.setId("3rd entry");
        re.setDccAddress("5");
        roster.addEntry(re);
        
        RosterFrame frame = new RosterFrame();
        frame.setVisible(true);
        frame.pack();
        RosterFrameScaffold operator = new RosterFrameScaffold(frame.getTitle());
        
        // set some CV values
        jmri.progdebugger.ProgDebugger prog = (jmri.progdebugger.ProgDebugger)InstanceManager.getDefault(GlobalProgrammerManager.class).getGlobalProgrammer();
        prog.resetCv(1, 3);
        prog.resetCv(29, 0);
                
        operator.pushIdentifyButton();
        // and wait for message that will come at end:
        //JUnitUtil.waitFor(() ->{
        //        return JUnitAppender.checkForMessage("Read address 3, but no such loco in roster") != null;
        //    }, "error message at end");
        JUnitUtil.waitFor(5000);
        RosterEntry[] selected = frame.getSelectedRosterEntries();
        Assert.assertEquals("selected ", 1, selected.length);
        
        JUnitUtil.dispose(frame);
    }

    @Test
    public void testIdentify3WithDecoderTypeMismatch() {
        // match on address if unique, even if decoder type not right
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // add entry to Roster
        Roster roster = Roster.getDefault();
        RosterEntry re;
        re = new RosterEntry();
        re.setId("1st entry is 3; mismatched types accepted");
        re.setDccAddress("3");
        re.setDecoderModel("Four Function Dual Mode");  // CV8 = 127 Atlas, CV7 = 46
        re.setDecoderFamily("Four Function Dual Mode"); 
        roster.addEntry(re);
        re = new RosterEntry();
        re.setId("2nd entry");
        re.setDccAddress("4");
        roster.addEntry(re);
        re = new RosterEntry();
        re.setId("3rd entry");
        re.setDccAddress("5");
        roster.addEntry(re);
        
        RosterFrame frame = new RosterFrame();
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
        JUnitUtil.waitFor(5000);
        RosterEntry[] selected = frame.getSelectedRosterEntries();
        Assert.assertEquals("selected ", 1, selected.length);
        
        JUnitUtil.dispose(frame);
    }

    @Test
    public void testIdentify3Multiple() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // add entry to Roster
        Roster roster = Roster.getDefault();
        RosterEntry re;
        re = new RosterEntry();
        re.setId("1st entry is 3");
        re.setDccAddress("3");
        roster.addEntry(re);
        re = new RosterEntry();
        re.setId("2nd entry is not 3");
        re.setDccAddress("4");
        roster.addEntry(re);
        re = new RosterEntry();
        re.setId("3rd entry is 3");
        re.setDccAddress("3");
        roster.addEntry(re);
        
        RosterFrame frame = new RosterFrame();
        frame.setVisible(true);
        frame.pack();
        RosterFrameScaffold operator = new RosterFrameScaffold(frame.getTitle());
        
        // set some CV values
        jmri.progdebugger.ProgDebugger prog = (jmri.progdebugger.ProgDebugger)InstanceManager.getDefault(GlobalProgrammerManager.class).getGlobalProgrammer();
        prog.resetCv(1, 3);
        prog.resetCv(29, 0);
                 
        operator.pushIdentifyButton();
        // and wait for message that will come at end:
        //JUnitUtil.waitFor(() ->{
        //        return JUnitAppender.checkForMessage("Read address 3, but no such loco in roster") != null;
        //    }, "error message at end");
        JUnitUtil.waitFor(5000);
        RosterEntry[] selected = frame.getSelectedRosterEntries();
        
        // Following is commented out because multiple selection isn't present yet
        // Assert.assertEquals("selected ", 1, selected.length);
        
        JUnitUtil.dispose(frame);
    }

    @Test
    public void testIdentify3ViaDecoderId() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // add entry to Roster
        Roster roster = Roster.getDefault();
        RosterEntry re;
        re = new RosterEntry();
        re.setId("1st entry is 3 Four Function Dual Mode");
        re.setDccAddress("3");
        re.setDecoderModel("Four Function Dual Mode");  // CV8 = 127 Atlas, CV7 = 46
        re.setDecoderFamily("Four Function Dual Mode"); 
        roster.addEntry(re);
        re = new RosterEntry();
        re.setId("2nd entry in not 3");
        re.setDccAddress("4");
        roster.addEntry(re);
        re = new RosterEntry();
        re.setId("3rd entry is 3 Dual Mode");
        re.setDccAddress("3");
        re.setDecoderModel("Dual Mode");  // CV8 = 127 Atlas, CV7 = 45
        re.setDecoderFamily("Dual Mode"); 
        roster.addEntry(re);
        
        RosterFrame frame = new RosterFrame();
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
        JUnitUtil.waitFor(5000);
        RosterEntry[] selected = frame.getSelectedRosterEntries();
        Assert.assertEquals("selected ", 1, selected.length);
        
        JUnitUtil.dispose(frame);
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
        Roster.getDefault(); // ensure exists
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RosterFrameTest.class);
}
