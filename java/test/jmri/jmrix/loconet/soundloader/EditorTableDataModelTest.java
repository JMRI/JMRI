package jmri.jmrix.loconet.soundloader;

import jmri.jmrix.loconet.spjfile.SpjFile;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(EditorTableDataModelTest.class);

}
