package jmri.jmrix.secsi.serialmon;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;
import jmri.jmrix.secsi.SecsiSystemConnectionMemo;
import jmri.jmrix.secsi.SerialTrafficControlScaffold;

/**
 * Test simple functioning of SerialMonFrame
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SerialMonFrameTest extends jmri.util.JmriJFrameTestBase {

    private SecsiSystemConnectionMemo memo = null;

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        memo = new SecsiSystemConnectionMemo();
        memo.setTrafficController(new SerialTrafficControlScaffold(memo));
        if(!GraphicsEnvironment.isHeadless()){
           frame = new SerialMonFrame(memo);
	    }
    }

    @After
    @Override
    public void tearDown() {
        memo.getTrafficController().terminateThreads();
	    memo = null;
    	super.tearDown();
    }
}
