package jmri.jmrix.cmri.serial.nodeiolist;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class NodeIOListActionTest {

    @Test
    public void testCTor() {
        NodeIOListAction t = new NodeIOListAction("test action",new CMRISystemConnectionMemo()); 
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testMemoCTor() {
        NodeIOListAction t = new NodeIOListAction(new CMRISystemConnectionMemo()); 
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(NodeIOListActionTest.class.getName());

}
