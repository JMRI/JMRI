package jmri.jmris.srcp;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.BeforeEach;

/**
 * Tests for the jmri.jmris.srcp.JmriSRCPServerFrame class
 *
 * @author Paul Bender
 */
public class JmriSRCPServerFrameTest extends jmri.util.JmriJFrameTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        if(!GraphicsEnvironment.isHeadless()){
           frame = new JmriSRCPServerFrame();
        }
    }

}
