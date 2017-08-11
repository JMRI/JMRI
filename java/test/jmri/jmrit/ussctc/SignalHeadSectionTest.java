package jmri.jmrit.ussctc;

import org.junit.*;

import jmri.util.*;
import jmri.*;
import java.beans.*;

import java.util.*;

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

    @Test
    public void testCurrentIndicationNormalStates() {
        SignalHeadSection s = new SignalHeadSection(Arrays.asList(new String[]{"IH1", "IH2"}), Arrays.asList(new String[]{"IH3", "IH4"}),
                        "Sec 1 Sign 1 L", "Sec 1 Sign 1 C", "Sec 1 Sign 1 R", 
                         "Sec 1 Sign 1 L", "Sec 1 Sign 1 R",
                        station) {
                // for testing purposes, turn off action on signal changes
                void layoutSignalHeadChanged(java.beans.PropertyChangeEvent e) {}
        };
        
        ih1.setHeld(false);
        ih2.setHeld(false);
        ih3.setHeld(false);
        ih4.setHeld(false);

        ih1.setAppearance(SignalHead.RED);
        ih2.setAppearance(SignalHead.RED);
        ih3.setAppearance(SignalHead.RED);
        ih4.setAppearance(SignalHead.RED);
        Assert.assertEquals("all RED", CodeGroupThreeBits.Triple010, s.getCurrentIndication());
        
        ih1.setAppearance(SignalHead.RED);
        ih2.setAppearance(SignalHead.YELLOW);
        ih3.setAppearance(SignalHead.RED);
        ih4.setAppearance(SignalHead.RED);
        Assert.assertEquals("one YELLOW right", CodeGroupThreeBits.Triple001, s.getCurrentIndication());
        
        ih1.setAppearance(SignalHead.RED);
        ih2.setAppearance(SignalHead.RED);
        ih3.setAppearance(SignalHead.RED);
        ih4.setAppearance(SignalHead.YELLOW);
        Assert.assertEquals("one YELLOW left", CodeGroupThreeBits.Triple100, s.getCurrentIndication());
                
        ih1.setAppearance(SignalHead.RED);
        ih2.setAppearance(SignalHead.RED);
        ih3.setAppearance(SignalHead.RED);
        ih4.setAppearance(SignalHead.FLASHYELLOW);
        Assert.assertEquals("one FLASHYELLOW left", CodeGroupThreeBits.Triple100, s.getCurrentIndication());
                
        ih1.setAppearance(SignalHead.RED);
        ih2.setAppearance(SignalHead.FLASHRED);
        ih3.setAppearance(SignalHead.RED);
        ih4.setAppearance(SignalHead.FLASHRED);
        Assert.assertEquals("FLASHRED both ways", CodeGroupThreeBits.Triple000, s.getCurrentIndication());
    }

    @Test
    public void testCurrentIndicationErrorStates() {
        SignalHeadSection s = new SignalHeadSection(Arrays.asList(new String[]{"IH1", "IH2"}), Arrays.asList(new String[]{"IH3", "IH4"}),
                        "Sec 1 Sign 1 L", "Sec 1 Sign 1 C", "Sec 1 Sign 1 R", 
                         "Sec 1 Sign 1 L", "Sec 1 Sign 1 R",
                        station) {
                // for testing purposes, turn off action on signal changes
                void layoutSignalHeadChanged(java.beans.PropertyChangeEvent e) {}
        };
        
        ih1.setHeld(false);
        ih2.setHeld(false);
        ih3.setHeld(false);
        ih4.setHeld(false);

        ih1.setAppearance(SignalHead.YELLOW);
        ih2.setAppearance(SignalHead.RED);
        ih3.setAppearance(SignalHead.YELLOW);
        ih4.setAppearance(SignalHead.RED);
        Assert.assertEquals("clear both", CodeGroupThreeBits.Triple000, s.getCurrentIndication());
        JUnitAppender.assertErrorMessage("Found both left and right clear: SignalHeadSection [\"IH1\", \"IH2\"],[\"IH3\", \"IH4\"]");

        ih1.setAppearance(SignalHead.YELLOW);
        ih2.setAppearance(SignalHead.RED);
        ih3.setAppearance(SignalHead.FLASHRED);
        ih4.setAppearance(SignalHead.RED);
        Assert.assertEquals("clear both", CodeGroupThreeBits.Triple000, s.getCurrentIndication());
        JUnitAppender.assertWarnMessage("Found left at restricting and right clear SignalHeadSection [\"IH1\", \"IH2\"],[\"IH3\", \"IH4\"]");

        ih1.setAppearance(SignalHead.FLASHRED);
        ih2.setAppearance(SignalHead.RED);
        ih3.setAppearance(SignalHead.YELLOW);
        ih4.setAppearance(SignalHead.RED);
        Assert.assertEquals("clear both", CodeGroupThreeBits.Triple000, s.getCurrentIndication());
        JUnitAppender.assertWarnMessage("Found left clear and right at restricting: SignalHeadSection [\"IH1\", \"IH2\"],[\"IH3\", \"IH4\"]");
    }
   
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
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();

        codeline = new CodeLine("Code Sequencer Start", "IT101", "IT102", "IT103", "IT104");
        
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
            public void requestIndicationStart() {
                requestIndicationStart = true;
            }
        };
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
