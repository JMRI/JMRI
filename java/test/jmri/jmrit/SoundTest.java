// SoundTest.java
package jmri.jmrit;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the Sound class.
 * <P>
 * Note: This makes noise!
 *
 * @author	Bob Jacobsen Copyright 2006, 2016
 */
public class SoundTest extends TestCase {

    public void testLoadAndPlay() {
        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
        
            String name = "bottle-open.wav";
            Sound snd = new Sound(name);
            snd.play();
            
        }    
    }

    // from here down is testing infrastructure
    public SoundTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SoundTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SoundTest.class);
        return suite;
    }

    // static private Logger log = LoggerFactory.getLogger(SoundTest.class.getName());
}
