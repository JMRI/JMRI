package jmri.jmrix.lenz;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class AbstractXNetSerialConnectionConfigTest {

    @Test
    public void testCTor() {
        AbstractXNetSerialConnectionConfig t = new AbstractXNetSerialConnectionConfig(){
           @Override
           public void setInstance(){
           }
           @Override
           public String name(){
              return "test";
           }
        };
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AbstractXNetSerialConnectionConfigTest.class);

}
