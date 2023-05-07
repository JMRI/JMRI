package jmri.util;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PortNameMapperTest {

    // no testCtor as tested class only supplies static methods

    @Test
    public void testDefaultIsNotFound() {
        Assertions.assertEquals("",PortNameMapper.getPortFromName("Not A Name"));
        Assertions.assertEquals("",PortNameMapper.getPortFromName("Not A Name Either"));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PortNameMapperTest.class);

}
