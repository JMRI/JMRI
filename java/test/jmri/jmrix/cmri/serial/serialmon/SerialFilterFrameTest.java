package jmri.jmrix.cmri.serial.serialmon;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import org.junit.*;


/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SerialFilterFrameTest extends jmri.util.JmriJFrameTestBase {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        if(!GraphicsEnvironment.isHeadless()){
           frame= new SerialFilterFrame(new CMRISystemConnectionMemo());
	}
    }

    @After
    @Override
    public void tearDown() {
        jmri.util.JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SerialFilterFrameTest.class);

}
