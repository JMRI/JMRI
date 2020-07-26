package jmri.managers.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DefaultUserMessagePreferencesXmlTest {

    @Test
    public void testCTor() {
        DefaultUserMessagePreferencesXml t = new DefaultUserMessagePreferencesXml();
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

    // private final static Logger log = LoggerFactory.getLogger(DefaultUserMessagePreferencesXmlTest.class);

}
