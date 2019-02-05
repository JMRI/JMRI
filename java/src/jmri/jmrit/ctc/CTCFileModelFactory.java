/*
 *  @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
package jmri.jmrit.ctc;

import javax.swing.JFileChooser;
import jmri.jmrit.XmlFile;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = apps.startup.StartupModelFactory.class)
public class CTCFileModelFactory extends apps.startup.AbstractFileModelFactory {
    @Override public Class<? extends apps.startup.StartupModel> getModelClass() { return CTCFileModel.class;}
    @Override public CTCFileModel newModel() { return new CTCFileModel();}
    @Override protected JFileChooser setFileChooser() { return XmlFile.userFileChooser("XML files", "xml");}
}
