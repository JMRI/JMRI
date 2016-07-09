package jmri.script;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.MissingResourceException;
import javax.script.ScriptEngineFactory;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import jmri.util.FileUtil;

/**
 *
 * @author rhwood
 */
public class ScriptFileChooser extends JFileChooser {

    public ScriptFileChooser() {
        super(FileUtil.getScriptsPath());
        this.init();
    }

    public ScriptFileChooser(String path) {
        super(path);
        this.init();
    }

    public ScriptFileChooser(File dir) {
        super(dir);
        this.init();
    }

    private void init() {
        List<String> allExtensions = new ArrayList<>();
        HashMap<String, FileFilter> filters = new HashMap<>();
        List<String> filterNames = new ArrayList<>();
        JmriScriptEngineManager.getDefault().getManager().getEngineFactories().stream().forEach((ScriptEngineFactory factory) -> {
            List<String> extensions = factory.getExtensions();
            allExtensions.addAll(extensions);
            String name = this.fileForLanguage(factory.getLanguageName());
            filterNames.add(name);
            filters.put(name, new FileNameExtensionFilter(name, extensions.toArray(new String[extensions.size()])));
        });
        FileFilter allScripts = new FileNameExtensionFilter(Bundle.getMessage("allScripts"), allExtensions.toArray(new String[allExtensions.size()]));
        this.addChoosableFileFilter(allScripts);
        filterNames.stream().sorted().forEach((filter) -> {
            this.addChoosableFileFilter(filters.get(filter));
        });
        this.setFileFilter(allScripts);
        this.setFileSelectionMode(JFileChooser.FILES_ONLY);
    }

    private String fileForLanguage(String language) {
        try {
            return Bundle.getMessage(language);
        } catch (MissingResourceException ex) {
            if (!language.endsWith(Bundle.getMessage("files"))) { // NOI18N
                return language + " " + Bundle.getMessage("files");
            }
            return language;
        }
    }
}
