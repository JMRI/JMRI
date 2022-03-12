package jmri.jmrix.cmri.serial;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * JUnit tests for the SerialNetworkPortAdapter class.
 *
 * @author      Paul Bender Copyright (C) 2016
 */
public class SerialNetworkPortAdapterTest extends jmri.jmrix.AbstractNetworkPortControllerTestBase {

    @Override
    @BeforeEach
    public void setUp(){
       JUnitUtil.setUp();
       jmri.jmrix.cmri.CMRISystemConnectionMemo memo = new jmri.jmrix.cmri.CMRISystemConnectionMemo();
       apc = new SerialNetworkPortAdapter(memo){
            @Override
            public void configure(){
            }
       };
    }

    @Override
    @AfterEach
    public void tearDown(){
       JUnitUtil.tearDown();
    }

}
