package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarsTableFrame;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class DeleteCarRosterActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CarsTableFrame ctf = new CarsTableFrame(true, null, null);
        DeleteCarRosterAction t = new DeleteCarRosterAction(ctf);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(ctf);
    }
    
    @Test
    public void testDelete() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CarsTableFrame ctf = new CarsTableFrame(true, null, null);
        DeleteCarRosterAction deleteRosterAction = new DeleteCarRosterAction(ctf);
        Assert.assertNotNull("exists", deleteRosterAction);

        JUnitOperationsUtil.initOperationsData();
        Assert.assertEquals("Number of cars", 9, InstanceManager.getDefault(CarManager.class).getNumEntries());

        Thread delete = new Thread(new Runnable() {
            @Override
            public void run() {
                deleteRosterAction.actionPerformed(new ActionEvent("Test Action", 0, null));
            }
        });
        delete.setName("Delete Cars"); // NOI18N
        delete.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return delete.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("carDeleteAll"), Bundle.getMessage("ButtonOK"));

        try {
            delete.join();
        } catch (InterruptedException e) {
            // do nothing
        }

        Assert.assertEquals("Number of cars", 0, InstanceManager.getDefault(CarManager.class).getNumEntries());
        JUnitUtil.dispose(ctf);
    }

    // private final static Logger log = LoggerFactory.getLogger(DeleteCarRosterActionTest.class);

}
