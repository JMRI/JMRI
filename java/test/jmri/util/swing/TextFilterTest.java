package jmri.util.swing;

import java.io.File;
import java.io.IOException;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 *
 * @author Randall Wood Copyright 2017
 */
public class TextFilterTest {

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    @Test
    public void testConstructor() {
        TextFilter instance = new TextFilter();
        Assert.assertNotNull(instance);
    }

    @Test
    public void testAccept(@TempDir File folder) throws IOException {
        File dir = folder;
        File txt = new File(folder, "text.txt");
        File xml = new File(folder, "xml.xml");
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
