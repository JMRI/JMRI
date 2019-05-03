package jmri.jmris;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the jmri.jmris.JmriServerFrame class
 *
 * @author Paul Bender
 */
public class JmriServerFrameTest extends jmri.util.JmriJFrameTestBase {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        if(!GraphicsEnvironment.isHeadless()){
           frame = new JmriServerFrame();
        }
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }

}
