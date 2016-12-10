package jmri.jmrix.lenz;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the XNetNetworkPortController class
 * <p>
 *
 * @author      Paul Bender Copyright (C) 2016
 */
public class XNetNetworkPortControllerTest extends jmri.jmrix.AbstractNetworkPortControllerTest {

    @Override
    @Before
    public void setUp(){
       apps.tests.Log4JFixture.setUp();
       JUnitUtil.resetInstanceManager();
       apc = new XNetNetworkPortController(){
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
