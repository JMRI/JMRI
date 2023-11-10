package jmri.util.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ExceptionContextTest {
        
    protected ExceptionContext ec; 

    @Test
    public void testCTor() {
        Assertions.assertNotNull(ec, "exists");
    }

    @Test
    public void testGetTitle() {
        Assertions.assertEquals("Test",ec.getTitle(), "Title");
    }

    @Test
    public void testGetOperation() {
        Assertions.assertEquals("Test Op",ec.getOperation(),"Operation");
    }

    @Test
    public void testGetHint() {
        Assertions.assertEquals("Test Hint",ec.getHint(),"Hint");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        ec = new ExceptionContext(new Exception("Test"),"Test Op","Test Hint");
    }

    @AfterEach
    public void tearDown() {
        ec = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ExceptionContextTest.class);

}
