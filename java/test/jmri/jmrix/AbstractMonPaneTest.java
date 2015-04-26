package jmri.jmrix;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the AbstractMonPane class
 * <p>
 * Copyright: Copyright (c) 2015</p>
 *
 * @author Bob Jacobsen
 * @version $Revision$
 */
public class AbstractMonPaneTest extends TestCase {


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
        
        jmri.util.JUnitUtil.releaseThread(this, 20);
        Assert.assertEquals("foo\n", a.getFrameText());

        a.entryField.setText("bar");
        a.enterButtonActionPerformed(null);

        jmri.util.JUnitUtil.releaseThread(this, 20);
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
        
        jmri.util.JUnitUtil.releaseThread(this, 20);
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
        
        a.entryField.setText("bar");
        a.enterButtonActionPerformed(null);

        jmri.util.JUnitUtil.releaseThread(this, 20);
        Assert.assertEquals("foo\n", a.getFrameText());
    }

    // from here down is testing infrastructure

    public AbstractMonPaneTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {AbstractMonPaneTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(AbstractMonPaneTest.class);
        return suite;
    }

}
