package jmri.jmrix;

import jmri.util.JmriJFrame;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit tests for the AbstractMonPane class
 * <p>
 * Not intended to be run by itself, but rather as part of inherited tests
 * <p>
 * Copyright: Copyright (c) 2015</p>
 *
 * @author Bob Jacobsen
 * @author Paul Bender Copyright (C) 2016
 */
public abstract class AbstractMonPaneTestBase extends jmri.util.swing.JmriPanelTest {

    // implementing classes must set pane to the pane under test in setUp.
    protected AbstractMonPane pane = null;

    // implementing classes must override setUp to set pane
    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    @Override
    public void tearDown() {
        pane = null;
        JUnitUtil.tearDown();
    }

    @Test
    public void testConcreteCtor() {
        assertDoesNotThrow( () -> ThreadingUtil.runOnGUIWithJmriException(() -> pane.initComponents() ));
    }

    @Test
    public void testInsertLine() {
        assertDoesNotThrow( () -> ThreadingUtil.runOnGUIWithJmriException( () -> pane.initComponents() ));

        setFrameTextOnGUIThread("foo");

        JUnitUtil.waitFor(() -> {
            return getFrameTextONGUIThread().equals("foo\n");
        }, "frame text");
        assertEquals("foo\n", getFrameTextONGUIThread());

        setFrameTextOnGUIThread("bar");

        JUnitUtil.waitFor(() -> {
            return getFrameTextONGUIThread().equals("foo\nbar\n");
        }, "frame text");
        assertEquals("foo\nbar\n", getFrameTextONGUIThread());
    }

    private void setFrameTextOnGUIThread(String text) {
        ThreadingUtil.runOnGUI(() -> {
            pane.entryField.setText(text);
            pane.enterButtonActionPerformed(null);
        });
    }

    protected String getFrameTextONGUIThread() {
        return ThreadingUtil.runOnGUIwithReturn(() -> pane.getFrameText());
    }

    @Test
    public void testClearButton() {
        assertDoesNotThrow( () -> ThreadingUtil.runOnGUI( () -> pane.initComponents() ));

        setFrameTextOnGUIThread("foo");

        ThreadingUtil.runOnGUI( () -> pane.clearButtonActionPerformed(null));

        JUnitUtil.waitFor(() -> {
            return getFrameTextONGUIThread().equals("");
        }, "frame text");
        assertEquals("", getFrameTextONGUIThread());
    }

    @Test
    @DisabledIfHeadless
    public void testFreezeButton() {
        Assumptions.assumeFalse( Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"), "Ignoring intermittent test");
        AbstractMonPaneScaffold s = new AbstractMonPaneScaffold(pane);

        // for Jemmy to work, we need the pane inside of a frame
        JmriJFrame f = new JmriJFrame();
        assertDoesNotThrow( () -> ThreadingUtil.runOnGUI( () -> pane.initComponents() ));

        ThreadingUtil.runOnGUI( () -> {
            f.add(pane);
            // set title if available
            if (pane.getTitle() != null) {
                f.setTitle(pane.getTitle());
            }
            f.pack();
            f.setVisible(true);
        });

        assertFalse(s.getFreezeButtonState());

        s.enterTextInEntryField("foo");
        s.clickEnterButton();
        s.clickFreezeButton();
        
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        assertTrue(s.getFreezeButtonState());

        s.enterTextInEntryField("bar");
        s.clickEnterButton();

        new org.netbeans.jemmy.QueueTool().waitEmpty(100);

        assertEquals("foo\n", getFrameTextONGUIThread());

        ThreadingUtil.runOnGUI( () -> {
            f.setVisible(false);
            f.dispose();
        });
    }

    @Test
    public void testFilterFormatting() {
        assertDoesNotThrow( () -> ThreadingUtil.runOnGUI( () -> pane.initComponents() ));

        setAndCheckFilterTextEntry("00", "00", "filter field unedited");

        setAndCheckFilterTextEntry("A0", "A0", "filter field unedited");

        setAndCheckFilterTextEntry("#", "", "filter field rejected");

        setAndCheckFilterTextEntry("ab", "AB", "filter field edited");
    }

    protected void setAndCheckFilterTextEntry(String entryText, String resultText, String errorMessage) {
        ThreadingUtil.runOnGUI( () -> pane.setFilterText(entryText));
        assertEquals(resultText,
            ThreadingUtil.runOnGUIwithReturn( () -> pane.getFilterText()), errorMessage);
    }

    // Test checking the Time Stamp checkbox.
    @Test
    @DisabledIfHeadless
    public void checkTimeStampCheckBox() {
        AbstractMonPaneScaffold s = new AbstractMonPaneScaffold(pane);

        // for Jemmy to work, we need the pane inside of a frame
        JmriJFrame f = new JmriJFrame();
        assertDoesNotThrow( () -> ThreadingUtil.runOnGUI( () -> pane.initComponents() ));

        ThreadingUtil.runOnGUI( () -> {
                    f.add(pane);
                    // set title if available
                    if (pane.getTitle() != null) {
                        f.setTitle(pane.getTitle());
                    }
                    f.pack();
                    f.setVisible(true);
                });

        s.checkTimeStampCheckBox();
        assertTrue(s.getTimeStampCheckBoxValue());
        ThreadingUtil.runOnGUI( () -> {
            f.setVisible(false);
            f.dispose();
        });    }

    // Test checking the Raw checkbox.
    @Test
    @DisabledIfHeadless
    public void checkRawCheckBox() {
        AbstractMonPaneScaffold s = new AbstractMonPaneScaffold(pane);

        // for Jemmy to work, we need the pane inside of a frame
        JmriJFrame f = new JmriJFrame();
        assertDoesNotThrow( () -> ThreadingUtil.runOnGUI( () -> pane.initComponents() ));

        ThreadingUtil.runOnGUI( () -> {
            f.add(pane);
            // set title if available
            if (pane.getTitle() != null) {
                f.setTitle(pane.getTitle());
            }
            f.pack();
            f.setVisible(true);
        });
        s.checkRawCheckBox();
        assertTrue(s.getRawCheckBoxValue());
        ThreadingUtil.runOnGUI( () -> {
            f.setVisible(false);
            f.dispose();
        });   }

    // Test checking the Always On Top checkbox.
    @Test
    @DisabledIfHeadless
    public void checkOnTopCheckBox() {
        AbstractMonPaneScaffold s = new AbstractMonPaneScaffold(pane);

        // for Jemmy to work, we need the pane inside of a frame
        JmriJFrame f = new JmriJFrame();
        assertDoesNotThrow( () -> ThreadingUtil.runOnGUI( () -> pane.initComponents() ));

        ThreadingUtil.runOnGUI( () -> {
            f.add(pane);
            // set title if available
            if (pane.getTitle() != null) {
                f.setTitle(pane.getTitle());
            }
            f.pack();
            f.setVisible(true);
        });
        s.checkOnTopCheckBox();
        assertTrue(s.getOnTopCheckBoxValue());
        ThreadingUtil.runOnGUI( () -> {
            f.setVisible(false);
            f.dispose();
        });
    }

    // Test checking the AutoScroll checkbox.
    @Test
    @DisabledIfHeadless
    public void checkAutoScrollCheckBox() {
        AbstractMonPaneScaffold s = new AbstractMonPaneScaffold(pane);

        // for Jemmy to work, we need the pane inside of a frame
        JmriJFrame f = new JmriJFrame();
        assertDoesNotThrow( () -> ThreadingUtil.runOnGUI( () -> pane.initComponents() ));

        ThreadingUtil.runOnGUI( () -> {
            f.add(pane);
            // set title if available
            if (pane.getTitle() != null) {
                f.setTitle(pane.getTitle());
            }
            f.pack();
            f.setVisible(true);
        });
        assertTrue(s.getAutoScrollCheckBoxValue());
        s.checkAutoScrollCheckBox();
        assertFalse(s.getAutoScrollCheckBoxValue());
        ThreadingUtil.runOnGUI( () -> {
            f.setVisible(false);
            f.dispose();
        });
    }

}
