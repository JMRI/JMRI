package jmri.jmrit.dispatcher;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AutoTrainsFrameTest extends jmri.util.JmriJFrameTestBase {
        
    DispatcherFrame d = null;

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/dispatcheroptions.xml");  // exist?
        if(!GraphicsEnvironment.isHeadless()){
           d = InstanceManager.getDefault(DispatcherFrame.class);
           frame = new AutoTrainsFrame(d);
    	}
    }

    @After
    @Override
    public void tearDown() {
	if(d!=null) {
           JUnitUtil.dispose(d);
	}
	d = null;
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AutoTrainsFrameTest.class);

}
