package jmri.jmrix.grapevine.nodetable;

import java.awt.GraphicsEnvironment;
import org.junit.*;
import jmri.util.JUnitUtil;
import jmri.jmrix.grapevine.GrapevineSystemConnectionMemo;
import jmri.jmrix.grapevine.SerialTrafficController;
import jmri.jmrix.grapevine.SerialTrafficControlScaffold;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class RenumberFrameTest extends jmri.util.JmriJFrameTestBase {

    private GrapevineSystemConnectionMemo memo = null; 

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();

        memo = new GrapevineSystemConnectionMemo();
        SerialTrafficController tc = new SerialTrafficControlScaffold(memo);
        memo.setTrafficController(tc);
        if(!GraphicsEnvironment.isHeadless()){
           frame = new RenumberFrame(memo);
        }
    }

    @After
    @Override
    public void tearDown() {
        memo = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(RenumberFrameTest.class);

}
