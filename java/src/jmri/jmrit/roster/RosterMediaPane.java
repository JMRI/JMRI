package jmri.jmrit.roster;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import jmri.jmrit.XmlFile;
import jmri.util.FileUtil;
import jmri.util.swing.EditableResizableImagePanel;

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
* @author	Lionel Jeanson   Copyright (C) 2009
* @version	$$
*/

/**
 * A media pane for roster configuration tool, contains:
 *      + a selector for roster image (a large image for throttle background...)
 *      + a selector for roster icon (a small image for list displays...)
 *      + a selector for roster URL (link to wikipedia page about prototype...)
 *      + a table displaying user attributes for that locomotive
 *
 * @author Lionel Jeanson - Copyright 2009
 */
public class RosterMediaPane extends javax.swing.JPanel {

	private static final long serialVersionUID = 2420617780437463773L;
	JLabel _imageFPlabel = new JLabel();
	EditableResizableImagePanel _imageFilePath;
	JLabel _iconFPlabel = new JLabel();
	EditableResizableImagePanel _iconFilePath;
	JLabel _URLlabel = new JLabel();
	JTextField _URL = new JTextField(30);
	RosterAttributesTableModel rosterAttributesModel;

	final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.roster.JmritRosterBundle");

	public RosterMediaPane(RosterEntry r) {
		_imageFilePath = new EditableResizableImagePanel(r.getImagePath(), 320, 240);
		_imageFilePath.setDropFolder(FileUtil.getUserResourcePath());
		_imageFilePath.setToolTipText(rb.getString("MediaRosterImageToolTip"));
		_imageFilePath.setBorder(BorderFactory.createLineBorder(java.awt.Color.blue));
		_imageFPlabel.setText(rb.getString("MediaRosterImageLabel"));

		_iconFilePath = new EditableResizableImagePanel(r.getIconPath(), 160, 120);
		_iconFilePath.setDropFolder(FileUtil.getUserResourcePath());
		_iconFilePath.setToolTipText(rb.getString("MediaRosterIconToolTip"));
		_iconFilePath.setBorder(BorderFactory.createLineBorder(java.awt.Color.blue));
		_iconFPlabel.setText(rb.getString("MediaRosterIconLabel"));

		_URL.setText(r.getURL());
		_URL.setToolTipText(rb.getString("MediaRosterURLToolTip"));
		_URLlabel.setText(rb.getString("MediaRosterURLLabel"));

		rosterAttributesModel = new RosterAttributesTableModel(r); //t, columnNames);
		JTable jtAttributes = new JTable();
		jtAttributes.setModel(rosterAttributesModel);
		JScrollPane jsp = new JScrollPane(jtAttributes);
//		jtAttributes.setFillsViewportHeight(true); // Java 1.6 only


		JPanel mediap = new JPanel();
		GridBagLayout gbLayout = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		Dimension textFieldDim = new Dimension(320,20);
		Dimension imageFieldDim = new Dimension(320,200);		
		Dimension iconFieldDim = new Dimension(160,100);
		Dimension tableDim = new Dimension(400,200);
		mediap.setLayout(gbLayout);

		gbc.insets = new Insets(0, 8, 0, 8);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbLayout.setConstraints( _imageFPlabel,gbc);
		mediap.add( _imageFPlabel);

		gbc.gridx = 1;
		gbc.gridy = 0;
		_imageFilePath.setMinimumSize(imageFieldDim);
		_imageFilePath.setMaximumSize(imageFieldDim);
		_imageFilePath.setPreferredSize(imageFieldDim);
		gbLayout.setConstraints( _imageFilePath,gbc);
		mediap.add( _imageFilePath);   

		gbc.gridx = 0;
		gbc.gridy = 2;
		gbLayout.setConstraints( _iconFPlabel,gbc);
		mediap.add( _iconFPlabel);

		gbc.gridx = 1;
		gbc.gridy = 2;
		_iconFilePath.setMinimumSize(iconFieldDim);
		_iconFilePath.setMaximumSize(iconFieldDim);		
		_iconFilePath.setPreferredSize(iconFieldDim);
		gbLayout.setConstraints( _iconFilePath,gbc);
		mediap.add( _iconFilePath); 

		gbc.gridx = 0;
		gbc.gridy = 4;
		gbLayout.setConstraints(_URLlabel,gbc);
		mediap.add(_URLlabel);

		gbc.gridx = 1;
		gbc.gridy = 4;
		_URL.setMinimumSize(textFieldDim);
		_URL.setPreferredSize(textFieldDim);
		gbLayout.setConstraints(_URL,gbc);        
		mediap.add(_URL);

		this.setLayout(new BorderLayout());
		add(mediap, BorderLayout.NORTH);
		add(new JLabel(rb.getString("MediaRosterAttributeTableDescription")), BorderLayout.CENTER); // some nothing in the middle
		jsp.setMinimumSize(tableDim);
		jsp.setMaximumSize(tableDim);
		jsp.setPreferredSize(tableDim);
		add(jsp, BorderLayout.SOUTH);
	}

	public boolean guiChanged(RosterEntry r) {
		if (!r.getURL().equals(_URL.getText())) return true;
		if (!r.getImagePath().equals(_imageFilePath.getImagePath())) return true;
		if (!r.getIconPath().equals(_iconFilePath.getImagePath())) return true;
		if (rosterAttributesModel.wasModified()) return true;
		return false;
	}

	public void update(RosterEntry r) {
		r.setURL(_URL.getText()) ;
		r.setImagePath(_imageFilePath.getImagePath()) ;
		r.setIconPath(_iconFilePath.getImagePath()) ;
		rosterAttributesModel.updateModel(r);
	}

	public void dispose() {
		if (log.isDebugEnabled()) {
			log.debug("dispose");
		}
	}

	private class RosterAttributesTableModel extends AbstractTableModel { 
		Vector<KeyValueModel>  attributes;
		String titles[];
		boolean wasModified;

		private class KeyValueModel {
			public KeyValueModel(String k, String v) { key=k; value=v; }
			public String key, value;
		}

		public RosterAttributesTableModel(RosterEntry r) { 
			setModel(r);

			titles = new String[2];
			titles[0] = rb.getString("MediaRosterAttributeName");
			titles[1] = rb.getString("MediaRosterAttributeValue");
		} 

		public void setModel(RosterEntry r) {
			if (r.getAttributes() != null) {
				attributes = new Vector<KeyValueModel>(r.getAttributes().size());
				Iterator<String> ite = r.getAttributes().iterator();
				while (ite.hasNext()) {
					String key = ite.next();
					KeyValueModel kv = new KeyValueModel(key, r.getAttribute(key));
					attributes.add(kv);
				} 
			}
			else
				attributes = new Vector<KeyValueModel>(0);
			wasModified = false;
		}

		public void updateModel(RosterEntry r) {
			// add and update keys
			for (int i=0; i<attributes.size(); i++) {
				KeyValueModel kv = attributes.get(i);
				if ( (kv.key.length()>0) && // only update if key value defined, will do the remove to
						((r.getAttributes() == null) || (r.getAttribute(kv.key)==null) || (kv.value.compareTo(r.getAttribute(kv.key))!=0)) )
					r.putAttribute(kv.key, kv.value);
			}
			//remove undefined keys
			if (r.getAttributes() != null) {
				Iterator<String> ite = r.getAttributes().iterator();
				while (ite.hasNext()) 
					if (! keyExist(ite.next())) // not very efficient algorithm!
						ite.remove();				
			}
			wasModified = false;
		}

		private boolean keyExist(String k) {
			if (k==null) return false;
			for (int i=0; i<attributes.size(); i++)
				if ( k.compareTo(attributes.get(i).key) == 0)
					return true;
			return false;
		}

		public int getColumnCount() { 
			return 2; 
		}

		public int getRowCount() { 
			return attributes.size()+1;
		}

		public String getColumnName(int col){ 
			return titles[col]; 
		}

		public Object getValueAt(int row, int col) {
			if (row<attributes.size()) {
				if (col == 0)
					return attributes.get(row).key;
				if (col == 1)
					return attributes.get(row).value;
			}
			return "...";
		}

		public void setValueAt(Object value, int row, int col) {
			KeyValueModel kv;
			
			if (row<attributes.size()) // already exist?
				kv =  attributes.get(row);
			else
				kv = new KeyValueModel("","");
			
			if (col == 0) // update key
                //Force keys to be save as a single string with no spaces
				if (! keyExist(((String) value).replaceAll("\\s", ""))) // if not exist
					kv.key = ((String) value).replaceAll("\\s", "");
				else {
					setValueAt(value+"-1", row, col); // else change key name
					return;
				}

			if (col == 1) // update value
				kv.value = (String) value;
			if (row<attributes.size()) // existing one
				attributes.set(row, kv);
			else
				attributes.add(row, kv); // new one

			if ((col==0) && (kv.key.compareTo("")==0))
				attributes.remove(row); // actually maybe remove
			
			wasModified = true;
			fireTableCellUpdated(row, col);
		}

		public boolean isCellEditable(int row, int col) {
			return true;
		}

		public boolean wasModified() {
			return wasModified;
		}
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RosterMediaPane.class.getName());
}
