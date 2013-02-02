// TrainsScheduleTableFrame.java

package jmri.jmrit.operations.trains;

import org.apache.log4j.Logger;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Enumeration;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.TableColumnModel;

import jmri.implementation.swing.SwingShutDownTask;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;

/**
 * Frame for adding and editing train schedules for operations.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2010, 2012
 * @version $Revision$
 */
public class TrainsScheduleTableFrame extends OperationsFrame implements PropertyChangeListener {

	public static SwingShutDownTask trainDirtyTask;

	public static final String NAME = Bundle.getMessage("Name"); // Sort by choices
	public static final String TIME = Bundle.getMessage("Time");

	TrainManager trainManager = TrainManager.instance();
	TrainScheduleManager scheduleManager = TrainScheduleManager.instance();
	TrainManagerXml trainManagerXml = TrainManagerXml.instance();

	TrainsScheduleTableModel trainsScheduleModel = new TrainsScheduleTableModel();
	javax.swing.JTable trainsScheduleTable = new javax.swing.JTable(trainsScheduleModel);
	JScrollPane trainsPane;

	// labels
	JLabel textSort = new JLabel(Bundle.getMessage("SortBy"));

	// radio buttons
	JRadioButton sortByName = new JRadioButton(NAME);
	JRadioButton sortByTime = new JRadioButton(TIME);
	JRadioButton noneButton = new JRadioButton(Bundle.getMessage("None"));

	// radio button groups
	ButtonGroup schGroup = new ButtonGroup();

	// major buttons
	JButton selectButton = new JButton(Bundle.getMessage("Select"));
	JButton clearButton = new JButton(Bundle.getMessage("Clear"));
	JButton applyButton = new JButton(Bundle.getMessage("Apply"));
	JButton saveButton = new JButton(Bundle.getMessage("Save"));

	// check boxes

	// panel
	JPanel schedule = new JPanel();

	// active schedule id
	private String _activeId = "";

	public TrainsScheduleTableFrame() {

		// set active id
		_activeId = trainManager.getTrainScheduleActiveId();

		// general GUI configuration
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		// Set up the jtable in a Scroll Pane..
		trainsPane = new JScrollPane(trainsScheduleTable);
		trainsPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		trainsPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		trainsScheduleModel.initTable(trainsScheduleTable, this);

		// Set up the control panel
		// row 1
		JPanel cp1 = new JPanel();
		cp1.setLayout(new BoxLayout(cp1, BoxLayout.X_AXIS));

		// row 1
		JPanel sortBy = new JPanel();
		sortBy.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("SortBy")));
		sortBy.add(sortByTime);
		sortBy.add(sortByName);

		// row 2
		schedule.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Active")));
		updateControlPanel();

		cp1.add(sortBy);
		cp1.add(schedule);

		JPanel cp3 = new JPanel();
		cp3.setBorder(BorderFactory.createTitledBorder(""));
		cp3.add(clearButton);
		cp3.add(selectButton);
		cp3.add(applyButton);
		cp3.add(saveButton);

		// tool tips
		selectButton.setToolTipText(Bundle.getMessage("SelectAllButtonTip"));
		clearButton.setToolTipText(Bundle.getMessage("ClearAllButtonTip"));
		applyButton.setToolTipText(Bundle.getMessage("ApplyButtonTip"));
		saveButton.setToolTipText(Bundle.getMessage("SaveButtonTip"));

		// place controls in scroll pane
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
		controlPanel.add(cp1);
		controlPanel.add(cp3);

		JScrollPane controlPane = new JScrollPane(controlPanel);
		// make sure control panel is the right size
		controlPane.setMinimumSize(new Dimension(500, 130));
		controlPane.setMaximumSize(new Dimension(2000, 200));
		controlPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

		getContentPane().add(trainsPane);
		getContentPane().add(controlPane);

		// setup buttons
		addButtonAction(clearButton);
		addButtonAction(selectButton);
		addButtonAction(applyButton);
		addButtonAction(saveButton);

		ButtonGroup sortGroup = new ButtonGroup();
		sortGroup.add(sortByTime);
		sortGroup.add(sortByName);
		sortByTime.setSelected(true);

		addRadioButtonAction(sortByTime);
		addRadioButtonAction(sortByName);

		addRadioButtonAction(noneButton);

		// build menu
		JMenuBar menuBar = new JMenuBar();
		JMenu toolMenu = new JMenu(Bundle.getMessage("Tools"));
		toolMenu.add(new TrainsScheduleEditAction());
		menuBar.add(toolMenu);
		setJMenuBar(menuBar);

		// add help menu to window
		addHelpMenu("package.jmri.jmrit.operations.Operations_Timetable", true); // NOI18N
		
		setTitle(Bundle.getMessage("TitleTimeTableTrains"));

		pack();

		scheduleManager.addPropertyChangeListener(this);
		addPropertyChangeTrainSchedules();
	}

	public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("radio button activated");
		if (ae.getSource() == sortByName) {
			trainsScheduleModel.setSort(trainsScheduleModel.SORTBYNAME);
		} else if (ae.getSource() == sortByTime) {
			trainsScheduleModel.setSort(trainsScheduleModel.SORTBYTIME);
		} else if (ae.getSource() == noneButton) {
			enableButtons(false);
			// must be one of the schedule radio buttons
		} else {
			enableButtons(true);
		}

	}

	TrainSwitchListEditFrame tslef;

	// add, build, print, switch lists, terminate, and save buttons
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("schedule train button activated");
		if (ae.getSource() == clearButton) {
			updateCheckboxes(false);
		}
		if (ae.getSource() == selectButton) {
			updateCheckboxes(true);
		}
		if (ae.getSource() == applyButton) {
			applySchedule();
		}
		if (ae.getSource() == saveButton) {
			storeValues();
			if (Setup.isCloseWindowOnSaveEnabled())
				dispose();
		}
	}

	private void updateControlPanel() {
		schedule.removeAll();
		noneButton.setName("");
		noneButton.setSelected(true);
		enableButtons(false);
		schedule.add(noneButton);
		schGroup.add(noneButton);
		List<String> l = scheduleManager.getSchedulesByIdList();
		for (int i = 0; i < l.size(); i++) {
			TrainSchedule ts = scheduleManager.getScheduleById(l.get(i));
			JRadioButton b = new JRadioButton();
			b.setText(ts.getName());
			b.setName(l.get(i));
			schedule.add(b);
			schGroup.add(b);
			addRadioButtonAction(b);
			if (b.getName().equals(_activeId)) {
				b.setSelected(true);
				enableButtons(true);
			}
		}
		schedule.revalidate();
	}

	private void updateCheckboxes(boolean selected) {
		TrainSchedule ts = TrainScheduleManager.instance().getScheduleById(getSelectedScheduleId());
		if (ts != null) {
			List<String> trains = trainManager.getTrainsByIdList();
			for (int j = 0; j < trains.size(); j++) {
				log.debug("train id: " + trains.get(j));
				if (selected)
					ts.addTrainId(trains.get(j));
				else
					ts.removeTrainId(trains.get(j));
			}
		}
	}

	private void applySchedule() {
		setActiveId();
		TrainSchedule ts = TrainScheduleManager.instance().getScheduleById(_activeId);
		if (ts != null) {
			List<String> trains = trainManager.getTrainsByIdList();
			for (int j = 0; j < trains.size(); j++) {
				log.debug("train id: " + trains.get(j));
				Train train = trainManager.getTrainById(trains.get(j));
				train.setBuildEnabled(ts.containsTrainId(trains.get(j)));
			}
		}
	}

	private void setActiveId() {
		_activeId = getSelectedScheduleId();
	}

	private String getSelectedScheduleId() {
		AbstractButton b;
		Enumeration<AbstractButton> en = schGroup.getElements();
		for (int i = 0; i < schGroup.getButtonCount(); i++) {
			b = en.nextElement();
			if (b.isSelected()) {
				log.debug("schedule radio button " + b.getText());
				return b.getName();
			}
		}
		return null;
	}

	protected void storeValues() {
		setActiveId();
		saveTableDetails(trainsScheduleTable);
		trainManager.setTrainSecheduleActiveId(_activeId);
		OperationsXml.save();
	}

	protected int[] getCurrentTableColumnWidths() {
		TableColumnModel tcm = trainsScheduleTable.getColumnModel();
		int[] widths = new int[tcm.getColumnCount()];
		for (int i = 0; i < tcm.getColumnCount(); i++)
			widths[i] = tcm.getColumn(i).getWidth();
		return widths;
	}

	public void dispose() {
		scheduleManager.removePropertyChangeListener(this);
		removePropertyChangeTrainSchedules();
		trainsScheduleModel.dispose();
		super.dispose();
	}

	private void addPropertyChangeTrainSchedules() {
		List<String> l = scheduleManager.getSchedulesByIdList();
		for (int i = 0; i < l.size(); i++) {
			TrainSchedule ts = scheduleManager.getScheduleById(l.get(i));
			if (ts != null)
				ts.addPropertyChangeListener(this);
		}
	}

	private void removePropertyChangeTrainSchedules() {
		List<String> l = scheduleManager.getSchedulesByIdList();
		for (int i = 0; i < l.size(); i++) {
			TrainSchedule ts = scheduleManager.getScheduleById(l.get(i));
			if (ts != null)
				ts.removePropertyChangeListener(this);
		}
	}

	private void enableButtons(boolean enable) {
		selectButton.setEnabled(enable);
		clearButton.setEnabled(enable);
		applyButton.setEnabled(enable);
	}

	public void propertyChange(PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled())
			log.debug("Property change " + e.getPropertyName() + " old: " + e.getOldValue()
					+ " new: " + e.getNewValue()); // NOI18N
		if (e.getPropertyName().equals(TrainScheduleManager.LISTLENGTH_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(TrainSchedule.NAME_CHANGED_PROPERTY)) {
			updateControlPanel();
		}
	}

	static Logger log = org.apache.log4j.Logger
			.getLogger(TrainsScheduleTableFrame.class.getName());
}
