// ChangeDeparturesTimeFrame.java

package jmri.jmrit.operations.trains;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 * Frame for copying a train for operations.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2013
 * @version $Revision: 17977 $
 */
public class ChangeDepartureTimesFrame extends OperationsFrame {

	
	// labels

	// text field

	// major buttons
	javax.swing.JButton changeButton = new javax.swing.JButton(Bundle.getMessage("Change"));

	// combo boxes
	javax.swing.JComboBox hourBox = new javax.swing.JComboBox();

	public ChangeDepartureTimesFrame() {
		// general GUI config

		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		// Set up the panels

		// Layout the panel by rows
		
		for (int i=0; i<24; i++) {
			hourBox.addItem(Integer.toString(i));
		}

		// row 2
		JPanel pHour = new JPanel();
		pHour.setLayout(new GridBagLayout());
		pHour.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("SelectHours")));
		addItem(pHour, hourBox, 0, 0);

		// row 4
		JPanel pButton = new JPanel();
		pButton.add(changeButton);

		getContentPane().add(pHour);
		getContentPane().add(pButton);

		// add help menu to window
		addHelpMenu("package.jmri.jmrit.operations.Operations_Trains", true); // NOI18N

		pack();
		setMinimumSize(new Dimension(Control.mediumPanelWidth, Control.smallPanelHeight));

		setTitle(Bundle.getMessage("TitleChangeDepartureTime"));

		// setup buttons
		addButtonAction(changeButton);
	}

	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == changeButton) {
			log.debug("save button activated");
			TrainManager trainManager = TrainManager.instance();
			List<Train> trains = trainManager.getTrainsByIdList();
			for (int i = 0; i < trains.size(); i++) {
				Train train = trains.get(i);
				int hour = Integer.parseInt((String) hourBox.getSelectedItem())
						+ Integer.parseInt(train.getDepartureTimeHour());
				if (hour > 23)
					hour = hour - 24;
				RouteLocation rl = train.getTrainDepartsRouteLocation();
				if (rl != null && !rl.getDepartureTime().equals("")) {
					rl.setDepartureTime(Integer.toString(hour), train.getDepartureTimeMinute());
				} else {
					train.setDepartureTime(Integer.toString(hour), train.getDepartureTimeMinute());
				}
			}
		}
	}

	static Logger log = LoggerFactory.getLogger(ChangeDepartureTimesFrame.class.getName());
}
