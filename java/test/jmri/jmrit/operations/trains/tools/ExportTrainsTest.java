package jmri.jmrit.operations.trains.tools;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitOperationsUtil;
import jmri.util.swing.JemmyUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ExportTrainsTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        ExportTrains t = new ExportTrains();
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testCreateFile() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ExportTrains exportTrains = new ExportTrains();
        Assert.assertNotNull("exists", exportTrains);

        JUnitOperationsUtil.initOperationsData();

        // should cause export complete dialog to appear
        Thread export = new Thread(new Runnable() {
            @Override
            public void run() {
                exportTrains.writeOperationsTrainsFile();
            }
        });
        export.setName("Export Trains"); // NOI18N
        export.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return export.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("ExportComplete"), "OK");

        java.io.File file = new java.io.File(ExportTrains.defaultOperationsFilename());
        Assert.assertTrue("Confirm file creation", file.exists());
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        super.setUp();
    }

    @Override
    @After
    public void tearDown() {
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ExportTrainsTest.class);

}
