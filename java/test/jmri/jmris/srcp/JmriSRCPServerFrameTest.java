package jmri.jmris.srcp;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the jmri.jmris.srcp.JmriSRCPServerFrame class
 *
 * @author Paul Bender
 */
public class JmriSRCPServerFrameTest extends jmri.util.JmriJFrameTestBase {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        if(!GraphicsEnvironment.isHeadless()){
           frame = new JmriSRCPServerFrame();
        }
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }

}
