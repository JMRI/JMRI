// OperationsSetupFrame.java

package jmri.jmrit.operations.setup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import jmri.jmrit.display.LocoIcon;
import jmri.jmrit.operations.ExceptionDisplayFrame;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.UnexpectedExceptionContext;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.routes.RouteManagerXml;

/**
 * Frame for user edit of operation parameters
 * 
 * @author Dan Boudreau Copyright (C) 2008, 2010, 2011, 2012
 * @version $Revision$
 */

public class OperationsSetupFrame extends OperationsFrame implements
		java.beans.PropertyChangeListener {

	// labels

	JLabel textIconNorth = new JLabel(Bundle.getMessage("IconNorth"));
	JLabel textIconSouth = new JLabel(Bundle.getMessage("IconSouth"));
	JLabel textIconEast = new JLabel(Bundle.getMessage("IconEast"));
	JLabel textIconWest = new JLabel(Bundle.getMessage("IconWest"));
	JLabel textIconLocal = new JLabel(Bundle.getMessage("IconLocal"));
	JLabel textIconTerminate = new JLabel(Bundle.getMessage("IconTerminate"));
	// JLabel textComment = new JLabel(Bundle.getMessage("Comment"));

	// major buttons
	JButton backupButton = new JButton(Bundle.getMessage("Backup"));
	JButton restoreButton = new JButton(Bundle.getMessage("Restore"));
	JButton saveButton = new JButton(Bundle.getMessage("Save"));

	// radio buttons
	JRadioButton scaleZ = new JRadioButton("Z"); // NOI18N
	JRadioButton scaleN = new JRadioButton("N"); // NOI18N
	JRadioButton scaleTT = new JRadioButton("TT"); // NOI18N
	JRadioButton scaleHOn3 = new JRadioButton("HOn3"); // NOI18N
	JRadioButton scaleOO = new JRadioButton("OO"); // NOI18N
	JRadioButton scaleHO = new JRadioButton("HO"); // NOI18N
	JRadioButton scaleSn3 = new JRadioButton("Sn3"); // NOI18N
	JRadioButton scaleS = new JRadioButton("S"); // NOI18N
	JRadioButton scaleOn3 = new JRadioButton("On3"); // NOI18N
	JRadioButton scaleO = new JRadioButton("O"); // NOI18N
	JRadioButton scaleG = new JRadioButton("G"); // NOI18N

	JRadioButton typeDesc = new JRadioButton(Bundle.getMessage("Descriptive"));
	JRadioButton typeAAR = new JRadioButton(Bundle.getMessage("AAR"));

	JRadioButton feetUnit = new JRadioButton(Bundle.getMessage("Feet"));
	JRadioButton meterUnit = new JRadioButton(Bundle.getMessage("Meter"));

	// check boxes
	JCheckBox eastCheckBox = new JCheckBox(Bundle.getMessage("eastwest"));
	JCheckBox northCheckBox = new JCheckBox(Bundle.getMessage("northsouth"));
	JCheckBox mainMenuCheckBox = new JCheckBox(Bundle.getMessage("MainMenu"));
	JCheckBox closeOnSaveCheckBox = new JCheckBox(Bundle.getMessage("CloseOnSave"));
	JCheckBox autoSaveCheckBox = new JCheckBox(Bundle.getMessage("AutoSave"));
	JCheckBox autoBackupCheckBox = new JCheckBox(Bundle.getMessage("AutoBackup"));
	JCheckBox iconCheckBox = new JCheckBox(Bundle.getMessage("trainIcon"));
	JCheckBox appendCheckBox = new JCheckBox(Bundle.getMessage("trainIconAppend"));
	// JCheckBox rfidCheckBox = new JCheckBox(Bundle.getMessage("EnableRfid"));

	// text field
	// JTextField ownerTextField = new JTextField(10);
	JTextField panelTextField = new JTextField(30);
	JTextField railroadNameTextField = new JTextField(35);
	JTextField maxLengthTextField = new JTextField(5);
	JTextField maxEngineSizeTextField = new JTextField(3);
	JTextField switchTimeTextField = new JTextField(3);
	JTextField travelTimeTextField = new JTextField(3);
	JTextField yearTextField = new JTextField(4);

	// combo boxes
	JComboBox northComboBox = new JComboBox();
	JComboBox southComboBox = new JComboBox();
	JComboBox eastComboBox = new JComboBox();
	JComboBox westComboBox = new JComboBox();
	JComboBox localComboBox = new JComboBox();
	JComboBox terminateComboBox = new JComboBox();

	protected static final String NEW_LINE = "\n"; // NOI18N

	public OperationsSetupFrame() {
		super(Bundle.getMessage("TitleOperationsSetup"));
	}

	public void initComponents() {

		// the following code sets the frame's initial state

		// create manager to load operation settings
		OperationsSetupXml.instance();

		// load fields
		railroadNameTextField.setText(Setup.getRailroadName());
		maxLengthTextField.setText(Integer.toString(Setup.getTrainLength()));
		maxEngineSizeTextField.setText(Integer.toString(Setup.getEngineSize()));
		switchTimeTextField.setText(Integer.toString(Setup.getSwitchTime()));
		travelTimeTextField.setText(Integer.toString(Setup.getTravelTime()));
		panelTextField.setText(Setup.getPanelName());
		// ownerTextField.setText(Setup.getOwnerName());
		yearTextField.setText(Setup.getYearModeled());

		// load checkboxes
		mainMenuCheckBox.setSelected(Setup.isMainMenuEnabled());
		closeOnSaveCheckBox.setSelected(Setup.isCloseWindowOnSaveEnabled());
		autoSaveCheckBox.setSelected(Setup.isAutoSaveEnabled());
		autoBackupCheckBox.setSelected(Setup.isAutoBackupEnabled());
		iconCheckBox.setSelected(Setup.isTrainIconCordEnabled());
		appendCheckBox.setSelected(Setup.isTrainIconAppendEnabled());

		// add tool tips
		backupButton.setToolTipText(Bundle.getMessage("BackupToolTip"));
		restoreButton.setToolTipText(Bundle.getMessage("RestoreToolTip"));
		saveButton.setToolTipText(Bundle.getMessage("SaveToolTip"));
		panelTextField.setToolTipText(Bundle.getMessage("EnterPanelName"));
		yearTextField.setToolTipText(Bundle.getMessage("EnterYearModeled"));
		autoSaveCheckBox.setToolTipText(Bundle.getMessage("AutoSaveTip"));
		autoBackupCheckBox.setToolTipText(Bundle.getMessage("AutoBackUpTip"));
		maxLengthTextField.setToolTipText(Bundle.getMessage("MaxLengthTip"));

		// Layout the panel by rows
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		JPanel panel = new JPanel();
		JScrollPane panelPane = new JScrollPane(panel);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panelPane.setBorder(BorderFactory.createTitledBorder(""));

		// row 1a
		JPanel p1 = new JPanel();
		p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));

		JPanel pRailroadName = new JPanel();
		pRailroadName.setLayout(new GridBagLayout());
		pRailroadName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("RailroadName")));
		addItem(pRailroadName, railroadNameTextField, 0, 0);
		p1.add(pRailroadName);

		// row 1b
		JPanel pTrainDir = new JPanel();
		pTrainDir.setLayout(new GridBagLayout());
		pTrainDir.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("direction")));
		addItemLeft(pTrainDir, northCheckBox, 1, 2);
		addItemLeft(pTrainDir, eastCheckBox, 2, 2);
		p1.add(pTrainDir);

		setDirectionCheckBox(Setup.getTrainDirection());

		// row 3a
		JPanel p3 = new JPanel();
		p3.setLayout(new BoxLayout(p3, BoxLayout.X_AXIS));

		JPanel pTrainLength = new JPanel();
		pTrainLength.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("MaxLength")));
		addItem(pTrainLength, maxLengthTextField, 0, 0);
		p3.add(pTrainLength);

		// row 3b
		JPanel pMaxEngine = new JPanel();
		pMaxEngine.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("MaxEngine")));
		addItem(pMaxEngine, maxEngineSizeTextField, 0, 0);
		p3.add(pMaxEngine);

		// row 3c
		JPanel p5 = new JPanel();
		p5.setLayout(new BoxLayout(p5, BoxLayout.X_AXIS));

		JPanel pSwitchTime = new JPanel();
		pSwitchTime.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("MoveTime")));
		addItem(pSwitchTime, switchTimeTextField, 0, 0);
		p3.add(pSwitchTime);

		// row 3d
		JPanel pTravelTime = new JPanel();
		pTravelTime.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TravelTime")));
		addItem(pTravelTime, travelTimeTextField, 0, 0);
		p3.add(pTravelTime);

		// row 2
		JPanel pScale = new JPanel();
		pScale.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Scale")));

		ButtonGroup scaleGroup = new ButtonGroup();
		scaleGroup.add(scaleZ);
		scaleGroup.add(scaleN);
		scaleGroup.add(scaleTT);
		scaleGroup.add(scaleHOn3);
		scaleGroup.add(scaleOO);
		scaleGroup.add(scaleHO);
		scaleGroup.add(scaleSn3);
		scaleGroup.add(scaleS);
		scaleGroup.add(scaleOn3);
		scaleGroup.add(scaleO);
		scaleGroup.add(scaleG);

		pScale.add(scaleZ);
		pScale.add(scaleN);
		pScale.add(scaleTT);
		pScale.add(scaleHOn3);
		pScale.add(scaleOO);
		pScale.add(scaleHO);
		pScale.add(scaleSn3);
		pScale.add(scaleS);
		pScale.add(scaleOn3);
		pScale.add(scaleO);
		pScale.add(scaleG);
		setScale();

		// row 4a
		JPanel p9 = new JPanel();
		p9.setLayout(new BoxLayout(p9, BoxLayout.X_AXIS));

		JPanel pCarTypeButtons = new JPanel();
		pCarTypeButtons.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("CarTypes")));
		ButtonGroup carTypeGroup = new ButtonGroup();
		carTypeGroup.add(typeDesc);
		carTypeGroup.add(typeAAR);
		pCarTypeButtons.add(typeDesc);
		pCarTypeButtons.add(typeAAR);
		p9.add(pCarTypeButtons);
		setCarTypes();

		// row 4b
		JPanel pLengthUnit = new JPanel();
		pLengthUnit.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutLength")));
		ButtonGroup lengthUnitGroup = new ButtonGroup();
		lengthUnitGroup.add(feetUnit);
		lengthUnitGroup.add(meterUnit);
		pLengthUnit.add(feetUnit);
		pLengthUnit.add(meterUnit);
		p9.add(pLengthUnit);
		setLengthUnit();

		// row 4c
		JPanel pYearModeled = new JPanel();
		pYearModeled.setBorder(BorderFactory.createTitledBorder(Bundle
				.getMessage("BorderLayoutYearModeled")));
		pYearModeled.add(yearTextField);

		p9.add(pYearModeled);

		// Option panel
		JPanel options = new JPanel();
		options.setLayout(new GridBagLayout());
		options.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutOptions")));
		addItem(options, mainMenuCheckBox, 0, 0);
		addItem(options, closeOnSaveCheckBox, 1, 0);
		addItem(options, autoSaveCheckBox, 2, 0);
		addItem(options, autoBackupCheckBox, 3, 0);

		// p9.add(options);

		// 1st scroll panel
		panel.add(p1);
		panel.add(pScale);
		panel.add(p3);
		panel.add(p5);
		panel.add(p9);

		// Icon panel
		JPanel pIcon = new JPanel();
		pIcon.setLayout(new BoxLayout(pIcon, BoxLayout.Y_AXIS));
		JScrollPane pIconPane = new JScrollPane(pIcon);
		pIconPane.setBorder(BorderFactory.createTitledBorder(Bundle
				.getMessage("BorderLayoutPanelOptions")));

		// row 1 Icon panel
		JPanel p1Icon = new JPanel();
		p1Icon.setLayout(new BoxLayout(p1Icon, BoxLayout.X_AXIS));

		JPanel pPanelName = new JPanel();
		pPanelName.setLayout(new GridBagLayout());
		pPanelName
				.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutPanelName")));
		addItem(pPanelName, panelTextField, 0, 0);
		p1Icon.add(pPanelName);

		JPanel pIconControl = new JPanel();
		pIconControl.setLayout(new GridBagLayout());
		pIconControl.setBorder(BorderFactory.createTitledBorder(Bundle
				.getMessage("BorderLayoutIconOptions")));
		addItem(pIconControl, appendCheckBox, 0, 0);
		addItem(pIconControl, iconCheckBox, 1, 0);
		p1Icon.add(pIconControl);

		pIcon.add(p1Icon);

		JPanel pIconColors = new JPanel();
		pIconColors.setLayout(new GridBagLayout());
		pIconColors.setBorder(BorderFactory.createTitledBorder(Bundle
				.getMessage("BorderLayoutIconColors")));

		addItem(pIconColors, textIconNorth, 0, 4);
		addItemLeft(pIconColors, northComboBox, 1, 4);
		addItem(pIconColors, textIconSouth, 0, 5);
		addItemLeft(pIconColors, southComboBox, 1, 5);
		addItem(pIconColors, textIconEast, 0, 8);
		addItemLeft(pIconColors, eastComboBox, 1, 8);
		addItem(pIconColors, textIconWest, 0, 9);
		addItemLeft(pIconColors, westComboBox, 1, 9);
		addItem(pIconColors, textIconLocal, 0, 10);
		addItemLeft(pIconColors, localComboBox, 1, 10);
		addItem(pIconColors, textIconTerminate, 0, 11);
		addItemLeft(pIconColors, terminateComboBox, 1, 11);

		pIcon.add(pIconColors);

		loadIconComboBox(northComboBox);
		loadIconComboBox(southComboBox);
		loadIconComboBox(eastComboBox);
		loadIconComboBox(westComboBox);
		loadIconComboBox(localComboBox);
		loadIconComboBox(terminateComboBox);
		northComboBox.setSelectedItem(Setup.getTrainIconColorNorth());
		southComboBox.setSelectedItem(Setup.getTrainIconColorSouth());
		eastComboBox.setSelectedItem(Setup.getTrainIconColorEast());
		westComboBox.setSelectedItem(Setup.getTrainIconColorWest());
		localComboBox.setSelectedItem(Setup.getTrainIconColorLocal());
		terminateComboBox.setSelectedItem(Setup.getTrainIconColorTerminate());

		// row 15
		JPanel pControl = new JPanel();
		pControl.setLayout(new GridBagLayout());
		addItem(pControl, restoreButton, 0, 9);
		addItem(pControl, backupButton, 1, 9);

		addItem(pControl, saveButton, 3, 9);

		getContentPane().add(panelPane);
		getContentPane().add(options);
		getContentPane().add(pIconPane);
		getContentPane().add(pControl);

		// setup buttons
		addButtonAction(backupButton);
		addButtonAction(restoreButton);
		addButtonAction(saveButton);
		addCheckBoxAction(eastCheckBox);
		addCheckBoxAction(northCheckBox);

		// build menu
		JMenuBar menuBar = new JMenuBar();
		JMenu toolMenu = new JMenu(Bundle.getMessage("Tools"));
		toolMenu.add(new OptionAction(Bundle.getMessage("TitleOptions")));
		toolMenu.add(new PrintOptionAction());
		toolMenu.add(new BuildReportOptionAction());
		toolMenu.add(new BackupFilesAction(Bundle.getMessage("Backup")));
		toolMenu.add(new RestoreFilesAction(Bundle.getMessage("Restore")));
		toolMenu.add(new LoadDemoAction(Bundle.getMessage("LoadDemo")));
		toolMenu.add(new ResetAction(Bundle.getMessage("ResetOperations")));
		toolMenu.add(new ManageBackupsAction(Bundle.getMessage("ManageAutoBackups")));

		menuBar.add(toolMenu);
		menuBar.add(new jmri.jmrit.operations.OperationsMenu());
		setJMenuBar(menuBar);
		addHelpMenu("package.jmri.jmrit.operations.Operations_Settings", true); // NOI18N

		initMinimumSize();
	}

	// Save, Delete, Add buttons
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == backupButton) {
			// Backup and Restore dialogs are now modal. so no need to check for an existing instance
			BackupDialog bd = new BackupDialog();
			bd.pack();
			bd.setLocationRelativeTo(null);
			bd.setVisible(true);
		}
		if (ae.getSource() == restoreButton) {
			RestoreDialog rd = new RestoreDialog();
			rd.pack();
			rd.setLocationRelativeTo(null);
			rd.setVisible(true);
		}
		if (ae.getSource() == saveButton) {
			save();
		}
	}

	private void save() {

		// check input fields
		int maxTrainLength;
		try {
			maxTrainLength = Integer.parseInt(maxLengthTextField.getText());
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this, Bundle.getMessage("MaxLength"),
					Bundle.getMessage("CanNotAcceptNumber"), JOptionPane.ERROR_MESSAGE);
			return;
		}

		try {
			Integer.parseInt(maxEngineSizeTextField.getText());
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this, Bundle.getMessage("MaxEngine"),
					Bundle.getMessage("CanNotAcceptNumber"), JOptionPane.ERROR_MESSAGE);
			return;
		}

		try {
			Integer.parseInt(switchTimeTextField.getText());
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this, Bundle.getMessage("MoveTime"),
					Bundle.getMessage("CanNotAcceptNumber"), JOptionPane.ERROR_MESSAGE);
			return;
		}

		try {
			Integer.parseInt(travelTimeTextField.getText());
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this, Bundle.getMessage("TravelTime"),
					Bundle.getMessage("CanNotAcceptNumber"), JOptionPane.ERROR_MESSAGE);
			return;
		}

		// if max train length has changed, check routes
		checkRoutes();

		// set car types
		if (typeDesc.isSelected() && !Setup.getCarTypes().equals(Setup.DESCRIPTIVE)
				|| typeAAR.isSelected() && !Setup.getCarTypes().equals(Setup.AAR)) {

			// backup files before changing car type descriptions
			AutoBackup backup = new AutoBackup();
			try {
				backup.autoBackup();
			} catch (Exception ex) {
				UnexpectedExceptionContext context = new UnexpectedExceptionContext(ex,
						"Auto backup before changing Car types"); // NOI18N
				new ExceptionDisplayFrame(context);
			}

			if (typeDesc.isSelected()) {
				CarTypes.instance().changeDefaultNames(Setup.DESCRIPTIVE);
				Setup.setCarTypes(Setup.DESCRIPTIVE);
			} else {
				CarTypes.instance().changeDefaultNames(Setup.AAR);
				Setup.setCarTypes(Setup.AAR);
			}

			// save all the modified files
			OperationsXml.save();
		}
		// main menu enabled?
		Setup.setMainMenuEnabled(mainMenuCheckBox.isSelected());
		Setup.setCloseWindowOnSaveEnabled(closeOnSaveCheckBox.isSelected());
		Setup.setAutoSaveEnabled(autoSaveCheckBox.isSelected());
		Setup.setAutoBackupEnabled(autoBackupCheckBox.isSelected());

		// add panel name to setup
		Setup.setPanelName(panelTextField.getText());
		
		// train Icon X&Y
		Setup.setTrainIconCordEnabled(iconCheckBox.isSelected());
		Setup.setTrainIconAppendEnabled(appendCheckBox.isSelected());
		
		// save train icon colors
		Setup.setTrainIconColorNorth((String) northComboBox.getSelectedItem());
		Setup.setTrainIconColorSouth((String) southComboBox.getSelectedItem());
		Setup.setTrainIconColorEast((String) eastComboBox.getSelectedItem());
		Setup.setTrainIconColorWest((String) westComboBox.getSelectedItem());
		Setup.setTrainIconColorLocal((String) localComboBox.getSelectedItem());
		Setup.setTrainIconColorTerminate((String) terminateComboBox.getSelectedItem());
		// set train direction
		int direction = 0;
		if (eastCheckBox.isSelected())
			direction = Setup.EAST + Setup.WEST;
		if (northCheckBox.isSelected())
			direction += Setup.NORTH + Setup.SOUTH;
		Setup.setTrainDirection(direction);
		// set max engine length
		Setup.setEngineSize(Integer.parseInt(maxEngineSizeTextField.getText()));
		// set switch time
		Setup.setSwitchTime(Integer.parseInt(switchTimeTextField.getText()));
		// set travel time
		Setup.setTravelTime(Integer.parseInt(travelTimeTextField.getText()));
		// set scale
		if (scaleZ.isSelected())
			Setup.setScale(Setup.Z_SCALE);
		if (scaleN.isSelected())
			Setup.setScale(Setup.N_SCALE);
		if (scaleTT.isSelected())
			Setup.setScale(Setup.TT_SCALE);
		if (scaleOO.isSelected())
			Setup.setScale(Setup.OO_SCALE);
		if (scaleHOn3.isSelected())
			Setup.setScale(Setup.HOn3_SCALE);
		if (scaleHO.isSelected())
			Setup.setScale(Setup.HO_SCALE);
		if (scaleSn3.isSelected())
			Setup.setScale(Setup.Sn3_SCALE);
		if (scaleS.isSelected())
			Setup.setScale(Setup.S_SCALE);
		if (scaleOn3.isSelected())
			Setup.setScale(Setup.On3_SCALE);
		if (scaleO.isSelected())
			Setup.setScale(Setup.O_SCALE);
		if (scaleG.isSelected())
			Setup.setScale(Setup.G_SCALE);
		Setup.setRailroadName(railroadNameTextField.getText());
		// Set Unit of Length
		if (feetUnit.isSelected())
			Setup.setLengthUnit(Setup.FEET);
		if (meterUnit.isSelected())
			Setup.setLengthUnit(Setup.METER);
		Setup.setYearModeled(yearTextField.getText());
		// warn about train length being too short
		if (maxTrainLength != Setup.getTrainLength()) {
			if (maxTrainLength < 500 && Setup.getLengthUnit().equals(Setup.FEET) || maxTrainLength < 160
					&& Setup.getLengthUnit().equals(Setup.METER)) {
				JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle
						.getMessage("LimitTrainLength"), new Object[] { maxTrainLength,
						Setup.getLengthUnit().toLowerCase() }), Bundle.getMessage("WarningTooShort"),
						JOptionPane.WARNING_MESSAGE);
			}
		}
		// set max train length
		Setup.setTrainLength(Integer.parseInt(maxLengthTextField.getText()));
		OperationsSetupXml.instance().writeOperationsFile();
		if (Setup.isCloseWindowOnSaveEnabled())
			dispose();
	}

	// if max train length has changed, check routes
	private void checkRoutes() {
		int maxLength = Integer.parseInt(maxLengthTextField.getText());
		if (maxLength > Setup.getTrainLength()) {
			JOptionPane.showMessageDialog(this, Bundle.getMessage("RouteLengthNotModified"), MessageFormat.format(
					Bundle.getMessage("MaxTrainLengthIncreased"), new Object[] { maxLength, Setup.getLengthUnit().toLowerCase() }),
					JOptionPane.INFORMATION_MESSAGE);
		}
		if (maxLength < Setup.getTrainLength()) {
			StringBuffer sb = new StringBuffer();
			RouteManager rm = RouteManager.instance();
			List<String> routes = rm.getRoutesByNameList();
			int count = 0;
			for (int i = 0; i < routes.size(); i++) {
				Route r = rm.getRouteById(routes.get(i));
				List<String> locations = r.getLocationsBySequenceList();
				for (int j = 0; j < locations.size(); j++) {
					RouteLocation rl = r.getLocationById(locations.get(j));
					if (rl.getMaxTrainLength() > maxLength) {
						String s = MessageFormat.format(Bundle.getMessage("RouteMaxLengthExceeds"),
								new Object[] { r.getName(), rl.getName(), rl.getMaxTrainLength(),
										maxLength });
						log.info(s);
						sb.append(s + NEW_LINE);
						count++;
						break;
					}
				}
				// maximum of 20 route warnings
				if (count > 20) {
					sb.append(Bundle.getMessage("More") + NEW_LINE);
					break;
				}
			}
			if (sb.length() > 0) {
				JOptionPane.showMessageDialog(null, sb.toString(),
						Bundle.getMessage("YouNeedToAdjustRoutes"), JOptionPane.WARNING_MESSAGE);
				if (JOptionPane.showConfirmDialog(null, MessageFormat.format(Bundle.getMessage("ChangeMaximumTrainDepartureLength"),
						new Object[] { maxLength }), Bundle.getMessage("ModifyAllRoutes"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					for (int i = 0; i < routes.size(); i++) {
						Route r = rm.getRouteById(routes.get(i));
						List<String> locations = r.getLocationsBySequenceList();
						for (int j = 0; j < locations.size(); j++) {
							RouteLocation rl = r.getLocationById(locations.get(j));
							if (rl.getMaxTrainLength() > maxLength) {
								log.debug("Setting route (" + r.getName() + ") routeLocation ("
										+ rl.getName() + ") max traim length to " + maxLength); // NOI18N
								rl.setMaxTrainLength(maxLength);
							}
						}
					}
					// save the route changes
					RouteManagerXml.instance().writeOperationsFile();
				}
			}
		}
	}

	public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == northCheckBox) {
			if (!northCheckBox.isSelected()) {
				eastCheckBox.setSelected(true);
			}
		}
		if (ae.getSource() == eastCheckBox) {
			if (!eastCheckBox.isSelected()) {
				northCheckBox.setSelected(true);
			}
		}
		int direction = 0;
		if (eastCheckBox.isSelected()) {
			direction += Setup.EAST;
		}
		if (northCheckBox.isSelected()) {
			direction += Setup.NORTH;
		}
		setDirectionCheckBox(direction);
		packFrame();
	}

	private void setScale() {
		int scale = Setup.getScale();
		switch (scale) {
		case Setup.Z_SCALE:
			scaleZ.setSelected(true);
			break;
		case Setup.N_SCALE:
			scaleN.setSelected(true);
			break;
		case Setup.TT_SCALE:
			scaleTT.setSelected(true);
			break;
		case Setup.HOn3_SCALE:
			scaleHOn3.setSelected(true);
			break;
		case Setup.OO_SCALE:
			scaleOO.setSelected(true);
			break;
		case Setup.HO_SCALE:
			scaleHO.setSelected(true);
			break;
		case Setup.Sn3_SCALE:
			scaleSn3.setSelected(true);
			break;
		case Setup.S_SCALE:
			scaleS.setSelected(true);
			break;
		case Setup.On3_SCALE:
			scaleOn3.setSelected(true);
			break;
		case Setup.O_SCALE:
			scaleO.setSelected(true);
			break;
		case Setup.G_SCALE:
			scaleG.setSelected(true);
			break;
		default:
			log.error("Unknown scale");
		}
	}

	private void setCarTypes() {
		typeDesc.setSelected(Setup.getCarTypes().equals(Setup.DESCRIPTIVE));
		typeAAR.setSelected(Setup.getCarTypes().equals(Setup.AAR));
	}

	private void setDirectionCheckBox(int direction) {
		eastCheckBox.setSelected((direction & Setup.EAST) > 0);
		textIconEast.setVisible((direction & Setup.EAST) > 0);
		eastComboBox.setVisible((direction & Setup.EAST) > 0);
		textIconWest.setVisible((direction & Setup.EAST) > 0);
		westComboBox.setVisible((direction & Setup.EAST) > 0);
		northCheckBox.setSelected((direction & Setup.NORTH) > 0);
		textIconNorth.setVisible((direction & Setup.NORTH) > 0);
		northComboBox.setVisible((direction & Setup.NORTH) > 0);
		textIconSouth.setVisible((direction & Setup.NORTH) > 0);
		southComboBox.setVisible((direction & Setup.NORTH) > 0);
	}

	private void setLengthUnit() {
		feetUnit.setSelected(Setup.getLengthUnit().equals(Setup.FEET));
		meterUnit.setSelected(Setup.getLengthUnit().equals(Setup.METER));
	}

	private void loadIconComboBox(JComboBox comboBox) {
		String[] colors = LocoIcon.getLocoColors();
		for (int i = 0; i < colors.length; i++) {
			comboBox.addItem(colors[i]);
		}
	}

	private void packFrame() {
		pack();
		/*
		 * all JMRI window position and size are now saved if (Setup.getOperationsSetupFrameSize()!= null)
		 * setSize(Setup.getOperationsSetupFrameSize()); /* else { setSize(getWidth(), getHeight()+20); // made the
		 * panel a bit larger to eliminate scroll bars }
		 */
	}

	public void propertyChange(java.beans.PropertyChangeEvent e) {
		log.debug("OperationsSetupFrame sees propertyChange " + e.getPropertyName() + " "
				+ e.getNewValue());

	}

	static Logger log = LoggerFactory
			.getLogger(OperationsSetupFrame.class.getName());
}
