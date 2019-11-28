package jmri.jmrit.ussctc;

import java.beans.*;
import java.util.*;
import jmri.*;
import jmri.util.*;
import org.junit.*;

/**
 * Tests for SignalHeadSection class in the jmri.jmrit.ussctc package
 *
 * @author	Bob Jacobsen Copyright 2007
 */
public class SignalHeadSectionTest {

    @Test
    public void testConstruction() {
        new SignalHeadSection(new ArrayList<String>(), new ArrayList<String>(),   // empty
                        "Sec 1 Sign 1 L", "Sec 1 Sign 1 C", "Sec 1 Sign 1 R", 
                         "Sec 1 Sign 1 L", "Sec 1 Sign 1 R",
                        station);
    }

    @Test
    public void testEmptyToString() {
        SignalHeadSection s = new SignalHeadSection(new ArrayList<String>(), new ArrayList<String>(),   // empty
                        "Sec 1 Sign 1 L", "Sec 1 Sign 1 C", "Sec 1 Sign 1 R", 
                         "Sec 1 Sign 1 L", "Sec 1 Sign 1 R",
                        station);
        Assert.assertEquals("SignalHeadSection [],[]", s.toString());
    }
 
    @Test
    public void testNamesToString() {
        SignalHeadSection s = new SignalHeadSection(Arrays.asList(new String[]{"IH1", "IH2"}), Arrays.asList(new String[]{"IH3"}),
                        "Sec 1 Sign 1 L", "Sec 1 Sign 1 C", "Sec 1 Sign 1 R", 
                         "Sec 1 Sign 1 L", "Sec 1 Sign 1 R",
                        station);
        Assert.assertEquals("SignalHeadSection [\"IH1\", \"IH2\"],[\"IH3\"]", s.toString());
    }

    boolean listened;
    
    @Test
    public void testListener() {
        final SignalHeadSection s = new SignalHeadSection(new ArrayList<String>(), new ArrayList<String>(),   // empty
                        "Sec 1 Sign 1 L", "Sec 1 Sign 1 C", "Sec 1 Sign 1 R", 
                         "Sec 1 Sign 1 L", "Sec 1 Sign 1 R",
                        station);
                        
        s.setLastIndication(CodeGroupThreeBits.Triple001);

        listened = false;
        PropertyChangeListener p = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                listened = true;
                Assert.assertEquals("LastIndication", e.getPropertyName());
                Assert.assertEquals(CodeGroupThreeBits.Triple001, e.getOldValue());
                Assert.assertEquals(CodeGroupThreeBits.Triple100, e.getNewValue());
                Assert.assertEquals(s, e.getSource());
            }
        };
        s.addPropertyChangeListener(p);
        Assert.assertTrue(! listened);
        
        s.setLastIndication(CodeGroupThreeBits.Triple100);
        
        Assert.assertTrue(listened);

        listened = false;
        s.removePropertyChangeListener(p);

        s.setLastIndication(CodeGroupThreeBits.Triple100);
        
        Assert.assertTrue(!listened);
        
        
    }

    /**
     * Set up and do a single check of the logic for calculating a current indication in the field
     * Takes a set of right and left
     * signal appearances to show, plus the last command that was send,
     * then changes the signals (as needed) and checks for 
     * the result. The test condition appears as the calling method name in the traceback, 
     * not as a separate string.
     */
    public void checkOneCI(int l1before, int l2before, int r1before, int r2before, CodeGroupThreeBits lastIndication,
                           int l1after,  int l2after,  int r1after,  int r2after, 
                           CodeGroupThreeBits checkIndication) {

        SignalHeadSection s = new SignalHeadSection(Arrays.asList(new String[]{"IH1", "IH2"}), Arrays.asList(new String[]{"IH3", "IH4"}),
                        "Sec 1 Sign 1 L", "Sec 1 Sign 1 C", "Sec 1 Sign 1 R", 
                         "Sec 1 Sign 1 L", "Sec 1 Sign 1 R",
                        station) {
                // for testing purposes, turn off action on signal changes
                @Override
                void layoutSignalHeadChanged(java.beans.PropertyChangeEvent e) {}
        };
        
        // set up
        ih1.setHeld(false);
        ih2.setHeld(false);
        ih3.setHeld(false);
        ih4.setHeld(false);
        JUnitUtil.setBeanStateAndWait(ih1, r1before);
        JUnitUtil.setBeanStateAndWait(ih2, r2before);
        JUnitUtil.setBeanStateAndWait(ih3, l1before);
        JUnitUtil.setBeanStateAndWait(ih4, l2before);
        s.setLastIndication(lastIndication);
        
        // sequence changes to test
        if (ih1.getAppearance() != r1after) JUnitUtil.setBeanStateAndWait(ih1, r1after);
        if (ih2.getAppearance() != r2after) JUnitUtil.setBeanStateAndWait(ih2, r2after);
        if (ih3.getAppearance() != l1after) JUnitUtil.setBeanStateAndWait(ih3, l1after);
        if (ih4.getAppearance() != l2after) JUnitUtil.setBeanStateAndWait(ih4, l2after);

        // final check
        Assert.assertEquals(checkIndication, s.getCurrentIndication());
                 
    }

    @Test public void testCI_StopAndAllRed() {
        checkOneCI(SignalHead.RED,SignalHead.RED,  SignalHead.RED,SignalHead.RED,   CodeGroupThreeBits.Triple010,
                   SignalHead.RED,SignalHead.RED,  SignalHead.RED,SignalHead.RED,
                   
                        CodeGroupThreeBits.Triple010);
    }

    // we do each test in left and right to make sure we've got the code correct
    
    // normal case of signal going from clear to stop in field
    @Test public void testCI_LeftGreenGoesToAllRed() {
        checkOneCI(SignalHead.RED,SignalHead.GREEN, SignalHead.RED,SignalHead.RED,   CodeGroupThreeBits.Triple100,
                   SignalHead.RED,SignalHead.RED,   SignalHead.RED,SignalHead.RED,
                   
                        CodeGroupThreeBits.Triple010);
    }
    @Test public void testCI_RightGreenGoesToAllRed() {
        checkOneCI(SignalHead.RED,SignalHead.RED, SignalHead.RED,SignalHead.YELLOW,   CodeGroupThreeBits.Triple001,
                   SignalHead.RED,SignalHead.RED, SignalHead.RED,SignalHead.RED,
                   
                        CodeGroupThreeBits.Triple010);
    }
    @Test public void testCI_LeftYellowGoesToAllRed() {
        checkOneCI(SignalHead.RED,SignalHead.GREEN, SignalHead.RED,SignalHead.RED,   CodeGroupThreeBits.Triple100,
                   SignalHead.RED,SignalHead.RED,   SignalHead.RED,SignalHead.RED,
                   
                        CodeGroupThreeBits.Triple010);
    }
    @Test public void testCI_RightYellowGoesToAllRed() {
        checkOneCI(SignalHead.RED,SignalHead.RED, SignalHead.RED,SignalHead.YELLOW,   CodeGroupThreeBits.Triple001,
                   SignalHead.RED,SignalHead.RED, SignalHead.RED,SignalHead.RED,
                   
                        CodeGroupThreeBits.Triple010);
    }

    // normal case of signals in the field clearing after a time
    @Test public void testCI_LeftClears() {
        checkOneCI(SignalHead.RED,SignalHead.RED,     SignalHead.RED,SignalHead.RED,   CodeGroupThreeBits.Triple100,
                   SignalHead.RED,SignalHead.GREEN,   SignalHead.RED,SignalHead.RED,
                   
                        CodeGroupThreeBits.Triple100);
    }
    @Test public void testCI_RightClears() {
        checkOneCI(SignalHead.RED,SignalHead.RED, SignalHead.RED,SignalHead.RED,   CodeGroupThreeBits.Triple001,
                   SignalHead.RED,SignalHead.RED, SignalHead.RED,SignalHead.GREEN,
                   
                        CodeGroupThreeBits.Triple001);
    }

    // normal case of signals in the field changing, while staying clear
    @Test public void testCI_LeftChanges() {
        checkOneCI(SignalHead.RED,SignalHead.GREEN,    SignalHead.RED,SignalHead.RED,   CodeGroupThreeBits.Triple100,
                   SignalHead.RED,SignalHead.YELLOW,   SignalHead.RED,SignalHead.RED,
                   
                        CodeGroupThreeBits.Triple100);
    }
    @Test public void testCI_RightChanges() {
        checkOneCI(SignalHead.RED,SignalHead.RED, SignalHead.RED,SignalHead.GREEN,   CodeGroupThreeBits.Triple001,
                   SignalHead.RED,SignalHead.RED, SignalHead.RED,SignalHead.YELLOW,
                   
                        CodeGroupThreeBits.Triple001);
    }

    // signal has been set to stop, but it taking some time - not typical?
    @Test public void testCI_LeftHasntDroppedYet() {
        checkOneCI(SignalHead.RED,SignalHead.GREEN,    SignalHead.RED,SignalHead.RED,   CodeGroupThreeBits.Triple010,
                   SignalHead.RED,SignalHead.GREEN,   SignalHead.RED,SignalHead.RED,
                   
                        CodeGroupThreeBits.Triple000);
    }
    @Test public void testCI_RightHasntDroppedYet() {
        checkOneCI(SignalHead.RED,SignalHead.RED,   SignalHead.RED,SignalHead.GREEN,   CodeGroupThreeBits.Triple010,
                   SignalHead.RED,SignalHead.RED,   SignalHead.RED,SignalHead.GREEN,
                   
                        CodeGroupThreeBits.Triple000);
    }
         
    // signal was at stop, but cleared in field - not typical!
    @Test public void testCI_StopClearsLeft() {
        checkOneCI(SignalHead.RED,SignalHead.RED,    SignalHead.RED,SignalHead.RED,   CodeGroupThreeBits.Triple010,
                   SignalHead.RED,SignalHead.YELLOW, SignalHead.RED,SignalHead.RED,
                   
                        CodeGroupThreeBits.Triple000);
    }
    @Test public void testCI_StopClearsRight() {
        checkOneCI(SignalHead.RED,SignalHead.RED,    SignalHead.RED,SignalHead.GREEN,   CodeGroupThreeBits.Triple010,
                   SignalHead.RED,SignalHead.RED, SignalHead.RED,SignalHead.GREEN,
                   
                        CodeGroupThreeBits.Triple000);
    }
         
    // tests of restricting cases
    
    @Test public void testCI_BothRestrictingAtStop() {
        checkOneCI(SignalHead.RED,SignalHead.RED,      SignalHead.RED,SignalHead.RED,   CodeGroupThreeBits.Triple010,
                   SignalHead.RED,SignalHead.FLASHRED, SignalHead.RED,SignalHead.FLASHRED,
                   
                        CodeGroupThreeBits.Triple000);
    }
    @Test public void testCI_BothRestrictingWhenLeft() {
        checkOneCI(SignalHead.RED,SignalHead.RED,      SignalHead.RED,SignalHead.RED,   CodeGroupThreeBits.Triple100,
                   SignalHead.RED,SignalHead.FLASHRED, SignalHead.RED,SignalHead.FLASHRED,
                   
                        CodeGroupThreeBits.Triple000);
    }
    @Test public void testCI_BothRestrictingWhenRight() {
        checkOneCI(SignalHead.RED,SignalHead.RED,      SignalHead.RED,SignalHead.RED,   CodeGroupThreeBits.Triple001,
                   SignalHead.RED,SignalHead.FLASHRED, SignalHead.RED,SignalHead.FLASHRED,
                   
                        CodeGroupThreeBits.Triple000);
    }
        
         
    // tests of some odd states and conditions
    
    @Test public void testCI_BothLeftAndRightFoundClearInsteadOfStop() {
        checkOneCI(SignalHead.RED,SignalHead.RED,   SignalHead.RED,SignalHead.RED,   CodeGroupThreeBits.Triple010,
                   SignalHead.RED,SignalHead.GREEN, SignalHead.RED,SignalHead.GREEN,
                   
                        CodeGroupThreeBits.Triple000);
        jmri.util.JUnitAppender.assertErrorMessage("Found both left and right clear: SignalHeadSection [\"IH1\", \"IH2\"],[\"IH3\", \"IH4\"]"); 
                        
    }
    @Test public void testCI_BothLeftAndRightFoundClearInsteadOfLeftOnly() {
        checkOneCI(SignalHead.RED,SignalHead.RED,   SignalHead.RED,SignalHead.RED,   CodeGroupThreeBits.Triple100,
                   SignalHead.RED,SignalHead.GREEN, SignalHead.RED,SignalHead.GREEN,
                   
                        CodeGroupThreeBits.Triple000);
        jmri.util.JUnitAppender.assertErrorMessage("Found both left and right clear: SignalHeadSection [\"IH1\", \"IH2\"],[\"IH3\", \"IH4\"]"); 
    }
    @Test public void testCI_BothLeftAndRightFoundClearInsteadOfRightOnly() {
        checkOneCI(SignalHead.RED,SignalHead.RED,   SignalHead.RED,SignalHead.RED,   CodeGroupThreeBits.Triple001,
                   SignalHead.RED,SignalHead.GREEN, SignalHead.RED,SignalHead.GREEN,
                   
                        CodeGroupThreeBits.Triple000);
        jmri.util.JUnitAppender.assertErrorMessage("Found both left and right clear: SignalHeadSection [\"IH1\", \"IH2\"],[\"IH3\", \"IH4\"]"); 
    }
         

    public void testCI_BothLeftRestrictingAndRightClearInsteadOfStop() {
        checkOneCI(SignalHead.RED,SignalHead.RED,   SignalHead.RED,SignalHead.RED,   CodeGroupThreeBits.Triple010,
                   SignalHead.RED,SignalHead.FLASHRED, SignalHead.RED,SignalHead.GREEN,
                   
                        CodeGroupThreeBits.Triple000);
        jmri.util.JUnitAppender.assertWarnMessage("Found left at restricting and right clear: SignalHeadSection [\"IH1\", \"IH2\"],[\"IH3\", \"IH4\"]"); 
    }
    @Test public void testCI_BothLeftRestrictingAndRightClearInsteadOfLeftOnly() {
        checkOneCI(SignalHead.RED,SignalHead.RED,   SignalHead.RED,SignalHead.RED,   CodeGroupThreeBits.Triple100,
                   SignalHead.RED,SignalHead.FLASHRED, SignalHead.RED,SignalHead.GREEN,
                   
                        CodeGroupThreeBits.Triple000);
        jmri.util.JUnitAppender.assertWarnMessage("Found left at restricting and right clear: SignalHeadSection [\"IH1\", \"IH2\"],[\"IH3\", \"IH4\"]"); 
    }
    public void testCI_BothLeftRestrictingAndRightClearInsteadOfRightOnly() {
        checkOneCI(SignalHead.RED,SignalHead.RED,   SignalHead.RED,SignalHead.RED,   CodeGroupThreeBits.Triple001,
                   SignalHead.RED,SignalHead.FLASHRED, SignalHead.RED,SignalHead.GREEN,
                   
                        CodeGroupThreeBits.Triple000);
        jmri.util.JUnitAppender.assertWarnMessage("Found left at restricting and right clear: SignalHeadSection [\"IH1\", \"IH2\"],[\"IH3\", \"IH4\"]"); 
    }
         
    @Test public void testCI_BothLeftClearAndRightRestrictingInsteadOfStop() {
        checkOneCI(SignalHead.RED,SignalHead.RED,   SignalHead.RED,SignalHead.RED,   CodeGroupThreeBits.Triple010,
                   SignalHead.RED,SignalHead.GREEN, SignalHead.RED,SignalHead.FLASHRED,
                   
                        CodeGroupThreeBits.Triple000);
        jmri.util.JUnitAppender.assertWarnMessage("Found left clear and right at restricting: SignalHeadSection [\"IH1\", \"IH2\"],[\"IH3\", \"IH4\"]"); 
    }
    @Test public void testCI_BothLeftClearAndRightRestrictingInsteadOfLeftOnly() {
        checkOneCI(SignalHead.RED,SignalHead.RED,   SignalHead.RED,SignalHead.RED,   CodeGroupThreeBits.Triple100,
                   SignalHead.RED,SignalHead.GREEN, SignalHead.RED,SignalHead.FLASHRED,
                   
                        CodeGroupThreeBits.Triple000);
        jmri.util.JUnitAppender.assertWarnMessage("Found left clear and right at restricting: SignalHeadSection [\"IH1\", \"IH2\"],[\"IH3\", \"IH4\"]"); 
    }
    @Test public void testCI_BothLeftClearAndRightRestrictingInsteadOfRightOnly() {
        checkOneCI(SignalHead.RED,SignalHead.RED,   SignalHead.RED,SignalHead.RED,   CodeGroupThreeBits.Triple001,
                   SignalHead.RED,SignalHead.GREEN, SignalHead.RED,SignalHead.FLASHRED,
                   
                        CodeGroupThreeBits.Triple000);
        jmri.util.JUnitAppender.assertWarnMessage("Found left clear and right at restricting: SignalHeadSection [\"IH1\", \"IH2\"],[\"IH3\", \"IH4\"]"); 
    }
         


    // common infrastructure
       
    CodeLine codeline;
    Station station;
    boolean requestIndicationStart;

    SignalHead ih1;
    SignalHead ih2;
    SignalHead ih3;
    SignalHead ih4;
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();

        codeline = new CodeLine("Code Indication Start", "Code Send Start", "IT101", "IT102", "IT103", "IT104");
        
        ih1 = new jmri.implementation.VirtualSignalHead("IH1");
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(ih1);
        ih2 = new jmri.implementation.VirtualSignalHead("IH2");
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(ih2);
        ih3 = new jmri.implementation.VirtualSignalHead("IH3");
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(ih3);
        ih4 = new jmri.implementation.VirtualSignalHead("IH4");
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(ih4);

        requestIndicationStart = false;
        station = new Station("test", codeline, new CodeButton("IS221", "IS222")) {
            @Override
            public void requestIndicationStart() {
                requestIndicationStart = true;
            }
        };
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
