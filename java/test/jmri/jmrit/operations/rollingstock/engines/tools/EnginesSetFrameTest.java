package jmri.jmrit.operations.rollingstock.engines.tools;

import javax.swing.JTable;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.rollingstock.engines.gui.EnginesTableFrame;
import jmri.util.*;
import jmri.util.swing.JemmyUtil;

/**
 * Tests for the Operations Engines GUI class
 *
 * @author Dan Boudreau Copyright (C) 2025
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class EnginesSetFrameTest extends OperationsTestCase {

    @Test
    public void testFrame() {
        JUnitOperationsUtil.initOperationsData();
        EnginesSetFrame f = new EnginesSetFrame();

        EnginesTableFrame etf = new EnginesTableFrame(true, null, null);
        JTable etm = etf.enginesTable;
        // select PC 5019
        ThreadingUtil.runOnGUI(() -> {
            etm.setRowSelectionInterval(1, 1);
            f.initComponents(etm);
        });

        // Save button is labeled "Apply"
        JemmyUtil.enterClickAndLeave(f.saveButton);

        JemmyUtil.pressDialogButton(Bundle.getMessage("enginePartConsist"), Bundle.getMessage("ButtonNo"));

        JUnitUtil.dispose(etf);
        JUnitUtil.dispose(f);
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }
    
    @Test
    public void testIgnoreButton() {
        EnginesSetFrame f = new EnginesSetFrame();

        EnginesTableFrame etf = new EnginesTableFrame(true, null, null);
        JTable etm = etf.enginesTable;
        
        Thread initialize = new Thread(new Runnable() {
            @Override
            public void run() {
                f.initComponents(etm);
            }
        });
        initialize.setName("engine set frame"); // NOI18N
        initialize.start();

        // no engines selected
        JemmyUtil.pressDialogButton(Bundle.getMessage("engineNoneSelected"), Bundle.getMessage("ButtonOK"));

        Assert.assertTrue("default", f.ignoreConsistCheckBox.isSelected());
        Assert.assertTrue("default", f.ignoreDestinationCheckBox.isSelected());
        Assert.assertTrue("default", f.ignoreLocationCheckBox.isSelected());
        Assert.assertTrue("default", f.ignoreTrainCheckBox.isSelected());
        Assert.assertTrue("default", f.ignoreStatusCheckBox.isSelected());
        
        JemmyUtil.enterClickAndLeave(f.ignoreAllButton);
        
        Assert.assertFalse("Now false", f.ignoreConsistCheckBox.isSelected());
        Assert.assertFalse("Now false", f.ignoreDestinationCheckBox.isSelected());
        Assert.assertFalse("Now false", f.ignoreLocationCheckBox.isSelected());
        Assert.assertFalse("Now false", f.ignoreTrainCheckBox.isSelected());
        Assert.assertFalse("Now false", f.ignoreStatusCheckBox.isSelected());
        
        JemmyUtil.enterClickAndLeave(f.ignoreAllButton);
        
        Assert.assertTrue("Now true", f.ignoreConsistCheckBox.isSelected());
        Assert.assertTrue("Now true", f.ignoreDestinationCheckBox.isSelected());
        Assert.assertTrue("Now true", f.ignoreLocationCheckBox.isSelected());
        Assert.assertTrue("Now true", f.ignoreTrainCheckBox.isSelected());
        Assert.assertTrue("Now true", f.ignoreStatusCheckBox.isSelected());
        
        JUnitUtil.dispose(etf);
        JUnitUtil.dispose(f);
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }
    
    @Test
    public void testIgnoreSave() {
        EnginesSetFrame f = new EnginesSetFrame();

        EnginesTableFrame etf = new EnginesTableFrame(true, null, null);
        JTable etm = etf.enginesTable;
        
        Thread initialize = new Thread(new Runnable() {
            @Override
            public void run() {
                f.initComponents(etm);
            }
        });
        initialize.setName("engine set frame"); // NOI18N
        initialize.start();

        // no engines selected
        JemmyUtil.pressDialogButton(Bundle.getMessage("engineNoneSelected"), Bundle.getMessage("ButtonOK"));

        // Save button is labeled "Apply"
        // warning dialog reappears if save button tried again
        JemmyUtil.enterClickAndLeave(f.saveButton);
        
        JemmyUtil.pressDialogButton(Bundle.getMessage("engineNoneSelected"), Bundle.getMessage("ButtonOK"));
        
        JUnitUtil.dispose(etf);
        JUnitUtil.dispose(f);
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

}
