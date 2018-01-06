package jmri.util.iharder.dnd;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class TransferableObjectTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testCTor() throws java.io.IOException  {
        TransferableObject t = new TransferableObject(folder.newFile());
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(TransferableObjectTest.class.getName());

}
