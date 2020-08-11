package jmri.profile;

import java.io.File;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class NullProfileTest {

    @Test
    public void testCTor(@TempDir File folder) throws java.io.IOException {
        File profileFolder = new File(folder, "test");
        NullProfile instance = new NullProfile("test", "test", profileFolder);
        Assert.assertNotNull("exists",instance);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(NullProfileTest.class);

}
