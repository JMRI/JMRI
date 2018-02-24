package jmri.jmrix.cmri.serial.nodeiolist;

import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(NodeIOListActionTest.class);

}
