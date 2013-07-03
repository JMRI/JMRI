// ManifestTextFrame.java

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
import jmri.jmrit.operations.trains.TrainSwitchListText;

/**
 * Frame for user edit of switch list text strings
 * 
 * @author Dan Boudreau Copyright (C) 2013
 * @version $Revision: 21846 $
 */

public class EditSwitchListTextFrame extends OperationsFrame {

	protected static final ResourceBundle rb = ResourceBundle
			.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");

	// major buttons
	JButton saveButton = new JButton(Bundle.getMessage("Save"));
	JButton resetButton = new JButton(rb.getString("Reset"));

	// text fields
	JTextField switchListForTextField = new JTextField(60);
	JTextField scheduledWorkTextField = new JTextField(60);
	
	JTextField departsAtTextField = new JTextField(60);
	JTextField departsAtExpectedArrivalTextField = new JTextField(60);
	JTextField departedExpectedTextField = new JTextField(60);
	
	JTextField visitNumberTextField = new JTextField(60);
	JTextField visitNumberDepartedTextField = new JTextField(60);
	JTextField visitNumberTerminatesTextField = new JTextField(60);
	JTextField visitNumberTerminatesDepartedTextField = new JTextField(60);
	JTextField visitNumberDoneTextField = new JTextField(60);
	
	JTextField trainDirectionChangeTextField = new JTextField(60);	
	JTextField noCarPickUpsTextField = new JTextField(60);
	JTextField noCarDropsTextField = new JTextField(60);
	JTextField trainDoneTextField = new JTextField(60);

	public EditSwitchListTextFrame() {
		super(Bundle.getMessage("TitleSwitchListText"));
	}

	public void initComponents() {

		// the following code sets the frame's initial state
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		// manifest text fields

		JPanel pSwitchList = new JPanel();
		JScrollPane pSwitchListPane = new JScrollPane(pSwitchList);
		pSwitchListPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutSwitchList")));
		pSwitchList.setLayout(new BoxLayout(pSwitchList, BoxLayout.Y_AXIS));

		JPanel pSwitchListForTextField = new JPanel();
		pSwitchListForTextField.setBorder(BorderFactory.createTitledBorder(rb
				.getString("SwitchListFor")));
		pSwitchListForTextField.add(switchListForTextField);
		switchListForTextField.setText(TrainSwitchListText.getStringSwitchListFor());
		pSwitchList.add(pSwitchListForTextField);

		JPanel pScheduledWorkTextField = new JPanel();
		pScheduledWorkTextField.setBorder(BorderFactory.createTitledBorder(rb.getString("ScheduledWork")));
		pScheduledWorkTextField.add(scheduledWorkTextField);
		scheduledWorkTextField.setText(TrainSwitchListText.getStringScheduledWork());
		pSwitchList.add(pScheduledWorkTextField);
		
		JPanel pDepartsAtTextField = new JPanel();
		pDepartsAtTextField.setBorder(BorderFactory.createTitledBorder(rb
				.getString("DepartsAt")));
		pDepartsAtTextField.add(departsAtTextField);
		departsAtTextField.setText(TrainSwitchListText.getStringDepartsAt());
		pSwitchList.add(pDepartsAtTextField);

		JPanel pDepartsAtExpectedArrivalTextField = new JPanel();
		pDepartsAtExpectedArrivalTextField
				.setBorder(BorderFactory.createTitledBorder(rb.getString("DepartsAtExpectedArrival")));
		pDepartsAtExpectedArrivalTextField.add(departsAtExpectedArrivalTextField);
		departsAtExpectedArrivalTextField.setText(TrainSwitchListText.getStringDepartsAtExpectedArrival());
		pSwitchList.add(pDepartsAtExpectedArrivalTextField);

		JPanel pDepartedExpectedTextField = new JPanel();
		pDepartedExpectedTextField.setBorder(BorderFactory.createTitledBorder(rb
				.getString("DepartedExpected")));
		pDepartedExpectedTextField.add(departedExpectedTextField);
		departedExpectedTextField.setText(TrainSwitchListText.getStringDepartedExpected());
		pSwitchList.add(pDepartedExpectedTextField);

		JPanel pVisitNumber = new JPanel();
		pVisitNumber.setBorder(BorderFactory.createTitledBorder(rb.getString("VisitNumber")));
		pVisitNumber.add(visitNumberTextField);
		visitNumberTextField.setText(TrainSwitchListText.getStringVisitNumber());
		pSwitchList.add(pVisitNumber);
		
		JPanel pVisitNumberDeparted = new JPanel();
		pVisitNumberDeparted.setBorder(BorderFactory.createTitledBorder(rb.getString("VisitNumberDeparted")));
		pVisitNumberDeparted.add(visitNumberDepartedTextField);
		visitNumberDepartedTextField.setText(TrainSwitchListText.getStringVisitNumberDeparted());
		pSwitchList.add(pVisitNumberDeparted);

		JPanel pVisitNumberTerminates = new JPanel();
		pVisitNumberTerminates.setBorder(BorderFactory.createTitledBorder(rb.getString("VisitNumberTerminates")));
		pVisitNumberTerminates.add(visitNumberTerminatesTextField);
		visitNumberTerminatesTextField.setText(TrainSwitchListText.getStringVisitNumberTerminates());
		pSwitchList.add(pVisitNumberTerminates);

		JPanel pVisitNumberTerminatesDepartedTextField = new JPanel();
		pVisitNumberTerminatesDepartedTextField.setBorder(BorderFactory.createTitledBorder(rb
				.getString("VisitNumberTerminatesDeparted")));
		pVisitNumberTerminatesDepartedTextField.add(visitNumberTerminatesDepartedTextField);
		visitNumberTerminatesDepartedTextField.setText(TrainSwitchListText.getStringVisitNumberTerminatesDeparted());
		pSwitchList.add(pVisitNumberTerminatesDepartedTextField);

		JPanel pVisitNumberDone = new JPanel();
		pVisitNumberDone.setBorder(BorderFactory.createTitledBorder(rb.getString("VisitNumberDone")));
		pVisitNumberDone.add(visitNumberDoneTextField);
		visitNumberDoneTextField.setText(TrainSwitchListText.getStringVisitNumberDone());
		pSwitchList.add(pVisitNumberDone);
		
		JPanel pTrainDirectionChange = new JPanel();
		pTrainDirectionChange.setBorder(BorderFactory.createTitledBorder(rb.getString("TrainDirectionChange")));
		pTrainDirectionChange.add(trainDirectionChangeTextField);
		trainDirectionChangeTextField.setText(TrainSwitchListText.getStringTrainDirectionChange());
		pSwitchList.add(pTrainDirectionChange);
		
		JPanel pNoCarPickUps = new JPanel();
		pNoCarPickUps.setBorder(BorderFactory.createTitledBorder(rb.getString("NoCarPickUps")));
		pNoCarPickUps.add(noCarPickUpsTextField);
		noCarPickUpsTextField.setText(TrainSwitchListText.getStringNoCarPickUps());
		pSwitchList.add(pNoCarPickUps);

		JPanel pNoCarDrops = new JPanel();
		pNoCarDrops.setBorder(BorderFactory.createTitledBorder(rb.getString("NoCarDrops")));
		pNoCarDrops.add(noCarDropsTextField);
		noCarDropsTextField.setText(TrainSwitchListText.getStringNoCarDrops());
		pSwitchList.add(pNoCarDrops);

		JPanel pTrainDone = new JPanel();
		pTrainDone.setBorder(BorderFactory.createTitledBorder(rb.getString("TrainDone")));
		pTrainDone.add(trainDoneTextField);
		trainDoneTextField.setText(TrainSwitchListText.getStringTrainDone());
		pSwitchList.add(pTrainDone);

		// add tool tips
		saveButton.setToolTipText(Bundle.getMessage("SaveToolTip"));

		// row 11
		JPanel pControl = new JPanel();
		pControl.setBorder(BorderFactory.createTitledBorder(""));
		pControl.setLayout(new GridBagLayout());
		addItem(pControl, resetButton, 0, 0);
		addItem(pControl, saveButton, 1, 0);

		getContentPane().add(pSwitchListPane);
		getContentPane().add(pControl);

		// setup buttons
		addButtonAction(resetButton);
		addButtonAction(saveButton);

		// build menu
		addHelpMenu("package.jmri.jmrit.operations.Operations_PrintOptions", true); // NOI18N

		pack();
		setVisible(true);
	}

	// Save buttons
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == resetButton) {
			switchListForTextField.setText(rb.getString("SwitchListFor"));
			scheduledWorkTextField.setText(rb.getString("ScheduledWork"));
			
			departsAtTextField.setText(rb.getString("DepartsAt"));
			departsAtExpectedArrivalTextField.setText(rb.getString("DepartsAtExpectedArrival"));
			departedExpectedTextField.setText(rb.getString("DepartedExpected"));
			
			visitNumberTextField.setText(rb.getString("VisitNumber"));
			visitNumberDepartedTextField.setText(rb.getString("VisitNumberDeparted"));
			visitNumberTerminatesTextField.setText(rb.getString("VisitNumberTerminates"));
			visitNumberTerminatesDepartedTextField.setText(rb.getString("VisitNumberTerminatesDeparted"));
			visitNumberDoneTextField.setText(rb.getString("VisitNumberDone"));
			
			trainDirectionChangeTextField.setText(rb.getString("TrainDirectionChange"));			
			noCarPickUpsTextField.setText(rb.getString("NoCarPickUps"));
			noCarDropsTextField.setText(rb.getString("NoCarDrops"));
			trainDoneTextField.setText(rb.getString("TrainDone"));
		}
		if (ae.getSource() == saveButton) {
			TrainSwitchListText.setStringSwitchListFor(switchListForTextField.getText());
			TrainSwitchListText.setStringScheduledWork(scheduledWorkTextField.getText());

			TrainSwitchListText.setStringDepartsAt(departsAtTextField.getText());
			TrainSwitchListText.setStringDepartsAtExpectedArrival(departsAtExpectedArrivalTextField.getText());
			TrainSwitchListText.setStringDepartedExpected(departedExpectedTextField.getText());
	
			TrainSwitchListText.setStringVisitNumber(visitNumberTextField.getText());
			TrainSwitchListText.setStringVisitNumberDeparted(visitNumberDepartedTextField.getText());
			TrainSwitchListText.setStringVisitNumberTerminates(visitNumberTerminatesTextField.getText());
			TrainSwitchListText.setStringVisitNumberTerminatesDeparted(visitNumberTerminatesDepartedTextField.getText());
			TrainSwitchListText.setStringVisitNumberDone(visitNumberDoneTextField.getText());
			
			TrainSwitchListText.setStringTrainDirectionChange(trainDirectionChangeTextField.getText());			
			TrainSwitchListText.setStringNoCarPickUps(noCarPickUpsTextField.getText());
			TrainSwitchListText.setStringNoCarDrops(noCarDropsTextField.getText());
			TrainSwitchListText.setStringTrainDone(trainDoneTextField.getText());

			OperationsSetupXml.instance().writeOperationsFile();

			if (Setup.isCloseWindowOnSaveEnabled())
				dispose();
		}
	}

	static Logger log = LoggerFactory.getLogger(OperationsSetupFrame.class.getName());
}
