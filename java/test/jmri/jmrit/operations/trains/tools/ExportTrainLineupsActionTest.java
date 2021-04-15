package jmri.jmrit.operations.trains.tools;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ExportTrainLineupsActionTest extends OperationsTestCase {
    @Test
    public void testCTor() {
        ExportTrainLineupsAction t = new ExportTrainLineupsAction();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ExportTrainLineupsAction a = new ExportTrainLineupsAction();
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
        
        java.io.File file = new java.io.File(ExportTrainLineups.defaultOperationsFilename());
        Assert.assertTrue("Confirm file creation", file.exists());
    }

    // private final static Logger log = LoggerFactory.getLogger(ExportTrainRosterActionTest.class);

}
