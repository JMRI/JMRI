package jmri.jmrit.symbolicprog;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.util.AlphanumComparator class.
 *
 * @author	Paul Bender Copyright 2016
 */
public class CVNameComparatorTest extends jmri.util.AlphanumComparatorTest {

    // ac is the object under test in superclass 
    @Test
    public void testDotOrder() {

        Assert.assertTrue("2 < 1.1", ac.compare("2", "1.1") < 0);
        Assert.assertTrue("1.1 > 2", ac.compare("1.1", "2") > 0);

        Assert.assertTrue("1.2 < 1.1.1", ac.compare("1.2", "1.1.1") < 0);
        Assert.assertTrue("1.1.2 > 1.2", ac.compare("1.1.1", "1.2") > 0);
        
        // odd cases
        Assert.assertTrue("2. > 1.1", ac.compare("2.", "1.1") > 0);
        Assert.assertTrue("1.1 < 2.", ac.compare("1.1", "2.") < 0);
        
        Assert.assertTrue("2. > 1..1", ac.compare("2.", "1..1") > 0);
        Assert.assertTrue("1..1 < 2.", ac.compare("1..1", "2.") < 0);

        Assert.assertTrue("1.1.1 > 1..1", ac.compare("1.1.1", "1..1") > 0);
        Assert.assertTrue("1..1 < 1.1.1", ac.compare("1..1", "1.1.1") < 0);

        Assert.assertTrue(".2. < 1.1", ac.compare(".2.", "1.1") < 0);
        Assert.assertTrue("1.1 > .2.", ac.compare("1.1", ".2.") > 0);
    }

    
    // from here down is testing infrastructure
    @Before
    @Override
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
        ac = new CVNameComparator();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();

    }

}
