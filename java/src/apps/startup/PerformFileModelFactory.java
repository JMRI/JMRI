package apps.startup;

import apps.PerformFileModel;
import apps.StartupModel;
import javax.swing.JFileChooser;
import jmri.jmrit.XmlFile;

/**
 *
 * @author Randall Wood 2016
 */
public class PerformFileModelFactory extends AbstractFileModelFactory {

    @Override
    public Class<? extends StartupModel> getModelClass() {
        return PerformFileModel.class;
    }

    @Override
    public PerformFileModel newModel() {
        return new PerformFileModel();
    }

    @Override
    protected JFileChooser setFileChooser() {
        return XmlFile.userFileChooser("XML files", "xml");
    }
    
}
