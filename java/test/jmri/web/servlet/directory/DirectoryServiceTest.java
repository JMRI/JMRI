package jmri.web.servlet.directory;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Randall Wood Copyright 2017
 */
public class DirectoryServiceTest {

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    @Test
    public void testCtor() {
        DirectoryService instance = new DirectoryService();
        Assert.assertNotNull(instance);
    }

}
