package jmri.jmrix.openlcb;

import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;

/**
 * OlcbProgrammerManagerTest.java
 *
 * Description: tests for the jmri.jmrix.openlcb.OlcbProgrammerManager class
 *
 * @author Bob Jacobsen
 */
public class OlcbProgrammerManagerTest {

    @Test
    public void testCtor() {
        new OlcbSystemConnectionMemo();
        OlcbProgrammerManager s = new OlcbProgrammerManager(new OlcbProgrammer());
        Assert.assertNotNull(s);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
