package jmri.jmris.simpleserver.parser;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SimpleVisitorTest {

    @Test
    public void testCTor() {
        SimpleVisitor t = new SimpleVisitor();
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SimpleVisitorTest.class);

}
