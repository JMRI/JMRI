// OptionsMenu.java

package jmri.jmrit.dispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import java.awt.*;
import jmri.Scale;

import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.util.JmriJFrame;
 
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Sets up and processes items in the Dispatcher Options menu.
 *
 * <P>
 * This file is part of JMRI.
 * <P>
 * JMRI is open source software; you can redistribute it and/or modify it 
 * under the terms of version 2 of the GNU General Public License as 
 * published by the Free Software Foundation. See the "COPYING" file for 
 * a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 *
 * @author			Dave Duchamp    Copyright (C) 2008
 * @version			$Revision$
 */

public class OptionsMenu extends JMenu {

	static final ResourceBundle rb = ResourceBundle
			.getBundle("jmri.jmrit.dispatcher.DispatcherBundle");

    public OptionsMenu(DispatcherFrame f) {
		dispatcher = f;
		this.setText(rb.getString("OptionsMenuTitle"));
		autoDispatchItem = new JCheckBoxMenuItem(rb.getString("AutoDispatchItem"));
		this.add(autoDispatchItem);
		autoDispatchItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				handleAutoDispatch(event);
			}
		});
		autoTurnoutsItem = new JCheckBoxMenuItem(rb.getString("AutoTurnoutsItem"));
		this.add(autoTurnoutsItem);
		autoTurnoutsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				handleAutoTurnouts(event);
			}
		});
		JMenuItem optionWindowItem = new JMenuItem(rb.getString("OptionWindowItem")+"...");
		this.add(optionWindowItem);
		optionWindowItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				optionWindowRequested(event);
			}
		});
		JMenuItem saveOptionsItem = new JMenuItem(rb.getString("SaveOptionsItem")+"...");
		this.add(saveOptionsItem);
		saveOptionsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				saveRequested(event);
			}
		});
		initializeMenu();
    }
	
	protected DispatcherFrame dispatcher = null;
	
	// Option menu items 
    private JCheckBoxMenuItem autoDispatchItem = null;
    private JCheckBoxMenuItem autoTurnoutsItem = null;
	// Initialize check box items in menu from Dispatcher
	public void initializeMenu() {
		autoDispatchItem.setSelected(dispatcher.getAutoAllocate());
		autoTurnoutsItem.setSelected(dispatcher.getAutoTurnouts());
	}

	private void handleAutoDispatch(ActionEvent e) {
		boolean set = autoDispatchItem.isSelected();
		dispatcher.setAutoAllocate(set);		
	}
	
	private void handleAutoTurnouts(ActionEvent e) {
		boolean set = autoTurnoutsItem.isSelected();
		dispatcher.setAutoTurnouts(set);			
	}

	// options window items
	JmriJFrame optionsFrame = null;
	Container optionsPane = null;
	JCheckBox useConnectivityCheckBox = new JCheckBox(rb.getString("UseConnectivity"));
	JComboBox layoutEditorBox = new JComboBox();
	ArrayList<LayoutEditor> layoutEditorList = new ArrayList<LayoutEditor>();
	JCheckBox autoAllocateCheckBox = new JCheckBox(rb.getString("AutoAllocateBox"));
	JCheckBox autoTurnoutsCheckBox = new JCheckBox(rb.getString("AutoTurnoutsBox"));
	JRadioButton trainsFromRoster = new JRadioButton(rb.getString("TrainsFromRoster"));
	JRadioButton trainsFromTrains = new JRadioButton(rb.getString("TrainsFromTrains"));
	JRadioButton trainsFromUser = new JRadioButton(rb.getString("TrainsFromUser"));
	JCheckBox detectionCheckBox = new JCheckBox(rb.getString("DetectionBox"));
	JCheckBox shortNameCheckBox = new JCheckBox(rb.getString("ShortNameBox"));
	JCheckBox nameInBlockCheckBox = new JCheckBox(rb.getString("NameInBlockBox"));
	JCheckBox extraColorForAllocatedCheckBox = new JCheckBox(rb.getString("ExtraColorForAllocatedBox"));
	JCheckBox nameInAllocatedBlockCheckBox = new JCheckBox(rb.getString("NameInAllocatedBlockBox"));
        JCheckBox supportVSDecoderCheckBox = new JCheckBox(rb.getString("SupportVSDecoder"));
	JComboBox layoutScaleBox = new JComboBox();
	JRadioButton scaleFeet = new JRadioButton(rb.getString("ScaleFeet"));
	JRadioButton scaleMeters = new JRadioButton(rb.getString("ScaleMeters"));
	
	private void optionWindowRequested(ActionEvent e) {
		if (optionsFrame == null) {
			optionsFrame = new JmriJFrame(rb.getString("OptionsMenuTitle"),false,true);
            optionsFrame.addHelpMenu("package.jmri.jmrit.dispatcher.Options", true);
			optionsPane = optionsFrame.getContentPane();
            optionsPane.setLayout(new BoxLayout(optionsFrame.getContentPane(), BoxLayout.Y_AXIS));
            JPanel p1 = new JPanel(); 
			p1.setLayout(new FlowLayout());
			p1.add(useConnectivityCheckBox);
			useConnectivityCheckBox.setToolTipText(rb.getString("UseConnectivityHint"));
			p1.add(layoutEditorBox);
			layoutEditorBox.setToolTipText(rb.getString("LayoutEditorHint"));
			optionsPane.add(p1);
			JPanel p2 = new JPanel();
			p2.setLayout(new FlowLayout());
			ButtonGroup trainsGroup = new ButtonGroup();
			p2.add(trainsFromRoster);
			trainsFromRoster.setToolTipText(rb.getString("TrainsFromRosterHint"));
			trainsGroup.add(trainsFromRoster);
			p2.add(new JLabel("     "));
			p2.add(trainsFromTrains);
			trainsFromTrains.setToolTipText(rb.getString("TrainsFromTrainsHint"));
			trainsGroup.add(trainsFromTrains);
			p2.add(new JLabel("     "));
			p2.add(trainsFromUser);
			trainsFromUser.setToolTipText(rb.getString("TrainsFromUserHint"));
			trainsGroup.add(trainsFromUser);
			optionsPane.add(p2);
			JPanel p3 = new JPanel();
			p3.setLayout(new FlowLayout());
			p3.add(detectionCheckBox);
			detectionCheckBox.setToolTipText(rb.getString("DetectionBoxHint"));
			optionsPane.add(p3);
			JPanel p4 = new JPanel();
			p4.setLayout(new FlowLayout());
			p4.add(autoAllocateCheckBox);
			autoAllocateCheckBox.setToolTipText(rb.getString("AutoAllocateBoxHint"));
			optionsPane.add(p4);
			JPanel p5 = new JPanel();
			p5.setLayout(new FlowLayout());
			p5.add(autoTurnoutsCheckBox);
			autoTurnoutsCheckBox.setToolTipText(rb.getString("AutoTurnoutsBoxHint"));
			optionsPane.add(p5);
			JPanel p6 = new JPanel();
			p6.setLayout(new FlowLayout());
			p6.add(shortNameCheckBox);
			shortNameCheckBox.setToolTipText(rb.getString("ShortNameBoxHint"));
			optionsPane.add(p6);
			JPanel p7 = new JPanel();
			p7.setLayout(new FlowLayout());
			p7.add(nameInBlockCheckBox);
			nameInBlockCheckBox.setToolTipText(rb.getString("NameInBlockBoxHint"));
			optionsPane.add(p7);
			JPanel p10 = new JPanel();
			p10.setLayout(new FlowLayout());
			p10.add(extraColorForAllocatedCheckBox);
			extraColorForAllocatedCheckBox.setToolTipText(rb.getString("ExtraColorForAllocatedBoxHint"));
			optionsPane.add(p10);
			JPanel p11 = new JPanel();
			p11.setLayout(new FlowLayout());
			p11.add(nameInAllocatedBlockCheckBox);
			nameInAllocatedBlockCheckBox.setToolTipText(rb.getString("NameInAllocatedBlockBoxHint"));
			optionsPane.add(p11);
			JPanel p13 =new JPanel();
			p13.setLayout(new FlowLayout());
			p13.add(supportVSDecoderCheckBox);
			supportVSDecoderCheckBox.setToolTipText(rb.getString("SupportVSDecoderBoxHint"));
			optionsPane.add(p13);
			JPanel p8 = new JPanel();
			initializeScaleCombo();
			p8.add(new JLabel(rb.getString("LayoutScale")+":"));
			p8.add(layoutScaleBox); 
			layoutScaleBox.setToolTipText(rb.getString("ScaleBoxHint"));
			optionsPane.add(p8);
			JPanel p12 = new JPanel();
			p12.setLayout(new FlowLayout());
			p12.add(new JLabel(rb.getString("Units")+"  "));
			ButtonGroup scaleGroup = new ButtonGroup();
			p12.add(scaleFeet);
			scaleFeet.setToolTipText(rb.getString("ScaleFeetHint"));
			scaleGroup.add(scaleFeet);
			p12.add(new JLabel("  "));
			p12.add(scaleMeters);
			scaleMeters.setToolTipText(rb.getString("ScaleMetersHint"));
			scaleGroup.add(scaleMeters);
			optionsPane.add(p12);
			optionsPane.add(new JSeparator());
			JPanel p9 = new JPanel();
			p9.setLayout(new FlowLayout());
			JButton cancelButton = null;
			p9.add(cancelButton = new JButton(rb.getString("CancelButton")));
            cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cancelOptions(e);
                }
            });
			cancelButton.setToolTipText(rb.getString("CancelButtonHint2"));
			p9.add(new JLabel("     "));
			JButton applyButton = null;
			p9.add(applyButton = new JButton(rb.getString("ApplyButton")));
            applyButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    applyOptions(e);
                }
            });
			applyButton.setToolTipText(rb.getString("ApplyButtonHint"));
			optionsPane.add(p9);
		}
		if (initializeLayoutEditorCombo()) {
			useConnectivityCheckBox.setVisible(true);
			layoutEditorBox.setVisible(true);
		}
		else {
			useConnectivityCheckBox.setVisible(false);
			layoutEditorBox.setVisible(false);
		}
		useConnectivityCheckBox.setSelected(dispatcher.getUseConnectivity());
		trainsFromRoster.setSelected(dispatcher.getTrainsFromRoster());
		trainsFromTrains.setSelected(dispatcher.getTrainsFromTrains());
		trainsFromUser.setSelected(dispatcher.getTrainsFromUser());
		detectionCheckBox.setSelected(dispatcher.getHasOccupancyDetection());
		autoAllocateCheckBox.setSelected(dispatcher.getAutoAllocate());
		autoTurnoutsCheckBox.setSelected(dispatcher.getAutoTurnouts());
		shortNameCheckBox.setSelected(dispatcher.getShortActiveTrainNames());
		nameInBlockCheckBox.setSelected(dispatcher.getShortNameInBlock());
		extraColorForAllocatedCheckBox.setSelected(dispatcher.getExtraColorForAllocated());
		nameInAllocatedBlockCheckBox.setSelected(dispatcher.getNameInAllocatedBlock());
		supportVSDecoderCheckBox.setSelected(dispatcher.getSupportVSDecoder());
		scaleMeters.setSelected(dispatcher.getUseScaleMeters());
		scaleFeet.setSelected(!dispatcher.getUseScaleMeters());
		optionsFrame.pack();
		optionsFrame.setVisible(true);
	}
	private void applyOptions(ActionEvent e) {
		if (layoutEditorList.size()>0) {
			int index = layoutEditorBox.getSelectedIndex();
			dispatcher.setLayoutEditor(layoutEditorList.get(index));
			dispatcher.setUseConnectivity(useConnectivityCheckBox.isSelected());
		}
		dispatcher.setTrainsFromRoster(trainsFromRoster.isSelected());
		dispatcher.setTrainsFromTrains(trainsFromTrains.isSelected());
		dispatcher.setTrainsFromUser(trainsFromUser.isSelected());
		dispatcher.setHasOccupancyDetection(detectionCheckBox.isSelected());
		dispatcher.setAutoAllocate(autoAllocateCheckBox.isSelected());
		autoDispatchItem.setSelected(autoAllocateCheckBox.isSelected());
		dispatcher.setAutoTurnouts(autoTurnoutsCheckBox.isSelected());
		autoTurnoutsItem.setSelected(autoTurnoutsCheckBox.isSelected());
		if (autoTurnoutsCheckBox.isSelected() && ( (layoutEditorList.size()==0) ||
								(!useConnectivityCheckBox.isSelected()) ) ) {
			JOptionPane.showMessageDialog(optionsFrame,rb.getString(
				"AutoTurnoutsWarn"),rb.getString("WarningTitle"),JOptionPane.WARNING_MESSAGE);
		}
		dispatcher.setShortActiveTrainNames(shortNameCheckBox.isSelected());
		dispatcher.setShortNameInBlock(nameInBlockCheckBox.isSelected());
		dispatcher.setExtraColorForAllocated(extraColorForAllocatedCheckBox.isSelected());
		dispatcher.setNameInAllocatedBlock(nameInAllocatedBlockCheckBox.isSelected());
		dispatcher.setSupportVSDecoder(supportVSDecoderCheckBox.isSelected());
		dispatcher.setScale(layoutScaleBox.getSelectedIndex()+1);
		dispatcher.setUseScaleMeters(scaleMeters.isSelected());
		optionsFrame.setVisible(false);	
		optionsFrame.dispose();  // prevent this window from being listed in the Window menu.
		optionsFrame = null;
		initializeMenu();
	}
	private void cancelOptions(ActionEvent e) {
		optionsFrame.setVisible(false);	
		optionsFrame.dispose();  // prevent this window from being listed in the Window menu.
		optionsFrame = null;
	}
			
	private void saveRequested(ActionEvent e) {
		try {
			OptionsFile.instance().writeDispatcherOptions(dispatcher);
		} 
		//catch (org.jdom.JDOMException jde) { 
		//	log.error("Exception writing Dispatcher options: "+jde); 
		//}                           
		catch (java.io.IOException ioe) { 
			log.error("Exception writing Dispatcher options: "+ioe); 
		}   
	}
	private boolean initializeLayoutEditorCombo() {
		// get list of Layout Editor panels
		layoutEditorList = jmri.jmrit.display.PanelMenu.instance().getLayoutEditorPanelList();
		if (layoutEditorList.size()==0) return false;
		layoutEditorBox.removeAllItems();
		for (int i=0; i<layoutEditorList.size(); i++) {
			layoutEditorBox.addItem(layoutEditorList.get(i).getTitle());
		}
		if (layoutEditorList.size()>1) {
			LayoutEditor le = dispatcher.getLayoutEditor();
			for (int j=0; j<layoutEditorList.size(); j++) {
				if (le == layoutEditorList.get(j)) {
					layoutEditorBox.setSelectedIndex(j);
				}
			}
		}
		else {
			layoutEditorBox.setSelectedIndex(0);
		}
		return true;
	}
	private void initializeScaleCombo() {
		layoutScaleBox.removeAllItems();
		for (int i=0; i<Scale.NUM_SCALES; i++) {
			layoutScaleBox.addItem(Scale.getScaleID(i+1));
		}
		layoutScaleBox.setSelectedIndex(dispatcher.getScale()-1);
	}
   
    static Logger log = LoggerFactory.getLogger(OptionsMenu.class.getName());
}

/* @(#)OptionsMenu.java */
