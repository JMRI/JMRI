package jmri.util.junit.rules;

import java.io.File;
import jmri.util.*;
import org.junit.*;

/**
 * Test the JmriTemporaryFolder rule
 * @author Bob Jacobsen Copyright 2018	
 */
public class JmriTemporaryFolderTest {

    @Rule
    public JmriTemporaryFolder folder1 = new JmriTemporaryFolder();
    
    @Rule
    public JmriTemporaryFolder folder2 = new JmriTemporaryFolder();
    
    @Test
    public void testDifferent() throws java.io.IOException {
        File file1 = folder1.newFolder();
        File file2 = folder2.newFolder();
        Assert.assertFalse(file1.toString().equals(file2.toString()));
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

}
