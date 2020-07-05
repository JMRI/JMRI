package jmri.util.startup;


import javax.swing.JFileChooser;

import jmri.jmrit.XmlFile;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Randall Wood 2016
 */
@ServiceProvider(service = StartupModelFactory.class)
@API(status = EXPERIMENTAL)
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
