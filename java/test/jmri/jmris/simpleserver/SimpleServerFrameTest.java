package jmri.jmris.simpleserver;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the jmri.jmris.simpleserver.SimpleServerFrame class
 *
 * @author Paul Bender
 */
public class SimpleServerFrameTest extends jmri.util.JmriJFrameTestBase {

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        if(!GraphicsEnvironment.isHeadless()){
           frame = new SimpleServerFrame();
        }
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }

}
