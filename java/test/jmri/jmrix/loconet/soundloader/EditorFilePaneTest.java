package jmri.jmrix.loconet.soundloader;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class EditorFilePaneTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testCTor() {
        EditorFilePane t = new EditorFilePane(folder.newFile("testFile"));
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

    private final static Logger log = LoggerFactory.getLogger(EditorFilePaneTest.class.getName());

}
