package jmri.jmrix.lenz;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AbstractXNetSerialConnectionConfigTest.class);

}
