package jmri.jmrix.loconet.soundloader;

import jmri.jmrix.loconet.spjfile.SpjFile;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(EditorTableDataModelTest.class);

}
