package jmri.util.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class UnexpectedExceptionContextTest extends ExceptionContextTest {

    @Test
    @Override
    public void testGetHint() {
        assertEquals( Bundle.getMessage("UnexpectedExceptionOperationHint"),ec.getHint(), "Hint");
    }

    @Test
    @Override
    public void testGetTitle() {
        assertEquals(Bundle.getMessage("UnexpectedExceptionOperationTitle","Test"),ec.getTitle());
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();    
        ec = new UnexpectedExceptionContext(new Exception("Test"),"Test Op");
    }

    @AfterEach
    @Override
    public void tearDown() {
        ec = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(UnexpectedExceptionContextTest.class);

}
