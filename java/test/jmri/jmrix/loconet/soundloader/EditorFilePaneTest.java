package jmri.jmrix.loconet.soundloader;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.io.File;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class EditorFilePaneTest {

    @Test
    public void testCTor() throws java.io.IOException {
        File testFile = new File("java/test/jmri/jmrix/loconet/spjfile/test.spj");
        EditorFilePane t = new EditorFilePane(testFile);
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

}
