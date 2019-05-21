package jmri.jmrit.audio.swing;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class AudioSourceFrameTest extends jmri.util.JmriJFrameTestBase {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        if(!GraphicsEnvironment.isHeadless()){
           // the second parameter should be an
           // jmri.jmrit.beantable.AudioTableAction.AudioSourceTableDataModel
           // object
           frame = new AudioSourceFrame("Source Frame Test",null);
	}
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AudioSourceFrameTest.class);

}
