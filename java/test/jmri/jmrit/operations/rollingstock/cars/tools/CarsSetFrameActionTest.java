package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;

import javax.swing.JTable;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.rollingstock.cars.CarsTableFrame;
import jmri.util.*;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class CarsSetFrameActionTest extends OperationsTestCase {
    
    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CarsTableFrame ctf = new CarsTableFrame(true, null, null);
        JTable ctm = ctf.carsTable;
        CarsSetFrameAction a = new CarsSetFrameAction(ctm);
        Assert.assertNotNull("exists", a);
        
        // should cause dialog no car selected to appear
        Thread action = new Thread(new Runnable() {
            @Override
            public void run() {
                a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
            }
        });
        action.setName("cars set frame"); // NOI18N
        action.start();
        
        JemmyUtil.pressDialogButton(Bundle.getMessage("carNoneSelected"), Bundle.getMessage("ButtonOK"));
       
        JmriJFrame f = JmriJFrame.getFrame(Bundle.getMessage("TitleSetCars"));
        Assert.assertNotNull("frame exists", f);
        JUnitUtil.dispose(f);
        
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

    // private final static Logger log = LoggerFactory.getLogger(CarsSetFrameActionTest.class);

}
