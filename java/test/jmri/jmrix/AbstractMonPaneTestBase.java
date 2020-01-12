package jmri.jmrix;

import java.awt.GraphicsEnvironment;
import jmri.util.JmriJFrame;
import org.junit.After;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

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

    // implementing classes must override setUp and tearDown.
    // to set pane.
    @Before
    @Override
    public void setUp() {
        pane = new AbstractMonPane() {
            @Override
            public String getTitle() {
                return "title";
            }

            @Override
            protected void init() {
            }
        };
    }

    @After
    @Override
    public void tearDown() {
        pane = null;
    }

    @Test
    public void testConcreteCtor() throws Exception {
        pane.initComponents();
    }

    @Test
    public void testInsertLine() throws Exception {
        pane.initComponents();

        pane.entryField.setText("foo");
        pane.enterButtonActionPerformed(null);

        JUnitUtil.waitFor(() -> {
            return pane.getFrameText().equals("foo\n");
        }, "frame text");
        Assert.assertEquals("foo\n", pane.getFrameText());

        pane.entryField.setText("bar");
        pane.enterButtonActionPerformed(null);

        JUnitUtil.waitFor(() -> {
            return pane.getFrameText().equals("foo\nbar\n");
        }, "frame text");
        Assert.assertEquals("foo\nbar\n", pane.getFrameText());
    }

    @Test
    public void testClearButton() throws Exception {
        pane.initComponents();

        pane.entryField.setText("foo");
        pane.enterButtonActionPerformed(null);

        pane.clearButtonActionPerformed(null);

        JUnitUtil.waitFor(() -> {
            return pane.getFrameText().equals("");
        }, "frame text");
        Assert.assertEquals("", pane.getFrameText());
    }

    @Test
    public void testFreezeButton() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AbstractMonPaneScaffold s = new AbstractMonPaneScaffold(pane);

        // for Jemmy to work, we need the pane inside of a frame
        JmriJFrame f = new JmriJFrame();
        try {
            pane.initComponents();
        } catch (Exception ex) {
            Assert.fail("Could not load pane: " + ex);
        }
        f.add(pane);
        // set title if available
        if (pane.getTitle() != null) {
            f.setTitle(pane.getTitle());
        }
        f.pack();
        f.setVisible(true);

        Assert.assertFalse(s.getFreezeButtonState());

        // there is no label on the entryField, so we access that directly.
        pane.entryField.setText("foo");
        s.clickEnterButton();
        s.clickFreezeButton();
        Assert.assertTrue(s.getFreezeButtonState());

        pane.entryField.setText("bar");
        s.clickEnterButton();

        JUnitUtil.waitFor(() -> {
            return pane.getFrameText().equals("foo\n");
        }, "frame text");
        Assert.assertEquals("foo\n", pane.getFrameText());

        f.dispose();
    }

    @Test
    public void testFilterFormatting() throws Exception {

        pane.initComponents();

        pane.setFilterText("00");
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        Assert.assertEquals("filter field unedited", "00", pane.getFilterText());

        pane.setFilterText("A0");
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        Assert.assertEquals("filter field unedited", "A0", pane.getFilterText());

        pane.setFilterText("#");
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        Assert.assertEquals("filter field rejected", "", pane.getFilterText());

        pane.setFilterText("ab");
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        Assert.assertEquals("filter field edited", "AB", pane.getFilterText());
    }

    // Test checking the Time Stamp checkbox.
    @Test
    public void checkTimeStampCheckBox() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AbstractMonPaneScaffold s = new AbstractMonPaneScaffold(pane);

        // for Jemmy to work, we need the pane inside of a frame
        JmriJFrame f = new JmriJFrame();
        try {
            pane.initComponents();
        } catch (Exception ex) {
            Assert.fail("Could not load pane: " + ex);
        }
        f.add(pane);
        // set title if available
        if (pane.getTitle() != null) {
            f.setTitle(pane.getTitle());
        }
        f.pack();
        f.setVisible(true);
        s.checkTimeStampCheckBox();
        Assert.assertTrue(s.getTimeStampCheckBoxValue());
        f.dispose();
    }

    // Test checking the Raw checkbox.
    @Test
    public void checkRawCheckBox() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AbstractMonPaneScaffold s = new AbstractMonPaneScaffold(pane);

        // for Jemmy to work, we need the pane inside of a frame
        JmriJFrame f = new JmriJFrame();
        try {
            pane.initComponents();
        } catch (Exception ex) {
            Assert.fail("Could not load pane: " + ex);
        }
        f.add(pane);
        // set title if available
        if (pane.getTitle() != null) {
            f.setTitle(pane.getTitle());
        }
        f.pack();
        f.setVisible(true);
        s.checkRawCheckBox();
        Assert.assertTrue(s.getRawCheckBoxValue());
        f.dispose();
    }

    // Test checking the Always On Top checkbox.
    @Test
    public void checkOnTopCheckBox() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AbstractMonPaneScaffold s = new AbstractMonPaneScaffold(pane);

        // for Jemmy to work, we need the pane inside of a frame
        JmriJFrame f = new JmriJFrame();
        try {
            pane.initComponents();
        } catch (Exception ex) {
            Assert.fail("Could not load pane: " + ex);
        }
        f.add(pane);
        // set title if available
        if (pane.getTitle() != null) {
            f.setTitle(pane.getTitle());
        }
        f.pack();
        f.setVisible(true);
        s.checkOnTopCheckBox();
        Assert.assertTrue(s.getOnTopCheckBoxValue());
        f.dispose();
    }

    // Test checking the AutoScroll checkbox.
    @Test
    public void checkAutoScrollCheckBox() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AbstractMonPaneScaffold s = new AbstractMonPaneScaffold(pane);

        // for Jemmy to work, we need the pane inside of a frame
        JmriJFrame f = new JmriJFrame();
        try {
            pane.initComponents();
        } catch (Exception ex) {
            Assert.fail("Could not load pane: " + ex);
        }
        f.add(pane);
        // set title if available
        if (pane.getTitle() != null) {
            f.setTitle(pane.getTitle());
        }
        f.pack();
        f.setVisible(true);
        Assert.assertTrue(s.getAutoScrollCheckBoxValue());
        s.checkAutoScrollCheckBox();
        Assert.assertFalse(s.getAutoScrollCheckBoxValue());
        f.setVisible(false);
        f.dispose();
    }

}
