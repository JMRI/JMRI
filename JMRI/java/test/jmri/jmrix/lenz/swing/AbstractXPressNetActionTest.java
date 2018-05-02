package jmri.jmrix.lenz.swing;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class AbstractXPressNetActionTest {

    @Test
    public void testCTor() {
        jmri.jmrix.lenz.XNetInterfaceScaffold tc = new jmri.jmrix.lenz.XNetInterfaceScaffold(new jmri.jmrix.lenz.LenzCommandStation());
        jmri.jmrix.lenz.XNetSystemConnectionMemo memo = new jmri.jmrix.lenz.XNetSystemConnectionMemo(tc);
        AbstractXPressNetAction t = new AbstractXPressNetAction("test",memo){
           @Override
           public void actionPerformed(java.awt.event.ActionEvent ae){
           }
        };
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

    // private final static Logger log = LoggerFactory.getLogger(AbstractXPressNetActionTest.class);

}
