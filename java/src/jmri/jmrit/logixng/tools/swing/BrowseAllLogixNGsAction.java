package jmri.jmrit.logixng.tools.swing;

import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.AbstractAction;

import jmri.InstanceManager;

import org.apache.commons.lang3.mutable.MutableInt;

/**
 * Swing action to browse all LogixNGs, Modules, Tables and Global variables.
 *
 * @author Daniel Bergqvist Copyright (C) 2024
 */
public class BrowseAllLogixNGsAction extends AbstractAction {

    public BrowseAllLogixNGsAction() {
        super(Bundle.getMessage("BrowseAllLogixNGs"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String title = Bundle.getMessage("BrowseAllLogixNGs");
        LogixNGBrowseWindow browseWindow =
                new LogixNGBrowseWindow(title);
        browseWindow.getPrintTreeSettings();
        browseWindow.makeBrowserWindow(true, true, title, title, (printTreeSettings) -> {
                StringWriter writer = new StringWriter();
                InstanceManager.getDefault(jmri.jmrit.logixng.LogixNG_Manager.class)
                        .printTree(printTreeSettings, new PrintWriter(writer), "    ", new MutableInt(0));
                return writer.toString();
            });
    }

}
