// EditManifestTextFrame.java

package jmri.jmrit.operations.setup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.GridBagLayout;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.TrainManifestText;

/**
 * Frame for user edit of manifest text strings
 * 
 * @author Dan Boudreau Copyright (C) 2013
 * @version $Revision: 21846 $
 */

public class EditManifestTextFrame extends OperationsFrame {

	protected static final ResourceBundle rb = ResourceBundle
			.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");

	// major buttons
	JButton saveButton = new JButton(Bundle.getMessage("Save"));
	JButton resetButton = new JButton(rb.getString("Reset"));

	// text field

	JTextField manifestForTrainTextField = new JTextField(60);
	JTextField validTextField = new JTextField(60);
	JTextField scheduledWorkAtTextField = new JTextField(60);
	JTextField scheduledWorkDepartureTextField = new JTextField(60);
	JTextField scheduledWorkArrivalTextField = new JTextField(60);
	JTextField noScheduledWorkAtTextField = new JTextField(60);
	JTextField departTimeTextField = new JTextField(60);
	JTextField trainDepartsCarsTextField = new JTextField(60);
	JTextField trainDepartsLoadsTextField = new JTextField(60);
	JTextField trainTerminatesInTextField = new JTextField(60);
	
	JTextField destinationTextField = new JTextField(60);
	JTextField toTextField = new JTextField(25);
	JTextField fromTextField = new JTextField(25);
	
	JTextField addHelpersAtTextField = new JTextField(60);
	JTextField removeHelpersAtTextField = new JTextField(60);
	JTextField locoChangeAtTextField = new JTextField(60);
	JTextField cabooseChangeAtTextField = new JTextField(60);
	JTextField locoAndCabooseChangeAtTextField = new JTextField(60);

	public EditManifestTextFrame() {
		super(Bundle.getMessage("TitleManifestText"));
	}

	public void initComponents() {

		// the following code sets the frame's initial state
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		// manifest text fields

		JPanel pManifest = new JPanel();
		JScrollPane pManifestPane = new JScrollPane(pManifest);
		pManifestPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutManifest")));
		pManifest.setLayout(new BoxLayout(pManifest, BoxLayout.Y_AXIS));

		JPanel pManifestForTrainTextField = new JPanel();
		pManifestForTrainTextField.setBorder(BorderFactory.createTitledBorder(rb
				.getString("ManifestForTrain")));
		pManifestForTrainTextField.add(manifestForTrainTextField);
		manifestForTrainTextField.setText(TrainManifestText.getStringManifestForTrain());
		pManifest.add(pManifestForTrainTextField);

		JPanel pValidTextField = new JPanel();
		pValidTextField.setBorder(BorderFactory.createTitledBorder(rb.getString("Valid")));
		pValidTextField.add(validTextField);
		validTextField.setText(TrainManifestText.getStringValid());
		pManifest.add(pValidTextField);

		JPanel pScheduledWorkAtTextField = new JPanel();
		pScheduledWorkAtTextField
				.setBorder(BorderFactory.createTitledBorder(rb.getString("ScheduledWorkAt")));
		pScheduledWorkAtTextField.add(scheduledWorkAtTextField);
		scheduledWorkAtTextField.setText(TrainManifestText.getStringScheduledWork());
		pManifest.add(pScheduledWorkAtTextField);

		JPanel pScheduledWorkDepartureTextField = new JPanel();
		pScheduledWorkDepartureTextField.setBorder(BorderFactory.createTitledBorder(rb
				.getString("WorkDepartureTime")));
		pScheduledWorkDepartureTextField.add(scheduledWorkDepartureTextField);
		scheduledWorkDepartureTextField.setText(TrainManifestText.getStringWorkDepartureTime());
		pManifest.add(pScheduledWorkDepartureTextField);

		JPanel pScheduledWorkArrivalTextField = new JPanel();
		pScheduledWorkArrivalTextField.setBorder(BorderFactory.createTitledBorder(rb
				.getString("WorkArrivalTime")));
		pScheduledWorkArrivalTextField.add(scheduledWorkArrivalTextField);
		scheduledWorkArrivalTextField.setText(TrainManifestText.getStringWorkArrivalTime());
		pManifest.add(pScheduledWorkArrivalTextField);

		JPanel pNoScheduledWorkAt = new JPanel();
		pNoScheduledWorkAt.setBorder(BorderFactory.createTitledBorder(rb.getString("NoScheduledWorkAt")));
		pNoScheduledWorkAt.add(noScheduledWorkAtTextField);
		noScheduledWorkAtTextField.setText(TrainManifestText.getStringNoScheduledWork());
		pManifest.add(pNoScheduledWorkAt);
		
		JPanel pDepartTime = new JPanel();
		pDepartTime.setBorder(BorderFactory.createTitledBorder(rb.getString("departureTime")));
		pDepartTime.add(departTimeTextField);
		departTimeTextField.setText(TrainManifestText.getStringDepartTime());
		pManifest.add(pDepartTime);

		JPanel pTrainDepartsCars = new JPanel();
		pTrainDepartsCars.setBorder(BorderFactory.createTitledBorder(rb.getString("TrainDepartsCars")));
		pTrainDepartsCars.add(trainDepartsCarsTextField);
		trainDepartsCarsTextField.setText(TrainManifestText.getStringTrainDepartsCars());
		pManifest.add(pTrainDepartsCars);

		JPanel pTrainDepartsLoadsTextField = new JPanel();
		pTrainDepartsLoadsTextField.setBorder(BorderFactory.createTitledBorder(rb
				.getString("TrainDepartsLoads")));
		pTrainDepartsLoadsTextField.add(trainDepartsLoadsTextField);
		trainDepartsLoadsTextField.setText(TrainManifestText.getStringTrainDepartsLoads());
		pManifest.add(pTrainDepartsLoadsTextField);

		JPanel pTrainTerminatesIn = new JPanel();
		pTrainTerminatesIn.setBorder(BorderFactory.createTitledBorder(rb.getString("TrainTerminatesIn")));
		pTrainTerminatesIn.add(trainTerminatesInTextField);
		trainTerminatesInTextField.setText(TrainManifestText.getStringTrainTerminates());
		pManifest.add(pTrainTerminatesIn);
		
		JPanel pDestination = new JPanel();
		pDestination.setBorder(BorderFactory.createTitledBorder(rb.getString("destination")));
		pDestination.add(destinationTextField);
		destinationTextField.setText(TrainManifestText.getStringDestination());
		pManifest.add(pDestination);
		
		JPanel pToFrom = new JPanel();
		pToFrom.setLayout(new BoxLayout(pToFrom, BoxLayout.X_AXIS));
		
		JPanel pTo = new JPanel();
		pTo.setBorder(BorderFactory.createTitledBorder(rb.getString("to")));
		pTo.add(toTextField);
		toTextField.setText(TrainManifestText.getStringTo());
		pToFrom.add(pTo);
		
		JPanel pFrom = new JPanel();
		pFrom.setBorder(BorderFactory.createTitledBorder(rb.getString("from")));
		pFrom.add(fromTextField);
		fromTextField.setText(TrainManifestText.getStringFrom());
		pToFrom.add(pFrom);
		
		pManifest.add(pToFrom);
		
		JPanel pAddHelpersAt = new JPanel();
		pAddHelpersAt.setBorder(BorderFactory.createTitledBorder(rb.getString("AddHelpersAt")));
		pAddHelpersAt.add(addHelpersAtTextField);
		addHelpersAtTextField.setText(TrainManifestText.getStringAddHelpers());
		pManifest.add(pAddHelpersAt);

		JPanel pRemoveHelpersAt = new JPanel();
		pRemoveHelpersAt.setBorder(BorderFactory.createTitledBorder(rb.getString("RemoveHelpersAt")));
		pRemoveHelpersAt.add(removeHelpersAtTextField);
		removeHelpersAtTextField.setText(TrainManifestText.getStringRemoveHelpers());
		pManifest.add(pRemoveHelpersAt);

		JPanel pLocoChangeAt = new JPanel();
		pLocoChangeAt.setBorder(BorderFactory.createTitledBorder(rb.getString("LocoChangeAt")));
		pLocoChangeAt.add(locoChangeAtTextField);
		locoChangeAtTextField.setText(TrainManifestText.getStringLocoChange());
		pManifest.add(pLocoChangeAt);

		JPanel pCabooseChangeAt = new JPanel();
		pCabooseChangeAt.setBorder(BorderFactory.createTitledBorder(rb.getString("CabooseChangeAt")));
		pCabooseChangeAt.add(cabooseChangeAtTextField);
		cabooseChangeAtTextField.setText(TrainManifestText.getStringCabooseChange());
		pManifest.add(pCabooseChangeAt);

		JPanel pLocoAndCabooseChangeAt = new JPanel();
		pLocoAndCabooseChangeAt.setBorder(BorderFactory.createTitledBorder(rb.getString("LocoAndCabooseChangeAt")));
		pLocoAndCabooseChangeAt.add(locoAndCabooseChangeAtTextField);
		locoAndCabooseChangeAtTextField.setText(TrainManifestText.getStringLocoAndCabooseChange());
		pManifest.add(pLocoAndCabooseChangeAt);

		// add tool tips
		saveButton.setToolTipText(Bundle.getMessage("SaveToolTip"));

		// row 11
		JPanel pControl = new JPanel();
		pControl.setBorder(BorderFactory.createTitledBorder(""));
		pControl.setLayout(new GridBagLayout());
		addItem(pControl, resetButton, 0, 0);
		addItem(pControl, saveButton, 1, 0);

		getContentPane().add(pManifestPane);
		getContentPane().add(pControl);

		// setup buttons
		addButtonAction(resetButton);
		addButtonAction(saveButton);

		// build menu
		addHelpMenu("package.jmri.jmrit.operations.Operations_PrintOptions", true); // NOI18N

		initMinimumSize();
	}

	// Save buttons
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == resetButton) {
			manifestForTrainTextField.setText(rb.getString("ManifestForTrain"));
			validTextField.setText(rb.getString("Valid"));
			scheduledWorkAtTextField.setText(rb.getString("ScheduledWorkAt"));
			scheduledWorkDepartureTextField.setText(rb.getString("WorkDepartureTime"));
			scheduledWorkArrivalTextField.setText(rb.getString("WorkArrivalTime"));
			noScheduledWorkAtTextField.setText(rb.getString("NoScheduledWorkAt"));
			departTimeTextField.setText(rb.getString("departureTime"));
			trainDepartsCarsTextField.setText(rb.getString("TrainDepartsCars"));
			trainDepartsLoadsTextField.setText(rb.getString("TrainDepartsLoads"));
			trainTerminatesInTextField.setText(rb.getString("TrainTerminatesIn"));
			
			destinationTextField.setText(rb.getString("destination"));
			toTextField.setText(rb.getString("to"));
			fromTextField.setText(rb.getString("from"));
			
			addHelpersAtTextField.setText(rb.getString("AddHelpersAt"));
			removeHelpersAtTextField.setText(rb.getString("RemoveHelpersAt"));
			locoChangeAtTextField.setText(rb.getString("LocoChangeAt"));
			cabooseChangeAtTextField.setText(rb.getString("CabooseChangeAt"));
			locoAndCabooseChangeAtTextField.setText(rb.getString("LocoAndCabooseChangeAt"));
		}
		if (ae.getSource() == saveButton) {
			TrainManifestText.setStringManifestForTrain(manifestForTrainTextField.getText());
			TrainManifestText.setStringValid(validTextField.getText());
			TrainManifestText.setStringScheduledWork(scheduledWorkAtTextField.getText());
			TrainManifestText.setStringWorkDepartureTime(scheduledWorkDepartureTextField.getText());
			TrainManifestText.setStringWorkArrivalTime(scheduledWorkArrivalTextField.getText());
			TrainManifestText.setStringNoScheduledWork(noScheduledWorkAtTextField.getText());
			TrainManifestText.setStringDepartTime(departTimeTextField.getText());
			TrainManifestText.setStringTrainDepartsCars(trainDepartsCarsTextField.getText());
			TrainManifestText.setStringTrainDepartsLoads(trainDepartsLoadsTextField.getText());
			TrainManifestText.setStringTrainTerminates(trainTerminatesInTextField.getText());
			
			TrainManifestText.setStringDestination(destinationTextField.getText());
			TrainManifestText.setStringTo(toTextField.getText());
			TrainManifestText.setStringFrom(fromTextField.getText());
			
			TrainManifestText.setStringAddHelpers(addHelpersAtTextField.getText());
			TrainManifestText.setStringRemoveHelpers(removeHelpersAtTextField.getText());
			TrainManifestText.setStringLocoChange(locoChangeAtTextField.getText());
			TrainManifestText.setStringCabooseChange(cabooseChangeAtTextField.getText());
			TrainManifestText.setStringLocoAndCabooseChange(locoAndCabooseChangeAtTextField.getText());

			OperationsSetupXml.instance().writeOperationsFile();
			
			// recreate all train manifests
			TrainManager.instance().setTrainsModified();

			if (Setup.isCloseWindowOnSaveEnabled())
				dispose();
		}
	}

	static Logger log = LoggerFactory.getLogger(OperationsSetupFrame.class.getName());
}
