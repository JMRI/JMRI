package jmri.jmrix.can.cbus.swing;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2018
 * @author Steve Young Copyright (C) 2018
 */
public class TextAreaFIFOTest {

    //  public int getLineCount( TextAreaFIFO t ) {
    //      log.warn("returning {}",t.getDocument().getDefaultRootElement().getElementCount());
    //  return t.getDocument().getDefaultRootElement().getElementCount();
    //  }
    
    @Test
    public void testCTor() {
        TextAreaFIFO t = new TextAreaFIFO(1);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TextAreaFIFOTest.class);

}
