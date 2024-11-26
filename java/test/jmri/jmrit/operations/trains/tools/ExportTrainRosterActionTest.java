package jmri.jmrit.operations.trains.tools;

import java.awt.event.ActionEvent;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ExportTrainRosterActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        ExportTrainRosterAction t = new ExportTrainRosterAction();
        Assert.assertNotNull("exists",t);
    }

    @Test
    @jmri.util.junit.annotations.DisabledIfHeadless
    public void testAction() {
        ExportTrainRosterAction a = new ExportTrainRosterAction();
        Assert.assertNotNull("exists", a);

        // should cause dialog to appear
        Thread doAction = new Thread(new Runnable() {
            @Override
            public void run() {
                a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
            }
        });
        doAction.setName("Do Action"); // NOI18N
        doAction.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return doAction.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("ExportComplete"), Bundle.getMessage("ButtonOK"));

        jmri.util.JUnitUtil.waitFor(() -> !doAction.isAlive(), "wait for doAction to complete");

        java.io.File file = new java.io.File(ExportTrains.defaultOperationsFilename());
        Assert.assertTrue("Confirm file creation", file.exists());
    }

    // private final static Logger log = LoggerFactory.getLogger(ExportTrainRosterActionTest.class);

}
