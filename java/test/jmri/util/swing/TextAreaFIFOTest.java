package jmri.util.swing;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2018
 * @author Steve Young Copyright (C) 2018
 */
public class TextAreaFIFOTest {

    // String newLine = System.getProperty("line.separator");
    
    @Test
    public void testCTor() {
        TextAreaFIFO t = new TextAreaFIFO(2);
        Assertions.assertNotNull( t, "exists");
    }

    @Test
    public void testLongTextTrimmedInBlocks() {
        TextAreaFIFO t = new TextAreaFIFO(100);
        int[] removes = {0};
        t.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                removes[0]++;
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
        for (int i = 0; i < 1000; i++) {
            int n = i;
            ThreadingUtil.runOnGUI(() -> t.append("line " + n + "\n")); // each line shown before the next
        }
        ThreadingUtil.runOnGUI(() -> {}); // after the trimming

        int lines = t.getLineCount() - 1; // the last is the empty line after the last newline
        Assertions.assertTrue(lines >= 100 && lines <= 110, "kept the maximum, up to a tenth more: " + lines);
        Assertions.assertTrue(t.getText().endsWith("line 999\n"), "kept the last lines");
        Assertions.assertTrue(removes[0] <= 100, "trimmed in blocks, not a line at a time: " + removes[0] + " removals");
    }

    @Test
    public void testShortTextTrimmedToTheLine() {
        TextAreaFIFO t = new TextAreaFIFO(2);
        for (int i = 0; i < 5; i++) {
            int n = i;
            ThreadingUtil.runOnGUI(() -> t.append("line " + n + "\n"));
        }
        ThreadingUtil.runOnGUI(() -> {});
        Assertions.assertEquals("line 3\nline 4\n", t.getText());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TextAreaFIFOTest.class);

}
