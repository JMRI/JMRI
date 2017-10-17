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
public class ProfileConfigurationTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();


    @Test
    public void testCTor() throws java.io.IOException {
        File profileFolder = new File(folder.newFolder(Profile.PROFILE), "test");
        Profile instance = new Profile("test", "test", profileFolder);
        instance.setName("saved");

        ProfileConfiguration t = new ProfileConfiguration(instance);
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

    // private final static Logger log = LoggerFactory.getLogger(ProfileConfigurationTest.class);

}
