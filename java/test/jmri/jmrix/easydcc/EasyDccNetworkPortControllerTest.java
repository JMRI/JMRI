package jmri.jmrix.easydcc;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the EasyDccNetworkPortController class
 * <p>
 *
 * @author      Paul Bender Copyright (C) 2016
 */
public class EasyDccNetworkPortControllerTest extends jmri.jmrix.AbstractNetworkPortControllerTestBase {

    @Override
    @Before
    public void setUp(){
       apps.tests.Log4JFixture.setUp();
       JUnitUtil.resetInstanceManager();
       EasyDccSystemConnectionMemo memo = new EasyDccSystemConnectionMemo();
       apc = new EasyDccNetworkPortController(memo){
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
