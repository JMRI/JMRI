package jmri.jmrix.dccpp;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the DCCppNetworkPortController class
 * <p>
 *
 * @author      Paul Bender Copyright (C) 2016
 */
public class DCCppNetworkPortControllerTest extends jmri.jmrix.AbstractNetworkPortControllerTestBase {

    @Override
    @Before
    public void setUp(){
       apps.tests.Log4JFixture.setUp();
       JUnitUtil.resetInstanceManager();
       DCCppInterfaceScaffold tc = new DCCppInterfaceScaffold(new DCCppCommandStation());

       DCCppSystemConnectionMemo memo = new DCCppSystemConnectionMemo(tc);
       apc = new DCCppNetworkPortController(memo){
            @Override
            public boolean status(){
              return true;
            }
            @Override
            public boolean okToSend(){
              return true;
            }
            @Override
            public void configure(){
            }
       };
    }

    @Override
    @After
    public void tearDown(){
       JUnitUtil.resetInstanceManager();
       apps.tests.Log4JFixture.tearDown();
    }
}
