package jmri.util.swing;

import java.io.File;
import java.io.IOException;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Randall Wood Copyright 2017
 */
public class TextFilterTest {

    @Test
    public void testConstructor() {
        TextFilter instance = new TextFilter();
        assertNotNull(instance);
    }

    @Test
    public void testAccept(@TempDir File folder) throws IOException {
        File dir = folder;
        File txt = new File(folder, "text.txt");
        File xml = new File(folder, "xml.xml");
        TextFilter instance = new TextFilter();
        assertTrue(instance.accept(dir));
        assertTrue(instance.accept(txt));
        assertFalse(instance.accept(xml));
    }

    @Test
    public void testGetDescription() {
        TextFilter instance = new TextFilter();
        assertEquals("Text Documents (*.txt)", instance.getDescription());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
