package jmri.jmrix.jmriclient;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class JMRIClientConnectionTypeListTest {

    @Test
    public void testCTor() {
        JMRIClientConnectionTypeList t = new JMRIClientConnectionTypeList();
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

    // private final static Logger log = LoggerFactory.getLogger(JMRIClientConnectionTypeListTest.class);

}
