// PrintOptionFrame.java

package jmri.jmrit.operations.setup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.swing.FontComboUtil;

/**
 * Frame for user edit of manifest and switch list print options
 * 
 * @author Dan Boudreau Copyright (C) 2008, 2010, 2011, 2012, 2013
 * @version $Revision$
 */

public class PrintOptionFrame extends OperationsFrame {

	// labels
	JLabel logoURL = new JLabel("");

	// major buttons
	JButton saveButton = new JButton(Bundle.getMessage("Save"));
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
//	JCheckBox twoColumnFormatCheckBox = new JCheckBox(Bundle.getMessage("TwoColumn"));
	JCheckBox formatSwitchListCheckBox = new JCheckBox(Bundle.getMessage("SameAsManifest"));
	JCheckBox editManifestCheckBox = new JCheckBox(Bundle.getMessage("UseTextEditor"));
	JCheckBox printLocCommentsCheckBox = new JCheckBox(Bundle.getMessage("PrintLocationComments"));
	JCheckBox printRouteCommentsCheckBox = new JCheckBox(Bundle.getMessage("PrintRouteComments"));
	JCheckBox printLoadsEmptiesCheckBox = new JCheckBox(Bundle.getMessage("PrintLoadsEmpties"));
	JCheckBox printTimetableNameCheckBox = new JCheckBox(Bundle.getMessage("PrintTimetableName"));
	JCheckBox use12hrFormatCheckBox = new JCheckBox(Bundle.getMessage("12hrFormat"));
	JCheckBox printValidCheckBox = new JCheckBox(Bundle.getMessage("PrintValid"));
	JCheckBox sortByTrackCheckBox = new JCheckBox(Bundle.getMessage("SortByTrack"));
	JCheckBox printHeadersCheckBox = new JCheckBox(Bundle.getMessage("PrintHeaders"));
	JCheckBox truncateCheckBox = new JCheckBox(Bundle.getMessage("Truncate"));
	JCheckBox departureTimeCheckBox = new JCheckBox(Bundle.getMessage("DepartureTime"));

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
	JComboBox fontComboBox = new JComboBox();
	JComboBox manifestFormatComboBox = Setup.getManifestFormatComboBox();
	JComboBox manifestOrientationComboBox = Setup.getOrientationComboBox();
	JComboBox fontSizeComboBox = new JComboBox();
	JComboBox pickupComboBox = Setup.getPrintColorComboBox(); // colors
	JComboBox dropComboBox = Setup.getPrintColorComboBox();
	JComboBox localComboBox = Setup.getPrintColorComboBox();
	JComboBox switchListOrientationComboBox = Setup.getOrientationComboBox();

	// message formats
	List<JComboBox> enginePickupMessageList = new ArrayList<JComboBox>();
	List<JComboBox> engineDropMessageList = new ArrayList<JComboBox>();
	List<JComboBox> carPickupMessageList = new ArrayList<JComboBox>();
	List<JComboBox> carDropMessageList = new ArrayList<JComboBox>();
	List<JComboBox> localMessageList = new ArrayList<JComboBox>();
	List<JComboBox> switchListCarPickupMessageList = new ArrayList<JComboBox>();
	List<JComboBox> switchListCarDropMessageList = new ArrayList<JComboBox>();
	List<JComboBox> switchListLocalMessageList = new ArrayList<JComboBox>();

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

	public PrintOptionFrame() {
		super(Bundle.getMessage("TitlePrintOptions"));
	}

	public void initComponents() {

		// the following code sets the frame's initial state

		// add tool tips
		saveButton.setToolTipText(Bundle.getMessage("SaveToolTip"));
		addLogoButton.setToolTipText(Bundle.getMessage("AddLogoToolTip"));
		removeLogoButton.setToolTipText(Bundle.getMessage("RemoveLogoToolTip"));
		tabFormatCheckBox.setToolTipText(Bundle.getMessage("TabComment"));
//		twoColumnFormatCheckBox.setToolTipText(Bundle.getMessage("TwoColumnTip"));
		printLocCommentsCheckBox.setToolTipText(Bundle.getMessage("AddLocationComments"));
		printRouteCommentsCheckBox.setToolTipText(Bundle.getMessage("AddRouteComments"));
		printLoadsEmptiesCheckBox.setToolTipText(Bundle.getMessage("LoadsEmptiesComment"));
		printTimetableNameCheckBox.setToolTipText(Bundle.getMessage("ShowTimetableTip"));
		use12hrFormatCheckBox.setToolTipText(Bundle.getMessage("Use12hrFormatTip"));
		printValidCheckBox.setToolTipText(Bundle.getMessage("PrintValidTip"));
		sortByTrackCheckBox.setToolTipText(Bundle.getMessage("SortByTrackTip"));
		printHeadersCheckBox.setToolTipText(Bundle.getMessage("PrintHeadersTip"));
		truncateCheckBox.setToolTipText(Bundle.getMessage("TruncateTip"));
		departureTimeCheckBox.setToolTipText(Bundle.getMessage("DepartureTimeTip"));
		editManifestCheckBox.setToolTipText(Bundle.getMessage("UseTextEditorTip"));

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
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
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
		pPickupColor.add(pickupComboBox);

		JPanel pDropColor = new JPanel();
		pDropColor
				.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutDropColor")));
		pDropColor.add(dropComboBox);

		JPanel pLocalColor = new JPanel();
		pLocalColor.setBorder(BorderFactory.createTitledBorder(Bundle
				.getMessage("BorderLayoutLocalColor")));
		pLocalColor.add(localComboBox);

		JPanel pSwitchFormat = new JPanel();
		pSwitchFormat.setBorder(BorderFactory.createTitledBorder(Bundle
				.getMessage("BorderLayoutSwitchListFormat")));
		pSwitchFormat.add(formatSwitchListCheckBox);

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

		pSwitchListOrientation.setLayout(new GridBagLayout());
		pSwitchListOrientation.setBorder(BorderFactory.createTitledBorder(Bundle
				.getMessage("BorderLayoutSwitchListOrientation")));
		addItem(pSwitchListOrientation, switchListOrientationComboBox, 0, 0);
		addItem(pSwitchListOrientation, new JLabel(" "), 1, 0); // pad
		addItem(pSwitchListOrientation, new JLabel(" "), 2, 0); // pad
		addItem(pSwitchListOrientation, new JLabel(" "), 3, 0); // pad

		pSl.add(pSwitchFormat);
		pSl.add(pSwitchListOrientation);

		// Manifest comments
		JPanel pManifestOptions = new JPanel();
		pManifestOptions.setBorder(BorderFactory.createTitledBorder(Bundle
				.getMessage("BorderLayoutManifestOptions")));
		pManifestOptions.add(printValidCheckBox);
		pManifestOptions.add(printLocCommentsCheckBox);
		pManifestOptions.add(printRouteCommentsCheckBox);
		pManifestOptions.add(printLoadsEmptiesCheckBox);
		pManifestOptions.add(use12hrFormatCheckBox);
		pManifestOptions.add(departureTimeCheckBox);
		pManifestOptions.add(printTimetableNameCheckBox);
		pManifestOptions.add(truncateCheckBox);
		pManifestOptions.add(sortByTrackCheckBox);
		pManifestOptions.add(printHeadersCheckBox);

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
		pManifest.add(pManifestOptions);
		pManifest.add(p2);
		pManifest.add(pComments);

		// row 11
		JPanel pControl = new JPanel();
		pControl.setBorder(BorderFactory.createTitledBorder(""));
		pControl.setLayout(new GridBagLayout());
		addItem(pControl, saveButton, 0, 0);

		getContentPane().add(pManifestPane);
		getContentPane().add(pControl);

		manifestOrientationComboBox.setSelectedItem(Setup.getManifestOrientation());
		switchListOrientationComboBox.setSelectedItem(Setup.getSwitchListOrientation());

		tabFormatCheckBox.setSelected(Setup.isTabEnabled());
//		twoColumnFormatCheckBox.setSelected(Setup.isTwoColumnFormatEnabled());
		formatSwitchListCheckBox.setSelected(Setup.isSwitchListFormatSameAsManifest());
		printLocCommentsCheckBox.setSelected(Setup.isPrintLocationCommentsEnabled());
		printRouteCommentsCheckBox.setSelected(Setup.isPrintRouteCommentsEnabled());
		printLoadsEmptiesCheckBox.setSelected(Setup.isPrintLoadsAndEmptiesEnabled());
		printTimetableNameCheckBox.setSelected(Setup.isPrintTimetableNameEnabled());
		use12hrFormatCheckBox.setSelected(Setup.is12hrFormatEnabled());
		printValidCheckBox.setSelected(Setup.isPrintValidEnabled());
		sortByTrackCheckBox.setSelected(Setup.isSortByTrackEnabled());
		printHeadersCheckBox.setSelected(Setup.isPrintHeadersEnabled());
		truncateCheckBox.setSelected(Setup.isTruncateManifestEnabled());
		departureTimeCheckBox.setSelected(Setup.isUseDepartureTimeEnabled());
		editManifestCheckBox.setSelected(Setup.isManifestEditorEnabled());

		hazardousTextField.setText(Setup.getHazardousMsg());

		setSwitchListVisible(!formatSwitchListCheckBox.isSelected());

		updateLogoButtons();
		dropComboBox.setSelectedItem(Setup.getDropTextColor());
		pickupComboBox.setSelectedItem(Setup.getPickupTextColor());
		localComboBox.setSelectedItem(Setup.getLocalTextColor());

		commentTextArea.setText(Setup.getMiaComment());

		// load font sizes 7 through 18
		for (int i = 7; i < 19; i++)
			fontSizeComboBox.addItem(i);
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
//		addCheckBoxAction(twoColumnFormatCheckBox);
		addCheckBoxAction(formatSwitchListCheckBox);
		
		addComboBoxAction(manifestFormatComboBox);

		// build menu
		JMenuBar menuBar = new JMenuBar();
		JMenu toolMenu = new JMenu(Bundle.getMessage("Tools"));
		toolMenu.add(new PrintMoreOptionAction());
		toolMenu.add(new EditManifestHeaderTextAction());
		toolMenu.add(new EditManifestTextAction());
		toolMenu.add(new EditSwitchListTextAction());
		menuBar.add(toolMenu);
		setJMenuBar(menuBar);
		addHelpMenu("package.jmri.jmrit.operations.Operations_PrintOptions", true); // NOI18N

		initMinimumSize();
	}

	// Add Remove Logo and Save buttons
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == addLogoButton) {
			log.debug("add logo button pressed");
			File f = selectFile();
			if (f != null)
				Setup.setManifestLogoURL(f.getAbsolutePath());
			updateLogoButtons();
		}
		if (ae.getSource() == removeLogoButton) {
			log.debug("remove logo button pressed");
			Setup.setManifestLogoURL("");
			updateLogoButtons();
		}
		// add or delete message comboBox
		if (ae.getSource() == addEngPickupComboboxButton)
			addComboBox(pEngPickup, enginePickupMessageList, Setup.getEngineMessageComboBox());
		if (ae.getSource() == deleteEngPickupComboboxButton)
			removeComboBox(pEngPickup, enginePickupMessageList);
		if (ae.getSource() == addEngDropComboboxButton)
			addComboBox(pEngDrop, engineDropMessageList, Setup.getEngineMessageComboBox());
		if (ae.getSource() == deleteEngDropComboboxButton)
			removeComboBox(pEngDrop, engineDropMessageList);

		if (ae.getSource() == addCarPickupComboboxButton)
			addComboBox(pPickup, carPickupMessageList, Setup.getCarMessageComboBox());
		if (ae.getSource() == deleteCarPickupComboboxButton)
			removeComboBox(pPickup, carPickupMessageList);
		if (ae.getSource() == addCarDropComboboxButton)
			addComboBox(pDrop, carDropMessageList, Setup.getCarMessageComboBox());
		if (ae.getSource() == deleteCarDropComboboxButton)
			removeComboBox(pDrop, carDropMessageList);

		if (ae.getSource() == addLocalComboboxButton)
			addComboBox(pLocal, localMessageList, Setup.getCarMessageComboBox());
		if (ae.getSource() == deleteLocalComboboxButton)
			removeComboBox(pLocal, localMessageList);

		if (ae.getSource() == addSwitchListPickupComboboxButton)
			addComboBox(pSwPickup, switchListCarPickupMessageList, Setup.getCarMessageComboBox());
		if (ae.getSource() == deleteSwitchListPickupComboboxButton)
			removeComboBox(pSwPickup, switchListCarPickupMessageList);
		if (ae.getSource() == addSwitchListDropComboboxButton)
			addComboBox(pSwDrop, switchListCarDropMessageList, Setup.getCarMessageComboBox());
		if (ae.getSource() == deleteSwitchListDropComboboxButton)
			removeComboBox(pSwDrop, switchListCarDropMessageList);

		if (ae.getSource() == addSwitchListLocalComboboxButton)
			addComboBox(pSwLocal, switchListLocalMessageList, Setup.getCarMessageComboBox());
		if (ae.getSource() == deleteSwitchListLocalComboboxButton)
			removeComboBox(pSwLocal, switchListLocalMessageList);

		if (ae.getSource() == saveButton) {
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
			Setup.setDropTextColor((String) dropComboBox.getSelectedItem());
			Setup.setPickupTextColor((String) pickupComboBox.getSelectedItem());
			Setup.setLocalTextColor((String) localComboBox.getSelectedItem());
			// save engine pick up message format
			Setup.setPickupEnginePrefix(pickupEngPrefix.getText());
			String[] format = new String[enginePickupMessageList.size()];
			for (int i = 0; i < enginePickupMessageList.size(); i++) {
				JComboBox b = enginePickupMessageList.get(i);
				format[i] = (String) b.getSelectedItem();
			}
			Setup.setPickupEngineMessageFormat(format);
			// save engine drop message format
			Setup.setDropEnginePrefix(dropEngPrefix.getText());
			format = new String[engineDropMessageList.size()];
			for (int i = 0; i < engineDropMessageList.size(); i++) {
				JComboBox b = engineDropMessageList.get(i);
				format[i] = (String) b.getSelectedItem();
			}
			Setup.setDropEngineMessageFormat(format);
			// save car pick up message format
			Setup.setPickupCarPrefix(pickupCarPrefix.getText());
			format = new String[carPickupMessageList.size()];
			for (int i = 0; i < carPickupMessageList.size(); i++) {
				JComboBox b = carPickupMessageList.get(i);
				format[i] = (String) b.getSelectedItem();
			}
			Setup.setPickupCarMessageFormat(format);
			// save car drop message format
			Setup.setDropCarPrefix(dropCarPrefix.getText());
			format = new String[carDropMessageList.size()];
			for (int i = 0; i < carDropMessageList.size(); i++) {
				JComboBox b = carDropMessageList.get(i);
				format[i] = (String) b.getSelectedItem();
			}
			Setup.setDropCarMessageFormat(format);
			// save local message format
			Setup.setLocalPrefix(localPrefix.getText());
			format = new String[localMessageList.size()];
			for (int i = 0; i < localMessageList.size(); i++) {
				JComboBox b = localMessageList.get(i);
				format[i] = (String) b.getSelectedItem();
			}
			Setup.setLocalMessageFormat(format);
			// save switch list car pick up message format
			Setup.setSwitchListPickupCarPrefix(switchListPickupCarPrefix.getText());
			format = new String[switchListCarPickupMessageList.size()];
			for (int i = 0; i < switchListCarPickupMessageList.size(); i++) {
				JComboBox b = switchListCarPickupMessageList.get(i);
				format[i] = (String) b.getSelectedItem();
			}
			Setup.setSwitchListPickupCarMessageFormat(format);
			// save switch list car drop message format
			Setup.setSwitchListDropCarPrefix(switchListDropCarPrefix.getText());
			format = new String[switchListCarDropMessageList.size()];
			for (int i = 0; i < switchListCarDropMessageList.size(); i++) {
				JComboBox b = switchListCarDropMessageList.get(i);
				format[i] = (String) b.getSelectedItem();
			}
			Setup.setSwitchListDropCarMessageFormat(format);
			// save switch list local message format
			Setup.setSwitchListLocalPrefix(switchListLocalPrefix.getText());
			format = new String[switchListLocalMessageList.size()];
			for (int i = 0; i < switchListLocalMessageList.size(); i++) {
				JComboBox b = switchListLocalMessageList.get(i);
				format[i] = (String) b.getSelectedItem();
			}
			Setup.setSwitchListLocalMessageFormat(format);
			// hazardous comment
			Setup.setHazardousMsg(hazardousTextField.getText());
			// misplaced car comment
			Setup.setMiaComment(commentTextArea.getText());	
			Setup.setSwitchListFormatSameAsManifest(formatSwitchListCheckBox.isSelected());
			Setup.setPrintLocationCommentsEnabled(printLocCommentsCheckBox.isSelected());
			Setup.setPrintRouteCommentsEnabled(printRouteCommentsCheckBox.isSelected());
			Setup.setPrintLoadsAndEmptiesEnabled(printLoadsEmptiesCheckBox.isSelected());
			Setup.set12hrFormatEnabled(use12hrFormatCheckBox.isSelected());
			Setup.setPrintValidEnabled(printValidCheckBox.isSelected());
			Setup.setSortByTrackEnabled(sortByTrackCheckBox.isSelected());
			Setup.setPrintHeadersEnabled(printHeadersCheckBox.isSelected());
			Setup.setPrintTimetableNameEnabled(printTimetableNameCheckBox.isSelected());
			Setup.setTruncateManifestEnabled(truncateCheckBox.isSelected());
			Setup.setUseDepartureTimeEnabled(departureTimeCheckBox.isSelected());
			Setup.setManifestEditorEnabled(editManifestCheckBox.isSelected());
//			Setup.setTwoColumnFormatEnabled(twoColumnFormatCheckBox.isSelected());
			
			// reload combo boxes if tab changed
			boolean oldTabEnabled = Setup.isTabEnabled();
			Setup.setTabEnabled(tabFormatCheckBox.isSelected());	
			if (oldTabEnabled ^ Setup.isTabEnabled())
				loadFormatComboBox();
			
			// recreate all train manifests
			TrainManager.instance().setTrainsModified();
			
			OperationsSetupXml.instance().writeOperationsFile();
			if (Setup.isCloseWindowOnSaveEnabled())
				dispose();
		}
	}

	public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == tabFormatCheckBox) {
			loadFontComboBox();
		}
		if (ae.getSource() == formatSwitchListCheckBox) {
			log.debug("Switch list check box activated");
			setSwitchListVisible(!formatSwitchListCheckBox.isSelected());
			setPreferredSize(null);
			pack();
		}
	}
	
	public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
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
	 * We always use the same file chooser in this class, so that the user's last-accessed directory remains available.
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

	private void updateLogoButtons() {
		boolean flag = Setup.getManifestLogoURL().equals("");
		addLogoButton.setVisible(flag);
		removeLogoButton.setVisible(!flag);
		logoURL.setText(Setup.getManifestLogoURL());
		pack();
	}

	private void addComboBox(JPanel panel, List<JComboBox> list, JComboBox box) {
		list.add(box);
		panel.add(box, list.size());
		panel.validate();
		pManifest.revalidate();
	}

	private void removeComboBox(JPanel panel, List<JComboBox> list) {
		for (int i = 0; i < list.size(); i++) {
			JComboBox cb = list.get(i);
			if (cb.getSelectedItem() == Setup.NONE) {
				list.remove(i);
				panel.remove(cb);
				panel.validate();
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
		for (int i = 0; i < format.length; i++) {
			JComboBox b = Setup.getEngineMessageComboBox();
			b.setSelectedItem(format[i]);
			pEngPickup.add(b);
			enginePickupMessageList.add(b);
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
		for (int i = 0; i < format.length; i++) {
			JComboBox b = Setup.getEngineMessageComboBox();
			b.setSelectedItem(format[i]);
			pEngDrop.add(b);
			engineDropMessageList.add(b);
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
		String[] pickFormat = Setup.getPickupCarMessageFormat();
		for (int i = 0; i < pickFormat.length; i++) {
			JComboBox b = Setup.getCarMessageComboBox();
			b.setSelectedItem(pickFormat[i]);
			pPickup.add(b);
			carPickupMessageList.add(b);
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
		String[] dropFormat = Setup.getDropCarMessageFormat();
		for (int i = 0; i < dropFormat.length; i++) {
			JComboBox b = Setup.getCarMessageComboBox();
			b.setSelectedItem(dropFormat[i]);
			pDrop.add(b);
			carDropMessageList.add(b);
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
		String[] localFormat = Setup.getLocalMessageFormat();
		for (int i = 0; i < localFormat.length; i++) {
			JComboBox b = Setup.getCarMessageComboBox();
			b.setSelectedItem(localFormat[i]);
			pLocal.add(b);
			localMessageList.add(b);
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
		pickFormat = Setup.getSwitchListPickupCarMessageFormat();
		for (int i = 0; i < pickFormat.length; i++) {
			JComboBox b = Setup.getCarMessageComboBox();
			b.setSelectedItem(pickFormat[i]);
			pSwPickup.add(b);
			switchListCarPickupMessageList.add(b);
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
		dropFormat = Setup.getSwitchListDropCarMessageFormat();
		for (int i = 0; i < dropFormat.length; i++) {
			JComboBox b = Setup.getCarMessageComboBox();
			b.setSelectedItem(dropFormat[i]);
			pSwDrop.add(b);
			switchListCarDropMessageList.add(b);
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
		localFormat = Setup.getSwitchListLocalMessageFormat();
		for (int i = 0; i < localFormat.length; i++) {
			JComboBox b = Setup.getCarMessageComboBox();
			b.setSelectedItem(localFormat[i]);
			pSwLocal.add(b);
			switchListLocalMessageList.add(b);
		}
		pSwLocal.add(addSwitchListLocalComboboxButton);
		pSwLocal.add(deleteSwitchListLocalComboboxButton);
	}

	private void loadFontComboBox() {
		fontComboBox.removeAllItems();
		List<String> fonts = FontComboUtil.getFonts(FontComboUtil.ALL);
		if (tabFormatCheckBox.isSelected() || !manifestFormatComboBox.getSelectedItem().equals(Setup.STANDARD_FORMAT))
			fonts = FontComboUtil.getFonts(FontComboUtil.MONOSPACED);
		for (String font : fonts) {
			fontComboBox.addItem(font);
		}
		fontComboBox.setSelectedItem(Setup.getFontName());
	}

	static Logger log = LoggerFactory
			.getLogger(OperationsSetupFrame.class.getName());
}
