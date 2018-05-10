package jmri.util.swing;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class UnexpectedExceptionContextTest extends ExceptionContextTest {

    @Test
    @Override
    public void testGetHint() {
        Assert.assertEquals("Hint",Bundle.getMessage("UnexpectedExceptionOperationHint"),ec.getHint());
    }

    @Test
    @Override
    public void testGetTitle() {
        Assert.assertEquals("Title",Bundle.getMessage("UnexpectedExceptionOperationTitle",Exception.class.getSimpleName()),ec.getTitle());
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();    
        ec = new UnexpectedExceptionContext(new Exception("Test"),"Test Op");
    }

    @After
    @Override
    public void tearDown() {
        ec = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(UnexpectedExceptionContextTest.class);

}
