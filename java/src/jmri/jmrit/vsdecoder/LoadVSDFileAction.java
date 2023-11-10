package jmri.jmrit.vsdecoder;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import jmri.util.swing.JmriJOptionPane;

/**
 * Load VSDecoder Profiles from XML.
 *
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 *
 * @author Mark Underwood Copyright (C) 2011
 */

/**
 * Load VSDecoder Profiles from XML
 *
 * Adapted from LoadXmlThrottleProfileAction by Glen Oberhauser (2004)
 *
 * @author Mark Underwood 2011
 */
public class LoadVSDFileAction extends AbstractAction {

    /**
     * Constructor
     *
     * @param s Name for the action.
     */
    public LoadVSDFileAction(String s) {
        super(s);
    }

    public LoadVSDFileAction() {
        this(Bundle.getMessage("VSDecoderFileMenuLoadVSDFile")); // File Chooser Title
        // Shouldn't this be in the resource bundle?
    }

    /**
     * The action is performed. Let the user choose the file to load from. Read
     * XML for each VSDecoder Profile.
     *
     * @param e The event causing the action.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser fileChooser;
        String dir_external =
                jmri.util.FileUtil.getExternalFilename(VSDecoderManager.instance().getVSDecoderPreferences().getDefaultVSDFilePath());

        fileChooser = new jmri.util.swing.JmriJFileChooser(dir_external);
        fileChooser.setFileFilter(new FileNameExtensionFilter(Bundle.getMessage("LoadVSDFileChooserFilterLabel"), "vsd", "zip")); // NOI18N
        fileChooser.setCurrentDirectory(new File(dir_external));
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);

        int retVal = fileChooser.showOpenDialog(null);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            loadVSDFile(fileChooser.getSelectedFile().toString());
        }
    }

    public static boolean loadVSDFile(String fp) {
        // Check whether the file exists
        String fp_external = jmri.util.FileUtil.getExternalFilename(fp);
        File file = new File(fp_external);
        if (!file.exists()) {
            log.error("Cannot locate VSD File {}", fp_external);
            if (!GraphicsEnvironment.isHeadless()) {
                JmriJOptionPane.showMessageDialog(null, "Cannot locate VSD File",
                        Bundle.getMessage("VSDFileError"), JmriJOptionPane.ERROR_MESSAGE);
            }
            return false;
        }

        // Check config.xml
        VSDFile vsdfile;
        try {
            // Create a VSD (zip) file.
            vsdfile = new VSDFile(fp_external);
            log.debug("VSD File name: {}", vsdfile.getName());
            if (vsdfile.isInitialized()) {
                VSDecoderManager.instance().loadProfiles(vsdfile);
            }
            // Cleanup and close files.
            vsdfile.close();

            if (!vsdfile.isInitialized() && !GraphicsEnvironment.isHeadless()) {
                JmriJOptionPane.showMessageDialog(null, vsdfile.getStatusMessage(),
                        Bundle.getMessage("VSDFileError"), JmriJOptionPane.ERROR_MESSAGE);
            }

            return vsdfile.isInitialized();

        } catch (java.util.zip.ZipException ze) {
            log.error("ZipException opening file {}", fp, ze);
            return false;
        } catch (java.io.IOException ze) {
            log.error("IOException opening file {}", fp, ze);
            return false;
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoadVSDFileAction.class);

}
