package jmri.jmrix.ieee802154.xbee;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import com.digi.xbee.api.XBeeNetwork;
import com.digi.xbee.api.XBeeDevice;

/**
 * XBeeConnectionMemoTest.java
 *
 * Description:	tests for the jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo
 * class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
@RunWith(MockitoJUnitRunner.class)
public class XBeeConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {
        
    @Mock private XBeeTrafficController tc;
    @Mock private XBeeNetwork xn;
    private XBeeAdapter xa;
    private XBeeDevice xb;

    private XBeeConnectionMemo memo = null;

    @Override
    @Test
    public void testProvidesConsistManager(){
       Assert.assertFalse("Provides ConsistManager",scm.provides(jmri.ConsistManager.class));
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        memo = new XBeeConnectionMemo();
        memo.setTrafficController(tc);
        xa= new XBeeAdapter(){
           @Override
           public boolean isOpen(){
              return true;
           }
        };  
        xb = new XBeeDevice(xa){
           @Override
           public XBeeNetwork getNetwork(){
              return xn;
           }
        };
        Mockito.when(tc.getXBee()).thenReturn(xb);
        memo.configureManagers();
        scm = memo;
    }

    @After
    @Override
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
