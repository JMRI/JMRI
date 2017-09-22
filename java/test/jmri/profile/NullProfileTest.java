package jmri.profile;

import java.io.File;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class NullProfileTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testCTor() throws java.io.IOException {
        File profileFolder = new File(folder.newFolder(Profile.PROFILE), "test");
        NullProfile instance = new NullProfile("test", "test", profileFolder);
        Assert.assertNotNull("exists",instance);
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

    // private final static Logger log = LoggerFactory.getLogger(NullProfileTest.class);

}
