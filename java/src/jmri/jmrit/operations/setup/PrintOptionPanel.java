package jmri.jmrit.operations.setup;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.*;

import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.operations.trains.TrainCommon;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.FileUtil;
import jmri.util.swing.FontComboUtil;
import jmri.util.swing.JmriJOptionPane;

/**
 * Frame for user edit of manifest and switch list print options
 *
 * @author Dan Boudreau Copyright (C) 2008, 2010, 2011, 2012, 2013
 */
public class PrintOptionPanel extends OperationsPreferencesPanel implements java.beans.PropertyChangeListener {

    private String ADD = "+";
    private String DELETE = "-";

    // labels
    JLabel logoURL = new JLabel("");

    // major buttons
    JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));
    JButton addLogoButton = new JButton(Bundle.getMessage("AddLogo"));
    JButton removeLogoButton = new JButton(Bundle.getMessage("RemoveLogo"));

    JButton addEngPickupComboboxButton = new JButton(ADD);
    JButton deleteEngPickupComboboxButton = new JButton(DELETE);
    JButton addEngDropComboboxButton = new JButton(ADD);
    JButton deleteEngDropComboboxButton = new JButton(DELETE);
    JButton addCarPickupComboboxButton = new JButton(ADD);
    JButton deleteCarPickupComboboxButton = new JButton(DELETE);
    JButton addCarDropComboboxButton = new JButton(ADD);
    JButton deleteCarDropComboboxButton = new JButton(DELETE);
    JButton addLocalComboboxButton = new JButton(ADD);
    JButton deleteLocalComboboxButton = new JButton(DELETE);
    JButton addSwitchListPickupComboboxButton = new JButton(ADD);
    JButton deleteSwitchListPickupComboboxButton = new JButton(DELETE);
    JButton addSwitchListDropComboboxButton = new JButton(ADD);
    JButton deleteSwitchListDropComboboxButton = new JButton(DELETE);
    JButton addSwitchListLocalComboboxButton = new JButton(ADD);
    JButton deleteSwitchListLocalComboboxButton = new JButton(DELETE);

    // check boxes
    JCheckBox tabFormatCheckBox = new JCheckBox(Bundle.getMessage("TabFormat"));
    JCheckBox formatSwitchListCheckBox = new JCheckBox(Bundle.getMessage("SameAsManifest"));
    JCheckBox editManifestCheckBox = new JCheckBox(Bundle.getMessage("UseTextEditor"));
    JCheckBox printLocCommentsCheckBox = new JCheckBox(Bundle.getMessage("PrintLocationComments"));
    JCheckBox printRouteCommentsCheckBox = new JCheckBox(Bundle.getMessage("PrintRouteComments"));
    JCheckBox printLoadsEmptiesCheckBox = new JCheckBox(Bundle.getMessage("PrintLoadsEmpties"));
    JCheckBox printCabooseLoadCheckBox = new JCheckBox(Bundle.getMessage("PrintCabooseLoad"));
    JCheckBox printPassengerLoadCheckBox = new JCheckBox(Bundle.getMessage("PrintPassengerLoad"));
    JCheckBox printTrainScheduleNameCheckBox = new JCheckBox(Bundle.getMessage("PrintTrainScheduleName"));
    JCheckBox use12hrFormatCheckBox = new JCheckBox(Bundle.getMessage("12hrFormat"));
    JCheckBox printValidCheckBox = new JCheckBox(Bundle.getMessage("PrintValid"));
    JCheckBox sortByTrackCheckBox = new JCheckBox(Bundle.getMessage("SortByTrack"));
    JCheckBox printHeadersCheckBox = new JCheckBox(Bundle.getMessage("PrintHeaders"));
    JCheckBox printPageHeaderCheckBox = new JCheckBox(Bundle.getMessage("PrintPageHeader"));
    JCheckBox truncateCheckBox = new JCheckBox(Bundle.getMessage("Truncate"));
    JCheckBox manifestDepartureTimeCheckBox = new JCheckBox(Bundle.getMessage("DepartureTime"));
    JCheckBox switchListDepartureTimeCheckBox = new JCheckBox(Bundle.getMessage("DepartureTime"));
    JCheckBox trackSummaryCheckBox = new JCheckBox(Bundle.getMessage("TrackSummary"));
    JCheckBox routeLocationCheckBox = new JCheckBox(Bundle.getMessage("RouteLocation"));
    JCheckBox groupCarMovesCheckBox = new JCheckBox(Bundle.getMessage("GroupCarMoves"));

    // text field
    JTextField pickupEngPrefix = new JTextField(10);
    JTextField dropEngPrefix = new JTextField(10);
    JTextField pickupCarPrefix = new JTextField(10);
    JTextField dropCarPrefix = new JTextField(10);
    JTextField localPrefix = new JTextField(10);
    JTextField switchListPickupCarPrefix = new JTextField(10);
    JTextField switchListDropCarPrefix = new JTextField(10);
    JTextField switchListLocalPrefix = new JTextField(10);
    JTextField hazardousTextField = new JTextField(20);

    // text area
    JTextArea commentTextArea = new JTextArea(2, 90);

    JScrollPane commentScroller = new JScrollPane(commentTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    // combo boxes
    JComboBox<String> fontComboBox = new JComboBox<>();
    JComboBox<String> manifestFormatComboBox = Setup.getManifestFormatComboBox();
    JComboBox<String> manifestOrientationComboBox = Setup.getOrientationComboBox();
    JComboBox<Integer> fontSizeComboBox = new JComboBox<>();
    JComboBox<String> switchListOrientationComboBox = Setup.getOrientationComboBox();

    JColorChooser pickupColorChooser = new JColorChooser();
    JColorChooser dropColorChooser = new JColorChooser();
    JColorChooser localColorChooser = new JColorChooser();
    JColorChooser missingCarColorChooser = new JColorChooser();

    // message formats
    List<JComboBox<String>> enginePickupMessageList = new ArrayList<>();
    List<JComboBox<String>> engineDropMessageList = new ArrayList<>();
    List<JComboBox<String>> carPickupMessageList = new ArrayList<>();
    List<JComboBox<String>> carDropMessageList = new ArrayList<>();
    List<JComboBox<String>> localMessageList = new ArrayList<>();
    List<JComboBox<String>> switchListCarPickupMessageList = new ArrayList<>();
    List<JComboBox<String>> switchListCarDropMessageList = new ArrayList<>();
    List<JComboBox<String>> switchListLocalMessageList = new ArrayList<>();

    // manifest panels
    JPanel pManifest = new JPanel();
    JPanel pEngPickup = new JPanel();
    JPanel pEngDrop = new JPanel();
    JPanel pPickup = new JPanel();
    JPanel pDrop = new JPanel();
    JPanel pLocal = new JPanel();

    // switch list panels
    JPanel pSwitchListOrientation = new JPanel();
    JPanel pSwPickup = new JPanel();
    JPanel pSwDrop = new JPanel();
    JPanel pSwLocal = new JPanel();

    public PrintOptionPanel() {

        // tool tips
        saveButton.setToolTipText(Bundle.getMessage("SaveToolTip"));
        addLogoButton.setToolTipText(Bundle.getMessage("AddLogoToolTip"));
        removeLogoButton.setToolTipText(Bundle.getMessage("RemoveLogoToolTip"));
        tabFormatCheckBox.setToolTipText(Bundle.getMessage("TabComment"));
        printLocCommentsCheckBox.setToolTipText(Bundle.getMessage("AddLocationComments"));
        printRouteCommentsCheckBox.setToolTipText(Bundle.getMessage("AddRouteComments"));
        printLoadsEmptiesCheckBox.setToolTipText(Bundle.getMessage("LoadsEmptiesComment"));
        printCabooseLoadCheckBox.setToolTipText(Bundle.getMessage("CabooseLoadTip"));
        printPassengerLoadCheckBox.setToolTipText(Bundle.getMessage("PassengerLoadTip"));
        printTrainScheduleNameCheckBox.setToolTipText(Bundle.getMessage("ShowTrainScheduleTip"));
        use12hrFormatCheckBox.setToolTipText(Bundle.getMessage("Use12hrFormatTip"));
        printValidCheckBox.setToolTipText(Bundle.getMessage("PrintValidTip"));
        sortByTrackCheckBox.setToolTipText(Bundle.getMessage("SortByTrackTip"));
        printHeadersCheckBox.setToolTipText(Bundle.getMessage("PrintHeadersTip"));
        printPageHeaderCheckBox.setToolTipText(Bundle.getMessage("PrintPageHeaderTip"));
        truncateCheckBox.setToolTipText(Bundle.getMessage("TruncateTip"));
        manifestDepartureTimeCheckBox.setToolTipText(Bundle.getMessage("DepartureTimeTip"));
        switchListDepartureTimeCheckBox.setToolTipText(Bundle.getMessage("SwitchListDepartureTimeTip"));
        routeLocationCheckBox.setToolTipText(Bundle.getMessage("RouteLocationTip"));
        editManifestCheckBox.setToolTipText(Bundle.getMessage("UseTextEditorTip"));
        trackSummaryCheckBox.setToolTipText(Bundle.getMessage("TrackSummaryTip"));
        groupCarMovesCheckBox.setToolTipText(Bundle.getMessage("GroupCarsTip"));

        addEngPickupComboboxButton.setToolTipText(Bundle.getMessage("AddMessageComboboxTip"));
        deleteEngPickupComboboxButton.setToolTipText(Bundle.getMessage("DeleteMessageComboboxTip"));
        addEngDropComboboxButton.setToolTipText(Bundle.getMessage("AddMessageComboboxTip"));
        deleteEngDropComboboxButton.setToolTipText(Bundle.getMessage("DeleteMessageComboboxTip"));

        addCarPickupComboboxButton.setToolTipText(Bundle.getMessage("AddMessageComboboxTip"));
        deleteCarPickupComboboxButton.setToolTipText(Bundle.getMessage("DeleteMessageComboboxTip"));
        addCarDropComboboxButton.setToolTipText(Bundle.getMessage("AddMessageComboboxTip"));
        deleteCarDropComboboxButton.setToolTipText(Bundle.getMessage("DeleteMessageComboboxTip"));
        addLocalComboboxButton.setToolTipText(Bundle.getMessage("AddMessageComboboxTip"));
        deleteLocalComboboxButton.setToolTipText(Bundle.getMessage("DeleteMessageComboboxTip"));

        addSwitchListPickupComboboxButton.setToolTipText(Bundle.getMessage("AddMessageComboboxTip"));
        deleteSwitchListPickupComboboxButton.setToolTipText(Bundle.getMessage("DeleteMessageComboboxTip"));
        addSwitchListDropComboboxButton.setToolTipText(Bundle.getMessage("AddMessageComboboxTip"));
        deleteSwitchListDropComboboxButton.setToolTipText(Bundle.getMessage("DeleteMessageComboboxTip"));
        addSwitchListLocalComboboxButton.setToolTipText(Bundle.getMessage("AddMessageComboboxTip"));
        deleteSwitchListLocalComboboxButton.setToolTipText(Bundle.getMessage("DeleteMessageComboboxTip"));

        // Manifest panel
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        pManifest.setLayout(new BoxLayout(pManifest, BoxLayout.Y_AXIS));
        JScrollPane pManifestPane = new JScrollPane(pManifest);
        pManifestPane.setBorder(BorderFactory.createTitledBorder(""));

        // row 1 font type, size, format, orientation, text colors
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));

        JPanel pFont = new JPanel();
        pFont.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutFont")));
        pFont.add(fontComboBox);

        JPanel pFontSize = new JPanel();
        pFontSize.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutFontSize")));
        pFontSize.add(fontSizeComboBox);

        JPanel pFormat = new JPanel();
        pFormat.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutFormat")));
        pFormat.add(tabFormatCheckBox);
        pFormat.add(manifestFormatComboBox);

        JPanel pOrientation = new JPanel();
        pOrientation.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutOrientation")));
        pOrientation.add(manifestOrientationComboBox);

        p1.add(pFont);
        p1.add(pFontSize);
        p1.add(pFormat);
        p1.add(pOrientation);
        p1.add(getColorChooserPanel(Bundle.getMessage("BorderLayoutPickupColor"), Setup.getPickupColor(),
                pickupColorChooser));
        p1.add(getColorChooserPanel(Bundle.getMessage("BorderLayoutDropColor"), Setup.getDropColor(),
                dropColorChooser));
        p1.add(getColorChooserPanel(Bundle.getMessage("BorderLayoutLocalColor"), Setup.getLocalColor(),
                localColorChooser));

        // load all of the message combo boxes, rows 2 through 5
        loadFormatComboBox();

        // Optional Switch List Panel
        JPanel pSl = new JPanel();
        pSl.setLayout(new BoxLayout(pSl, BoxLayout.X_AXIS));

        JPanel pSwitchFormat = new JPanel();
        pSwitchFormat.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutSwitchListFormat")));
        pSwitchFormat.add(formatSwitchListCheckBox);

        pSwitchListOrientation
                .setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutSwitchListOrientation")));
        pSwitchListOrientation.add(switchListOrientationComboBox);

        pSl.add(pSwitchFormat);
        pSl.add(pSwitchListOrientation);

        JPanel pM = new JPanel();
        pM.setLayout(new BoxLayout(pM, BoxLayout.X_AXIS));

        // Switch List options
        JPanel pSwitchOptions = new JPanel();
        pSwitchOptions.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutSwitchListOptions")));
        pSwitchOptions.add(trackSummaryCheckBox);
        pSwitchOptions.add(routeLocationCheckBox);
        pSwitchOptions.add(switchListDepartureTimeCheckBox);

        // Manifest options
        JPanel pManifestOptions = new JPanel();
        pManifestOptions.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutManifestOptions")));
        pManifestOptions.add(printLocCommentsCheckBox);
        pManifestOptions.add(printRouteCommentsCheckBox);
        pManifestOptions.add(manifestDepartureTimeCheckBox);
        pManifestOptions.add(truncateCheckBox);

        pM.add(pSwitchOptions);
        pM.add(pManifestOptions);

        // Manifest and Switch List options
        JPanel pManifestSwtichListOptions = new JPanel();
        pManifestSwtichListOptions.setLayout(new GridBagLayout());
        pManifestSwtichListOptions.setBorder(
                BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutManifestSwitchListOptions")));
        addItemLeft(pManifestSwtichListOptions, printValidCheckBox, 0, 0);
        addItemLeft(pManifestSwtichListOptions, printLoadsEmptiesCheckBox, 1, 0);
        addItemLeft(pManifestSwtichListOptions, groupCarMovesCheckBox, 2, 0);
        addItemLeft(pManifestSwtichListOptions, printCabooseLoadCheckBox, 3, 0);
        addItemLeft(pManifestSwtichListOptions, printPassengerLoadCheckBox, 4, 0);

        addItemLeft(pManifestSwtichListOptions, use12hrFormatCheckBox, 0, 1);
        addItemLeft(pManifestSwtichListOptions, printTrainScheduleNameCheckBox, 1, 1);
        addItemLeft(pManifestSwtichListOptions, sortByTrackCheckBox, 2, 1);
        addItemLeft(pManifestSwtichListOptions, printHeadersCheckBox, 3, 1);
        addItemLeft(pManifestSwtichListOptions, printPageHeaderCheckBox, 4, 1);

        JPanel p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.X_AXIS));

        // Use text editor for manifest
        JPanel pEdit = new JPanel();
        pEdit.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutManifestPreview")));
        pEdit.add(editManifestCheckBox);

        // manifest logo
        JPanel pLogo = new JPanel();
        pLogo.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutLogo")));
        pLogo.add(removeLogoButton);
        pLogo.add(addLogoButton);
        pLogo.add(logoURL);

        p2.add(pEdit);
        p2.add(pLogo);

        // comments
        JPanel pComments = new JPanel();
        pComments.setLayout(new BoxLayout(pComments, BoxLayout.X_AXIS));

        // missing cars comment
        JPanel pCommentMia = new JPanel();
        pCommentMia.setLayout(new GridBagLayout());
        pCommentMia.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutCommentOptions")));
        addItem(pCommentMia, commentScroller, 0, 0);
        addItem(pCommentMia, getColorChooserPanel(Setup.getMiaComment(), missingCarColorChooser), 2, 0);

        // Hazardous comment
        JPanel pHazardous = new JPanel();
        pHazardous.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutHazardous")));
        pHazardous.add(hazardousTextField);

        pComments.add(pCommentMia);
        pComments.add(pHazardous);

        pManifest.add(p1);
        pManifest.add(pEngPickup);
        pManifest.add(pEngDrop);
        pManifest.add(pPickup);
        pManifest.add(pDrop);
        pManifest.add(pLocal);
        pManifest.add(pSl);
        pManifest.add(pSwPickup);
        pManifest.add(pSwDrop);
        pManifest.add(pSwLocal);
        pManifest.add(pM);
        pManifest.add(pManifestSwtichListOptions);
        pManifest.add(p2);
        pManifest.add(pComments);

        // row 11
        JPanel pControl = new JPanel();
        pControl.setBorder(BorderFactory.createTitledBorder(""));
        pControl.setLayout(new GridBagLayout());
        addItem(pControl, saveButton, 0, 0);

        add(pManifestPane);
        add(pControl);

        manifestFormatComboBox.setSelectedItem(Setup.getManifestFormat());
        manifestOrientationComboBox.setSelectedItem(Setup.getManifestOrientation());
        switchListOrientationComboBox.setSelectedItem(Setup.getSwitchListOrientation());

        tabFormatCheckBox.setSelected(Setup.isTabEnabled());
        formatSwitchListCheckBox.setSelected(Setup.isSwitchListFormatSameAsManifest());
        printLocCommentsCheckBox.setSelected(Setup.isPrintLocationCommentsEnabled());
        printRouteCommentsCheckBox.setSelected(Setup.isPrintRouteCommentsEnabled());
        printLoadsEmptiesCheckBox.setSelected(Setup.isPrintLoadsAndEmptiesEnabled());
        printCabooseLoadCheckBox.setSelected(Setup.isPrintCabooseLoadEnabled());
        printPassengerLoadCheckBox.setSelected(Setup.isPrintPassengerLoadEnabled());
        printTrainScheduleNameCheckBox.setSelected(Setup.isPrintTrainScheduleNameEnabled());
        use12hrFormatCheckBox.setSelected(Setup.is12hrFormatEnabled());
        printValidCheckBox.setSelected(Setup.isPrintValidEnabled());
        sortByTrackCheckBox.setSelected(Setup.isSortByTrackNameEnabled());
        printPageHeaderCheckBox.setSelected(Setup.isPrintPageHeaderEnabled());
        printHeadersCheckBox.setSelected(Setup.isPrintHeadersEnabled());
        truncateCheckBox.setSelected(Setup.isPrintTruncateManifestEnabled());
        manifestDepartureTimeCheckBox.setSelected(Setup.isUseDepartureTimeEnabled());
        trackSummaryCheckBox.setSelected(Setup.isPrintTrackSummaryEnabled());
        trackSummaryCheckBox.setEnabled(Setup.isSwitchListRealTime());
        routeLocationCheckBox.setSelected(Setup.isSwitchListRouteLocationCommentEnabled());
        switchListDepartureTimeCheckBox.setSelected(Setup.isUseSwitchListDepartureTimeEnabled());
        editManifestCheckBox.setSelected(Setup.isManifestEditorEnabled());
        groupCarMovesCheckBox.setSelected(Setup.isGroupCarMovesEnabled());

        commentTextArea.setText(TrainCommon.getTextColorString(Setup.getMiaComment()));
        hazardousTextField.setText(Setup.getHazardousMsg());

        setSwitchListVisible(!formatSwitchListCheckBox.isSelected());

        updateLogoButtons();

        loadFontSizeComboBox();
        loadFontComboBox();

        // setup buttons
        addButtonAction(addLogoButton);
        addButtonAction(removeLogoButton);
        addButtonAction(saveButton);

        addButtonAction(addEngPickupComboboxButton);
        addButtonAction(deleteEngPickupComboboxButton);
        addButtonAction(addEngDropComboboxButton);
        addButtonAction(deleteEngDropComboboxButton);

        addButtonAction(addCarPickupComboboxButton);
        addButtonAction(deleteCarPickupComboboxButton);
        addButtonAction(addCarDropComboboxButton);
        addButtonAction(deleteCarDropComboboxButton);
        addButtonAction(addLocalComboboxButton);
        addButtonAction(deleteLocalComboboxButton);

        addButtonAction(addSwitchListPickupComboboxButton);
        addButtonAction(deleteSwitchListPickupComboboxButton);
        addButtonAction(addSwitchListDropComboboxButton);
        addButtonAction(deleteSwitchListDropComboboxButton);
        addButtonAction(addSwitchListLocalComboboxButton);
        addButtonAction(deleteSwitchListLocalComboboxButton);

        addCheckBoxAction(tabFormatCheckBox);
        addCheckBoxAction(formatSwitchListCheckBox);
        addCheckBoxAction(truncateCheckBox);

        addComboBoxAction(manifestFormatComboBox);

        Setup.getDefault().addPropertyChangeListener(this);
    }

    // Add Remove Logo and Save buttons
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == addLogoButton) {
            log.debug("add logo button pressed");
            File f = selectFile();
            if (f != null) {
                Setup.setManifestLogoURL(FileUtil.getPortableFilename(f));
            }
            updateLogoButtons();
        }
        if (ae.getSource() == removeLogoButton) {
            log.debug("remove logo button pressed");
            Setup.setManifestLogoURL("");
            updateLogoButtons();
        }
        // add or delete message comboBox
        if (ae.getSource() == addEngPickupComboboxButton) {
            addComboBox(pEngPickup, enginePickupMessageList, Setup.getEngineMessageComboBox());
        }
        if (ae.getSource() == deleteEngPickupComboboxButton) {
            removeComboBox(pEngPickup, enginePickupMessageList);
        }
        if (ae.getSource() == addEngDropComboboxButton) {
            addComboBox(pEngDrop, engineDropMessageList, Setup.getEngineMessageComboBox());
        }
        if (ae.getSource() == deleteEngDropComboboxButton) {
            removeComboBox(pEngDrop, engineDropMessageList);
        }

        if (ae.getSource() == addCarPickupComboboxButton) {
            addComboBox(pPickup, carPickupMessageList, Setup.getCarMessageComboBox());
        }
        if (ae.getSource() == deleteCarPickupComboboxButton) {
            removeComboBox(pPickup, carPickupMessageList);
        }
        if (ae.getSource() == addCarDropComboboxButton) {
            addComboBox(pDrop, carDropMessageList, Setup.getCarMessageComboBox());
        }
        if (ae.getSource() == deleteCarDropComboboxButton) {
            removeComboBox(pDrop, carDropMessageList);
        }

        if (ae.getSource() == addLocalComboboxButton) {
            addComboBox(pLocal, localMessageList, Setup.getCarMessageComboBox());
        }
        if (ae.getSource() == deleteLocalComboboxButton) {
            removeComboBox(pLocal, localMessageList);
        }

        if (ae.getSource() == addSwitchListPickupComboboxButton) {
            addComboBox(pSwPickup, switchListCarPickupMessageList, Setup.getCarMessageComboBox());
        }
        if (ae.getSource() == deleteSwitchListPickupComboboxButton) {
            removeComboBox(pSwPickup, switchListCarPickupMessageList);
        }
        if (ae.getSource() == addSwitchListDropComboboxButton) {
            addComboBox(pSwDrop, switchListCarDropMessageList, Setup.getCarMessageComboBox());
        }
        if (ae.getSource() == deleteSwitchListDropComboboxButton) {
            removeComboBox(pSwDrop, switchListCarDropMessageList);
        }

        if (ae.getSource() == addSwitchListLocalComboboxButton) {
            addComboBox(pSwLocal, switchListLocalMessageList, Setup.getCarMessageComboBox());
        }
        if (ae.getSource() == deleteSwitchListLocalComboboxButton) {
            removeComboBox(pSwLocal, switchListLocalMessageList);
        }

        if (ae.getSource() == saveButton) {
            this.savePreferences();
            var topLevelAncestor = getTopLevelAncestor();
            if (Setup.isCloseWindowOnSaveEnabled() && topLevelAncestor instanceof PrintOptionFrame) {
                ((PrintOptionFrame) topLevelAncestor).dispose();
            }
        }
    }

    @Override
    public void checkBoxActionPerformed(ActionEvent ae) {
        if (ae.getSource() == tabFormatCheckBox) {
            loadFontComboBox();
        }
        if (ae.getSource() == formatSwitchListCheckBox) {
            log.debug("Switch list check box activated");
            setSwitchListVisible(!formatSwitchListCheckBox.isSelected());
            setPreferredSize(null);
            var topLevelAncestor = getTopLevelAncestor();
            if (topLevelAncestor instanceof PrintOptionFrame) {
                ((PrintOptionFrame) topLevelAncestor).pack();
            }
        }
        if (ae.getSource() == truncateCheckBox && truncateCheckBox.isSelected()) {
            if (JmriJOptionPane.showConfirmDialog(this, Bundle.getMessage("EnableTruncateWarning"),
                    Bundle.getMessage("TruncateManifests?"), JmriJOptionPane.YES_NO_OPTION) == JmriJOptionPane.NO_OPTION) {
                truncateCheckBox.setSelected(false);
            }
        }
    }

    @Override
    public void comboBoxActionPerformed(ActionEvent ae) {
        if (ae.getSource() == manifestFormatComboBox) {
            loadFontComboBox();
        }
    }

    private void setSwitchListVisible(boolean b) {
        pSwitchListOrientation.setVisible(b);
        pSwPickup.setVisible(b);
        pSwDrop.setVisible(b);
        pSwLocal.setVisible(b);
    }

    /**
     * We always use the same file chooser in this class, so that the user's
     * last-accessed directory remains available.
     */
    JFileChooser fc = jmri.jmrit.XmlFile.userFileChooser(Bundle.getMessage("Images"));

    private File selectFile() {
        if (fc == null) {
            log.error("Could not find user directory");
        } else {
            fc.setDialogTitle(Bundle.getMessage("FindDesiredImage"));
            // when reusing the chooser, make sure new files are included
            fc.rescanCurrentDirectory();
            int retVal = fc.showOpenDialog(null);
            // handle selection or cancel
            if (retVal == JFileChooser.APPROVE_OPTION) {
                return fc.getSelectedFile();
            }
        }
        return null;
    }

    private void updateLogoButtons() {
        boolean flag = Setup.getManifestLogoURL().equals(Setup.NONE);
        addLogoButton.setVisible(flag);
        removeLogoButton.setVisible(!flag);
        logoURL.setText(Setup.getManifestLogoURL());
        var topLevelAncestor = getTopLevelAncestor();
        if (topLevelAncestor instanceof PrintOptionFrame) {
            ((PrintOptionFrame) topLevelAncestor).pack();
        }
    }

    private void addComboBox(JPanel panel, List<JComboBox<String>> list, JComboBox<String> box) {
        list.add(box);
        panel.add(box, list.size());
        panel.revalidate();
        pManifest.revalidate();
    }

    private void removeComboBox(JPanel panel, List<JComboBox<String>> list) {
        for (int i = 0; i < list.size(); i++) {
            JComboBox<String> cb = list.get(i);
            if (cb.getSelectedItem().equals(Setup.BLANK)) {
                list.remove(i);
                panel.remove(cb);
                panel.revalidate();
                pManifest.revalidate();
                return;
            }
        }
    }

    private void loadFormatComboBox() {
        // loco pick up message format
        pEngPickup.removeAll();
        enginePickupMessageList.clear();
        pEngPickup.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutPickupEngine")));
        pEngPickup.add(pickupEngPrefix);
        pickupEngPrefix.setText(Setup.getPickupEnginePrefix());
        String[] format = Setup.getPickupEngineMessageFormat();
        for (String f : format) {
            JComboBox<String> cb = Setup.getEngineMessageComboBox();
            cb.setSelectedItem(f);
            pEngPickup.add(cb);
            enginePickupMessageList.add(cb);
        }
        pEngPickup.add(addEngPickupComboboxButton);
        pEngPickup.add(deleteEngPickupComboboxButton);
        pEngPickup.revalidate();

        // loco set out message format
        pEngDrop.removeAll();
        engineDropMessageList.clear();
        pEngDrop.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutDropEngine")));
        pEngDrop.add(dropEngPrefix);
        dropEngPrefix.setText(Setup.getDropEnginePrefix());
        format = Setup.getDropEngineMessageFormat();
        for (String f : format) {
            JComboBox<String> cb = Setup.getEngineMessageComboBox();
            cb.setSelectedItem(f);
            pEngDrop.add(cb);
            engineDropMessageList.add(cb);
        }
        pEngDrop.add(addEngDropComboboxButton);
        pEngDrop.add(deleteEngDropComboboxButton);
        pEngDrop.revalidate();

        // car pickup message format
        pPickup.removeAll();
        carPickupMessageList.clear();
        pPickup.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutPickupCar")));
        pPickup.add(pickupCarPrefix);
        pickupCarPrefix.setText(Setup.getPickupCarPrefix());
        String[] pickFormat = Setup.getPickupManifestMessageFormat();
        for (String pf : pickFormat) {
            JComboBox<String> cb = Setup.getCarMessageComboBox();
            cb.setSelectedItem(pf);
            pPickup.add(cb);
            carPickupMessageList.add(cb);
        }
        pPickup.add(addCarPickupComboboxButton);
        pPickup.add(deleteCarPickupComboboxButton);
        pPickup.revalidate();

        // car drop message format
        pDrop.removeAll();
        carDropMessageList.clear();
        pDrop.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutDropCar")));
        pDrop.add(dropCarPrefix);
        dropCarPrefix.setText(Setup.getDropCarPrefix());
        String[] dropFormat = Setup.getDropManifestMessageFormat();
        for (String lf : dropFormat) {
            JComboBox<String> cb = Setup.getCarMessageComboBox();
            cb.setSelectedItem(lf);
            pDrop.add(cb);
            carDropMessageList.add(cb);
        }
        pDrop.add(addCarDropComboboxButton);
        pDrop.add(deleteCarDropComboboxButton);
        pDrop.revalidate();

        // local car move message format
        pLocal.removeAll();
        localMessageList.clear();
        pLocal.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutLocal")));
        pLocal.add(localPrefix);
        localPrefix.setText(Setup.getLocalPrefix());
        String[] localFormat = Setup.getLocalManifestMessageFormat();
        for (String lf : localFormat) {
            JComboBox<String> cb = Setup.getCarMessageComboBox();
            cb.setSelectedItem(lf);
            pLocal.add(cb);
            localMessageList.add(cb);
        }
        pLocal.add(addLocalComboboxButton);
        pLocal.add(deleteLocalComboboxButton);
        pLocal.revalidate();

        // switch list car pickup message format
        pSwPickup.removeAll();
        switchListCarPickupMessageList.clear();
        pSwPickup.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutSwitchListPickupCar")));
        pSwPickup.add(switchListPickupCarPrefix);
        switchListPickupCarPrefix.setText(Setup.getSwitchListPickupCarPrefix());
        pickFormat = Setup.getPickupSwitchListMessageFormat();
        for (String pf : pickFormat) {
            JComboBox<String> cb = Setup.getCarMessageComboBox();
            cb.setSelectedItem(pf);
            pSwPickup.add(cb);
            switchListCarPickupMessageList.add(cb);
        }
        pSwPickup.add(addSwitchListPickupComboboxButton);
        pSwPickup.add(deleteSwitchListPickupComboboxButton);

        // switch list car drop message format
        pSwDrop.removeAll();
        switchListCarDropMessageList.clear();
        pSwDrop.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutSwitchListDropCar")));
        pSwDrop.add(switchListDropCarPrefix);
        switchListDropCarPrefix.setText(Setup.getSwitchListDropCarPrefix());
        dropFormat = Setup.getDropSwitchListMessageFormat();
        for (String df : dropFormat) {
            JComboBox<String> cb = Setup.getCarMessageComboBox();
            cb.setSelectedItem(df);
            pSwDrop.add(cb);
            switchListCarDropMessageList.add(cb);
        }
        pSwDrop.add(addSwitchListDropComboboxButton);
        pSwDrop.add(deleteSwitchListDropComboboxButton);

        // switch list local car move message format
        pSwLocal.removeAll();
        switchListLocalMessageList.clear();
        pSwLocal.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutSwitchListLocal")));
        pSwLocal.add(switchListLocalPrefix);
        switchListLocalPrefix.setText(Setup.getSwitchListLocalPrefix());
        localFormat = Setup.getLocalSwitchListMessageFormat();
        for (String lf : localFormat) {
            JComboBox<String> cb = Setup.getCarMessageComboBox();
            cb.setSelectedItem(lf);
            pSwLocal.add(cb);
            switchListLocalMessageList.add(cb);
        }
        pSwLocal.add(addSwitchListLocalComboboxButton);
        pSwLocal.add(deleteSwitchListLocalComboboxButton);
    }

    private void loadFontSizeComboBox() {
        loadFontSizeComboBox(fontSizeComboBox);
        fontSizeComboBox.setSelectedItem(Setup.getManifestFontSize());
    }

    private void loadFontComboBox() {
        fontComboBox.removeAllItems();
        List<String> fonts = FontComboUtil.getFonts(FontComboUtil.ALL);
        if (tabFormatCheckBox.isSelected() || !manifestFormatComboBox.getSelectedItem().equals(Setup.STANDARD_FORMAT)) {
            fonts = FontComboUtil.getFonts(FontComboUtil.MONOSPACED);
        }
        for (String font : fonts) {
            fontComboBox.addItem(font);
        }
        fontComboBox.setSelectedItem(Setup.getFontName());
    }

    @Override
    public String getTabbedPreferencesTitle() {
        return Bundle.getMessage("TitlePrintOptions");
    }

    @Override
    public String getPreferencesTooltip() {
        return null;
    }

    @Override
    public void savePreferences() {
        // font name
        Setup.setFontName((String) fontComboBox.getSelectedItem());
        // font size
        Setup.setManifestFontSize((Integer) fontSizeComboBox.getSelectedItem());
        // page orientation
        Setup.setManifestOrientation((String) manifestOrientationComboBox.getSelectedItem());
        Setup.setSwitchListOrientation((String) switchListOrientationComboBox.getSelectedItem());
        // format
        Setup.setManifestFormat((String) manifestFormatComboBox.getSelectedItem());
        // drop and pick up color option
        Setup.setDropColor(dropColorChooser.getColor());
        Setup.setPickupColor(pickupColorChooser.getColor());
        Setup.setLocalColor(localColorChooser.getColor());
        // save engine pick up message format
        Setup.setPickupEnginePrefix(pickupEngPrefix.getText());
        String[] format = new String[enginePickupMessageList.size()];
        for (int i = 0; i < enginePickupMessageList.size(); i++) {
            JComboBox<?> cb = enginePickupMessageList.get(i);
            format[i] = (String) cb.getSelectedItem();
        }
        Setup.setPickupEngineMessageFormat(format);
        // save engine drop message format
        Setup.setDropEnginePrefix(dropEngPrefix.getText());
        format = new String[engineDropMessageList.size()];
        for (int i = 0; i < engineDropMessageList.size(); i++) {
            JComboBox<?> cb = engineDropMessageList.get(i);
            format[i] = (String) cb.getSelectedItem();
        }
        Setup.setDropEngineMessageFormat(format);
        // save car pick up message format
        Setup.setPickupCarPrefix(pickupCarPrefix.getText());
        format = new String[carPickupMessageList.size()];
        for (int i = 0; i < carPickupMessageList.size(); i++) {
            JComboBox<?> cb = carPickupMessageList.get(i);
            format[i] = (String) cb.getSelectedItem();
        }
        Setup.setPickupManifestMessageFormat(format);
        // save car drop message format
        Setup.setDropCarPrefix(dropCarPrefix.getText());
        format = new String[carDropMessageList.size()];
        for (int i = 0; i < carDropMessageList.size(); i++) {
            JComboBox<?> cb = carDropMessageList.get(i);
            format[i] = (String) cb.getSelectedItem();
        }
        Setup.setDropManifestMessageFormat(format);
        // save local message format
        Setup.setLocalPrefix(localPrefix.getText());
        format = new String[localMessageList.size()];
        for (int i = 0; i < localMessageList.size(); i++) {
            JComboBox<?> cb = localMessageList.get(i);
            format[i] = (String) cb.getSelectedItem();
        }
        Setup.setLocalManifestMessageFormat(format);
        // save switch list car pick up message format
        Setup.setSwitchListPickupCarPrefix(switchListPickupCarPrefix.getText());
        format = new String[switchListCarPickupMessageList.size()];
        for (int i = 0; i < switchListCarPickupMessageList.size(); i++) {
            JComboBox<?> cb = switchListCarPickupMessageList.get(i);
            format[i] = (String) cb.getSelectedItem();
        }
        Setup.setPickupSwitchListMessageFormat(format);
        // save switch list car drop message format
        Setup.setSwitchListDropCarPrefix(switchListDropCarPrefix.getText());
        format = new String[switchListCarDropMessageList.size()];
        for (int i = 0; i < switchListCarDropMessageList.size(); i++) {
            JComboBox<?> cb = switchListCarDropMessageList.get(i);
            format[i] = (String) cb.getSelectedItem();
        }
        Setup.setDropSwitchListMessageFormat(format);
        // save switch list local message format
        Setup.setSwitchListLocalPrefix(switchListLocalPrefix.getText());
        format = new String[switchListLocalMessageList.size()];
        for (int i = 0; i < switchListLocalMessageList.size(); i++) {
            JComboBox<?> cb = switchListLocalMessageList.get(i);
            format[i] = (String) cb.getSelectedItem();
        }
        Setup.setLocalSwitchListMessageFormat(format);
        // hazardous comment
        Setup.setHazardousMsg(hazardousTextField.getText());
        // misplaced car comment
        Setup.setMiaComment(
                TrainCommon.formatColorString(commentTextArea.getText(), missingCarColorChooser.getColor()));
        Setup.setSwitchListFormatSameAsManifest(formatSwitchListCheckBox.isSelected());
        Setup.setPrintLocationCommentsEnabled(printLocCommentsCheckBox.isSelected());
        Setup.setPrintRouteCommentsEnabled(printRouteCommentsCheckBox.isSelected());
        Setup.setPrintLoadsAndEmptiesEnabled(printLoadsEmptiesCheckBox.isSelected());
        Setup.setPrintCabooseLoadEnabled(printCabooseLoadCheckBox.isSelected());
        Setup.setPrintPassengerLoadEnabled(printPassengerLoadCheckBox.isSelected());
        Setup.set12hrFormatEnabled(use12hrFormatCheckBox.isSelected());
        Setup.setPrintValidEnabled(printValidCheckBox.isSelected());
        Setup.setSortByTrackNameEnabled(sortByTrackCheckBox.isSelected());
        Setup.setPrintPageHeaderEnabled(printPageHeaderCheckBox.isSelected());
        Setup.setPrintHeadersEnabled(printHeadersCheckBox.isSelected());
        Setup.setPrintTrainScheduleNameEnabled(printTrainScheduleNameCheckBox.isSelected());
        Setup.setPrintTruncateManifestEnabled(truncateCheckBox.isSelected());
        Setup.setUseDepartureTimeEnabled(manifestDepartureTimeCheckBox.isSelected());
        Setup.setManifestEditorEnabled(editManifestCheckBox.isSelected());
        Setup.setPrintTrackSummaryEnabled(trackSummaryCheckBox.isSelected());
        Setup.setUseSwitchListDepartureTimeEnabled(switchListDepartureTimeCheckBox.isSelected());
        Setup.setSwitchListRouteLocationCommentEnabled(routeLocationCheckBox.isSelected());
        Setup.setGroupCarMoves(groupCarMovesCheckBox.isSelected());

        // reload combo boxes if tab changed
        boolean oldTabEnabled = Setup.isTabEnabled();
        Setup.setTabEnabled(tabFormatCheckBox.isSelected());
        if (oldTabEnabled ^ Setup.isTabEnabled()) {
            loadFormatComboBox();
        }

        // recreate all train manifests
        InstanceManager.getDefault(TrainManager.class).setTrainsModified();

        InstanceManager.getDefault(OperationsSetupXml.class).writeOperationsFile();
    }

    @Override
    public boolean isDirty() {
        if ( // font name
        !Setup.getFontName().equals(fontComboBox.getSelectedItem())
        // font size
                || Setup.getManifestFontSize() != (Integer) fontSizeComboBox.getSelectedItem()
                // page orientation
                ||
                !Setup.getManifestOrientation().equals(manifestOrientationComboBox.getSelectedItem()) ||
                !Setup.getSwitchListOrientation().equals(switchListOrientationComboBox.getSelectedItem())
                // format
                ||
                !Setup.getManifestFormat().equals(manifestFormatComboBox.getSelectedItem())
                // drop and pick up color option
                ||
                !Setup.getDropColor().equals(dropColorChooser.getColor()) ||
                !Setup.getPickupColor().equals(pickupColorChooser.getColor()) ||
                !Setup.getLocalColor().equals(localColorChooser.getColor())
                // hazardous comment
                ||
                !Setup.getHazardousMsg().equals(hazardousTextField.getText())
                // misplaced car comment
                ||
                !Setup.getMiaComment().equals(
                        TrainCommon.formatColorString(commentTextArea.getText(), missingCarColorChooser.getColor())) ||
                Setup.isSwitchListFormatSameAsManifest() != formatSwitchListCheckBox.isSelected() ||
                Setup.isPrintLocationCommentsEnabled() != printLocCommentsCheckBox.isSelected() ||
                Setup.isPrintRouteCommentsEnabled() != printRouteCommentsCheckBox.isSelected() ||
                Setup.isPrintLoadsAndEmptiesEnabled() != printLoadsEmptiesCheckBox.isSelected() ||
                Setup.isPrintCabooseLoadEnabled() != printCabooseLoadCheckBox.isSelected() ||
                Setup.isPrintPassengerLoadEnabled() != printPassengerLoadCheckBox.isSelected() ||
                Setup.is12hrFormatEnabled() != use12hrFormatCheckBox.isSelected() ||
                Setup.isPrintValidEnabled() != printValidCheckBox.isSelected() ||
                Setup.isSortByTrackNameEnabled() != sortByTrackCheckBox.isSelected() ||
                Setup.isPrintHeadersEnabled() != printHeadersCheckBox.isSelected() ||
                Setup.isPrintPageHeaderEnabled() != printPageHeaderCheckBox.isSelected() ||
                Setup.isPrintTrainScheduleNameEnabled() != printTrainScheduleNameCheckBox.isSelected() ||
                Setup.isPrintTruncateManifestEnabled() != truncateCheckBox.isSelected() ||
                Setup.isUseDepartureTimeEnabled() != manifestDepartureTimeCheckBox.isSelected() ||
                Setup.isManifestEditorEnabled() != editManifestCheckBox.isSelected() ||
                Setup.isSwitchListRouteLocationCommentEnabled() != routeLocationCheckBox.isSelected() ||
                Setup.isPrintTrackSummaryEnabled() != trackSummaryCheckBox.isSelected() ||
                Setup.isUseSwitchListDepartureTimeEnabled() != switchListDepartureTimeCheckBox.isSelected() ||
                Setup.isGroupCarMovesEnabled() != groupCarMovesCheckBox.isSelected() ||
                Setup.isTabEnabled() != this.tabFormatCheckBox.isSelected()) {
            return true;
        }
        // save engine pick up message format
        String[] format = new String[enginePickupMessageList.size()];
        for (int i = 0; i < enginePickupMessageList.size(); i++) {
            JComboBox<?> cb = enginePickupMessageList.get(i);
            format[i] = (String) cb.getSelectedItem();
        }
        if (!Setup.getPickupEnginePrefix().equals(pickupEngPrefix.getText()) ||
                !Arrays.equals(Setup.getPickupEngineMessageFormat(), format)) {
            return true;
        }
        // save engine drop message format
        format = new String[engineDropMessageList.size()];
        for (int i = 0; i < engineDropMessageList.size(); i++) {
            JComboBox<?> cb = engineDropMessageList.get(i);
            format[i] = (String) cb.getSelectedItem();
        }
        if (!Setup.getDropEnginePrefix().equals(dropEngPrefix.getText()) ||
                !Arrays.equals(Setup.getDropEngineMessageFormat(), format)) {
            return true;
        }
        // save car pick up message format
        format = new String[carPickupMessageList.size()];
        for (int i = 0; i < carPickupMessageList.size(); i++) {
            JComboBox<?> cb = carPickupMessageList.get(i);
            format[i] = (String) cb.getSelectedItem();
        }
        if (!Setup.getPickupCarPrefix().equals(this.pickupCarPrefix.getText()) ||
                !Arrays.equals(Setup.getPickupManifestMessageFormat(), format)) {
            return true;
        }
        // save car drop message format
        format = new String[carDropMessageList.size()];
        for (int i = 0; i < carDropMessageList.size(); i++) {
            JComboBox<?> cb = carDropMessageList.get(i);
            format[i] = (String) cb.getSelectedItem();
        }
        if (!Setup.getDropCarPrefix().equals(this.dropCarPrefix.getText()) ||
                !Arrays.equals(Setup.getDropManifestMessageFormat(), format)) {
            return true;
        }
        // save local message format
        format = new String[localMessageList.size()];
        for (int i = 0; i < localMessageList.size(); i++) {
            JComboBox<?> cb = localMessageList.get(i);
            format[i] = (String) cb.getSelectedItem();
        }
        if (!Setup.getLocalPrefix().equals(this.localPrefix.getText()) ||
                !Arrays.equals(Setup.getLocalManifestMessageFormat(), format)) {
            return true;
        }
        // save switch list car pick up message format
        format = new String[switchListCarPickupMessageList.size()];
        for (int i = 0; i < switchListCarPickupMessageList.size(); i++) {
            JComboBox<?> cb = switchListCarPickupMessageList.get(i);
            format[i] = (String) cb.getSelectedItem();
        }
        if (!Setup.getSwitchListPickupCarPrefix().equals(this.switchListPickupCarPrefix.getText()) ||
                !Arrays.equals(Setup.getPickupSwitchListMessageFormat(), format)) {
            return true;
        }
        // save switch list car drop message format
        format = new String[switchListCarDropMessageList.size()];
        for (int i = 0; i < switchListCarDropMessageList.size(); i++) {
            JComboBox<?> cb = switchListCarDropMessageList.get(i);
            format[i] = (String) cb.getSelectedItem();
        }
        if (!Setup.getSwitchListDropCarPrefix().equals(this.switchListDropCarPrefix.getText()) ||
                !Arrays.equals(Setup.getDropSwitchListMessageFormat(), format)) {
            return true;
        }
        // save switch list local message format
        format = new String[switchListLocalMessageList.size()];
        for (int i = 0; i < switchListLocalMessageList.size(); i++) {
            JComboBox<?> cb = switchListLocalMessageList.get(i);
            format[i] = (String) cb.getSelectedItem();
        }
        return !Setup.getSwitchListLocalPrefix().equals(this.switchListLocalPrefix.getText()) ||
                !Arrays.equals(Setup.getLocalSwitchListMessageFormat(), format);
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(),
                    e.getNewValue());
        }
        if (e.getPropertyName().equals(Setup.REAL_TIME_PROPERTY_CHANGE)) {
            trackSummaryCheckBox.setEnabled(Setup.isSwitchListRealTime());
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PrintOptionPanel.class);

}
