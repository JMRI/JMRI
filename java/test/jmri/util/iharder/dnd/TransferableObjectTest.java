package jmri.util.iharder.dnd;

import java.io.File;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TransferableObjectTest {

    @Test
    public void testCTor(@TempDir File folder) throws java.io.IOException  {
        TransferableObject t = new TransferableObject(folder);
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TransferableObjectTest.class.getName());

}
