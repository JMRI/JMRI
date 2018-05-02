package jmri.jmrix.ecos;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * JUnit tests for the EcosPortController class
 * <p>
 *
 * @author      Paul Bender Copyright (C) 2016
 */
public class EcosPortControllerTest extends jmri.jmrix.AbstractNetworkPortControllerTestBase {

    @Override
    @Before
    public void setUp(){
       JUnitUtil.setUp();
       EcosSystemConnectionMemo memo = new EcosSystemConnectionMemo();
       apc = new EcosPortController(memo){
           @Override
           public boolean status(){
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
       JUnitUtil.tearDown();
    }
}
