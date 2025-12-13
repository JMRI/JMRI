package jmri.jmrit.operations.rollingstock.tools;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.setup.Control;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

/**
 * @author Daniel Boudreau Copyright (C) 2025
 */
public class AttributeCharacterLengthFrameTest extends OperationsTestCase {

    @Test
    public void testSaveButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        AttributeCharacterLengthFrame aclf = new AttributeCharacterLengthFrame();
        Assert.assertNotNull(aclf);
        
        JFrameOperator jfo = new JFrameOperator(aclf.getTitle());
        Assert.assertNotNull(jfo);

        // confirm that the default number of colors is correct
        JComboBoxOperator comboBoxOp = new JComboBoxOperator(jfo);
        Assert.assertEquals(17, comboBoxOp.getItemCount());
        
        // select 20 characters maximum
        comboBoxOp.selectItem(16);
        JemmyUtil.enterClickAndLeave(aclf.saveButton);
        Assert.assertEquals("New max length", 20, Control.max_len_string_attibute);
        
        // select 4 characters maximum
        comboBoxOp.selectItem(0);
        JemmyUtil.enterClickAndLeave(aclf.saveButton);
        Assert.assertEquals("New max length", 4, Control.max_len_string_attibute);

        JUnitUtil.dispose(aclf);
    }
}
