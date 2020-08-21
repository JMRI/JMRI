package jmri.jmris;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.BeforeEach;

/**
 * Tests for the jmri.jmris.JmriServerFrame class
 *
 * @author Paul Bender
 */
public class JmriServerFrameTest extends jmri.util.JmriJFrameTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        if(!GraphicsEnvironment.isHeadless()){
           frame = new JmriServerFrame();
        }
    }

}
