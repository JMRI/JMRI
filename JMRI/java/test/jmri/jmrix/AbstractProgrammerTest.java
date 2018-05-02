package jmri.jmrix;

import java.util.List;
import jmri.ProgListener;
import jmri.ProgrammingMode;
import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * JUnit tests for the AbstractProgrammer class
 * <p>
 * Copyright: Copyright (c) 2002</p>
 *
 * @author Bob Jacobsen
 */
public class AbstractProgrammerTest extends TestCase {

    AbstractProgrammer abstractprogrammer;

    public void testDefault() {
        Assert.assertEquals("Check Default", ProgrammingMode.DIRECTMODE,
                abstractprogrammer.getMode());        
    }
    
    public void testDefaultViaBestMode() {
        // Programmer implementation that uses getBestMode for setting default
        abstractprogrammer = new AbstractProgrammer() {

            @Override
            public List<ProgrammingMode> getSupportedModes() {
                java.util.ArrayList<ProgrammingMode> retval = new java.util.ArrayList<ProgrammingMode>();
                
                retval.add(ProgrammingMode.DIRECTMODE);
                retval.add(ProgrammingMode.PAGEMODE);
                retval.add(ProgrammingMode.REGISTERMODE);

                return retval;
            }

            @Override
            public ProgrammingMode getBestMode() { return ProgrammingMode.REGISTERMODE; }
            
            @Override
            public void writeCV(int i, int j, ProgListener l) {}
            @Override
            public void confirmCV(String i, int j, ProgListener l) {}
            @Override
            public void readCV(int i, ProgListener l) {}
            @Override
            public void timeout() {}
            @Override
            public boolean getCanRead() { return true;}
        };

        Assert.assertEquals("Check Default", ProgrammingMode.REGISTERMODE,
                abstractprogrammer.getMode());        
    }
    
    public void testSetGetMode() {
        abstractprogrammer.setMode(ProgrammingMode.REGISTERMODE);
        Assert.assertEquals("Check mode matches set", ProgrammingMode.REGISTERMODE,
                abstractprogrammer.getMode());        
    }
    
    public void testSetModeNull() {
        try {
            abstractprogrammer.setMode(null);
        } catch (IllegalArgumentException e) { return;  /* OK */ }
        
        Assert.fail("should not have setMode(null)");        
    }
    
    public void testRegisterFromCV() {
        int cv1 = -1;

        try {
            Assert.assertEquals("test CV 1", 1,
                    abstractprogrammer.registerFromCV(cv1 = 1));
            Assert.assertEquals("test CV 2", 2,
                    abstractprogrammer.registerFromCV(cv1 = 2));
            Assert.assertEquals("test CV 3", 3,
                    abstractprogrammer.registerFromCV(cv1 = 3));
            Assert.assertEquals("test CV 4", 4,
                    abstractprogrammer.registerFromCV(cv1 = 4));
            Assert.assertEquals("test CV 29", 5,
                    abstractprogrammer.registerFromCV(cv1 = 29));
            Assert.assertEquals("test CV 7", 7,
                    abstractprogrammer.registerFromCV(cv1 = 7));
            Assert.assertEquals("test CV 8", 8,
                    abstractprogrammer.registerFromCV(cv1 = 8));
        } catch (Exception e) {
            Assert.fail("unexpected exception while cv = " + cv1);
        }

        // now try for some exceptions
        for (cv1 = 5; cv1 < 29; cv1++) {
            if (cv1 == 7 || cv1 == 8) {
                continue;
            }
            try {
                abstractprogrammer.registerFromCV(cv1); // should assert
                Assert.fail("did not throw as expected for cv = " + cv1);
            } catch (Exception e) {
                jmri.util.JUnitAppender.assertWarnMessage("Unhandled register from cv:  "+cv1);
            }
        }
    }

    // from here down is testing infrastructure
    public AbstractProgrammerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {AbstractProgrammerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(AbstractProgrammerTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();

        abstractprogrammer = new AbstractProgrammer() {

            @Override
            public List<ProgrammingMode> getSupportedModes() {
                java.util.ArrayList<ProgrammingMode> retval = new java.util.ArrayList<ProgrammingMode>();
                
                retval.add(ProgrammingMode.DIRECTMODE);
                retval.add(ProgrammingMode.PAGEMODE);
                retval.add(ProgrammingMode.REGISTERMODE);

                return retval;
            }

            @Override
            public void writeCV(int i, int j, ProgListener l) {}
            @Override
            public void confirmCV(String i, int j, ProgListener l) {}
            @Override
            public void readCV(int i, ProgListener l) {}
            @Override
            public void timeout() {}
            @Override
            public boolean getCanRead() { return true;}
        };
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }

}
