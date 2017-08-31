package jmri.jmrix.jmriclient.json;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * JUnit tests for the JsonNetworkPortController class
 * <p>
 *
 * @author      Paul Bender Copyright (C) 2016
 */
public class JsonNetworkPortControllerTest extends jmri.jmrix.AbstractNetworkPortControllerTestBase {

    @Override
    @Before
    public void setUp(){
       JUnitUtil.setUp();
       apc = new JsonNetworkPortController(){
            @Override
            public void configure(){
            }
       };
    }

    @Override
    @After
    public void tearDown(){
       JUnitUtil.tearDown();
    }
}
