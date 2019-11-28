package jmri.jmrit.operations.setup;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import jmri.InstanceManager;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.FileUtil;
import jmri.util.swing.FontComboUtil;
import jmri.util.swing.SplitButtonColorChooserPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for user edit of manifest and switch list print options
 *
 * @author Dan Boudreau Copyright (C) 2008, 2010, 2011, 2012, 2013
 */
public class PrintOptionPanel extends OperationsPreferencesPanel {

    private static final Logger log = LoggerFactory.getLogger(PrintOptionPanel.class);

    // labels
    JLabel logoURL = new JLabel("");

    // major buttons
    JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));
    JButton addLogoButton = new JButton(Bundle.getMessage("AddLogo"));
    JButton removeLogoButton = new JButton(Bundle.getMessage("RemoveLogo"));

    JButton addEngPickupComboboxButton = new JButton("+");
    JButton deleteEngPickupComboboxButton = new JButton("-");
    JButton addEngDropComboboxButton = new JButton("+");
    JButton deleteEngDropComboboxButton = new JButton("-");
    JButton addCarPickupComboboxButton = new JButton("+");
    JButton deleteCarPickupComboboxButton = new JButton("-");
    JButton addCarDropComboboxButton = new JButton("+");
    JButton deleteCarDropComboboxButton = new JButton("-");
    JButton addLocalComboboxButton = new JButton("+");
    JButton deleteLocalComboboxButton = new JButton("-");
    JButton addSwitchListPickupComboboxButton = new JButton("+");
    JButton deleteSwitchListPickupComboboxButton = new JButton("-");
    JButton addSwitchListDropComboboxButton = new JButton("+");
    JButton deleteSwitchListDropComboboxButton = new JButton("-");
    JButton addSwitchListLocalComboboxButton = new JButton("+");
    JButton deleteSwitchListLocalComboboxButton = new JButton("-");

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
    JCheckBox truncateCheckBox = new JCheckBox(Bundle.getMessage("Truncate"));
    JCheckBox departureTimeCheckBox = new JCheckBox(Bundle.getMessage("DepartureTime"));
    JCheckBox trackSummaryCheckBox = new JCheckBox(Bundle.getMessage("TrackSummary"));
    JCheckBox routeLocationCheckBox = new JCheckBox(Bundle.getMessage("RouteLocation"));

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

    JScrollPane commentScroller = new JScrollPane(commentTextArea,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    // combo boxes
    JComboBox<String> fontComboBox = new JComboBox<>();
    JComboBox<String> manifestFormatComboBox = Setup.getManifestFormatComboBox();
    JComboBox<String> manifestOrientationComboBox = Setup.getOrientationComboBox();
    JComboBox<Integer> fontSizeComboBox = new JComboBox<>();
    private JColorChooser pickupColorChooser = null;
    private JColorChooser dropColorChooser = null;
    private JColorChooser localColorChooser = null;
    JComboBox<String> switchListOrientationComboBox = Setup.getOrientationComboBox();

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

        // the following code sets the frame's initial state
        // add tool tips
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
        truncateCheckBox.setToolTipText(Bundle.getMessage("TruncateTip"));
        departureTimeCheckBox.setToolTipText(Bundle.getMessage("DepartureTimeTip"));
        routeLocationCheckBox.setToolTipText(Bundle.getMessage("RouteLocationTip"));
        editManifestCheckBox.setToolTipText(Bundle.getMessage("UseTextEditorTip"));
        trackSummaryCheckBox.setToolTipText(Bundle.getMessage("TrackSummaryTip"));

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
        deleteSwitchListPickupComboboxButton.setToolTipText(Bundle
                .getMessage("DeleteMessageComboboxTip"));
        addSwitchListDropComboboxButton.setToolTipText(Bundle.getMessage("AddMessageComboboxTip"));
        deleteSwitchListDropComboboxButton.setToolTipText(Bundle.getMessage("DeleteMessageComboboxTip"));
        addSwitchListLocalComboboxButton.setToolTipText(Bundle.getMessage("AddMessageComboboxTip"));
        deleteSwitchListLocalComboboxButton
                .setToolTipText(Bundle.getMessage("DeleteMessageComboboxTip"));

        // Manifest panel
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        pManifest.setLayout(new BoxLayout(pManifest, BoxLayout.Y_AXIS));
        JScrollPane pManifestPane = new JScrollPane(pManifest);
        pManifestPane.setBorder(BorderFactory.createTitledBorder(""));

        // row 1 font type and size
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

        manifestFormatComboBox.setSelectedItem(Setup.getManifestFormat());

        JPanel pOrientation = new JPanel();
        pOrientation.setBorder(BorderFactory.createTitledBorder(Bundle
                .getMessage("BorderLayoutOrientation")));
        pOrientation.add(manifestOrientationComboBox);

        JPanel pPickupColor = new JPanel();
        pPickupColor.setBorder(BorderFactory.createTitledBorder(Bundle
                .getMessage("BorderLayoutPickupColor")));
        pickupColorChooser = new JColorChooser(Setup.getPickupColor());
        AbstractColorChooserPanel pickupColorPanels[] = {new SplitButtonColorChooserPanel()};
        pickupColorChooser.setChooserPanels(pickupColorPanels);
        pickupColorChooser.setPreviewPanel(new JPanel());
        pPickupColor.add(pickupColorChooser);

        JPanel pDropColor = new JPanel();
        pDropColor.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutDropColor")));
        dropColorChooser = new JColorChooser(Setup.getDropColor());
        AbstractColorChooserPanel dropColorPanels[] = {new SplitButtonColorChooserPanel()};
        dropColorChooser.setChooserPanels(dropColorPanels);
        dropColorChooser.setPreviewPanel(new JPanel());
        pDropColor.add(dropColorChooser);

        JPanel pLocalColor = new JPanel();
        pLocalColor.setBorder(BorderFactory.createTitledBorder(Bundle
                .getMessage("BorderLayoutLocalColor")));
        localColorChooser = new JColorChooser(Setup.getLocalColor());
        AbstractColorChooserPanel localColorPanels[] = {new SplitButtonColorChooserPanel()};
        localColorChooser.setChooserPanels(localColorPanels);
        localColorChooser.setPreviewPanel(new JPanel());
        pLocalColor.add(localColorChooser);

        p1.add(pFont);
        p1.add(pFontSize);
        p1.add(pFormat);
        p1.add(pOrientation);
        p1.add(pPickupColor);
        p1.add(pDropColor);
        p1.add(pLocalColor);

        // load all of the message combo boxes
        loadFormatComboBox();

        // Optional Switch List Panel
        JPanel pSl = new JPanel();
        pSl.setLayout(new BoxLayout(pSl, BoxLayout.X_AXIS));

        JPanel pSwitchFormat = new JPanel();
        pSwitchFormat.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutSwitchListFormat")));
        pSwitchFormat.add(formatSwitchListCheckBox);

        pSwitchListOrientation.setBorder(BorderFactory.createTitledBorder(Bundle
                .getMessage("BorderLayoutSwitchListOrientation")));
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

        // Manifest options
        JPanel pManifestOptions = new JPanel();
        pManifestOptions.setBorder(BorderFactory.createTitledBorder(Bundle
                .getMessage("BorderLayoutManifestOptions")));
        pManifestOptions.add(printLocCommentsCheckBox);
        pManifestOptions.add(printRouteCommentsCheckBox);
        pManifestOptions.add(departureTimeCheckBox);
        pManifestOptions.add(truncateCheckBox);
        
        pM.add(pSwitchOptions);
        pM.add(pManifestOptions);

        // Manifest and Switch List options
        JPanel pManifestSwtichListOptions = new JPanel();
        pManifestSwtichListOptions.setBorder(BorderFactory.createTitledBorder(Bundle
                .getMessage("BorderLayoutManifestSwitchListOptions")));
        pManifestSwtichListOptions.add(printValidCheckBox);
        pManifestSwtichListOptions.add(printLoadsEmptiesCheckBox);
        pManifestSwtichListOptions.add(printCabooseLoadCheckBox);
        pManifestSwtichListOptions.add(printPassengerLoadCheckBox);
        pManifestSwtichListOptions.add(use12hrFormatCheckBox);
        pManifestSwtichListOptions.add(printTrainScheduleNameCheckBox);
        pManifestSwtichListOptions.add(sortByTrackCheckBox);
        pManifestSwtichListOptions.add(printHeadersCheckBox);

        JPanel p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.X_AXIS));

        // Use text editor for manifest
        JPanel pEdit = new JPanel();
        pEdit.setBorder(BorderFactory.createTitledBorder(Bundle
                .getMessage("BorderLayoutManifestPreview")));
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
        JPanel pComment = new JPanel();
        pComment.setLayout(new GridBagLayout());
        pComment.setBorder(BorderFactory.createTitledBorder(Bundle
                .getMessage("BorderLayoutCommentOptions")));
        addItem(pComment, commentScroller, 0, 0);

        // Hazardous comment
        JPanel pHazardous = new JPanel();
        pHazardous
                .setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutHazardous")));
        pHazardous.add(hazardousTextField);

        pComments.add(pComment);
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
        printHeadersCheckBox.setSelected(Setup.isPrintHeadersEnabled());
        truncateCheckBox.setSelected(Setup.isTruncateManifestEnabled());
        departureTimeCheckBox.setSelected(Setup.isUseDepartureTimeEnabled());
        trackSummaryCheckBox.setSelected(Setup.isTrackSummaryEnabled());
        trackSummaryCheckBox.setEnabled(Setup.isSwitchListRealTime());
        routeLocationCheckBox.setSelected(Setup.isSwitchListRouteLocationCommentEnabled());
        editManifestCheckBox.setSelected(Setup.isManifestEditorEnabled());

        hazardousTextField.setText(Setup.getHazardousMsg());

        setSwitchListVisible(!formatSwitchListCheckBox.isSelected());

        updateLogoButtons();
        dropColorChooser.setColor(Setup.getDropColor());
        pickupColorChooser.setColor(Setup.getPickupColor());
        localColorChooser.setColor(Setup.getLocalColor());

        commentTextArea.setText(Setup.getMiaComment());

        // load font sizes 7 through 18
        for (int i = 7; i < 19; i++) {
            fontSizeComboBox.addItem(i);
        }
        fontSizeComboBox.setSelectedItem(Setup.getManifestFontSize());
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

        initMinimumSize();
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
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
    }

    @Override
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE",
            justification = "checks for instance of PrintOptionFrame") // NOI18N
    public void checkBoxActionPerformed(ActionEvent ae) {
        if (ae.getSource() == tabFormatCheckBox) {
            loadFontComboBox();
        }
        if (ae.getSource() == formatSwitchListCheckBox) {
            log.debug("Switch list check box activated");
            setSwitchListVisible(!formatSwitchListCheckBox.isSelected());
            setPreferredSize(null);
            if (this.getTopLevelAncestor() instanceof PrintOptionFrame) {
                ((PrintOptionFrame) this.getTopLevelAncestor()).pack();
            }
        }
        if (ae.getSource() == truncateCheckBox && truncateCheckBox.isSelected()) {
            if (JOptionPane.showConfirmDialog(this, Bundle.getMessage("EnableTruncateWarning"),
                    Bundle.getMessage("TruncateManifests?"),
                    JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
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
                File file = fc.getSelectedFile();
                return file;
            }
        }
        return null;
    }

    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE",
            justification = "checks for instance of PrintOptionFrame") // NOI18N
    private void updateLogoButtons() {
        boolean flag = Setup.getManifestLogoURL().equals(Setup.NONE);
        addLogoButton.setVisible(flag);
        removeLogoButton.setVisible(!flag);
        logoURL.setText(Setup.getManifestLogoURL());
        if (this.getTopLevelAncestor() instanceof PrintOptionFrame) {
            ((PrintOptionFrame) this.getTopLevelAncestor()).pack();
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
        pEngPickup.setBorder(BorderFactory.createTitledBorder(Bundle
                .getMessage("BorderLayoutPickupEngine")));
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
        pSwPickup.setBorder(BorderFactory.createTitledBorder(Bundle
                .getMessage("BorderLayoutSwitchListPickupCar")));
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
        pSwDrop.setBorder(BorderFactory.createTitledBorder(Bundle
                .getMessage("BorderLayoutSwitchListDropCar")));
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
        pSwLocal.setBorder(BorderFactory.createTitledBorder(Bundle
                .getMessage("BorderLayoutSwitchListLocal")));
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
        Setup.setMiaComment(commentTextArea.getText());
        Setup.setSwitchListFormatSameAsManifest(formatSwitchListCheckBox.isSelected());
        Setup.setPrintLocationCommentsEnabled(printLocCommentsCheckBox.isSelected());
        Setup.setPrintRouteCommentsEnabled(printRouteCommentsCheckBox.isSelected());
        Setup.setPrintLoadsAndEmptiesEnabled(printLoadsEmptiesCheckBox.isSelected());
        Setup.setPrintCabooseLoadEnabled(printCabooseLoadCheckBox.isSelected());
        Setup.setPrintPassengerLoadEnabled(printPassengerLoadCheckBox.isSelected());
        Setup.set12hrFormatEnabled(use12hrFormatCheckBox.isSelected());
        Setup.setPrintValidEnabled(printValidCheckBox.isSelected());
        Setup.setSortByTrackNameEnabled(sortByTrackCheckBox.isSelected());
        Setup.setPrintHeadersEnabled(printHeadersCheckBox.isSelected());
        Setup.setPrintTrainScheduleNameEnabled(printTrainScheduleNameCheckBox.isSelected());
        Setup.setTruncateManifestEnabled(truncateCheckBox.isSelected());
        Setup.setUseDepartureTimeEnabled(departureTimeCheckBox.isSelected());
        Setup.setManifestEditorEnabled(editManifestCheckBox.isSelected());
        Setup.setTrackSummaryEnabled(trackSummaryCheckBox.isSelected());
        Setup.setSwitchListRouteLocationCommentEnabled(routeLocationCheckBox.isSelected());

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
                ||
                Setup.getManifestFontSize() != (Integer) fontSizeComboBox.getSelectedItem()
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
                !Setup.getMiaComment().equals(commentTextArea.getText()) ||
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
                Setup.isPrintTrainScheduleNameEnabled() != printTrainScheduleNameCheckBox.isSelected() ||
                Setup.isTruncateManifestEnabled() != truncateCheckBox.isSelected() ||
                Setup.isUseDepartureTimeEnabled() != departureTimeCheckBox.isSelected() ||
                Setup.isManifestEditorEnabled() != editManifestCheckBox.isSelected() ||
                Setup.isSwitchListRouteLocationCommentEnabled() != routeLocationCheckBox.isSelected() ||
                Setup.isTrackSummaryEnabled() != trackSummaryCheckBox.isSelected() ||
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
}
