package jmri.util.swing;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import jmri.InstanceManager;

public class JmriJFileChooser extends JFileChooser {


    public JmriJFileChooser() {
        super();
        setJFileChooserFormat();
    }

    public JmriJFileChooser(String path) {
        super(path);
        setJFileChooserFormat();
    }

    public JmriJFileChooser(FileSystemView fsv) {
        super(fsv);
        setJFileChooserFormat();
    }


    public JmriJFileChooser(File f) {
        super(f);
        setJFileChooserFormat();
    }

    private void setJFileChooserFormat() {
        javax.swing.Action details;
        switch (InstanceManager.getDefault(jmri.util.gui.GuiLafPreferencesManager.class).getJFileChooserFormat()) {
            case 1:
                details = this.getActionMap().get("viewTypeList");
                break;
            case 2:
                details = this.getActionMap().get("viewTypeDetails");
                break;
            default:
                return;
        }
        if (details!=null) {
            details.actionPerformed(null);
        }
    }
}
