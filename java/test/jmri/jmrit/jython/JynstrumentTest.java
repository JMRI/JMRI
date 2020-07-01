package jmri.jmrit.jython;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class JynstrumentTest {

    @Test
    public void testCTor() {
        Jynstrument t = new Jynstrument(){
           @Override
           public String getExpectedContextClassName(){
              return "Test Jynstrument";
           }

           @Override
           public void init(){
           }

           @Override
           protected void quit(){
           }
        };
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(JynstrumentTest.class);

}
