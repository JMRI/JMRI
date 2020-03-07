package jmri.jmrix.mrc;

import java.util.Date;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
           @Override
           public void notifyXmit(Date timestamp, MrcMessage m){
           }
           @Override
           public void notifyRcv(Date timestamp, MrcMessage m){
           }
           @Override
           public void notifyFailedXmit(Date timestamp, MrcMessage m){
           }
        };
        MrcTrafficListenerFilter t = new MrcTrafficListenerFilter(~0,tl);
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

    // private final static Logger log = LoggerFactory.getLogger(MrcTrafficListenerFilterTest.class);

}
