package jmri.jmrit.operations.trains.excel;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsManager;
import jmri.jmrit.operations.setup.Control;

/**
 * Frame for user edit of the file name and setup of an Excel program.
 *
 * @author Dan Boudreau Copyright (C) 2013
 * 
 */
public class SetupExcelProgramFrame extends OperationsFrame {

    // checkboxes
    protected static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.setup.JmritOperationsSetupBundle");
    JCheckBox generateCheckBox = new JCheckBox();

    // text windows
    JTextField fileNameTextField = new JTextField(30);

    // major buttons
    JButton addButton = new JButton(Bundle.getMessage("Add"));
    JButton testButton = new JButton(Bundle.getMessage("Test"));
    JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));

    // directory
    JPanel pDirectoryName = new JPanel();

    @Override
    public void initComponents() {

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Layout the panel by rows
        // row 1
        JPanel pOptions = new JPanel();
        pOptions.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Options")));
        pOptions.add(generateCheckBox);

        // row 2
        pDirectoryName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Directory")));

        JPanel pFileName = new JPanel();
        pFileName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("FileName")));
        pFileName.add(fileNameTextField);

        // row 4 buttons
        JPanel pButtons = new JPanel();
        pButtons.setLayout(new GridBagLayout());
        addItem(pButtons, addButton, 1, 0);
        addItem(pButtons, testButton, 2, 0);
        addItem(pButtons, saveButton, 3, 0);

        getContentPane().add(pOptions);
        getContentPane().add(pDirectoryName);
        getContentPane().add(pFileName);
        getContentPane().add(pButtons);

        // setup buttons
        addButtonAction(addButton);
        addButtonAction(testButton);
        addButtonAction(saveButton);

        addHelpMenu("package.jmri.jmrit.operations.Operations_SetupExcelProgram", true); // NOI18N
        setTitle(Bundle.getMessage("MenuItemSetupExcelProgram"));

        initMinimumSize(new Dimension(Control.panelWidth400, Control.panelHeight300));
    }

    /**
     * Opens a dialog window in either the csvManifest or csvSwitchLists
     * directory
     * @param directoryName The string name of the directory
     * @return The File selected.
     *
     */
    protected File selectFile(String directoryName) {
        JFileChooser fc = jmri.jmrit.XmlFile.userFileChooser(Bundle.getMessage("ExcelProgramFiles"), "xls", "xlsm"); // NOI18N
        fc.setCurrentDirectory(InstanceManager.getDefault(OperationsManager.class).getFile(directoryName));
        fc.setDialogTitle(Bundle.getMessage("FindDesiredExcelFile"));
        // when reusing the chooser, make sure new files are included
        fc.rescanCurrentDirectory();
        int retVal = fc.showOpenDialog(null);
        // handle selection or cancel
        if (retVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            // Run the script from it's filename
            return file;
        }
        return null;
    }

//    private final static Logger log = LoggerFactory.getLogger(SetupExcelProgramFrame.class);
}
