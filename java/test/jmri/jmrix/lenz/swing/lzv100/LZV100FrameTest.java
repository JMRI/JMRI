package jmri.jmrix.lenz.swing.lzv100;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * LZV100FrameTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.swing.lzv100.LZV100Frame class
 *
 * @author	Paul Bender
 */
public class LZV100FrameTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // infrastructure objects
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());

        LZV100Frame f = new LZV100Frame(new XNetSystemConnectionMemo(tc));
        Assert.assertNotNull(f);
        f.dispose();
    }

    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
