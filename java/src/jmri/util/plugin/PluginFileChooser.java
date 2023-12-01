package jmri.util.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import jmri.util.FileUtil;

/**
 * File chooser for plugins.
 *
 * @author Daniel Bergqvist (C) 2023
 */
public class PluginFileChooser extends jmri.util.swing.JmriJFileChooser {

    public PluginFileChooser() {
        super(FileUtil.getPreferencesPath());
        this.init();
    }

    public PluginFileChooser(String path) {
        super(path);
        this.init();
    }

    public PluginFileChooser(File dir) {
        super(dir);
        this.init();
    }

    private void init() {
        List<String> allExtensions = new ArrayList<>();
        allExtensions.add("JAR");
        FileFilter allPlugins = new FileNameExtensionFilter(Bundle.getMessage("allPlugins"), allExtensions.toArray(new String[allExtensions.size()]));
        this.addChoosableFileFilter(allPlugins);
        this.setFileFilter(allPlugins);
        this.setFileSelectionMode(JFileChooser.FILES_ONLY);
    }

//    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PluginFileChooser.class);
}
