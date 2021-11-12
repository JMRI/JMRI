package jmri.util;

import org.junit.Assert;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.*;

/**
 * Test FailingClass.
 * @author Daniel Bergqvist (c) 2021
 */
public class FailingClassTest {
    
    @Disabled
    @Test
    public void testAlwaysFails() {
        FailingClass obj = new FailingClass();
        Assert.assertNotNull(obj);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
