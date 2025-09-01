package jmri.util;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class JmriLocalEntityResolverTest {

    @Test
    public void testCTor() {
        JmriLocalEntityResolver t = new JmriLocalEntityResolver();
        Assertions.assertNotNull( t, "exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(JmriLocalEntityResolverTest.class);

}
