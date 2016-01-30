package jmri.jmrix;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.util.JUnitUtil;

/**
 * JUnit tests for the AbstractMonPane class
 * <p>
 * Copyright: Copyright (c) 2015</p>
 *
 * @author Bob Jacobsen
 */
public class AbstractMonPaneTest extends jmri.util.SwingTestCase {


    protected void setUp() {
    }

    protected void tearDown() {
    }

    public void testConcreteCtor() throws Exception {
        AbstractMonPane a = new AbstractMonPane() {
            public String getTitle() { return "title"; }
            protected void init() {}
        };
        
        a.initComponents();
    }

    public void testInsertLine() throws Exception {
        AbstractMonPane a = new AbstractMonPane() {
            public String getTitle() { return "title"; }
            protected void init() {}
        };
        
        a.initComponents();
        
        a.entryField.setText("foo");
        a.enterButtonActionPerformed(null);
        
        JUnitUtil.waitFor(()->{return a.getFrameText().equals("foo\n");}, "frame text");
        Assert.assertEquals("foo\n", a.getFrameText());

        a.entryField.setText("bar");
        a.enterButtonActionPerformed(null);

        JUnitUtil.waitFor(()->{return a.getFrameText().equals("foo\nbar\n");}, "frame text");
        Assert.assertEquals("foo\nbar\n", a.getFrameText());
    }

    public void testClearButton() throws Exception {
        AbstractMonPane a = new AbstractMonPane() {
            public String getTitle() { return "title"; }
            protected void init() {}
        };
        
        a.initComponents();
        
        a.entryField.setText("foo");
        a.enterButtonActionPerformed(null);
        
        a.clearButtonActionPerformed(null);
        
        JUnitUtil.waitFor(()->{return a.getFrameText().equals("");}, "frame text");
        Assert.assertEquals("", a.getFrameText());
    }

    public void testFreezeButton() throws Exception {
        AbstractMonPane a = new AbstractMonPane() {
            public String getTitle() { return "title"; }
            protected void init() {}
        };
        
        a.initComponents();
        
        Assert.assertFalse(a.freezeButton.isSelected());

        a.entryField.setText("foo");
        a.enterButtonActionPerformed(null);
        
        a.freezeButton.setSelected(true);
        flushAWT();
        
        a.entryField.setText("bar");
        a.enterButtonActionPerformed(null);

        JUnitUtil.waitFor(()->{return a.getFrameText().equals("foo\n");}, "frame text");
        Assert.assertEquals("foo\n", a.getFrameText());
    }

    public void testFilterFormatting() throws Exception {
        AbstractMonPane a = new AbstractMonPane() {
            public String getTitle() { return "title"; }
            protected void init() {}
        };
        
        a.initComponents();

        a.setFilterText("00");
        flushAWT();
        Assert.assertEquals("filter field unedited", "00", a.getFilterText());

        a.setFilterText("A0");
        flushAWT();
        Assert.assertEquals("filter field unedited", "A0", a.getFilterText());

        a.setFilterText("#");
        flushAWT();
        Assert.assertEquals("filter field rejected", "", a.getFilterText());

        a.setFilterText("ab");
        flushAWT();
        Assert.assertEquals("filter field edited", "AB", a.getFilterText());

    }


    // from here down is testing infrastructure

    public AbstractMonPaneTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        apps.tests.Log4JFixture.initLogging();
        String[] testCaseName = {AbstractMonPaneTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(AbstractMonPaneTest.class);
        return suite;
    }

}
