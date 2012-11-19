package jmri.jmrit.vsdecoder.swing;

/*
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 *
 * @author			Mark Underwood Copyright (C) 2011
 * @version			$Revision: 21510 $
 */

import java.util.ResourceBundle;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Set;
import java.util.Map;
import java.util.Iterator;
import javax.swing.event.EventListenerList;
import jmri.util.JmriJFrame;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector2d;
import javax.swing.*;
import java.awt.*;
import javax.swing.table.AbstractTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import jmri.util.PhysicalLocation;
import jmri.jmrit.vsdecoder.VSDecoderBundle;
import jmri.jmrit.vsdecoder.VSDecoderManager;
import jmri.jmrit.vsdecoder.listener.ListeningSpot;
import jmri.ReporterManager;
import jmri.Reporter;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Location;


public class ManageLocationsFrame extends JFrame {

    // Uncomment this when we add labels...
    //private static final ResourceBundle rb = VSDecoderBundle.bundle();
    public static enum PropertyChangeID { MUTE, VOLUME_CHANGE, ADD_DECODER, REMOVE_DECODER }

    public static final Map<PropertyChangeID, String> PCIDMap;
    static {
	Map<PropertyChangeID, String> aMap = new HashMap<PropertyChangeID, String>();
	aMap.put(PropertyChangeID.MUTE, "VSDMF:Mute");
	aMap.put(PropertyChangeID.VOLUME_CHANGE, "VSDMF:VolumeChange");
	aMap.put(PropertyChangeID.ADD_DECODER, "VSDMF:AddDecoder");
	aMap.put(PropertyChangeID.REMOVE_DECODER, "VSDMF:RemoveDecoder");
	PCIDMap = Collections.unmodifiableMap(aMap);
    }

    protected EventListenerList listenerList = new javax.swing.event.EventListenerList();

    private JTabbedPane tabbedPane;
    private JPanel listenerPanel;
    private JPanel reporterPanel;
    private JPanel opsPanel;

    private Object[][] reporterData;  // positions of Reporters
    private Object[][] opsData;       // positions of Operations Locations
    private Object[][] locData;       // positions of Listener Locations
    private LocationTableModel reporterModel;
    private LocationTableModel opsModel;
    private ListenerTableModel locModel;
    private ListeningSpot listenerLoc;


    public ManageLocationsFrame(ListeningSpot listener, 
				Object[][] reporters,
				Object[][] ops) {
	super();
	//reporterMap = reporters;
	//opsMap = ops;
	reporterData = reporters;
	opsData = ops;
	listenerLoc = listener;
	initGui();
    }
    
    private void initGui() {
	// Panel for managing listeners
	listenerPanel = new JPanel();
	listenerPanel.setLayout(new BorderLayout());

	// Audio Mode Buttons
	JRadioButton b1 = new JRadioButton("Room Ambient");
	b1.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    modeRadioButtonPressed(e);
		}
	    });
	JRadioButton b2 = new JRadioButton("Headphones");
	b2.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    modeRadioButtonPressed(e);
		}
	    });
	b2.setEnabled(false);
	ButtonGroup bg = new ButtonGroup();
	bg.add(b1);
	bg.add(b2);
	b1.setSelected(true);
	JPanel modePanel = new JPanel();
	modePanel.setLayout(new BoxLayout(modePanel, BoxLayout.LINE_AXIS));
	modePanel.add(b1);
	modePanel.add(b2);

	// Build Listener Locations Table
	locData = new Object[1][7];
	locData[0][0] = listenerLoc.getName();
	locData[0][1] = new Boolean(true);
	locData[0][2] = listenerLoc.getLocation().x;
	locData[0][3] = listenerLoc.getLocation().y;
	locData[0][4] = listenerLoc.getLocation().z;
	locData[0][5] = listenerLoc.getBearing();
	locData[0][6] = listenerLoc.getAzimuth();
	
	log.debug("Listener:" + listenerLoc.toString());
	log.debug("locData:");
	for (int i = 0; i < 7; i++) {
	    log.debug("" + locData[0][i]);
	}
	    
	JPanel locPanel = new JPanel();
	locPanel.setLayout(new BoxLayout(locPanel, BoxLayout.LINE_AXIS));
	JScrollPane locScrollPanel = new JScrollPane();
	locModel = new ListenerTableModel(locData);
	JTable locTable = new JTable(locModel);
	locTable.setFillsViewportHeight(true);
	locTable.setPreferredScrollableViewportSize(new Dimension(520, 200));

	locScrollPanel.getViewport().add(locTable);

	listenerPanel.add(modePanel, BorderLayout.NORTH);
	listenerPanel.add(locScrollPanel, BorderLayout.CENTER);
	

	reporterPanel = new JPanel();
	reporterPanel.setLayout(new GridBagLayout());
	GridBagConstraints gbcr = new GridBagConstraints();
	gbcr.gridx = 0; gbcr.gridy = GridBagConstraints.RELATIVE;
	gbcr.fill = GridBagConstraints.NONE;
	gbcr.anchor = GridBagConstraints.LINE_START;
	gbcr.weightx = 1.0; gbcr.weighty = 1.0;
	JScrollPane reporterScrollPanel = new JScrollPane();
	reporterModel = new LocationTableModel(reporterData);
	JTable reporterTable = new JTable(reporterModel);
	reporterTable.setFillsViewportHeight(true);
	reporterScrollPanel.getViewport().add(reporterTable);
	reporterTable.setPreferredScrollableViewportSize(new Dimension(520, 200));

	opsPanel = new JPanel();
	opsPanel.setLayout(new GridBagLayout());
	opsPanel.revalidate();
	JScrollPane opsScrollPanel = new JScrollPane();
	opsModel = new LocationTableModel(opsData);
	JTable opsTable = new JTable(opsModel);
	opsTable.setFillsViewportHeight(true);
	opsTable.setPreferredScrollableViewportSize(new Dimension(520, 200));

	opsScrollPanel.getViewport().add(opsTable);

	tabbedPane = new JTabbedPane();
	tabbedPane.addTab("Reporters", reporterScrollPanel);
	tabbedPane.addTab("Operations Locations", opsScrollPanel);
	tabbedPane.addTab("Listeners", listenerPanel);

	JPanel buttonPane = new JPanel();
	buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
	JButton cancelButton = new JButton("Close");
	cancelButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    cancelButtonPressed(e);
		}
	    });
	JButton saveButton = new JButton("Save");
	saveButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    saveButtonPressed(e);
		}
	    });
	buttonPane.add(cancelButton);
	buttonPane.add(saveButton);

	this.getContentPane().setLayout(new GridBagLayout());
	GridBagConstraints gbc1 = new GridBagConstraints();
	gbc1.gridx = 0; gbc1.gridy = 0;
	gbc1.fill = GridBagConstraints.BOTH;
	gbc1.anchor = GridBagConstraints.CENTER;
	gbc1.weightx = 1.0; gbc1.weighty = 1.0;
	GridBagConstraints gbc2 = new GridBagConstraints();
	gbc2.gridx = 0; gbc2.gridy = 1;
	gbc2.fill = GridBagConstraints.NONE;
	gbc2.anchor = GridBagConstraints.CENTER;
	gbc2.weightx = 1.0; gbc2.weighty = 1.0;

	this.getContentPane().add(tabbedPane, gbc1);
	this.getContentPane().add(buttonPane, gbc2);

	this.pack();
	this.setVisible(true);
    }

    private void saveButtonPressed(ActionEvent e) {
	int value = JOptionPane.showConfirmDialog(null, "Save Reporters and Ops Locations?", "Save Changes", JOptionPane.YES_NO_OPTION);
	if (value == JOptionPane.YES_OPTION) {
	    saveTableValues();
	    OperationsXml.save();
	}
	if (Setup.isCloseWindowOnSaveEnabled())
	    dispose();
    }

    private void saveTableValues() {
	if ((Boolean)locModel.getValueAt(0,1)) {
	    listenerLoc.setLocation((Double)locModel.getValueAt(0,2), 
				    (Double)locModel.getValueAt(0,3),
				    (Double)locModel.getValueAt(0,4));
	    listenerLoc.setOrientation((Double)locModel.getValueAt(0,5),
				       (Double)locModel.getValueAt(0,6));
	    VSDecoderManager.instance().getVSDecoderPreferences().setListenerPosition(listenerLoc);
	}
	
	HashMap<String, PhysicalLocation> data = reporterModel.getDataMap();
	ReporterManager mgr = jmri.InstanceManager.reporterManagerInstance();
	for (String s : data.keySet()) {
	    log.debug("Reporter: " + s + " Location: " + data.get(s));
	    Reporter r = mgr.getByDisplayName(s);
	    PhysicalLocation.setBeanPhysicalLocation(data.get(s), r);
	}

	data = opsModel.getDataMap();
	LocationManager lmgr = LocationManager.instance();
	for (String s : data.keySet()) {
	    log.debug("OpsLocation: " + s + " Location: " + data.get(s));
	    Location l = lmgr.getLocationByName(s);
	    l.setPhysicalLocation(data.get(s));
	}
    }

    private void modeRadioButtonPressed(ActionEvent e) {
    }

    private void cancelButtonPressed(ActionEvent e) {
	dispose();
    }

    static private org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ManageLocationsFrame.class.getName());

}

class LocationTableModel extends AbstractTableModel {
    private String[] columnNames = { "Name",
				     "Use Location",
				     "X",
				     "Y",
				     "Z" };
    private Object[][]rowData = {
	{ "Fred", new Boolean(false), 0.0d, 1.0d, 0.0d },
	{ "Barney", new Boolean(false), 1.0d, 0.0d, 0.0d },
	{ "Wilma", new Boolean(false), 0.0d, 0.0d, 1.0d },
	{ "BamBam", new Boolean(false), 1.0d, 1.0d, 0.0d },
	{ "Pebbles", new Boolean(false), 0.0d, 1.0d, 1.0d }
    };

    public LocationTableModel(Object[][] dataMap) {
	super();
	rowData = dataMap;
    }

    public HashMap<String, PhysicalLocation> getDataMap() {
	// Includes only the ones with the checkbox made
	HashMap<String, PhysicalLocation> retv = new HashMap<String, PhysicalLocation>();
	for (Object[] row : rowData) {
	    if ((Boolean)row[1]) {
		retv.put((String)row[0], 
			 new PhysicalLocation((Float)row[2], (Float)row[3], (Float)row[4]));
	    }
	}
	return(retv);
    }

    public String getColumnName(int col) {
	return columnNames[col].toString();
    }

    public int getRowCount() { return rowData.length; }
    public int getColumnCount() { return columnNames.length; }
    public Object getValueAt(int row, int col) {
	return rowData[row][col];
    }
    public boolean isCellEditable(int row, int col) { return true; }
    public void setValueAt(Object value, int row, int col) {
	rowData[row][col] = value;
	fireTableCellUpdated(row, col);
    }

    public Class<?> getColumnClass(int columnIndex) {
	switch(columnIndex) {
	case 1:
	    return Boolean.class;
	case 4:
	case 3:
	case 2:
	    return Float.class;
	case 0:
	default:
	    return super.getColumnClass(columnIndex);
	}
    }

}

class ListenerTableModel extends AbstractTableModel {
    private String[] columnNames = { "Name",
				     "Use Location",
				     "X", "Y", "Z", "Bearing", "Azimuth" };
    private Object[][]rowData = null;

    public ListenerTableModel(Object[][] dataMap) {
	super();
	rowData = dataMap;
    }

    public HashMap<String, ListeningSpot> getDataMap() {
	// Includes only the ones with the checkbox made
	HashMap<String, ListeningSpot> retv = new HashMap<String, ListeningSpot>();
	ListeningSpot spot = null;
	for (Object[] row : rowData) {
	    if ((Boolean)row[1]) {
		spot = new ListeningSpot();
		spot.setName((String)row[0]);
		spot.setLocation((Double)row[2], (Double)row[3], (Double)row[4]);
		spot.setOrientation((Double)row[5], (Double)row[6]);
		retv.put((String)row[0], spot);
	    }
	}
	return(retv);
    }

    public String getColumnName(int col) {
	return columnNames[col].toString();
    }

    public int getRowCount() { return rowData.length; }
    public int getColumnCount() { return columnNames.length; }
    public Object getValueAt(int row, int col) {
	return rowData[row][col];
    }
    public boolean isCellEditable(int row, int col) { return true; }
    public void setValueAt(Object value, int row, int col) {
	rowData[row][col] = value;
	fireTableCellUpdated(row, col);
    }

    public Class<?> getColumnClass(int columnIndex) {
	switch(columnIndex) {
	case 1:
	    return Boolean.class;
	case 6:
	case 5:
	case 4:
	case 3:
	case 2:
	    return Double.class;
	case 0:
	default:
	    return super.getColumnClass(columnIndex);
	}
    }

}