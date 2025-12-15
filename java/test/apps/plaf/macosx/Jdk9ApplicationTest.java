package apps.plaf.macosx;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the Jdk9Application class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class Jdk9ApplicationTest  {

    @Test
    public void testCtor(){
        Assertions.assertNotNull(new Jdk9Application());
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
