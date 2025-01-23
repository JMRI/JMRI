package jmri.jmrit.operations.routes.tools;

import java.awt.GraphicsEnvironment;
import java.io.File;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.routes.*;
import jmri.util.JUnitOperationsUtil;
import jmri.util.swing.JemmyUtil;

/**
 * @author Daniel Boudreau Copyright (C) 2025
 */
public class ImportRoutesTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        ImportRoutes t = new ImportRoutes();
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testImport() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();

        JUnitOperationsUtil.createFiveLocationTurnRoute();
        Route route3 = JUnitOperationsUtil.createThreeLocationRoute();
        Assert.assertEquals("Route name", "Route Acton-Boston-Chelmsford", route3.getName());

        // confirm two routes exist
        RouteManager routeManager = InstanceManager.getDefault(RouteManager.class);
        Assert.assertEquals("Number of routes", 3, routeManager.getRoutesByNameList().size());

        // modify route, route location comment is one of the last exported elements
        RouteLocation rl = route3.getTerminatesRouteLocation();
        rl.setComment("This comment is for testing");
        rl.setCommentTextColor("blue");

        ExportRoutes exportRoutes = new ExportRoutes();
        Assert.assertNotNull("exists", exportRoutes);
        // next should cause export complete dialog to appear
        Thread export = new Thread(exportRoutes::writeOperationsRoutesFile);
        export.setName("Export Routes"); // NOI18N
        export.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return export.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("ExportComplete"), Bundle.getMessage("ButtonOK"));

        jmri.util.JUnitUtil.waitFor(() -> !export.isAlive(), "wait for export to complete");

        java.io.File file = new java.io.File(ExportRoutes.defaultOperationsFilename());
        Assert.assertTrue("Confirm file creation", file.exists());

        // now delete all routes
        for (Route route : routeManager.getRoutesByNameList()) {
            routeManager.deregister(route);
        }
        Assert.assertEquals("Number of routes", 0, routeManager.getRoutesByNameList().size());

        // now import
        ImportRoutes importRoutes = new ImportRoutes();
        Assert.assertNotNull("exists", importRoutes);
        // next should cause import complete dialog to appear
        Thread importThread = new ImportRoutes() {
            @Override
            protected File getFile() {
                return new File(OperationsXml.getFileLocation() +
                        OperationsXml.getOperationsDirectoryName() +
                        File.separator +
                        ExportRoutes.getOperationsFileName());
            }
        };
        importThread.setName("Test Import Routes"); // NOI18N
        importThread.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return importThread.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("SuccessfulImport"), Bundle.getMessage("ButtonOK"));

        jmri.util.JUnitUtil.waitFor(() -> !importThread.isAlive(), "wait for export to complete");

        Assert.assertEquals("Number of routes", 3, routeManager.getRoutesByNameList().size());

        // confirm import included comment and color
        route3 = routeManager.getRouteByName("Route Acton-Boston-Chelmsford");
        Assert.assertNotNull(route3);
        rl = route3.getTerminatesRouteLocation();
        Assert.assertEquals("confirm location name", "Chelmsford", rl.getName());
        Assert.assertEquals("confirm comment", "This comment is for testing", rl.getComment());
        Assert.assertEquals("confirm comment color", "blue", rl.getCommentTextColor());
    }
}
