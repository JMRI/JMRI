package jmri.jmrit.operations.setup;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import jmri.InstanceManager;

/**
 * Frame for user edit of the build report options
 *
 * @author Dan Boudreau Copyright (C) 2008, 2010, 2011, 2012, 2013
 * 
 */
public class BuildReportOptionPanel extends OperationsPreferencesPanel {

//    private static final Logger log = LoggerFactory.getLogger(OperationsSetupPanel.class);

    // major buttons
    JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));

    // radio buttons
    JRadioButton buildReportMin = new JRadioButton(Bundle.getMessage("Minimal"));
    JRadioButton buildReportNor = new JRadioButton(Bundle.getMessage("Normal"));
    JRadioButton buildReportMax = new JRadioButton(Bundle.getMessage("Detailed"));
    JRadioButton buildReportVD = new JRadioButton(Bundle.getMessage("VeryDetailed"));

    JRadioButton buildReportRouterNor = new JRadioButton(Bundle.getMessage("Normal"));
    JRadioButton buildReportRouterMax = new JRadioButton(Bundle.getMessage("Detailed"));
    JRadioButton buildReportRouterVD = new JRadioButton(Bundle.getMessage("VeryDetailed"));

    // check boxes
    JCheckBox buildReportCheckBox = new JCheckBox(Bundle.getMessage("BuildReportEdit"));
    JCheckBox buildReportIndentCheckBox = new JCheckBox(Bundle.getMessage("BuildReportIndent"));
    JCheckBox buildReportAlwaysPreviewCheckBox = new JCheckBox(Bundle.getMessage("BuildReportAlwaysPreview"));

    // combo boxes
    JComboBox<Integer> fontSizeComboBox = new JComboBox<>();

    public BuildReportOptionPanel() {

        // the following code sets the frame's initial state
        // add tool tips
        saveButton.setToolTipText(Bundle.getMessage("SaveToolTip"));
        buildReportCheckBox.setToolTipText(Bundle.getMessage("CreatesTextFileTip"));

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // build report
        JPanel pReport = new JPanel();
        pReport.setLayout(new GridBagLayout());
        pReport.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutReportOptions")));

        // build report options
        addItemWidth(pReport, buildReportCheckBox, 3, 1, 1);
        addItemWidth(pReport, buildReportIndentCheckBox, 3, 1, 2);
        addItemWidth(pReport, buildReportAlwaysPreviewCheckBox, 3, 1, 3);

        JPanel pFontSize = new JPanel();
        pFontSize.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutFontSize")));
        pFontSize.add(fontSizeComboBox);

        JPanel pLevel = new JPanel();
        pLevel.setLayout(new GridBagLayout());
        pLevel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BuildReport")));

        // build report level radio buttons
        addItemLeft(pLevel, buildReportMin, 1, 0);
        addItemLeft(pLevel, buildReportNor, 2, 0);
        addItemLeft(pLevel, buildReportMax, 3, 0);
        addItemLeft(pLevel, buildReportVD, 4, 0);

        JPanel pRouterLevel = new JPanel();
        pRouterLevel.setLayout(new GridBagLayout());
        pRouterLevel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BuildReportRouter")));

        // build report level radio buttons
        addItemLeft(pRouterLevel, buildReportRouterNor, 2, 0);
        addItemLeft(pRouterLevel, buildReportRouterMax, 3, 0);
        addItemLeft(pRouterLevel, buildReportRouterVD, 4, 0);

        // controls
        JPanel pControl = new JPanel();
        pControl.setBorder(BorderFactory.createTitledBorder(""));
        pControl.setLayout(new GridBagLayout());
        addItem(pControl, saveButton, 0, 0);

        add(pReport);
        add(pLevel);
        add(pRouterLevel);
        add(pFontSize);
        add(pControl);

        buildReportCheckBox.setSelected(Setup.isBuildReportEditorEnabled());
        buildReportIndentCheckBox.setSelected(Setup.isBuildReportIndentEnabled());
        buildReportIndentCheckBox.setEnabled(buildReportCheckBox.isSelected());
        buildReportAlwaysPreviewCheckBox.setSelected(Setup.isBuildReportAlwaysPreviewEnabled());

        ButtonGroup buildReportGroup = new ButtonGroup();
        buildReportGroup.add(buildReportMin);
        buildReportGroup.add(buildReportNor);
        buildReportGroup.add(buildReportMax);
        buildReportGroup.add(buildReportVD);

        ButtonGroup buildReportRouterGroup = new ButtonGroup();
        buildReportRouterGroup.add(buildReportRouterNor);
        buildReportRouterGroup.add(buildReportRouterMax);
        buildReportRouterGroup.add(buildReportRouterVD);

        setBuildReportRadioButton();
        setBuildReportRouterRadioButton();

        // load font sizes 5 through 14
        for (int i = 5; i < 15; i++) {
            fontSizeComboBox.addItem(i);
        }
        fontSizeComboBox.setSelectedItem(Setup.getBuildReportFontSize());

        addButtonAction(saveButton);
        addCheckBoxAction(buildReportCheckBox);

        addRadioButtonAction(buildReportMin);
        addRadioButtonAction(buildReportNor);
        addRadioButtonAction(buildReportMax);
        addRadioButtonAction(buildReportVD);

        initMinimumSize(new Dimension(Control.panelWidth500, Control.panelHeight500));
    }

    // Save button
    @Override
    public void buttonActionPerformed(ActionEvent ae) {
        if (ae.getSource() == saveButton) {
            this.savePreferences();
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
    }

    @Override
    protected void checkBoxActionPerformed(ActionEvent ae) {
        buildReportIndentCheckBox.setEnabled(buildReportCheckBox.isSelected());
    }

    @Override
    protected void radioButtonActionPerformed(ActionEvent ae) {
        setBuildReportRouterRadioButton(); // enable detailed and very detailed if needed
    }

    private void setBuildReportRadioButton() {
        buildReportMin.setSelected(Setup.getBuildReportLevel().equals(Setup.BUILD_REPORT_MINIMAL));
        buildReportNor.setSelected(Setup.getBuildReportLevel().equals(Setup.BUILD_REPORT_NORMAL));
        buildReportMax.setSelected(Setup.getBuildReportLevel().equals(Setup.BUILD_REPORT_DETAILED));
        buildReportVD.setSelected(Setup.getBuildReportLevel().equals(Setup.BUILD_REPORT_VERY_DETAILED));
    }

    private void setBuildReportRouterRadioButton() {
        // Enabled for the router only if the build report is very detailed
        buildReportRouterNor.setEnabled(buildReportVD.isSelected());
        buildReportRouterMax.setEnabled(buildReportVD.isSelected());
        buildReportRouterVD.setEnabled(buildReportVD.isSelected());

        buildReportRouterMax.setSelected(Setup.getRouterBuildReportLevel()
                .equals(Setup.BUILD_REPORT_DETAILED));
        buildReportRouterVD.setSelected(Setup.getRouterBuildReportLevel().equals(
                Setup.BUILD_REPORT_VERY_DETAILED));
        buildReportRouterNor.setSelected(Setup.getRouterBuildReportLevel().equals(Setup.BUILD_REPORT_NORMAL)
                || !buildReportVD.isSelected());
    }

    @Override
    public String getTabbedPreferencesTitle() {
        return Bundle.getMessage("TitleBuildReportOptions");
    }

    @Override
    public String getPreferencesTooltip() {
        return null;
    }

    @Override
    public void savePreferences() {
        // font size
        Setup.setBuildReportFontSize((Integer) fontSizeComboBox.getSelectedItem());

        // build report level
        if (buildReportMin.isSelected()) {
            Setup.setBuildReportLevel(Setup.BUILD_REPORT_MINIMAL);
        } else if (buildReportNor.isSelected()) {
            Setup.setBuildReportLevel(Setup.BUILD_REPORT_NORMAL);
        } else if (buildReportMax.isSelected()) {
            Setup.setBuildReportLevel(Setup.BUILD_REPORT_DETAILED);
        } else if (buildReportVD.isSelected()) {
            Setup.setBuildReportLevel(Setup.BUILD_REPORT_VERY_DETAILED);
        }

        // router build report level
        String oldReportLevel = Setup.getRouterBuildReportLevel();
        if (buildReportRouterNor.isSelected()) {
            Setup.setRouterBuildReportLevel(Setup.BUILD_REPORT_NORMAL);
        } else if (buildReportRouterMax.isSelected()) {
            Setup.setRouterBuildReportLevel(Setup.BUILD_REPORT_DETAILED);
        } else if (buildReportRouterVD.isSelected()) {
            Setup.setRouterBuildReportLevel(Setup.BUILD_REPORT_VERY_DETAILED);
        }

        if (!oldReportLevel.equals(Setup.getRouterBuildReportLevel())) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("buildReportRouter"), Bundle
                    .getMessage("buildReportRouterTitle"), JOptionPane.INFORMATION_MESSAGE);
        }

        Setup.setBuildReportEditorEnabled(buildReportCheckBox.isSelected());
        Setup.setBuildReportIndentEnabled(buildReportIndentCheckBox.isSelected());
        Setup.setBuildReportAlwaysPreviewEnabled(buildReportAlwaysPreviewCheckBox.isSelected());

        InstanceManager.getDefault(OperationsSetupXml.class).writeOperationsFile();
    }

    @Override
    public boolean isDirty() {
        String reportLevel = Setup.getBuildReportLevel();
        if (buildReportMin.isSelected()) {
            reportLevel = Setup.BUILD_REPORT_MINIMAL;
        } else if (buildReportNor.isSelected()) {
            reportLevel = Setup.BUILD_REPORT_NORMAL;
        } else if (buildReportMax.isSelected()) {
            reportLevel = Setup.BUILD_REPORT_DETAILED;
        } else if (buildReportVD.isSelected()) {
            reportLevel = Setup.BUILD_REPORT_VERY_DETAILED;
        }

        String routerReportLevel = Setup.getRouterBuildReportLevel();
        if (buildReportRouterNor.isSelected()) {
            routerReportLevel = Setup.BUILD_REPORT_NORMAL;
        } else if (buildReportRouterMax.isSelected()) {
            routerReportLevel = Setup.BUILD_REPORT_DETAILED;
        } else if (buildReportRouterVD.isSelected()) {
            routerReportLevel = Setup.BUILD_REPORT_VERY_DETAILED;
        }

        return (Setup.getBuildReportFontSize() != (Integer) fontSizeComboBox.getSelectedItem()
                || !reportLevel.equals(Setup.getBuildReportLevel())
                || !routerReportLevel.equals(Setup.getRouterBuildReportLevel())
                || Setup.isBuildReportEditorEnabled() != buildReportCheckBox.isSelected()
                || Setup.isBuildReportIndentEnabled() != buildReportIndentCheckBox.isSelected()
                || Setup.isBuildReportAlwaysPreviewEnabled() != buildReportAlwaysPreviewCheckBox.isSelected());
    }
}
