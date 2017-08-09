package jmri.jmrix.cmri.serial;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
<<<<<<< HEAD
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
=======
>>>>>>> JMRI/master

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SerialLightTest {

    private jmri.jmrix.cmri.CMRISystemConnectionMemo memo = null;
    private SerialTrafficControlScaffold tcis = null;
<<<<<<< HEAD
    private SerialNode n = null;
=======
>>>>>>> JMRI/master

    @Test
    public void test2ParamCTor() {
        SerialLight t = new SerialLight("CL4",memo);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void test3ParamCTor() {
        SerialLight t = new SerialLight("CL4","t4",memo);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        // prepare an interface
        tcis = new SerialTrafficControlScaffold();
        memo = new jmri.jmrix.cmri.CMRISystemConnectionMemo();
        memo.setTrafficController(tcis);
<<<<<<< HEAD
        n = new SerialNode(0, SerialNode.SMINI,tcis);
=======
        new SerialNode(0, SerialNode.SMINI,tcis);
>>>>>>> JMRI/master
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

<<<<<<< HEAD
    private final static Logger log = LoggerFactory.getLogger(SerialLightTest.class.getName());

=======
>>>>>>> JMRI/master
}
