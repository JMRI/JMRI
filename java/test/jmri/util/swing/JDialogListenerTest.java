package jmri.util.swing;

import javax.swing.JDialog;
import javax.swing.JFrame;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 *
 * @author Steve Young Copyright (C) 2023
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class JDialogListenerTest {

    @Test
    public void testJDialogListener(){
        JFrame f = new JFrame("JDialogListener test Frame");
        JDialog dialog = new JDialog(f);

        Assertions.assertEquals(0, f.getPropertyChangeListeners().length);

        JDialogListener pcl = new JDialogListener(dialog);
        f.addPropertyChangeListener(pcl);

        Assertions.assertEquals(1, f.getPropertyChangeListeners().length);

        var listener = f.getPropertyChangeListeners()[0];
        Assertions.assertTrue(listener instanceof JDialogListener,"dialog is not listener class");
        Assertions.assertTrue(pcl == listener,"dialog is not the listener");

        JDialog dialogFromListener = ((JDialogListener)listener).getDialog();
        Assertions.assertTrue(dialog == dialogFromListener,"dialog is located");

        f.removePropertyChangeListener(pcl);
        Assertions.assertEquals(0, f.getPropertyChangeListeners().length);
        dialog.dispose();
        f.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
