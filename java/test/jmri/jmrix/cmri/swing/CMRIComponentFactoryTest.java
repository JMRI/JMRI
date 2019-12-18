package jmri.jmrix.cmri.swing;

import jmri.jmrix.cmri.serial.SerialTrafficControlScaffold;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class CMRIComponentFactoryTest {

    private jmri.jmrix.cmri.CMRISystemConnectionMemo memo = null;
    private SerialTrafficControlScaffold tcis = null;

    @Test
    public void testCTor() {
        CMRIComponentFactory t = new CMRIComponentFactory(memo);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface
        tcis = new SerialTrafficControlScaffold();
        memo = new jmri.jmrix.cmri.CMRISystemConnectionMemo();
        memo.setTrafficController(tcis);
    }

    @After
    public void tearDown() {
        if (tcis != null) tcis.terminateThreads();
        tcis = null;
        memo = null;
	    JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CMRIComponentFactoryTest.class);

}
