package jmri.jmrix.loconet.soundloader;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
