package jmri.util.swing;

import java.io.File;
import java.io.IOException;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Randall Wood Copyright 2017
 */
public class TextFilterTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    @Test
    public void testConstructor() {
        TextFilter instance = new TextFilter();
        Assert.assertNotNull(instance);
    }

    @Test
    public void testAccept() throws IOException {
        File dir = folder.newFolder();
        File txt = folder.newFile("text.txt");
        File xml = folder.newFile("xml.xml");
        TextFilter instance = new TextFilter();
        Assert.assertTrue(instance.accept(dir));
        Assert.assertTrue(instance.accept(txt));
        Assert.assertFalse(instance.accept(xml));
    }

    @Test
    public void testGetDescription() {
        TextFilter instance = new TextFilter();
        Assert.assertEquals("Text Documents (*.txt)", instance.getDescription());
    }

}
