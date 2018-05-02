package apps.startup;

import apps.PerformFileModel;
import javax.swing.JFileChooser;
import jmri.jmrit.XmlFile;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Randall Wood 2016
 */
@ServiceProvider(service = StartupModelFactory.class)
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
