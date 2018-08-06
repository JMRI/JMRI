package jmri.jmrit.display.palette;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 *
 * @author Copyright (C) 2017   
 */
public class ColorDialogTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ControlPanelEditor cpe = new ControlPanelEditor("Fred");
        ColorDialog cd = new ColorDialog(cpe, cpe.getTargetPanel(), null);
        Assert.assertNotNull("exists",cd);
        cd.setModalityType(java.awt.Dialog.ModalityType.MODELESS);     // doesn't help either
        JDialogOperator jdo = new JDialogOperator(cd);
        JButtonOperator jbo = new JButtonOperator(jdo , jmri.jmrit.display.palette.Bundle.getMessage("ButtonDone"));
        jbo.push();     // why does it not push - ?? 
        JUnitUtil.dispose(cpe);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ColorDialogTest.class);
}
