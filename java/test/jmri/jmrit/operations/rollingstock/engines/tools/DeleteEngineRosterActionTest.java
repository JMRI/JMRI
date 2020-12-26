package jmri.jmrit.operations.rollingstock.engines.tools;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.event.ActionEvent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DeleteEngineRosterActionTest extends OperationsTestCase {

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testCTor() {
        DeleteEngineRosterAction t = new DeleteEngineRosterAction();
        assertThat(t).withFailMessage("exists").isNotNull();
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testDelete() {
        DeleteEngineRosterAction deleteRosterAction = new DeleteEngineRosterAction();
        assertThat(deleteRosterAction).withFailMessage("exists").isNotNull();

        JUnitOperationsUtil.initOperationsData();
        assertThat(InstanceManager.getDefault(EngineManager.class).getNumEntries()).withFailMessage("Number of engines").isEqualTo(4);

        Thread delete = new Thread(new Runnable() {
            @Override
            public void run() {
                deleteRosterAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
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

        assertThat(InstanceManager.getDefault(EngineManager.class).getNumEntries()).withFailMessage("Number of engines").isEqualTo(0);
        

    }

    // private final static Logger log = LoggerFactory.getLogger(DeleteEngineRosterActionTest.class);

}
