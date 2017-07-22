package jmri.profile;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import java.io.File;

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
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ProfileConfigurationTest.class.getName());

}
