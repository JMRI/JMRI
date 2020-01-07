package jmri.util;

import java.awt.GraphicsEnvironment;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class JmriJFrameTest extends JmriJFrameTestBase {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        if(!GraphicsEnvironment.isHeadless()){
           frame = new JmriJFrame("JmriJFrame ConstructorTest");
        }
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(JmriJFrameTest.class);

}
