package jmri.jmrix.mrc;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Date;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class MrcTrafficListenerFilterTest {

    @Test
    public void testCTor() {
        MrcSystemConnectionMemo memo = new MrcSystemConnectionMemo();
        MrcInterfaceScaffold tc = new MrcInterfaceScaffold();
        memo.setMrcTrafficController(tc);
        jmri.InstanceManager.store(memo, MrcSystemConnectionMemo.class);
        MrcTrafficListener tl = new MrcTrafficListener(){
           public void notifyXmit(Date timestamp, MrcMessage m){
           }
           public void notifyRcv(Date timestamp, MrcMessage m){
           }
           public void notifyFailedXmit(Date timestamp, MrcMessage m){
           }
        };
        MrcTrafficListenerFilter t = new MrcTrafficListenerFilter(~0,tl);
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

    private final static Logger log = LoggerFactory.getLogger(MrcTrafficListenerFilterTest.class.getName());

}
