package jmri.jmrix;

import java.awt.GraphicsEnvironment;

import jmri.util.JmriJFrame;
import jmri.util.ThreadingUtil;

import org.assertj.swing.edt.GuiActionRunner;

import jmri.util.JUnitUtil;

import org.junit.Assume;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

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
        Throwable thrown = catchThrowable( () -> GuiActionRunner.execute(() -> pane.initComponents()));
        assertThat(thrown).isNull();
    }

    @Test
    public void testInsertLine() {
        Throwable thrown = catchThrowable( () -> GuiActionRunner.execute( () ->  pane.initComponents()));
        assertThat(thrown).isNull();

        setFrameTextOnGUIThread("foo");

        JUnitUtil.waitFor(() -> {
            return getFrameTextONGUIThread().equals("foo\n");
        }, "frame text");
        assertThat("foo\n").isEqualTo(getFrameTextONGUIThread());

        setFrameTextOnGUIThread("bar");

        JUnitUtil.waitFor(() -> {
            return getFrameTextONGUIThread().equals("foo\nbar\n");
        }, "frame text");
        assertThat("foo\nbar\n").isEqualTo(getFrameTextONGUIThread());
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
        Throwable thrown = catchThrowable( () -> GuiActionRunner.execute( () ->  pane.initComponents()));
        assertThat(thrown).isNull();

        setFrameTextOnGUIThread("foo");

        ThreadingUtil.runOnGUI( () -> pane.clearButtonActionPerformed(null));

        JUnitUtil.waitFor(() -> {
            return getFrameTextONGUIThread().equals("");
        }, "frame text");
        assertThat("").isEqualTo(getFrameTextONGUIThread());
    }

    @Test
    public void testFreezeButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));
        AbstractMonPaneScaffold s = new AbstractMonPaneScaffold(pane);

        // for Jemmy to work, we need the pane inside of a frame
        JmriJFrame f = new JmriJFrame();
        Throwable thrown = catchThrowable( () -> GuiActionRunner.execute( () ->  pane.initComponents()));
        assertThat(thrown).isNull();

        ThreadingUtil.runOnGUI( () -> {
            f.add(pane);
            // set title if available
            if (pane.getTitle() != null) {
                f.setTitle(pane.getTitle());
            }
            f.pack();
            f.setVisible(true);
        });

        assertThat(s.getFreezeButtonState()).isFalse();

        s.enterTextInEntryField("foo");
        s.clickEnterButton();
        s.clickFreezeButton();
        
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        assertThat(s.getFreezeButtonState()).isTrue();

        s.enterTextInEntryField("bar");
        s.clickEnterButton();

        new org.netbeans.jemmy.QueueTool().waitEmpty(100);

        assertThat("foo\n").isEqualTo(getFrameTextONGUIThread());

        ThreadingUtil.runOnGUI( () -> {
            f.setVisible(false);
            f.dispose();
        });
    }

    @Test
    public void testFilterFormatting() {
        Throwable thrown = catchThrowable( () -> GuiActionRunner.execute( () ->  pane.initComponents()));
        assertThat(thrown).isNull();

        setAndCheckFilterTextEntry("00", "00", "filter field unedited");

        setAndCheckFilterTextEntry("A0", "A0", "filter field unedited");

        setAndCheckFilterTextEntry("#", "", "filter field rejected");

        setAndCheckFilterTextEntry("ab", "AB", "filter field edited");
    }

    protected void setAndCheckFilterTextEntry(String entryText, String resultText, String errorMessage) {
        ThreadingUtil.runOnGUI( () -> pane.setFilterText(entryText));
        assertThat(resultText).withFailMessage(errorMessage)
                .isEqualTo(ThreadingUtil.runOnGUIwithReturn( () -> pane.getFilterText()));
    }

    // Test checking the Time Stamp checkbox.
    @Test
    public void checkTimeStampCheckBox() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AbstractMonPaneScaffold s = new AbstractMonPaneScaffold(pane);

        // for Jemmy to work, we need the pane inside of a frame
        JmriJFrame f = new JmriJFrame();
        Throwable thrown = catchThrowable( () -> GuiActionRunner.execute( () ->  pane.initComponents()));
        assertThat(thrown).isNull();

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
        assertThat(s.getTimeStampCheckBoxValue()).isTrue();
        ThreadingUtil.runOnGUI( () -> {
            f.setVisible(false);
            f.dispose();
        });    }

    // Test checking the Raw checkbox.
    @Test
    public void checkRawCheckBox() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AbstractMonPaneScaffold s = new AbstractMonPaneScaffold(pane);

        // for Jemmy to work, we need the pane inside of a frame
        JmriJFrame f = new JmriJFrame();
        Throwable thrown = catchThrowable( () -> GuiActionRunner.execute( () ->  pane.initComponents()));
        assertThat(thrown).isNull();

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
        assertThat(s.getRawCheckBoxValue()).isTrue();
        ThreadingUtil.runOnGUI( () -> {
            f.setVisible(false);
            f.dispose();
        });   }

    // Test checking the Always On Top checkbox.
    @Test
    public void checkOnTopCheckBox() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AbstractMonPaneScaffold s = new AbstractMonPaneScaffold(pane);

        // for Jemmy to work, we need the pane inside of a frame
        JmriJFrame f = new JmriJFrame();
        Throwable thrown = catchThrowable( () -> GuiActionRunner.execute( () ->  pane.initComponents()));
        assertThat(thrown).isNull();

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
        assertThat(s.getOnTopCheckBoxValue()).isTrue();
        ThreadingUtil.runOnGUI( () -> {
            f.setVisible(false);
            f.dispose();
        });
    }

    // Test checking the AutoScroll checkbox.
    @Test
    public void checkAutoScrollCheckBox() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AbstractMonPaneScaffold s = new AbstractMonPaneScaffold(pane);

        // for Jemmy to work, we need the pane inside of a frame
        JmriJFrame f = new JmriJFrame();
        Throwable thrown = catchThrowable( () -> GuiActionRunner.execute( () ->  pane.initComponents()));
        assertThat(thrown).isNull();

        ThreadingUtil.runOnGUI( () -> {
            f.add(pane);
            // set title if available
            if (pane.getTitle() != null) {
                f.setTitle(pane.getTitle());
            }
            f.pack();
            f.setVisible(true);
        });
        assertThat(s.getAutoScrollCheckBoxValue()).isTrue();
        s.checkAutoScrollCheckBox();
        assertThat(s.getAutoScrollCheckBoxValue()).isFalse();
        ThreadingUtil.runOnGUI( () -> {
            f.setVisible(false);
            f.dispose();
        });
    }

}
