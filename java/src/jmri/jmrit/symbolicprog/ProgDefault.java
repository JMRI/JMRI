package jmri.jmrit.symbolicprog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import jmri.InstanceManager;
import jmri.jmrit.XmlFile;
import jmri.util.FileUtil;
import jmri.util.XmlFilenameFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Functions for use with programmer files, including the default file name.
 * <p>
 * This was refactored from LocoSelPane in JMRI 1.5.3, which was the right
 * thing to do anyway. But the real reason was that on MacOS Classic the static
 * member holding the default programmer name was being overwritten when the
 * class was (erroneously) initialized for a second time. This refactoring did
 * not fix the problem. What did fix it was an ugly hack in the
 * {@link CombinedLocoSelPane} class; see comments there for more information.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 */
public class ProgDefault {

    static public String[] findListOfProgFiles() {
        // create an array of file names from prefs/programmers, count entries
        int np = 0;
        String[] sp = {};
        FileUtil.createDirectory(FileUtil.getUserFilesPath() + "programmers");
        File fp = new File(FileUtil.getUserFilesPath() + "programmers");
        XmlFilenameFilter filter = new XmlFilenameFilter();
        if (fp.exists()) {
            sp = fp.list(filter);
            if (sp != null) {
                np = sp.length;
            } else {
                sp = new String[]{};
                np = 0;
            }
        } else {
            log.warn(FileUtil.getUserFilesPath() + "programmers was missing, though tried to create it");
        }
        if (log.isDebugEnabled()) {
            log.debug("Got " + np + " programmers from " + fp.getPath());
        }
        // create an array of file names from xml/programmers, count entries
        fp = new File(XmlFile.xmlDir() + "programmers");
        int nx = 0;
        String[] sx = {};
        if (fp.exists()) {
            sx = fp.list(filter);
            if (sx != null) {
                nx = sx.length;
            } else {
                sx = new String[]{};
            }
            log.debug("Got {} programmers from {}", nx, fp.getPath());
        } else {
            // create an array of file names from jmri.jar!xml/programmers, count entries
            List<String> sr = new ArrayList<>();
            JarFile jar = FileUtil.jmriJarFile();
            if (jar != null) {
                jar.stream().forEach((je) -> {
                    String name = je.getName();
                    if (name.startsWith("xml" + File.separator + "programmers") && name.endsWith(".xml")) {
                        sr.add(name.substring(name.lastIndexOf(File.separator)));
                    }
                });
                sx = sr.toArray(new String[sr.size()]);
                nx = sx.length;
                log.debug("Got {} programmers from jmri.jar", nx);
            }
        }
        // copy the programmer entries to the final array
        // note: this results in duplicate entries if the same name is also local.
        // But for now I can live with that.
        String sbox[] = new String[np + nx];
        int n = 0;
        if (np > 0) {
            for (String s : sp) {
                sbox[n++] = s.substring(0, s.length() - 4);
            }
        }
        if (nx > 0) {
            for (String s : sx) {
                sbox[n++] = s.substring(0, s.length() - 4);
            }
        }
        return sbox;
    }

    synchronized static public String getDefaultProgFile() {
        return InstanceManager.getDefault(ProgrammerConfigManager.class).getDefaultFile();
    }

    synchronized static public void setDefaultProgFile(String s) {
        InstanceManager.getDefault(ProgrammerConfigManager.class).setDefaultFile(s);
    }

    private final static Logger log = LoggerFactory.getLogger(ProgDefault.class);
}
