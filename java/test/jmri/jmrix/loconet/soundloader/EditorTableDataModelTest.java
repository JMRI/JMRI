package jmri.jmrix.loconet.soundloader;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.loconet.spjfile.SpjFile;
/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class EditorTableDataModelTest {

    @Test
    public void testCTor() {
        SpjFile testFile = new SpjFile(new java.io.File("java/test/jmri/jmrix/loconet/spjfile/test.spj"));
        EditorTableDataModel t = new EditorTableDataModel(testFile);
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

    private final static Logger log = LoggerFactory.getLogger(EditorTableDataModelTest.class.getName());

}
