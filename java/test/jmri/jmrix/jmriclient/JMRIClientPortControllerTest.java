package jmri.jmrix.jmriclient;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * JUnit tests for the JMRIClientPortController class.
 *
 * @author      Paul Bender Copyright (C) 2016
 */
public class JMRIClientPortControllerTest extends jmri.jmrix.AbstractNetworkPortControllerTestBase {

    @Override
    @Before
    public void setUp(){
       JUnitUtil.setUp();
       JMRIClientSystemConnectionMemo memo = new JMRIClientSystemConnectionMemo();
       apc = new JMRIClientPortController(memo){
            @Override
            public void configure(){
            }
       };
    }

    @Override
    @After
    public void tearDown(){
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
       JUnitUtil.tearDown();
    }

}
