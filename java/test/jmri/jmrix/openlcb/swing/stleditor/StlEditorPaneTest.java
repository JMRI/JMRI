package jmri.jmrix.openlcb.swing.stleditor;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the Bundle class
 *
 * @author Bob Jacobsen Copyright (C) 2012
 */
public class StlEditorPaneTest  {

    @Test public void testSeveralGetEnum() {
        Assert.assertEquals(StlEditorPane.Operator.AN, StlEditorPane.getEnum("AN"));

        Assert.assertEquals(StlEditorPane.Operator.Cp, StlEditorPane.getEnum(")"));

        Assert.assertEquals(StlEditorPane.Operator.ANp, StlEditorPane.getEnum("AN("));

        Assert.assertEquals(StlEditorPane.Operator.AN, StlEditorPane.getEnum("An"));

        Assert.assertEquals(StlEditorPane.Operator.ANp, StlEditorPane.getEnum("An("));
    }
    
    @Test public void testSeveralFindOperatorBoundary() {
        Assert.assertEquals(2, StlEditorPane.findOperatorBoundary("AN"));

        Assert.assertEquals(2, StlEditorPane.findOperatorBoundary("A("));

        Assert.assertEquals(2, StlEditorPane.findOperatorBoundary("A(O1.0"));

        Assert.assertEquals(1, StlEditorPane.findOperatorBoundary("AI2.4"));

        Assert.assertEquals(1, StlEditorPane.findOperatorBoundary("L#0#50"));

        Assert.assertEquals(-1, StlEditorPane.findOperatorBoundary("//"));
    }
}
