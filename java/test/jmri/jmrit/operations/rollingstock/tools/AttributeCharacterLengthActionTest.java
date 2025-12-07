package jmri.jmrit.operations.rollingstock.tools;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;

/**
 * @author Daniel Boudreau Copyright (C) 2025
 */
public class AttributeCharacterLengthActionTest extends OperationsTestCase {

    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        AttributeCharacterLengthAction a = new AttributeCharacterLengthAction();
        Assert.assertNotNull("exists", a);
        a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
        
        JmriJFrame f = JmriJFrame.getFrame("Change maximum character length");
        Assert.assertNotNull("frame exists", f);
        JUnitUtil.dispose(f);
    }
}
