package jmri.jmrit.operations.rollingstock.engines.tools;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.swing.JemmyUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DeleteEngineRosterActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        DeleteEngineRosterAction t = new DeleteEngineRosterAction("Test Action");
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testDelete() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        DeleteEngineRosterAction deleteRosterAction = new DeleteEngineRosterAction("Test Action");
        Assert.assertNotNull("exists", deleteRosterAction);

        JUnitOperationsUtil.initOperationsData();
        Assert.assertEquals("Number of engines", 4, InstanceManager.getDefault(EngineManager.class).getNumEntries());

        Thread delete = new Thread(new Runnable() {
            @Override
            public void run() {
                deleteRosterAction.actionPerformed(new ActionEvent("Test Action", 0, null));
            }
        });
        delete.setName("Delete Engines"); // NOI18N
        delete.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return delete.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("engineDeleteAll"), Bundle.getMessage("ButtonOK"));

        try {
            delete.join();
        } catch (InterruptedException e) {
            // do nothing
        }

        Assert.assertEquals("Number of engines", 0, InstanceManager.getDefault(EngineManager.class).getNumEntries());
    }

    // private final static Logger log = LoggerFactory.getLogger(DeleteEngineRosterActionTest.class);

}
