package jmri.jmrit.operations.trains.tools;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitOperationsUtil;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Daniel Boudreau Copyright (C) 2019
 */
public class ExportTimetableTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        ExportTimetable t = new ExportTimetable();
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testCreateFile() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ExportTimetable exportTimetable = new ExportTimetable();
        Assert.assertNotNull("exists", exportTimetable);

        JUnitOperationsUtil.initOperationsData();

        // next should cause export complete dialog to appear
        Thread export = new Thread(new Runnable() {
            @Override
            public void run() {
                exportTimetable.writeOperationsTimetableFile();
            }
        });
        export.setName("Export Trains"); // NOI18N
        export.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return export.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("ExportComplete"), Bundle.getMessage("ButtonOK"));

        java.io.File file = new java.io.File(ExportTimetable.defaultOperationsFilename());
        Assert.assertTrue("Confirm file creation", file.exists());
    }

    // private final static Logger log = LoggerFactory.getLogger(ExportTrainsTest.class);

}
