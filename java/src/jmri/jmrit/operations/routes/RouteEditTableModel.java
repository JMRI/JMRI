// RouteEditTableModel.java

package jmri.jmrit.operations.routes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.*;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;

/**
 * Table Model for edit of route locations used by operations
 * 
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision$
 */
public class RouteEditTableModel extends javax.swing.table.AbstractTableModel implements
		PropertyChangeListener {

	// Defines the columns
	private static final int IDCOLUMN = 0;
	private static final int NAMECOLUMN = IDCOLUMN + 1;
	private static final int TRAINCOLUMN = NAMECOLUMN + 1;
	private static final int MAXMOVESCOLUMN = TRAINCOLUMN + 1;
	private static final int PICKUPCOLUMN = MAXMOVESCOLUMN + 1;
	private static final int DROPCOLUMN = PICKUPCOLUMN + 1;
	private static final int WAITCOLUMN = DROPCOLUMN + 1;
	private static final int MAXLENGTHCOLUMN = WAITCOLUMN + 1;
	private static final int GRADE = MAXLENGTHCOLUMN + 1;
	private static final int TRAINICONX = GRADE + 1;
	private static final int TRAINICONY = TRAINICONX + 1;
	private static final int COMMENTCOLUMN = TRAINICONY + 1;
	private static final int UPCOLUMN = COMMENTCOLUMN + 1;
	private static final int DOWNCOLUMN = UPCOLUMN + 1;
	private static final int DELETECOLUMN = DOWNCOLUMN + 1;

	private static final int HIGHESTCOLUMN = DELETECOLUMN + 1;

	private boolean _showWait = true;
	private JTable _table;
	private Route _route;
	private RouteEditFrame _frame;

	public RouteEditTableModel() {
		super();
	}

	public void setWait(boolean showWait) {
		_showWait = showWait;
		fireTableStructureChanged();
		initTable(_table);
	}

	synchronized void updateList() {
		if (_route == null)
			return;
		// first, remove listeners from the individual objects
		removePropertyChangeRouteLocations();
		list = _route.getLocationsBySequenceList();
		// and add them back in
		for (int i = 0; i < list.size(); i++) {
			// log.debug("location ids: " + list.get(i));
			_route.getLocationById(list.get(i)).addPropertyChangeListener(this);
		}
	}

	List<String> list = new ArrayList<String>();

	void initTable(RouteEditFrame frame, JTable table, Route route) {
		_frame = frame;
		_table = table;
		_route = route;
		if (_route != null)
			_route.addPropertyChangeListener(this);
		initTable(table);
	}

	void initTable(JTable table) {
		// Install the button handlers
		TableColumnModel tcm = table.getColumnModel();
		ButtonRenderer buttonRenderer = new ButtonRenderer();
		TableCellEditor buttonEditor = new ButtonEditor(new javax.swing.JButton());
		tcm.getColumn(COMMENTCOLUMN).setCellRenderer(buttonRenderer);
		tcm.getColumn(COMMENTCOLUMN).setCellEditor(buttonEditor);
		tcm.getColumn(UPCOLUMN).setCellRenderer(buttonRenderer);
		tcm.getColumn(UPCOLUMN).setCellEditor(buttonEditor);
		tcm.getColumn(DOWNCOLUMN).setCellRenderer(buttonRenderer);
		tcm.getColumn(DOWNCOLUMN).setCellEditor(buttonEditor);
		tcm.getColumn(DELETECOLUMN).setCellRenderer(buttonRenderer);
		tcm.getColumn(DELETECOLUMN).setCellEditor(buttonEditor);
		table.setDefaultRenderer(JComboBox.class, new jmri.jmrit.symbolicprog.ValueRenderer());
		table.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());

		setPreferredWidths(table);

		// set row height
		table.setRowHeight(new JComboBox().getPreferredSize().height);
		updateList();
		// have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	}

	private void setPreferredWidths(JTable table) {
		// set column preferred widths
		if (_frame.loadTableDetails(table))
			return; // done
		table.getColumnModel().getColumn(IDCOLUMN).setPreferredWidth(40);
		table.getColumnModel().getColumn(NAMECOLUMN).setPreferredWidth(150);
		table.getColumnModel().getColumn(TRAINCOLUMN).setPreferredWidth(95);
		table.getColumnModel().getColumn(MAXMOVESCOLUMN).setPreferredWidth(50);
		table.getColumnModel().getColumn(PICKUPCOLUMN).setPreferredWidth(65);
		table.getColumnModel().getColumn(DROPCOLUMN).setPreferredWidth(65);
		table.getColumnModel().getColumn(WAITCOLUMN).setPreferredWidth(60);
		table.getColumnModel().getColumn(MAXLENGTHCOLUMN).setPreferredWidth(75);
		table.getColumnModel().getColumn(GRADE).setPreferredWidth(50);
		table.getColumnModel().getColumn(TRAINICONX).setPreferredWidth(35);
		table.getColumnModel().getColumn(TRAINICONY).setPreferredWidth(35);
		table.getColumnModel().getColumn(COMMENTCOLUMN).setPreferredWidth(70);
		table.getColumnModel().getColumn(UPCOLUMN).setPreferredWidth(60);
		table.getColumnModel().getColumn(DOWNCOLUMN).setPreferredWidth(70);
		table.getColumnModel().getColumn(DELETECOLUMN).setPreferredWidth(70);
	}

	public int getRowCount() {
		return list.size();
	}

	public int getColumnCount() {
		return HIGHESTCOLUMN;
	}

	public String getColumnName(int col) {
		switch (col) {
		case IDCOLUMN:
			return Bundle.getMessage("Id");
		case NAMECOLUMN:
			return Bundle.getMessage("Location");
		case TRAINCOLUMN:
			return Bundle.getMessage("TrainDirection");
		case MAXMOVESCOLUMN:
			return Bundle.getMessage("MaxMoves");
		case PICKUPCOLUMN:
			return Bundle.getMessage("Pickups");
		case DROPCOLUMN:
			return Bundle.getMessage("Drops");
		case WAITCOLUMN: {
			if (_showWait)
				return Bundle.getMessage("Wait");
			else
				return Bundle.getMessage("Time");
		}
		case MAXLENGTHCOLUMN:
			return Bundle.getMessage("MaxLength");
		case GRADE:
			return Bundle.getMessage("Grade");
		case TRAINICONX:
			return Bundle.getMessage("X");
		case TRAINICONY:
			return Bundle.getMessage("Y");
		case COMMENTCOLUMN:
			return Bundle.getMessage("Comment");
		case UPCOLUMN:
			return Bundle.getMessage("Up");
		case DOWNCOLUMN:
			return Bundle.getMessage("Down");
		case DELETECOLUMN:
			return Bundle.getMessage("Delete");
		default:
			return "unknown"; // NOI18N
		}
	}

	public Class<?> getColumnClass(int col) {
		switch (col) {
		case IDCOLUMN:
			return String.class;
		case NAMECOLUMN:
			return String.class;
		case TRAINCOLUMN:
			return JComboBox.class;
		case MAXMOVESCOLUMN:
			return String.class;
		case PICKUPCOLUMN:
			return JComboBox.class;
		case DROPCOLUMN:
			return JComboBox.class;
		case WAITCOLUMN: {
			if (_showWait)
				return String.class;
			else
				return JComboBox.class;
		}
		case MAXLENGTHCOLUMN:
			return String.class;
		case GRADE:
			return String.class;
		case TRAINICONX:
			return String.class;
		case TRAINICONY:
			return String.class;
		case COMMENTCOLUMN:
			return JButton.class;
		case UPCOLUMN:
			return JButton.class;
		case DOWNCOLUMN:
			return JButton.class;
		case DELETECOLUMN:
			return JButton.class;
		default:
			return null;
		}
	}

	public boolean isCellEditable(int row, int col) {
		switch (col) {
		case DELETECOLUMN:
		case TRAINCOLUMN:
		case MAXMOVESCOLUMN:
		case PICKUPCOLUMN:
		case DROPCOLUMN:
		case WAITCOLUMN:
		case MAXLENGTHCOLUMN:
		case GRADE:
		case TRAINICONX:
		case TRAINICONY:
		case COMMENTCOLUMN:
		case UPCOLUMN:
		case DOWNCOLUMN:
			return true;
		default:
			return false;
		}
	}

	public Object getValueAt(int row, int col) {
		if (row >= list.size())
			return "ERROR unknown " + row; // NOI18N
		RouteLocation rl = _route.getLocationById(list.get(row));
		if (rl == null)
			return "ERROR unknown route location " + row; // NOI18N
		switch (col) {
		case IDCOLUMN:
			return rl.getId();
		case NAMECOLUMN:
			return rl.getName();
		case TRAINCOLUMN: {
			JComboBox cb = Setup.getComboBox();
			cb.setSelectedItem(rl.getTrainDirectionString());
			return cb;
		}
		case MAXMOVESCOLUMN:
			return Integer.toString(rl.getMaxCarMoves());
		case PICKUPCOLUMN: {
			JComboBox cb = getYesNoComboBox();
			cb.setSelectedItem(rl.canPickup() ? Bundle.getMessage("yes") : Bundle.getMessage("no"));
			return cb;
		}
		case DROPCOLUMN: {
			JComboBox cb = getYesNoComboBox();
			cb.setSelectedItem(rl.canDrop() ? Bundle.getMessage("yes") : Bundle.getMessage("no"));
			return cb;
		}
		case WAITCOLUMN: {
			if (_showWait) {
				return Integer.toString(rl.getWait());
			} else {
				JComboBox cb = getTimeComboBox();
				cb.setSelectedItem(rl.getDepartureTime());
				return cb;
			}
		}
		case MAXLENGTHCOLUMN:
			return Integer.toString(rl.getMaxTrainLength());
		case GRADE:
			return Double.toString(rl.getGrade());
		case TRAINICONX:
			return Integer.toString(rl.getTrainIconX());
		case TRAINICONY:
			return Integer.toString(rl.getTrainIconY());
		case COMMENTCOLUMN: {
			if (rl.getComment().equals(""))
				return Bundle.getMessage("Add");
			else
				return Bundle.getMessage("Edit");
		}
		case UPCOLUMN:
			return Bundle.getMessage("Up");
		case DOWNCOLUMN:
			return Bundle.getMessage("Down");
		case DELETECOLUMN:
			return Bundle.getMessage("Delete");
		default:
			return "unknown " + col; // NOI18N
		}
	}

	public void setValueAt(Object value, int row, int col) {
		if (value == null) {
			log.debug("Warning route table row " + row + " still in edit");
			return;
		}
		switch (col) {
		case COMMENTCOLUMN:
			setComment(row);
			break;
		case UPCOLUMN:
			moveUpRouteLocation(row);
			break;
		case DOWNCOLUMN:
			moveDownRouteLocation(row);
			break;
		case DELETECOLUMN:
			deleteRouteLocation(row);
			break;
		case TRAINCOLUMN:
			setTrainDirection(value, row);
			break;
		case MAXMOVESCOLUMN:
			setMaxTrainMoves(value, row);
			break;
		case PICKUPCOLUMN:
			setPickup(value, row);
			break;
		case DROPCOLUMN:
			setDrop(value, row);
			break;
		case WAITCOLUMN: {
			if (_showWait)
				setWait(value, row);
			else
				setDepartureTime(value, row);
		}
			break;
		case MAXLENGTHCOLUMN:
			setMaxTrainLength(value, row);
			break;
		case GRADE:
			setGrade(value, row);
			break;
		case TRAINICONX:
			setTrainIconX(value, row);
			break;
		case TRAINICONY:
			setTrainIconY(value, row);
			break;
		default:
			break;
		}
	}

	private void moveUpRouteLocation(int row) {
		log.debug("move location up");
		String id = list.get(row);
		RouteLocation rl = _route.getLocationById(id);
		_route.moveLocationUp(rl);
	}

	private void moveDownRouteLocation(int row) {
		log.debug("move location down");
		String id = list.get(row);
		RouteLocation rl = _route.getLocationById(id);
		_route.moveLocationDown(rl);
	}

	private void deleteRouteLocation(int row) {
		log.debug("Delete location");
		String id = list.get(row);
		RouteLocation rl = _route.getLocationById(id);
		_route.deleteLocation(rl);
	}

	private int _trainDirection = Setup.getDirectionInt((String) Setup.getComboBox().getItemAt(0));

	public int getLastTrainDirection() {
		return _trainDirection;
	}

	private void setTrainDirection(Object value, int row) {
		RouteLocation rl = _route.getLocationById(list.get(row));
		_trainDirection = Setup.getDirectionInt((String) ((JComboBox) value).getSelectedItem());
		rl.setTrainDirection(_trainDirection);
		// update train icon
		rl.setTrainIconCoordinates();
	}

	private void setMaxTrainMoves(Object value, int row) {
		RouteLocation rl = _route.getLocationById(list.get(row));
		int moves;
		try {
			moves = Integer.parseInt(value.toString());
		} catch (NumberFormatException e) {
			log.error("Location moves must be a number");
			return;
		}
		if (moves <= 100) {
			rl.setMaxCarMoves(moves);
		} else {
			log.error("Location moves can not exceed 100");
			JOptionPane.showMessageDialog(null, Bundle.getMessage("MaximumLocationMoves"),
					Bundle.getMessage("CanNotChangeMoves"), JOptionPane.ERROR_MESSAGE);
		}
	}

	private void setDrop(Object value, int row) {
		RouteLocation rl = _route.getLocationById(list.get(row));
		rl.setCanDrop(((String) ((JComboBox) value).getSelectedItem()).equals(Bundle
				.getMessage("yes")));
	}

	private void setPickup(Object value, int row) {
		RouteLocation rl = _route.getLocationById(list.get(row));
		rl.setCanPickup(((String) ((JComboBox) value).getSelectedItem()).equals(Bundle
				.getMessage("yes")));
	}

	private int _maxTrainLength = Setup.getTrainLength();

	public int getLastMaxTrainLength() {
		return _maxTrainLength;
	}

	private void setWait(Object value, int row) {
		RouteLocation rl = _route.getLocationById(list.get(row));
		int wait;
		try {
			wait = Integer.parseInt(value.toString());
		} catch (NumberFormatException e) {
			log.error("Location wait must be a number");
			JOptionPane.showMessageDialog(null, Bundle.getMessage("EnterWaitTimeMinutes"),
					Bundle.getMessage("WaitTimeNotValid"), JOptionPane.ERROR_MESSAGE);
			return;
		}
		rl.setWait(wait);
	}

	private void setDepartureTime(Object value, int row) {
		RouteLocation rl = _route.getLocationById(list.get(row));
		rl.setDepartureTime(((String) ((JComboBox) value).getSelectedItem()));
	}

	private void setMaxTrainLength(Object value, int row) {
		RouteLocation rl = _route.getLocationById(list.get(row));
		int length;
		try {
			length = Integer.parseInt(value.toString());
		} catch (NumberFormatException e) {
			log.error("Maximum departure length must be a number");
			return;
		}
		if (length < 0) {
			log.error("Maximum departure length must be a postive number");
			return;
		}
		if (length <= Setup.getTrainLength()) {
			rl.setMaxTrainLength(length);
			_maxTrainLength = length;
		} else {
			log.error("Maximum departure length can not exceed maximum train length");
			JOptionPane.showMessageDialog(null, MessageFormat.format(
					Bundle.getMessage("DepartureLengthNotExceed"),
					new Object[] { length, Setup.getTrainLength() }), Bundle
					.getMessage("CanNotChangeMaxLength"), JOptionPane.ERROR_MESSAGE);
		}
	}

	private void setGrade(Object value, int row) {
		RouteLocation rl = _route.getLocationById(list.get(row));
		double grade;
		try {
			grade = Double.parseDouble(value.toString());
		} catch (NumberFormatException e) {
			log.error("grade must be a number");
			return;
		}
		if (grade <= 6 && grade >= -6) {
			rl.setGrade(grade);
		} else {
			log.error("Maximum grade is 6 percent");
			JOptionPane.showMessageDialog(null, Bundle.getMessage("MaxGrade"),
					Bundle.getMessage("CanNotChangeGrade"), JOptionPane.ERROR_MESSAGE);
		}
	}

	private void setTrainIconX(Object value, int row) {
		RouteLocation rl = _route.getLocationById(list.get(row));
		int x;
		try {
			x = Integer.parseInt(value.toString());
		} catch (NumberFormatException e) {
			log.error("Train icon x coordinate must be a number");
			return;
		}
		rl.setTrainIconX(x);
	}

	private void setTrainIconY(Object value, int row) {
		RouteLocation rl = _route.getLocationById(list.get(row));
		int y;
		try {
			y = Integer.parseInt(value.toString());
		} catch (NumberFormatException e) {
			log.error("Train icon y coordinate must be a number");
			return;
		}
		rl.setTrainIconY(y);
	}

	private void setComment(int row) {
		log.debug("Set comment for row " + row);
		final RouteLocation rl = _route.getLocationById(list.get(row));
		// Create comment panel
		final JDialog dialog = new JDialog();
		dialog.setLayout(new BorderLayout());
		dialog.setTitle(Bundle.getMessage("Comment") + " " + rl.getName());
		final JTextArea commentTextArea = new JTextArea(5, 100);
		JScrollPane commentScroller = new JScrollPane(commentTextArea);
		dialog.add(commentScroller, BorderLayout.CENTER);
		commentTextArea.setText(rl.getComment());
		
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
		dialog.add(buttonPane, BorderLayout.SOUTH);
		
		JButton okayButton = new JButton(Bundle.getMessage("Okay"));
		okayButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				rl.setComment(commentTextArea.getText());
				dialog.dispose();				
				return;
			}
		});
		buttonPane.add(okayButton);
	
		JButton cancelButton = new JButton(Bundle.getMessage("Cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				dialog.dispose();
				return;
			}
		});
		buttonPane.add(cancelButton);
		
		dialog.setModal(true);
		dialog.pack();
		dialog.setVisible(true);
	}

	private JComboBox getYesNoComboBox() {
		JComboBox cb = new JComboBox();
		cb.addItem(Bundle.getMessage("yes"));
		cb.addItem(Bundle.getMessage("no"));
		return cb;
	}

	private JComboBox getTimeComboBox() {
		JComboBox timeBox = new JComboBox();
		String hour;
		String minute;
		timeBox.addItem("");
		for (int i = 0; i < 24; i++) {
			if (i < 10)
				hour = "0" + Integer.toString(i);
			else
				hour = Integer.toString(i);
			for (int j = 0; j < 60; j = j + 5) {
				if (j < 10)
					minute = "0" + Integer.toString(j);
				else
					minute = Integer.toString(j);

				timeBox.addItem(hour + ":" + minute);
			}
		}
		return timeBox;
	}

	// this table listens for changes to a route and it's locations
	public void propertyChange(PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled())
			log.debug("Property change " + e.getPropertyName() + " old: " + e.getOldValue()
					+ " new: " + e.getNewValue()); // NOI18N
		if (e.getPropertyName().equals(Route.LISTCHANGE_CHANGED_PROPERTY)) {
			updateList();
			fireTableDataChanged();
		}

		if (e.getSource().getClass().equals(RouteLocation.class)) {
			String id = ((RouteLocation) e.getSource()).getId();
			int row = list.indexOf(id);
			if (Control.showProperty && log.isDebugEnabled())
				log.debug("Update route table row: " + row + " id: " + id);
			if (row >= 0)
				fireTableRowsUpdated(row, row);

		}
	}

	private void removePropertyChangeRouteLocations() {
		for (int i = 0; i < list.size(); i++) {
			// if object has been deleted, it's not here; ignore it
			RouteLocation rl = _route.getLocationById(list.get(i));
			if (rl != null)
				rl.removePropertyChangeListener(this);
		}
	}

	public void dispose() {
		if (log.isDebugEnabled())
			log.debug("dispose");
		removePropertyChangeRouteLocations();
		if (_route != null)
			_route.removePropertyChangeListener(this);
		list.clear();
		fireTableDataChanged();
	}

	static Logger log = LoggerFactory
			.getLogger(RouteEditTableModel.class.getName());
}
