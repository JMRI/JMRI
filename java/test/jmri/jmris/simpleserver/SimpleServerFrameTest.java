package jmri.jmris.simpleserver;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import org.junit.jupiter.api.BeforeEach;

/**
 * Tests for the jmri.jmris.simpleserver.SimpleServerFrame class
 *
 * @author Paul Bender
 */
public class SimpleServerFrameTest extends jmri.util.JmriJFrameTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        if(!GraphicsEnvironment.isHeadless()){
            ThreadingUtil.runOnGUI( () -> frame = new SimpleServerFrame());
        }
    }

}
