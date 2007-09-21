// jmri.jmrit.display.LayoutEditorTools.java

package jmri.jmrit.display;

import jmri.InstanceManager;
import jmri.util.JmriJFrame;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ResourceBundle;
import java.util.ArrayList;

import javax.swing.*;

import jmri.Sensor;
import jmri.Turnout;
import jmri.SignalHead;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.blockboss.BlockBossLogic;

/**
 * Layout Editor Tools provides tools making use of layout connectivity available 
 *	in Layout Editor panels.
 * <P>
 * The tools in this module are accessed via the Tools menu in Layout Editor.
 * <P>
 * @author Dave Duchamp Copyright (c) 2007
 * @version $Revision: 1.2 $
 */

public class LayoutEditorTools 
{

	// Defined text resource
	ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.LayoutEditorBundle");
	
	// constants
	public int NONE = 0;  // Signal at Turnout Positions
	public int A1 = 1;
	public int A2 = 2;
	public int B1 = 3;
	public int B2 = 4;
	public int C1 = 5;
	public int C2 = 6;
	public int D1 = 7;
	public int D2 = 8;
	
	// operational instance variables shared between tools
	private LayoutEditor layoutEditor = null;
    private MultiIconEditor signalIconEditor = null;
    private JFrame signalFrame = null;
	private boolean needRedraw = false;
	private BlockBossLogic logic = null;
	private SignalHead auxSignal = null;
	
	// constructor method
	public LayoutEditorTools(LayoutEditor thePanel) {
		layoutEditor = thePanel;
	}

	/**
	 * Tool to set signals at a turnout, including placing the signal icons and 
	 *		optionally setup of Simple Signal Logic for each signal head
	 * <P>
	 * This tool assumes left facing signal head icons have been selected, and 
	 *		will rotate the signal head icons accordingly.
	 * <P>
	 * This tool will place throat icons on the right side of the track, and 
	 *		continuing and diverging icons on the outside edge of the turnout.
	 * <P>
	 * This tool only places signal icons if the turnout is either mostly vertical 
	 *		or mostly horizontal. Some user adjustment may be needed.
	 */	
	
	// operational variables for Set Signals at Turnout tool
	private JmriJFrame setSignalsFrame = null;
	private boolean setSignalsOpen = false;
	private JTextField turnoutNameField = new JTextField(16);
	private JTextField throatContinuingField = new JTextField(16);
	private JTextField throatDivergingField = new JTextField(16);
	private JTextField continuingField = new JTextField(16);
	private JTextField divergingField = new JTextField(16);
	private JCheckBox setThroatContinuing = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupLogicThroatContinuing = new JCheckBox(rb.getString("SetLogic"));
	private JCheckBox setThroatDiverging = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupLogicThroatDiverging = new JCheckBox(rb.getString("SetLogic"));
	private JCheckBox setContinuing = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupLogicContinuing = new JCheckBox(rb.getString("SetLogic"));
	private JCheckBox setDiverging = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupLogicDiverging = new JCheckBox(rb.getString("SetLogic"));
	private JButton getSavedSignalHeads = null;
	private JButton changeSignalIcon = null;
	private JButton setSignalsDone = null;
	private JButton setSignalsCancel = null;
	private LayoutTurnout layoutTurnout = null;
	private boolean layoutTurnoutHorizontal = false;
	private boolean layoutTurnoutVertical = false;
	private boolean layoutTurnoutThroatLeft = false;
	private boolean layoutTurnoutThroatUp = false;
	private boolean layoutTurnoutBUp = false;
	private boolean layoutTurnoutBLeft = false;
	private boolean turnoutFromMenu = false;
	private Turnout turnout = null;
	private SignalHead throatContinuingHead = null;
	private SignalHead throatDivergingHead = null;
	private SignalHead continuingHead = null;
	private SignalHead divergingHead = null;

	// display dialog for Set Signals at Turnout tool
	public void setSignalsAtTurnoutFromMenu( LayoutTurnout to, 
					MultiIconEditor theEditor, JFrame theFrame ) {
		turnoutFromMenu = true;
		layoutTurnout = to;
		turnout = to.getTurnout();
		turnoutNameField.setText(to.getTurnoutName());
		setSignalsAtTurnout(theEditor,theFrame);
	}
	public void setSignalsAtTurnout( MultiIconEditor theEditor, JFrame theFrame ) {
		signalIconEditor = theEditor;
		signalFrame = theFrame;
		if (setSignalsOpen) {
			setSignalsFrame.setVisible(true);
			return;
		}
		// Initialize if needed
		if (setSignalsFrame == null) {
            setSignalsFrame = new JmriJFrame( rb.getString("SignalsAtTurnout") );
            setSignalsFrame.addHelpMenu("package.jmri.jmrit.display.SetSignalsAtTurnout", true);
            setSignalsFrame.setLocation(70,30);
            Container theContentPane = setSignalsFrame.getContentPane();        
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));
			JPanel panel1 = new JPanel(); 
            panel1.setLayout(new FlowLayout());
			if (turnoutFromMenu) {
				JLabel turnoutNameLabel = new JLabel( rb.getString("Turnout")+" "+
					rb.getString("Name")+" : "+layoutTurnout.getTurnoutName());
				panel1.add(turnoutNameLabel);
			}
			else {
				JLabel turnoutNameLabel = new JLabel( rb.getString("Turnout")+" "+
																rb.getString("Name") );
				panel1.add(turnoutNameLabel);
				panel1.add(turnoutNameField);
				turnoutNameField.setToolTipText(rb.getString("SignalsTurnoutNameHint"));
			}
            theContentPane.add(panel1);
			theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
			JLabel shTitle = new JLabel(rb.getString("SignalHeads"));
			panel2.add(shTitle);
			panel2.add(new JLabel("   "));
            panel2.add(getSavedSignalHeads = new JButton(rb.getString("GetSaved")));
            getSavedSignalHeads.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						turnoutSignalsGetSaved(e);
					}
				});
            getSavedSignalHeads.setToolTipText( rb.getString("GetSavedHint") );			
			theContentPane.add(panel2);
            JPanel panel21 = new JPanel();
            panel21.setLayout(new FlowLayout());
			JLabel throatContinuingLabel = new JLabel(rb.getString("ThroatContinuing")+" : ");
			panel21.add(throatContinuingLabel);
			panel21.add(throatContinuingField);
			theContentPane.add(panel21);
			throatContinuingField.setToolTipText(rb.getString("SignalHeadNameHint"));
            JPanel panel22 = new JPanel();
            panel22.setLayout(new FlowLayout());
			panel22.add(new JLabel("   "));
			panel22.add(setThroatContinuing);
			setThroatContinuing.setToolTipText(rb.getString("PlaceHeadHint"));
			panel22.add(new JLabel("  "));
			panel22.add(setupLogicThroatContinuing);
			setupLogicThroatContinuing.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel22);
            JPanel panel31 = new JPanel();
            panel31.setLayout(new FlowLayout());
			JLabel throatDivergingLabel = new JLabel(rb.getString("ThroatDiverging")+" : ");
			panel31.add(throatDivergingLabel);
			panel31.add(throatDivergingField);
			theContentPane.add(panel31);
			throatDivergingField.setToolTipText(rb.getString("SignalHeadNameHint"));
            JPanel panel32 = new JPanel();
            panel32.setLayout(new FlowLayout());
			panel32.add(new JLabel("   "));
			panel32.add(setThroatDiverging);
			setThroatDiverging.setToolTipText(rb.getString("PlaceHeadHint"));
			panel32.add(new JLabel("  "));
			panel32.add(setupLogicThroatDiverging);
			setupLogicThroatDiverging.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel32);
            JPanel panel41 = new JPanel();
            panel41.setLayout(new FlowLayout());
			JLabel continuingLabel = new JLabel(rb.getString("Continuing")+" : ");
			panel41.add(continuingLabel);
			panel41.add(continuingField);
			theContentPane.add(panel41);
			continuingField.setToolTipText(rb.getString("SignalHeadNameHint"));
            JPanel panel42 = new JPanel();
            panel42.setLayout(new FlowLayout());
			panel42.add(new JLabel("   "));
			panel42.add(setContinuing);
			setContinuing.setToolTipText(rb.getString("PlaceHeadHint"));
			panel42.add(new JLabel("  "));
			panel42.add(setupLogicContinuing);
			setupLogicContinuing.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel42);
            JPanel panel51 = new JPanel();
            panel51.setLayout(new FlowLayout());
			JLabel divergingLabel = new JLabel(rb.getString("Diverging")+" : ");
			panel51.add(divergingLabel);
			panel51.add(divergingField);
			theContentPane.add(panel51);
			divergingField.setToolTipText(rb.getString("SignalHeadNameHint"));
            JPanel panel52 = new JPanel();
            panel52.setLayout(new FlowLayout());
			panel52.add(new JLabel("   "));
			panel52.add(setDiverging);
			setDiverging.setToolTipText(rb.getString("PlaceHeadHint"));
			panel52.add(new JLabel("  "));
			panel52.add(setupLogicDiverging);
			setupLogicDiverging.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel52);
			theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
            JPanel panel6 = new JPanel();
            panel6.setLayout(new FlowLayout());
            panel6.add(changeSignalIcon = new JButton(rb.getString("ChangeSignalIcon")));
            changeSignalIcon.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						signalFrame.show();
					}
				});
            changeSignalIcon.setToolTipText( rb.getString("ChangeSignalIconHint") );
			panel6.add(new JLabel("  "));
            panel6.add(setSignalsDone = new JButton(rb.getString("Done")));
            setSignalsDone.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setSignalsDonePressed(e);
                }
            });
            setSignalsDone.setToolTipText( rb.getString("SignalDoneHint") );
            panel6.add(setSignalsCancel = new JButton(rb.getString("Cancel")));
            setSignalsCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setSignalsCancelPressed(e);
                }
            });
            setSignalsCancel.setToolTipText( rb.getString("CancelHint") );
            theContentPane.add(panel6);
			setSignalsFrame.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					setSignalsCancelPressed(null);
				}
			});
		}
        setSignalsFrame.pack();
        setSignalsFrame.setVisible(true);		
		setSignalsOpen = true;
	}	
	private void turnoutSignalsGetSaved (ActionEvent a) {
		if ( !getTurnoutInformation(false) ) return;
		throatContinuingField.setText(layoutTurnout.getSignalA1Name());	
		throatDivergingField.setText(layoutTurnout.getSignalA2Name());
		continuingField.setText(layoutTurnout.getSignalB1Name());
		divergingField.setText(layoutTurnout.getSignalC1Name());	
	}
	private void setSignalsCancelPressed (ActionEvent a) {
		setSignalsOpen = false;
		turnoutFromMenu = false;
		setSignalsFrame.setVisible(false);
	}
	private void setSignalsDonePressed (ActionEvent a) {
		// process turnout name
		if ( !getTurnoutInformation(false) ) return;
		// process signal head names
		if ( !getTurnoutSignalHeadInformation() ) return;
		// place signals as requested
		if (setThroatContinuing.isSelected()) {
			if (isHeadOnPanel(throatContinuingHead) &&
				(throatContinuingHead!=getHeadFromName(layoutTurnout.getSignalA1Name()))) {
				JOptionPane.showMessageDialog(setSignalsFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new String[]{throatContinuingField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!layoutTurnoutHorizontal) && (!layoutTurnoutVertical) ) {
				JOptionPane.showMessageDialog(setSignalsFrame,
					rb.getString("InfoMessage2"),"",JOptionPane.INFORMATION_MESSAGE);
				if (throatContinuingHead!=getHeadFromName(layoutTurnout.getSignalA1Name())) {				
					removeSignalHeadFromPanel(layoutTurnout.getSignalA1Name());
					removeAssignment(throatContinuingHead);
					layoutTurnout.setSignalA1Name(throatContinuingField.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(layoutTurnout.getSignalA1Name());
				placeThroatContinuing();
				removeAssignment(throatContinuingHead);
				layoutTurnout.setSignalA1Name(throatContinuingField.getText().trim());
				needRedraw = true;
			}		
		}
		else {
			int assigned = isHeadAssignedHere(throatContinuingHead);
			if (assigned == NONE) {
				if ( isHeadOnPanel(throatContinuingHead) && 
									isHeadAssignedAnywhere(throatContinuingHead) ) {
					JOptionPane.showMessageDialog(setSignalsFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError8"),
							new String[]{throatContinuingField.getText().trim()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSignalHeadFromPanel(layoutTurnout.getSignalA1Name());
					removeAssignment(throatContinuingHead);
					layoutTurnout.setSignalA1Name(throatContinuingField.getText().trim());
				}
			}
			else if (assigned!=A1) {
// need to figure out what to do in this case.			
			}
		}
		if ( (setThroatDiverging.isSelected()) && (throatDivergingHead!=null) ) {
			if (isHeadOnPanel(throatDivergingHead) && 
				(throatDivergingHead!=getHeadFromName(layoutTurnout.getSignalA2Name()))) {
				JOptionPane.showMessageDialog(setSignalsFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new String[]{throatDivergingField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!layoutTurnoutHorizontal) && (!layoutTurnoutVertical) ) {
				JOptionPane.showMessageDialog(setSignalsFrame,
					rb.getString("InfoMessage2"),"",JOptionPane.INFORMATION_MESSAGE);
				if (throatDivergingHead!=getHeadFromName(layoutTurnout.getSignalA2Name())) {
					removeSignalHeadFromPanel(layoutTurnout.getSignalA2Name());
					removeAssignment(throatDivergingHead);
					layoutTurnout.setSignalA2Name(throatDivergingField.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(layoutTurnout.getSignalA2Name());
				placeThroatDiverging();
				removeAssignment(throatDivergingHead);
				layoutTurnout.setSignalA2Name(throatDivergingField.getText().trim());
				needRedraw = true;
			}		
		}
		else if (throatDivergingHead!=null) {
			int assigned = isHeadAssignedHere(throatDivergingHead);
			if (assigned == NONE) {
				if (isHeadOnPanel(throatDivergingHead) && 
									isHeadAssignedAnywhere(throatDivergingHead) ) {
					JOptionPane.showMessageDialog(setSignalsFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError8"),
							new String[]{throatDivergingField.getText().trim()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSignalHeadFromPanel(layoutTurnout.getSignalA2Name());
					removeAssignment(throatDivergingHead);
					layoutTurnout.setSignalA2Name(throatDivergingField.getText().trim());
				}
			}
			else if (assigned!=A2) {
// need to figure out what to do in this case.			
			}
		}
		else if (throatDivergingHead==null) {
			removeSignalHeadFromPanel(layoutTurnout.getSignalA2Name());
			layoutTurnout.setSignalA2Name("");
		}
		if (setContinuing.isSelected()) {
			if (isHeadOnPanel(continuingHead) && 
				(continuingHead!=getHeadFromName(layoutTurnout.getSignalB1Name()))) {
				JOptionPane.showMessageDialog(setSignalsFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new String[]{continuingField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!layoutTurnoutHorizontal) && (!layoutTurnoutVertical) ) {
				JOptionPane.showMessageDialog(setSignalsFrame,
					rb.getString("InfoMessage2"),"",JOptionPane.INFORMATION_MESSAGE);
				if (continuingHead!=getHeadFromName(layoutTurnout.getSignalB1Name())) {
					removeSignalHeadFromPanel(layoutTurnout.getSignalB1Name());
					removeAssignment(continuingHead);
					layoutTurnout.setSignalB1Name(continuingField.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(layoutTurnout.getSignalB1Name());
				placeContinuing();
				removeAssignment(continuingHead);
				layoutTurnout.setSignalB1Name(continuingField.getText().trim());
				needRedraw = true;
			}		
		}
		else {
			int assigned = isHeadAssignedHere(continuingHead);
			if (assigned == NONE) {
				if (isHeadOnPanel(continuingHead)  && 
									isHeadAssignedAnywhere(continuingHead) ) {
					JOptionPane.showMessageDialog(setSignalsFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError8"),
							new String[]{continuingField.getText().trim()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSignalHeadFromPanel(layoutTurnout.getSignalB1Name());
					removeAssignment(continuingHead);
					layoutTurnout.setSignalB1Name(continuingField.getText().trim());
				}
			}
			else if (assigned!=B1) {
// need to figure out what to do in this case.			
			}
		}
		if (setDiverging.isSelected()) {
			if (isHeadOnPanel(divergingHead) && 
				(divergingHead!=getHeadFromName(layoutTurnout.getSignalC1Name()))) {
				JOptionPane.showMessageDialog(setSignalsFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new String[]{divergingField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!layoutTurnoutHorizontal) && (!layoutTurnoutVertical) ) {
				JOptionPane.showMessageDialog(setSignalsFrame,
					rb.getString("InfoMessage2"),"",JOptionPane.INFORMATION_MESSAGE);
				if (divergingHead!=getHeadFromName(layoutTurnout.getSignalC1Name())) {
					removeSignalHeadFromPanel(layoutTurnout.getSignalC1Name());
					removeAssignment(divergingHead);
					layoutTurnout.setSignalC1Name(divergingField.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(layoutTurnout.getSignalC1Name());
				placeDiverging();
				removeAssignment(divergingHead);
				layoutTurnout.setSignalC1Name(divergingField.getText().trim());
				needRedraw = true;
			}		
		}
		else {
			int assigned = isHeadAssignedHere(divergingHead);
			if (assigned == NONE) {
				if (isHeadOnPanel(divergingHead) && 
									isHeadAssignedAnywhere(divergingHead) ) {
					JOptionPane.showMessageDialog(setSignalsFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError8"),
							new String[]{divergingField.getText().trim()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSignalHeadFromPanel(layoutTurnout.getSignalC1Name());
					removeAssignment(divergingHead);
					layoutTurnout.setSignalC1Name(divergingField.getText().trim());
				}
			}
			else if (assigned!=C1) {
// need to figure out what to do in this case.			
			}
		}
		// setup Logic if requested and enough information is available
		if (setupLogicThroatContinuing.isSelected()) {
			setLogicThroatContinuing();
		}
		if ( (throatDivergingHead!=null) && setupLogicThroatDiverging.isSelected() ) {
			setLogicThroatDiverging();
		}
		if (setupLogicContinuing.isSelected()) {
			setLogicContinuing();
		}
		if ( setupLogicDiverging.isSelected() ) {
			setLogicDiverging();
		}		
		// finish up
		setSignalsOpen = false;
		turnoutFromMenu = false;
		setSignalsFrame.setVisible(false);
		if (needRedraw) {
			layoutEditor.redrawPanel();
			needRedraw = false;
			layoutEditor.setDirty();
		}
	}
	private boolean getTurnoutInformation(boolean doubleCrossover) {
		LayoutTurnout t = null;
		String str = "";
		if ( (!turnoutFromMenu && !doubleCrossover) ||
					(!xoverFromMenu && doubleCrossover) ) {
			turnout = null;
			layoutTurnout = null;
			if (!doubleCrossover) str = turnoutNameField.getText().trim();
			else str = xoverTurnoutNameField.getText().trim();
			if ( (str==null) || (str.equals("")) ) {
				JOptionPane.showMessageDialog(setSignalsFrame,rb.getString("SignalsError1"),
									rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return false;
			}
			turnout = jmri.InstanceManager.turnoutManagerInstance().getTurnout(str);
			if (turnout==null) {
				JOptionPane.showMessageDialog(setSignalsFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError2"),
						new String[]{str}), rb.getString("Error"),
							JOptionPane.ERROR_MESSAGE);
				return false ;
			}
			else if ( (turnout.getUserName()==null) || (turnout.getUserName()=="") ||
									!turnout.getUserName().equals(str) ) {
				str = str.toUpperCase();
				if (!doubleCrossover) 
					turnoutNameField.setText(str);
				else
					xoverTurnoutNameField.setText(str);
			}
			for (int i=0;i<layoutEditor.turnoutList.size();i++) {
				t = (LayoutTurnout)layoutEditor.turnoutList.get(i);
				if (t.getTurnout() == turnout) {
					layoutTurnout = t;
					if ( (t.getTurnoutType()==LayoutTurnout.DOUBLE_XOVER) &&
									(!doubleCrossover) ) {
						javax.swing.JOptionPane.showMessageDialog(layoutEditor,
								rb.getString("InfoMessage1"),"",
								javax.swing.JOptionPane.INFORMATION_MESSAGE);
						setSignalsCancelPressed(null);
						return false;
					}
					if ( (!(t.getTurnoutType()==LayoutTurnout.DOUBLE_XOVER)) &&
									(doubleCrossover) ) {
						javax.swing.JOptionPane.showMessageDialog(layoutEditor,
								rb.getString("InfoMessage8"),"",
								javax.swing.JOptionPane.INFORMATION_MESSAGE);
						setXoverSignalsCancelPressed(null);
						return false;
					}
				}
			}
		}
		else {
			t = layoutTurnout;
		}
		if (t!=null) {
			double delX = t.getCoordsA().getX() - t.getCoordsB().getX();
			double delY = t.getCoordsA().getY() - t.getCoordsB().getY();
			layoutTurnoutHorizontal = false;
			layoutTurnoutVertical = false;
			layoutTurnoutThroatLeft = false;
			layoutTurnoutThroatUp = false;
			layoutTurnoutBUp = false;
			layoutTurnoutBLeft = false;
			if (java.lang.Math.abs(delX) > 2.0*java.lang.Math.abs(delY)) {
				layoutTurnoutHorizontal = true;
				if (delX < 0.0) layoutTurnoutThroatLeft = true;
				if (t.getCoordsB().getY() < t.getCoordsC().getY())
						layoutTurnoutBUp = true;
			}
			if (java.lang.Math.abs(delY) > 2.0*java.lang.Math.abs(delX)) { 
				layoutTurnoutVertical = true;
				if (delY <0.0) layoutTurnoutThroatUp = true;
				if (t.getCoordsB().getX() < t.getCoordsC().getX())
					layoutTurnoutBLeft = true;
			}
			return true;
		}
		JOptionPane.showMessageDialog(setSignalsFrame,
				java.text.MessageFormat.format(rb.getString("SignalsError3"),
						new String[]{str}), rb.getString("Error"),
							JOptionPane.ERROR_MESSAGE);
		return false;
	}
	private boolean getTurnoutSignalHeadInformation() {
		throatContinuingHead = getSignalHeadFromEntry(throatContinuingField,true,setSignalsFrame);
		if (throatContinuingHead==null) return false;
		throatDivergingHead = getSignalHeadFromEntry(throatDivergingField,false,setSignalsFrame);
		continuingHead = getSignalHeadFromEntry(continuingField,true,setSignalsFrame);
		if (continuingHead==null) return false;
		divergingHead = getSignalHeadFromEntry(divergingField,true,setSignalsFrame);
		if (divergingHead==null) return false;
		return true;
	}
	private NamedIcon testIcon = null;
	private void placeThroatContinuing() {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		if( layoutTurnoutHorizontal && layoutTurnoutThroatLeft ) {
			setSignalHeadOnPanel(2,throatContinuingField.getText().trim(),
				(int)(layoutTurnout.getCoordsA().getX()-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsA().getY()+4) );
		}
		else if( layoutTurnoutHorizontal && (!layoutTurnoutThroatLeft) ) {
			setSignalHeadOnPanel(0,throatContinuingField.getText().trim(),
				(int)(layoutTurnout.getCoordsA().getX()),
				(int)(layoutTurnout.getCoordsA().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutVertical && layoutTurnoutThroatUp ) {
			setSignalHeadOnPanel(1,throatContinuingField.getText().trim(),
				(int)(layoutTurnout.getCoordsA().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsA().getY()-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutVertical && (!layoutTurnoutThroatUp) ) {
			setSignalHeadOnPanel(3,throatContinuingField.getText().trim(),
				(int)(layoutTurnout.getCoordsA().getX()+4),
				(int)(layoutTurnout.getCoordsA().getY()) );
		}

	}
	private void placeThroatDiverging() {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		if( layoutTurnoutHorizontal && layoutTurnoutThroatLeft ) {
			setSignalHeadOnPanel(2,throatDivergingField.getText().trim(),
				(int)(layoutTurnout.getCoordsA().getX()-4-(2*testIcon.getIconWidth())),
				(int)(layoutTurnout.getCoordsA().getY()+4) );
		}
		else if( layoutTurnoutHorizontal && (!layoutTurnoutThroatLeft) ) {
			setSignalHeadOnPanel(0,throatDivergingField.getText().trim(),
				(int)(layoutTurnout.getCoordsA().getX()+4+testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsA().getY()-4-testIcon.getIconHeight()));
		}
		else if( layoutTurnoutVertical && layoutTurnoutThroatUp ) {
			setSignalHeadOnPanel(1,throatDivergingField.getText().trim(),
				(int)(layoutTurnout.getCoordsA().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsA().getY()-4-(2*testIcon.getIconHeight())));
		}
		else if( layoutTurnoutVertical && (!layoutTurnoutThroatUp) ) {
			setSignalHeadOnPanel(3,throatDivergingField.getText().trim(),
				(int)(layoutTurnout.getCoordsA().getX()+4),
				(int)(layoutTurnout.getCoordsA().getY()+4+testIcon.getIconHeight()));
		}
	}
	private void placeContinuing() {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		if( layoutTurnoutHorizontal && layoutTurnoutThroatLeft && layoutTurnoutBUp) {
			setSignalHeadOnPanel(0,continuingField.getText().trim(),
				(int)(layoutTurnout.getCoordsB().getX()),
				(int)(layoutTurnout.getCoordsB().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutHorizontal && layoutTurnoutThroatLeft && (!layoutTurnoutBUp) ) {
			setSignalHeadOnPanel(0,continuingField.getText().trim(),
				(int)(layoutTurnout.getCoordsB().getX()),
				(int)(layoutTurnout.getCoordsB().getY()+4) );
		}
		else if( layoutTurnoutHorizontal && (!layoutTurnoutThroatLeft) && layoutTurnoutBUp ) {
			setSignalHeadOnPanel(2,continuingField.getText().trim(),
				(int)(layoutTurnout.getCoordsB().getX()-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsB().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutHorizontal && (!layoutTurnoutThroatLeft) && (!layoutTurnoutBUp) ) {
			setSignalHeadOnPanel(2,continuingField.getText().trim(),
				(int)(layoutTurnout.getCoordsB().getX()-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsB().getY()+4) );
		}
		else if( layoutTurnoutVertical && layoutTurnoutThroatUp && layoutTurnoutBLeft ) {
			setSignalHeadOnPanel(3,continuingField.getText().trim(),
				(int)(layoutTurnout.getCoordsB().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsB().getY()) );
		}
		else if( layoutTurnoutVertical && layoutTurnoutThroatUp && (!layoutTurnoutBLeft) ) {
			setSignalHeadOnPanel(3,continuingField.getText().trim(),
				(int)(layoutTurnout.getCoordsB().getX()+4),
				(int)(layoutTurnout.getCoordsB().getY()) );
		}
		else if( layoutTurnoutVertical && (!layoutTurnoutThroatUp) && layoutTurnoutBLeft ) {
			setSignalHeadOnPanel(1,continuingField.getText().trim(),
				(int)(layoutTurnout.getCoordsB().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsB().getY()-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutVertical && (!layoutTurnoutThroatUp) && (!layoutTurnoutBLeft) ) {
			setSignalHeadOnPanel(1,continuingField.getText().trim(),
				(int)(layoutTurnout.getCoordsB().getX()+4),
				(int)(layoutTurnout.getCoordsB().getY()-testIcon.getIconHeight()) );
		}
	}
	private void placeDiverging() {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		if( layoutTurnoutHorizontal && layoutTurnoutThroatLeft && layoutTurnoutBUp) {
			setSignalHeadOnPanel(0,divergingField.getText().trim(),
				(int)(layoutTurnout.getCoordsC().getX()),
				(int)(layoutTurnout.getCoordsC().getY()+4) );
		}
		else if( layoutTurnoutHorizontal && layoutTurnoutThroatLeft && (!layoutTurnoutBUp) ) {
			setSignalHeadOnPanel(0,divergingField.getText().trim(),
				(int)(layoutTurnout.getCoordsC().getX()),
				(int)(layoutTurnout.getCoordsC().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutHorizontal && (!layoutTurnoutThroatLeft) && layoutTurnoutBUp ) {
			setSignalHeadOnPanel(2,divergingField.getText().trim(),
				(int)(layoutTurnout.getCoordsC().getX()-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsC().getY()+4) );
		}
		else if( layoutTurnoutHorizontal && (!layoutTurnoutThroatLeft) && (!layoutTurnoutBUp) ) {
			setSignalHeadOnPanel(2,divergingField.getText().trim(),
				(int)(layoutTurnout.getCoordsC().getX()-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsC().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutVertical && layoutTurnoutThroatUp && layoutTurnoutBLeft ) {
			setSignalHeadOnPanel(3,divergingField.getText().trim(),
				(int)(layoutTurnout.getCoordsC().getX()+4),
				(int)(layoutTurnout.getCoordsC().getY()) );
		}
		else if( layoutTurnoutVertical && layoutTurnoutThroatUp && (!layoutTurnoutBLeft) ) {
			setSignalHeadOnPanel(3,divergingField.getText().trim(),
				(int)(layoutTurnout.getCoordsC().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsC().getY()) );
		}
		else if( layoutTurnoutVertical && (!layoutTurnoutThroatUp) && layoutTurnoutBLeft ) {
			setSignalHeadOnPanel(1,divergingField.getText().trim(),
				(int)(layoutTurnout.getCoordsC().getX()+4),
				(int)(layoutTurnout.getCoordsC().getY()-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutVertical && (!layoutTurnoutThroatUp) && (!layoutTurnoutBLeft) ) {
			setSignalHeadOnPanel(1,divergingField.getText().trim(),
				(int)(layoutTurnout.getCoordsC().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsC().getY()-testIcon.getIconHeight()) );
		}
	}
	private void setLogicThroatContinuing() {
		TrackSegment track = (TrackSegment)layoutTurnout.getConnectB();
		if (track==null) {
			JOptionPane.showMessageDialog(setSignalsFrame,
					rb.getString("InfoMessage7"),"",JOptionPane.INFORMATION_MESSAGE);			
			return;
		}
		LayoutBlock block = track.getLayoutBlock();
		if (block==null) {
			JOptionPane.showMessageDialog(setSignalsFrame,
					rb.getString("InfoMessage6"),"",JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		Sensor occupancy = block.getOccupancySensor();
		if (occupancy==null) {
			JOptionPane.showMessageDialog(setSignalsFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage4"),
					new String[]{block.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);						
			return;
		}
		SignalHead nextHead = getNextSignalFromObject(track,
													(Object)layoutTurnout);
		if ( (nextHead==null) && (!reachedEndBumper()) ) {
			JOptionPane.showMessageDialog(setSignalsFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage5"),
					new String[]{block.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (throatDivergingHead!=null) {
			if (!initializeBlockBossLogic(throatContinuingField.getText().trim())) return;
			logic.setMode(BlockBossLogic.TRAILINGMAIN);
			logic.setTurnout(turnout.getSystemName());
			logic.setSensor1(occupancy.getSystemName());
			if (nextHead!=null) {
				logic.setWatchedSignal1(nextHead.getSystemName(),false);
			}
			if (auxSignal!=null) {
				logic.setWatchedSignal1Alt(auxSignal.getSystemName());
			}
			finalizeBlockBossLogic();
			return;
		}
		SignalHead savedAuxSignal = auxSignal;
		TrackSegment track2 = (TrackSegment)layoutTurnout.getConnectC();
		if (track2==null) {
			JOptionPane.showMessageDialog(setSignalsFrame,
					rb.getString("InfoMessage7"),"",JOptionPane.INFORMATION_MESSAGE);			
			return;
		}
		LayoutBlock block2 = track2.getLayoutBlock();
		if (block2==null) {
			JOptionPane.showMessageDialog(setSignalsFrame,
					rb.getString("InfoMessage6"),"",JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		Sensor occupancy2 = block2.getOccupancySensor();
		if (occupancy2==null) {
			JOptionPane.showMessageDialog(setSignalsFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage4"),
					new String[]{block2.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);						
			return;
		}
		SignalHead nextHead2 = getNextSignalFromObject(track2,
													(Object)layoutTurnout);
		if ( (nextHead2==null) && (!reachedEndBumper()) ) {
			JOptionPane.showMessageDialog(setSignalsFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage5"),
					new String[]{block2.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (!initializeBlockBossLogic(throatContinuingField.getText().trim())) return;
		logic.setMode(BlockBossLogic.FACING);
		logic.setTurnout(turnout.getSystemName());
		logic.setWatchedSensor1(occupancy.getSystemName());
		logic.setWatchedSensor2(occupancy2.getSystemName());
		if (nextHead!=null) {
			logic.setWatchedSignal1(nextHead.getSystemName(),false);
		}
		if (savedAuxSignal!=null) {
			logic.setWatchedSignal1Alt(savedAuxSignal.getSystemName());
		}
		if (nextHead2!=null) {
			logic.setWatchedSignal2(nextHead2.getSystemName());
		}
		if (auxSignal!=null) {
			logic.setWatchedSignal2Alt(auxSignal.getSystemName());
		}		
		if (!layoutTurnout.isMainlineC())
			logic.setLimitSpeed2(true);
		finalizeBlockBossLogic();
	}
	private void setLogicThroatDiverging() {
		TrackSegment track = (TrackSegment)layoutTurnout.getConnectC();
		if (track==null) {
			JOptionPane.showMessageDialog(setSignalsFrame,
					rb.getString("InfoMessage7"),"",JOptionPane.INFORMATION_MESSAGE);			
			return;
		}
		LayoutBlock block = track.getLayoutBlock();
		if (block==null) {
			JOptionPane.showMessageDialog(setSignalsFrame,
					rb.getString("InfoMessage6"),"",JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		Sensor occupancy = block.getOccupancySensor();
		if (occupancy==null) {
			JOptionPane.showMessageDialog(setSignalsFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage4"),
					new String[]{block.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);						
			return;
		}
		SignalHead nextHead = getNextSignalFromObject(track,
													(Object)layoutTurnout);
		if ( (nextHead==null) && (!reachedEndBumper()) ) {
			JOptionPane.showMessageDialog(setSignalsFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage5"),
					new String[]{block.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (!initializeBlockBossLogic(throatDivergingField.getText().trim())) return;
		logic.setMode(BlockBossLogic.TRAILINGDIVERGING);
		logic.setTurnout(turnout.getSystemName());
		logic.setSensor1(occupancy.getSystemName());
		logic.setWatchedSignal1(nextHead.getSystemName(),false);
		if (auxSignal!=null) {
			logic.setWatchedSignal1Alt(auxSignal.getSystemName());
		}
		if (!layoutTurnout.isMainlineC())
			logic.setLimitSpeed2(true);
		finalizeBlockBossLogic();			
	}
	private void setLogicContinuing() {
		TrackSegment track = (TrackSegment)layoutTurnout.getConnectA();
		if (track==null) {
			JOptionPane.showMessageDialog(setSignalsFrame,
					rb.getString("InfoMessage7"),"",JOptionPane.INFORMATION_MESSAGE);			
			return;
		}
		LayoutBlock block = track.getLayoutBlock();
		if (block==null) {
			JOptionPane.showMessageDialog(setSignalsFrame,
					rb.getString("InfoMessage6"),"",JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		Sensor occupancy = block.getOccupancySensor();
		if (occupancy==null) {
			JOptionPane.showMessageDialog(setSignalsFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage4"),
					new String[]{block.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);						
			return;
		}
		SignalHead nextHead = getNextSignalFromObject(track,
													(Object)layoutTurnout);
		if ( (nextHead==null) && (!reachedEndBumper()) ) {
			JOptionPane.showMessageDialog(setSignalsFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage5"),
					new String[]{block.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (!initializeBlockBossLogic(continuingField.getText().trim())) return;
		logic.setMode(BlockBossLogic.TRAILINGMAIN);
		logic.setTurnout(turnout.getSystemName());
		logic.setSensor1(occupancy.getSystemName());
		logic.setWatchedSignal1(nextHead.getSystemName(),false);
		if (auxSignal!=null) {
			logic.setWatchedSignal1Alt(auxSignal.getSystemName());
		}
		finalizeBlockBossLogic();	
	}
	private void setLogicDiverging() {
		TrackSegment track = (TrackSegment)layoutTurnout.getConnectA();
		if (track==null) {
			JOptionPane.showMessageDialog(setSignalsFrame,
					rb.getString("InfoMessage7"),"",JOptionPane.INFORMATION_MESSAGE);			
			return;
		}
		LayoutBlock block = track.getLayoutBlock();
		if (block==null) {
			JOptionPane.showMessageDialog(setSignalsFrame,
					rb.getString("InfoMessage6"),"",JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		Sensor occupancy = block.getOccupancySensor();
		if (occupancy==null) {
			JOptionPane.showMessageDialog(setSignalsFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage4"),
					new String[]{block.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);						
			return;
		}
		SignalHead nextHead = getNextSignalFromObject(track,
													(Object)layoutTurnout);
		if ( (nextHead==null) && (!reachedEndBumper()) ) {
			JOptionPane.showMessageDialog(setSignalsFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage5"),
					new String[]{block.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (!initializeBlockBossLogic(divergingField.getText().trim())) return;
		logic.setMode(BlockBossLogic.TRAILINGDIVERGING);
		logic.setTurnout(turnout.getSystemName());
		logic.setSensor1(occupancy.getSystemName());
		if (nextHead!=null) {
			logic.setWatchedSignal1(nextHead.getSystemName(),false);
		}
		if (auxSignal!=null) {
			logic.setWatchedSignal1Alt(auxSignal.getSystemName());
		}
		if (!layoutTurnout.isMainlineC())
			logic.setLimitSpeed2(true);
		finalizeBlockBossLogic();		
	}
	
	/** 
	 * Utility routines used by multiple tools
	 */
	 
	/**
	 * Returns the SignalHead corresponding to an entry field in the specified dialog.
	 *		This also takes care of UpperCase and trimming of leading and trailing blanks.
	 *		If entry is required, and no entry is present, and error message is sent.
	 *      An error message also results if a signal head with the entered name is not
	 *      found in the SignalTable.
	 */
	public SignalHead getSignalHeadFromEntry(JTextField signalName,boolean requireEntry,
					JmriJFrame frame) {
		String str = signalName.getText().trim();
		if ( (str==null) || (str.equals("")) ) {
			if (requireEntry) {
				JOptionPane.showMessageDialog(frame,rb.getString("SignalsError5"),
									rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
			}
			return null;
		}
		SignalHead head = jmri.InstanceManager.signalHeadManagerInstance().getSignalHead(str);
		if (head==null) {
			JOptionPane.showMessageDialog(frame,
					java.text.MessageFormat.format(rb.getString("SignalsError4"),
						new String[]{str}), rb.getString("Error"),
							JOptionPane.ERROR_MESSAGE);
			return null ;
		}
		else if ( (head.getUserName()==null) || (head.getUserName()=="") ||
									!head.getUserName().equals(str) ) {
			str = str.toUpperCase();
			signalName.setText(str);
		}
		return (head);
	}
	/**
	 * Returns a SignalHead given a name
	 */
	public SignalHead getHeadFromName(String str) {
		if ( (str==null) || (str.equals("")) ) {
			return null;
		}
		return  (jmri.InstanceManager.signalHeadManagerInstance().getSignalHead(str));
	}
	/**
	 * Places a signal head icon on the panel after rotation at the designated place, with all
	 *		with all icons taken care of.
	 */
	public void setSignalHeadOnPanel(int rotation, String headName,
					int xLoc, int yLoc) {
		LayoutSignalHeadIcon l = new LayoutSignalHeadIcon();
        l.setRedIcon(signalIconEditor.getIcon(0));
        l.setFlashRedIcon(signalIconEditor.getIcon(1));
        l.setYellowIcon(signalIconEditor.getIcon(2));
        l.setFlashYellowIcon(signalIconEditor.getIcon(3));
        l.setGreenIcon(signalIconEditor.getIcon(4));
        l.setFlashGreenIcon(signalIconEditor.getIcon(5));
        l.setDarkIcon(signalIconEditor.getIcon(6));
        l.setHeldIcon(signalIconEditor.getIcon(7));
		l.setSignalHead(headName);
		l.setLocation(xLoc,yLoc);
		if (rotation>0) {
			l.getRedIcon().setRotation(rotation,l);
			l.getFlashRedIcon().setRotation(rotation,l);
			l.getYellowIcon().setRotation(rotation,l);
			l.getFlashYellowIcon().setRotation(rotation,l);
			l.getGreenIcon().setRotation(rotation,l);
			l.getFlashGreenIcon().setRotation(rotation,l);
			l.getDarkIcon().setRotation(rotation,l);
			l.getHeldIcon().setRotation(rotation,l);
		}		
		layoutEditor.putSignal(l);
	}
	/** 
	 * Returns an index if the specified signal head is assigned to the
	 *		LayoutTurnout initialized. Otherwise returns the NONE index.
	 *		The index specifies the turnout position of the signal head
	 *		according to the code listed at the beginning of this module.
	 */
	private int isHeadAssignedHere(SignalHead head) {
		String sysName = head.getSystemName();
		String uName = head.getUserName();
		String name = layoutTurnout.getSignalA1Name();
		if ( (name!=null) && (name.length()>0) && ((name.equals(uName)) || 
						(name.equals(sysName))) ) return A1;
		name = layoutTurnout.getSignalA2Name();
		if ( (name!=null) && (name.length()>0) && ((name.equals(uName)) || 
						(name.equals(sysName))) ) return A2;
		name = layoutTurnout.getSignalB1Name();
		if ( (name!=null) && (name.length()>0) && ((name.equals(uName)) || 
						(name.equals(sysName))) ) return B1;
		name = layoutTurnout.getSignalB2Name();
		if ( (name!=null) && (name.length()>0) && ((name.equals(uName)) || 
						(name.equals(sysName))) ) return B2;
		name = layoutTurnout.getSignalC1Name();
		if ( (name!=null) && (name.length()>0) && ((name.equals(uName)) || 
						(name.equals(sysName))) ) return C1;
		name = layoutTurnout.getSignalC2Name();
		if ( (name!=null) && (name.length()>0) && ((name.equals(uName)) || 
						(name.equals(sysName))) ) return C2;
		name = layoutTurnout.getSignalD1Name();
		if ( (name!=null) && (name.length()>0) && ((name.equals(uName)) || 
						(name.equals(sysName))) ) return D1;
		name = layoutTurnout.getSignalD2Name();
		if ( (name!=null) && (name.length()>0) && ((name.equals(uName)) || 
						(name.equals(sysName))) ) return D2;
		return NONE;
	}
	/**
	 * Returns true if an icon for the specified SignalHead is on the panel
	 */
	public boolean isHeadOnPanel(SignalHead head) 
	{
		LayoutSignalHeadIcon h = null;
		for (int i=0;i<layoutEditor.signalList.size();i++) {
			h = (LayoutSignalHeadIcon)layoutEditor.signalList.get(i);
			if (h.getSignalHead() == head) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Returns true if the specified Signal Head is assigned to an object
	 *		on the panel, regardless of whether an icon is displayed or not
	 */
	public boolean isHeadAssignedAnywhere(SignalHead head) 
	{
		String sName = head.getSystemName();
		String uName = head.getUserName();
		for (int i=0;i<layoutEditor.turnoutList.size();i++) {
			LayoutTurnout to = (LayoutTurnout)layoutEditor.turnoutList.get(i);
			if ((to.getSignalA1Name()!=null) &&
					(to.getSignalA1Name().equals(sName) || ((uName!=null) && 
					(to.getSignalA1Name().equals(uName))))) return true;
			if ((to.getSignalA2Name()!=null) &&
					(to.getSignalA2Name().equals(sName) || ((uName!=null) && 
					(to.getSignalA2Name().equals(uName))))) return true;
			if ((to.getSignalB1Name()!=null) &&
					(to.getSignalB1Name().equals(sName) || ((uName!=null) && 
					(to.getSignalB1Name().equals(uName))))) return true;
			if ((to.getSignalB2Name()!=null) &&
					(to.getSignalB2Name().equals(sName) || ((uName!=null) && 
					(to.getSignalB2Name().equals(uName))))) return true;
			if ((to.getSignalC1Name()!=null) &&
					(to.getSignalC1Name().equals(sName) || ((uName!=null) && 
					(to.getSignalC1Name().equals(uName))))) return true;
			if ((to.getSignalC2Name()!=null) &&
					(to.getSignalC2Name().equals(sName) || ((uName!=null) && 
					(to.getSignalC2Name().equals(uName))))) return true;
			if ((to.getSignalD1Name()!=null) &&
					(to.getSignalD1Name().equals(sName) || ((uName!=null) && 
					(to.getSignalD1Name().equals(uName))))) return true;
			if ((to.getSignalD2Name()!=null) &&
					(to.getSignalD2Name().equals(sName) || ((uName!=null) && 
					(to.getSignalD2Name().equals(uName))))) return true;
		}
		for (int i=0;i<layoutEditor.pointList.size();i++) {
			PositionablePoint po = (PositionablePoint)layoutEditor.pointList.get(i);
			if ((po.getEastBoundSignal()!=null) &&
					(po.getEastBoundSignal().equals(sName) || ((uName!=null) && 
					(po.getEastBoundSignal().equals(uName))))) return true;
			if ((po.getWestBoundSignal()!=null) &&
					(po.getWestBoundSignal().equals(sName) || ((uName!=null) && 
					(po.getWestBoundSignal().equals(uName))))) return true;
		}
		for (int i=0;i<layoutEditor.xingList.size();i++) {
			LevelXing x = (LevelXing)layoutEditor.xingList.get(i);
			if ((x.getSignalAName()!=null) &&
					(x.getSignalAName().equals(sName) || ((uName!=null) && 
					(x.getSignalAName().equals(uName))))) return true;
			if ((x.getSignalBName()!=null) &&
					(x.getSignalBName().equals(sName) || ((uName!=null) && 
					(x.getSignalBName().equals(uName))))) return true;
			if ((x.getSignalCName()!=null) &&
					(x.getSignalCName().equals(sName) || ((uName!=null) && 
					(x.getSignalCName().equals(uName))))) return true;
			if ((x.getSignalDName()!=null) &&
					(x.getSignalDName().equals(sName) || ((uName!=null) && 
					(x.getSignalDName().equals(uName))))) return true;
		}
		return false;
	}
	/**
	 * Removes the assignment of the specified SignalHead to either a turnout, 
	 *		a positionable point, or a level crossing wherever it is assigned
	 */
	public void removeAssignment(SignalHead head) 
	{
		String sName = head.getSystemName();
		String uName = head.getUserName();
		for (int i=0;i<layoutEditor.turnoutList.size();i++) {
			LayoutTurnout to = (LayoutTurnout)layoutEditor.turnoutList.get(i);
			if ((to.getSignalA1Name()!=null) &&
					(to.getSignalA1Name().equals(sName) || ((uName!=null) && 
					(to.getSignalA1Name().equals(uName))))) to.setSignalA1Name("");
			if ((to.getSignalA2Name()!=null) &&
					(to.getSignalA2Name().equals(sName) || ((uName!=null) && 
					(to.getSignalA2Name().equals(uName))))) to.setSignalA2Name("");
			if ((to.getSignalB1Name()!=null) &&
					(to.getSignalB1Name().equals(sName) || ((uName!=null) && 
					(to.getSignalB1Name().equals(uName))))) to.setSignalB1Name("");
			if ((to.getSignalB2Name()!=null) &&
					(to.getSignalB2Name().equals(sName) || ((uName!=null) && 
					(to.getSignalB2Name().equals(uName))))) to.setSignalB2Name("");
			if ((to.getSignalC1Name()!=null) &&
					(to.getSignalC1Name().equals(sName) || ((uName!=null) && 
					(to.getSignalC1Name().equals(uName))))) to.setSignalC1Name("");
			if ((to.getSignalC2Name()!=null) &&
					(to.getSignalC2Name().equals(sName) || ((uName!=null) && 
					(to.getSignalC2Name().equals(uName))))) to.setSignalC2Name("");
			if ((to.getSignalD1Name()!=null) &&
					(to.getSignalD1Name().equals(sName) || ((uName!=null) && 
					(to.getSignalD1Name().equals(uName))))) to.setSignalD1Name("");
			if ((to.getSignalD2Name()!=null) &&
					(to.getSignalD2Name().equals(sName) || ((uName!=null) && 
					(to.getSignalD2Name().equals(uName))))) to.setSignalD2Name("");
		}
		for (int i=0;i<layoutEditor.pointList.size();i++) {
			PositionablePoint po = (PositionablePoint)layoutEditor.pointList.get(i);
			if ((po.getEastBoundSignal()!=null) &&
					(po.getEastBoundSignal().equals(sName) || ((uName!=null) && 
					(po.getEastBoundSignal().equals(uName))))) 
				po.setEastBoundSignal("");
			if ((po.getWestBoundSignal()!=null) &&
					(po.getWestBoundSignal().equals(sName) || ((uName!=null) && 
					(po.getWestBoundSignal().equals(uName))))) 
				po.setWestBoundSignal("");
		}
		for (int i=0;i<layoutEditor.xingList.size();i++) {
			LevelXing x = (LevelXing)layoutEditor.xingList.get(i);
			if ((x.getSignalAName()!=null) &&
					(x.getSignalAName().equals(sName) || ((uName!=null) && 
					(x.getSignalAName().equals(uName))))) x.setSignalAName("");
			if ((x.getSignalBName()!=null) &&
					(x.getSignalBName().equals(sName) || ((uName!=null) && 
					(x.getSignalBName().equals(uName))))) x.setSignalBName("");
			if ((x.getSignalCName()!=null) &&
					(x.getSignalCName().equals(sName) || ((uName!=null) && 
					(x.getSignalCName().equals(uName))))) x.setSignalCName("");
			if ((x.getSignalDName()!=null) &&
					(x.getSignalDName().equals(sName) || ((uName!=null) && 
					(x.getSignalDName().equals(uName))))) x.setSignalDName("");
		}
	}
	/**
	 * Removes the SignalHead with the specified name from the panel and from
	 *		assignment to any turnout, positionable point, or level crossing
	 */
	public void removeSignalHeadFromPanel(String signalName) {
		if ( (signalName==null) || (signalName.length()<1) ) return;
		SignalHead head = jmri.InstanceManager.signalHeadManagerInstance().
														getSignalHead(signalName);
		removeAssignment(head);
		LayoutSignalHeadIcon h = null;
		int index = -1;
		for (int i=0;(i<layoutEditor.signalList.size())&&(index==-1);i++) {
			h = (LayoutSignalHeadIcon)layoutEditor.signalList.get(i);
			if (h.getSignalHead() == head) {
				index = i;
			}
		}
		if (index!=(-1)) {
			layoutEditor.signalList.remove(index);
			h.remove();
			h.dispose();
			needRedraw = true;
		}
	}
	/* 
	 * Initializes a BlockBossLogic for creation of a signal logic for the signal
	 *		head named in "signalHeadName".
	 * Should not be called until enough informmation has been gathered to allow
	 *		configuration of the Simple Signal Logic.
	 */
	public boolean initializeBlockBossLogic(String signalHeadName) {
		logic = BlockBossLogic.getStoppedObject(signalHeadName);
		if (logic==null) {
			log.error("Trouble creating BlockBossLogic for '"+signalHeadName+"'.");
			return false;
		}
		return true;
	}
	/* 
	 * Finalizes a successfully created signal logic 
	 */
	public void finalizeBlockBossLogic() {
		if (logic==null) return;
		logic.retain();
		logic.start();
		logic = null;
	}
	/*
	 * Returns the signal head at the end of the block "track" is assigned to.
	 *		"track" is the Track Segment leaving "object".
	 *		"object" must be either an anchor point or one of the connecting
	 *			points of a turnout or level crossing.
	 * Note: returns 'null' is signal is not present where it is expected, or
	 *		if an End Bumper is reached. To test for end bumper, use the 
	 *      associated routine "reachedEndBumper()".
	 */		
	public SignalHead getNextSignalFromObject(TrackSegment track, Object object) {
		hitEndBumper = false;
		auxSignal = null;
		TrackSegment t = track;
		Object obj = object;
		boolean inBlock = true;
		int type = 0;
		Object connect = null;
		while (inBlock) {
			if (t.getConnect1()==obj) {
				type = t.getType2();
				connect = (Object)t.getConnect2();
			}
			else {
				type = t.getType1();
				connect = (Object)t.getConnect1();
			}
			if (type==layoutEditor.POS_POINT) {
				PositionablePoint p = (PositionablePoint)connect;
				if (p.getType()==PositionablePoint.END_BUMPER) {
					hitEndBumper = true;
					return null;
				}
				if (p.getConnect1()==t)
					t=p.getConnect2();
				else
					t=p.getConnect1();
				if (t==null) return null;
				if (track.getLayoutBlock()!=t.getLayoutBlock()) {
					// p is a block boundary - should be signalled
					String signalName;
					if (isAtWestEndOfAnchor(t,p)) 
						signalName = p.getWestBoundSignal();
					else signalName = p.getEastBoundSignal();
					if ((signalName==null)||(signalName.equals(""))) return null;
					return jmri.InstanceManager.signalHeadManagerInstance().
									getSignalHead(signalName);
				}
				obj = (Object)p;
			}
			else if (type==layoutEditor.TURNOUT_A) {
				// Reached turnout throat, should be signalled
				LayoutTurnout to = (LayoutTurnout)connect;
				String signalName = to.getSignalA2Name();
				if ((!(signalName==null))&&(!(signalName.equals("")))) 
					auxSignal = jmri.InstanceManager.signalHeadManagerInstance().
									getSignalHead(signalName);
				signalName = to.getSignalA1Name();
				if ((signalName==null)||(signalName.equals(""))) return null;
				return jmri.InstanceManager.signalHeadManagerInstance().
									getSignalHead(signalName);
			}
			else if (type==layoutEditor.TURNOUT_B) {
				// Reached turnout continuing, should be signalled
				LayoutTurnout to = (LayoutTurnout)connect;
				String signalName = to.getSignalB2Name();
				if ((!(signalName==null))&&(!(signalName.equals("")))) 
					auxSignal = jmri.InstanceManager.signalHeadManagerInstance().
									getSignalHead(signalName);
				signalName = to.getSignalB1Name();
				if ((signalName==null)||(signalName.equals(""))) return null;
				return jmri.InstanceManager.signalHeadManagerInstance().
									getSignalHead(signalName);
			}
			else if (type==layoutEditor.TURNOUT_C) {
				// Reached turnout diverging, should be signalled
				LayoutTurnout to = (LayoutTurnout)connect;
				String signalName = to.getSignalC2Name();
				if ((!(signalName==null))&&(!(signalName.equals("")))) 
					auxSignal = jmri.InstanceManager.signalHeadManagerInstance().
									getSignalHead(signalName);
				signalName = to.getSignalC1Name();
				if ((signalName==null)||(signalName.equals(""))) return null;
				return jmri.InstanceManager.signalHeadManagerInstance().
									getSignalHead(signalName);
			}
			else if (type==layoutEditor.TURNOUT_D) {
				// Reached turnout xover 4, should be signalled
				LayoutTurnout to = (LayoutTurnout)connect;
				String signalName = to.getSignalD2Name();
				if ((!(signalName==null))&&(!(signalName.equals("")))) 
					auxSignal = jmri.InstanceManager.signalHeadManagerInstance().
									getSignalHead(signalName);
				signalName = to.getSignalD1Name();
				if ((signalName==null)||(signalName.equals(""))) return null;
				return jmri.InstanceManager.signalHeadManagerInstance().
									getSignalHead(signalName);
			}
			else if (type==layoutEditor.LEVEL_XING_A) {
				// Reached level crossing that may or may not be a block boundary
				LevelXing x = (LevelXing)connect;
				String signalName = x.getSignalAName();
				if ((signalName!=null)&&(!signalName.equals(""))) 
					return jmri.InstanceManager.signalHeadManagerInstance().
									getSignalHead(signalName);
				t=(TrackSegment)x.getConnectC();
				if (t==null) return null;
				if (track.getLayoutBlock()!=t.getLayoutBlock()) return null;
				obj = (Object)x;				
			}
			else if (type==layoutEditor.LEVEL_XING_B) {
				// Reached level crossing that may or may not be a block boundary
				LevelXing x = (LevelXing)connect;
				String signalName = x.getSignalBName();
				if ((signalName!=null)&&(!signalName.equals(""))) 
					return jmri.InstanceManager.signalHeadManagerInstance().
									getSignalHead(signalName);
				t=(TrackSegment)x.getConnectD();
				if (t==null) return null;
				if (track.getLayoutBlock()!=t.getLayoutBlock()) return null;
				obj = (Object)x;				
			}
			else if (type==layoutEditor.LEVEL_XING_C) {
				// Reached level crossing that may or may not be a block boundary
				LevelXing x = (LevelXing)connect;
				String signalName = x.getSignalCName();
				if ((signalName!=null)&&(!signalName.equals(""))) 
					return jmri.InstanceManager.signalHeadManagerInstance().
									getSignalHead(signalName);
				t=(TrackSegment)x.getConnectA();
				if (t==null) return null;
				if (track.getLayoutBlock()!=t.getLayoutBlock()) return null;
				obj = (Object)x;				
			}
			else if (type==layoutEditor.LEVEL_XING_D) {
				// Reached level crossing that may or may not be a block boundary
				LevelXing x = (LevelXing)connect;
				String signalName = x.getSignalDName();
				if ((signalName!=null)&&(!signalName.equals(""))) 
					return jmri.InstanceManager.signalHeadManagerInstance().
									getSignalHead(signalName);
				t=(TrackSegment)x.getConnectB();
				if (t==null) return null;
				if (track.getLayoutBlock()!=t.getLayoutBlock()) return null;
				obj = (Object)x;				
			}
		}
		return null;
	}
	private boolean hitEndBumper = false;
	/*
	 * Returns 'true' if an end bumper was reached during the last call to 
	 *		GetNextSignalFromObject.
	 */
	public boolean reachedEndBumper() {return hitEndBumper;}
	/* 
	 * Returns 'true' if "track" enters a block boundary at the west(north) end of
	 *		"point". Returns "false" otherwise.
	 *	"track" is a TrackSegment connected to "point".
	 *  "point" is an anchor point serving as a block boundary.
	 */
	public boolean isAtWestEndOfAnchor(TrackSegment t, PositionablePoint p) {
		TrackSegment tx = null;
		if (p.getConnect1()==t)
			tx = p.getConnect2();
		else if (p.getConnect2()==t)
			tx = p.getConnect1();
		else {
			log.error("track not connected to anchor point");
			return false;
		}
		Point2D point1;
		if (t.getConnect1()==(Object)p) 
			point1 = layoutEditor.getCoords(t.getConnect2(),t.getType2());
		else 
			point1 = layoutEditor.getCoords(t.getConnect1(),t.getType1());
		Point2D point2;
		if (tx.getConnect1()==(Object)p) 
			point2 = layoutEditor.getCoords(tx.getConnect2(),tx.getType2());
		else 
			point2 = layoutEditor.getCoords(tx.getConnect1(),tx.getType1());
		double delX = point1.getX() - point2.getX();
		double delY = point1.getY() - point2.getY();
		if (java.lang.Math.abs(delX) > 2.0*java.lang.Math.abs(delY)) {
			// track is Horizontal
			if (delX>0.0) return false;
			else return true;
		}
		else if(java.lang.Math.abs(delY) > 2.0*java.lang.Math.abs(delX)) {
			// track is Vertical
			if (delY>0.0) return false;
			else return true;
		}
		// track is not vertical or horizontal
		log.error ("Track is not vertical or horizontal at anchor");
		return false;		
	}

	/** 
	 * Tool to set signals at a block boundary, including placing the signal icons and 
	 *		setup of Simple Signal Logic for each signal head
	 * <P>
	 * Block boundary must be at an Anchor Point on the LayoutEditor panel.
	 */
	// operational variables for Set Signals at Block Boundary tool
	private JmriJFrame setSignalsAtBoundaryFrame = null;
	private boolean setSignalsAtBoundaryOpen = false;
	private JTextField block1NameField = new JTextField(16);
	private JTextField block2NameField = new JTextField(16);
	private JTextField eastBoundField = new JTextField(16);
	private JTextField westBoundField = new JTextField(16);
	private JCheckBox setEastBound = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupLogicEastBound = new JCheckBox(rb.getString("SetLogic"));
	private JCheckBox setWestBound = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupLogicWestBound = new JCheckBox(rb.getString("SetLogic"));
	private JButton getAnchorSavedSignalHeads = null;
	private JButton changeSignalAtBoundaryIcon = null;
	private JButton setSignalsAtBoundaryDone = null;
	private JButton setSignalsAtBoundaryCancel = null;
	private LayoutBlock block1 = null;
	private LayoutBlock block2 = null;
	private TrackSegment eastTrack = null;
	private TrackSegment westTrack = null;
	private boolean trackHorizontal = false;
	private boolean trackVertical = false;
	private boolean boundaryFromMenu = false;
	private PositionablePoint boundary = null;
	private SignalHead eastBoundHead = null;
	private SignalHead westBoundHead = null;

	// display dialog for Set Signals at Block Boundary tool
	public void setSignalsAtBlockBoundaryFromMenu( PositionablePoint p, MultiIconEditor theEditor, 
					JFrame theFrame ) {
		boundaryFromMenu = true;
		boundary = p;
		block1NameField.setText(boundary.getConnect1().getLayoutBlock().getID());
		block2NameField.setText(boundary.getConnect2().getLayoutBlock().getID());
		setSignalsAtBlockBoundary(theEditor,theFrame);
		return;
	}		
		
	public void setSignalsAtBlockBoundary( MultiIconEditor theEditor, JFrame theFrame ) {
		signalIconEditor = theEditor;
		signalFrame = theFrame;
		if (setSignalsAtBoundaryOpen) {
			setSignalsAtBoundaryFrame.setVisible(true);
			return;
		}
		// Initialize if needed
		if (setSignalsAtBoundaryFrame == null) {
            setSignalsAtBoundaryFrame = new JmriJFrame( rb.getString("SignalsAtBoundary") );
            setSignalsAtBoundaryFrame.addHelpMenu("package.jmri.jmrit.display.SetSignalsAtBoundary", true);
            setSignalsAtBoundaryFrame.setLocation(70,30);
            Container theContentPane = setSignalsAtBoundaryFrame.getContentPane();  
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));
			JPanel panel11 = new JPanel(); 
            panel11.setLayout(new FlowLayout());
			if (boundaryFromMenu) {
				JLabel block1NameLabel = new JLabel( rb.getString("Block")+" 1 "+
					rb.getString("Name")+" : "+boundary.getConnect1().getLayoutBlock().getID());
				panel11.add(block1NameLabel);			
			}
			else {
				JLabel block1NameLabel = new JLabel( rb.getString("Block")+" 1 "+
																rb.getString("Name")+" : ");
				panel11.add(block1NameLabel);
				panel11.add(block1NameField);
				block1NameField.setToolTipText(rb.getString("SignalsBlockNameHint"));
			}
            theContentPane.add(panel11);
			JPanel panel12 = new JPanel(); 
            panel12.setLayout(new FlowLayout());
			if (boundaryFromMenu) {
				JLabel block2NameLabel = new JLabel( rb.getString("Block")+" 2 "+
					rb.getString("Name")+" : "+boundary.getConnect2().getLayoutBlock().getID());
				panel12.add(block2NameLabel);			
			}
			else {
				JLabel block2NameLabel = new JLabel( rb.getString("Block")+" 2 "+
																rb.getString("Name")+" : ");
				panel12.add(block2NameLabel);
				panel12.add(block2NameField);
				block2NameField.setToolTipText(rb.getString("SignalsBlockNameHint"));
			}
            theContentPane.add(panel12);
			theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
			JLabel shTitle = new JLabel(rb.getString("SignalHeads"));
			panel2.add(shTitle);
			panel2.add(new JLabel("   "));
            panel2.add(getAnchorSavedSignalHeads = new JButton(rb.getString("GetSaved")));
            getAnchorSavedSignalHeads.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						getSavedAnchorSignals(e);
					}
				});
            getAnchorSavedSignalHeads.setToolTipText( rb.getString("GetSavedHint") );			
			theContentPane.add(panel2);
            JPanel panel21 = new JPanel();
            panel21.setLayout(new FlowLayout());
			JLabel eastBoundLabel = new JLabel(rb.getString("East/SouthBound")+" : ");
			panel21.add(eastBoundLabel);
			panel21.add(eastBoundField);
			theContentPane.add(panel21);
			eastBoundField.setToolTipText(rb.getString("SignalHeadEastNameHint"));
            JPanel panel22 = new JPanel();
            panel22.setLayout(new FlowLayout());
			panel22.add(new JLabel("   "));
			panel22.add(setEastBound);
			setEastBound.setToolTipText(rb.getString("AnchorPlaceHeadHint"));
			panel22.add(new JLabel("  "));
			panel22.add(setupLogicEastBound);
			setupLogicEastBound.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel22);
            JPanel panel31 = new JPanel();
            panel31.setLayout(new FlowLayout());
			JLabel westBoundLabel = new JLabel(rb.getString("West/NorthBound")+" : ");
			panel31.add(westBoundLabel);
			panel31.add(westBoundField);
			theContentPane.add(panel31);
			westBoundField.setToolTipText(rb.getString("SignalHeadWestNameHint"));
            JPanel panel32 = new JPanel();
            panel32.setLayout(new FlowLayout());
			panel32.add(new JLabel("   "));
			panel32.add(setWestBound);
			setWestBound.setToolTipText(rb.getString("AnchorPlaceHeadHint"));
			panel32.add(new JLabel("  "));
			panel32.add(setupLogicWestBound);
			setupLogicWestBound.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel32);			
			theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
            JPanel panel6 = new JPanel();
            panel6.setLayout(new FlowLayout());
            panel6.add(changeSignalAtBoundaryIcon = new JButton(rb.getString("ChangeSignalIcon")));
            changeSignalAtBoundaryIcon.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						signalFrame.show();
					}
				});
            changeSignalAtBoundaryIcon.setToolTipText( rb.getString("ChangeSignalIconHint") );
			panel6.add(new JLabel("  "));
            panel6.add(setSignalsAtBoundaryDone = new JButton(rb.getString("Done")));
            setSignalsAtBoundaryDone.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setSignalsAtBoundaryDonePressed(e);
                }
            });
            setSignalsAtBoundaryDone.setToolTipText( rb.getString("SignalDoneHint") );
            panel6.add(setSignalsAtBoundaryCancel = new JButton(rb.getString("Cancel")));
            setSignalsAtBoundaryCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setSignalsAtBoundaryCancelPressed(e);
                }
            });
            setSignalsAtBoundaryCancel.setToolTipText( rb.getString("CancelHint") );
            theContentPane.add(panel6);
			setSignalsAtBoundaryFrame.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					setSignalsAtBoundaryCancelPressed(null);
				}
			});
		}
        setSignalsAtBoundaryFrame.pack();
        setSignalsAtBoundaryFrame.setVisible(true);		
		setSignalsAtBoundaryOpen = true;
	}	
	private void getSavedAnchorSignals (ActionEvent a) {
		if ( !getBlockInformation() ) return;
		eastBoundField.setText(boundary.getEastBoundSignal());	
		westBoundField.setText(boundary.getWestBoundSignal());
	}
	private void setSignalsAtBoundaryCancelPressed (ActionEvent a) {
		setSignalsAtBoundaryOpen = false;
		boundaryFromMenu = false;
		setSignalsAtBoundaryFrame.setVisible(false);
	}
	private void setSignalsAtBoundaryDonePressed (ActionEvent a) {
		if ( !getBlockInformation() ) return;
		eastBoundHead = getSignalHeadFromEntry(eastBoundField,false,setSignalsAtBoundaryFrame);
		westBoundHead = getSignalHeadFromEntry(westBoundField,false,setSignalsAtBoundaryFrame);
		if ( (eastBoundHead==null) && (westBoundHead==null) ) {
			JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
							rb.getString("SignalsError12"),
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
			return;
		}
		// place or update signals as requested
		if ( (eastBoundHead!=null) && setEastBound.isSelected() ) {
			if (isHeadOnPanel(eastBoundHead) && 
					(eastBoundHead!=getHeadFromName(boundary.getEastBoundSignal()))) { 
				JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new String[]{eastBoundField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!trackHorizontal) && (!trackVertical) ) {
				JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
					rb.getString("InfoMessage3"),"",JOptionPane.INFORMATION_MESSAGE);
				if (eastBoundHead!=getHeadFromName(boundary.getEastBoundSignal())) {				
					removeSignalHeadFromPanel(boundary.getEastBoundSignal());
					removeAssignment(eastBoundHead);
					boundary.setEastBoundSignal(eastBoundField.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(boundary.getEastBoundSignal());
				placeEastBound();
				removeAssignment(eastBoundHead);
				boundary.setEastBoundSignal(eastBoundField.getText().trim());
				needRedraw = true;
			}		
		}
		else if ( (eastBoundHead!=null) && 
				(eastBoundHead!=getHeadFromName(boundary.getEastBoundSignal())) &&
				(eastBoundHead!=getHeadFromName(boundary.getWestBoundSignal())) ) {
			if (isHeadOnPanel(eastBoundHead)) {
				JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError13"),
						new String[]{eastBoundField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}		
			else {
				removeSignalHeadFromPanel(boundary.getEastBoundSignal());
				removeAssignment(eastBoundHead);
				boundary.setEastBoundSignal(eastBoundField.getText().trim());
			}
		}
		else if ( (eastBoundHead!=null) &&  
				(eastBoundHead==getHeadFromName(boundary.getWestBoundSignal())) ) {
// need to figure out what to do in this case.			
		}
		if ( (westBoundHead!=null) && setWestBound.isSelected() ) {
			if (isHeadOnPanel(westBoundHead) &&
					(westBoundHead!=getHeadFromName(boundary.getWestBoundSignal()))) { 
				JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new String[]{westBoundField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!trackHorizontal) && (!trackVertical) ) {
				JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
					rb.getString("InfoMessage3"),"",JOptionPane.INFORMATION_MESSAGE);
				if (westBoundHead!=getHeadFromName(boundary.getWestBoundSignal())) {
					removeSignalHeadFromPanel(boundary.getWestBoundSignal());
					removeAssignment(westBoundHead);
					boundary.setWestBoundSignal(westBoundField.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(boundary.getWestBoundSignal());
				placeWestBound();
				removeAssignment(westBoundHead);
				boundary.setWestBoundSignal(westBoundField.getText().trim());
				needRedraw = true;
			}		
		}
		else if ( (westBoundHead!=null) && 
				(westBoundHead!=getHeadFromName(boundary.getEastBoundSignal())) &&
				(westBoundHead!=getHeadFromName(boundary.getWestBoundSignal())) ) {
			if (isHeadOnPanel(westBoundHead)) {
				JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError13"),
						new String[]{westBoundField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}		
			else {
				removeSignalHeadFromPanel(boundary.getWestBoundSignal());
				removeAssignment(westBoundHead);
				boundary.setWestBoundSignal(westBoundField.getText().trim());
			}
		}
		else if ( (westBoundHead!=null) &&  
				(westBoundHead==getHeadFromName(boundary.getEastBoundSignal())) ) {
// need to figure out what to do in this case.			
		}
		if ( (eastBoundHead!=null) && setupLogicEastBound.isSelected() ) {
			setLogicEastBound();
		}
		if ( (westBoundHead!=null) && setupLogicWestBound.isSelected() ) {
			setLogicWestBound();
		}
		setSignalsAtBoundaryOpen = false;
		boundaryFromMenu = false;
		setSignalsAtBoundaryFrame.setVisible(false);	
		if (needRedraw) {
			layoutEditor.redrawPanel();
			needRedraw = false;
			layoutEditor.setDirty();
		}
	}
	private boolean getBlockInformation() {
		if (!boundaryFromMenu) {
			block1 = getBlockFromEntry(block1NameField);
			if (block1==null) return false;
			block2 = getBlockFromEntry(block2NameField);
			if (block2==null) return false;
			PositionablePoint p = null;
			boundary = null;
			for (int i = 0;(i<layoutEditor.pointList.size()) && (boundary==null);i++) {
				p = (PositionablePoint)layoutEditor.pointList.get(i);
				if (p.getType() == PositionablePoint.ANCHOR) {
					LayoutBlock bA = null;
					LayoutBlock bB = null;
					if (p.getConnect1()!=null) bA = p.getConnect1().getLayoutBlock();
					if (p.getConnect2()!=null) bB = p.getConnect2().getLayoutBlock();
					if ( (bA!=null) && (bB!=null) && (bA!=bB) ) {
						if ( ( (bA==block1) && (bB==block2) ) ||
									( (bA==block2) && (bB==block1) ) ) {
							boundary = p;
						}
					}
				}
			}
			if (boundary==null) {
				JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
							rb.getString("SignalsError7"),
									rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		// set track orientation at boundary
		TrackSegment track1 = boundary.getConnect1();
		Point2D point1;
		if (track1.getConnect1()==(Object)boundary) 
			point1 = layoutEditor.getCoords(track1.getConnect2(),track1.getType2());
		else 
			point1 = layoutEditor.getCoords(track1.getConnect1(),track1.getType1());
		TrackSegment track2 = boundary.getConnect2();
		Point2D point2;
		if (track2.getConnect1()==(Object)boundary) 
			point2 = layoutEditor.getCoords(track2.getConnect2(),track2.getType2());
		else 
			point2 = layoutEditor.getCoords(track2.getConnect1(),track2.getType1());
		double delX = point1.getX() - point2.getX();
		double delY = point1.getY() - point2.getY();
		trackVertical = false;
		trackHorizontal = false;
		if (java.lang.Math.abs(delX) > 2.0*java.lang.Math.abs(delY)) {
			trackHorizontal = true;
			if (delX>0.0) {
				eastTrack = track1;
				westTrack = track2;
			}
			else {
				eastTrack = track2;
				westTrack = track1;
			}
		}
		if (java.lang.Math.abs(delY) > 2.0*java.lang.Math.abs(delX)) {
			trackVertical = true;
			if (delY>0.0) {
				eastTrack = track1;		// south
				westTrack = track2;		// north
			}
			else {
				eastTrack = track2;		// south
				westTrack = track1;		// north
			}
		}
		return true;
	}		
	private LayoutBlock getBlockFromEntry(JTextField blockNameField) {
		String str = blockNameField.getText().trim();
		if ( (str==null) || (str.equals("")) ) {
			JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,rb.getString("SignalsError9"),
									rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
			return null;
		}
		LayoutBlock block = jmri.InstanceManager.layoutBlockManagerInstance().
														getByUserName(str);
		if (block==null) {
			JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError10"),
						new String[]{str}), rb.getString("Error"),
							JOptionPane.ERROR_MESSAGE);
			return null ;
		}
		if ( !block.isOnPanel(layoutEditor) ) {
			JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError11"),
						new String[]{str}), rb.getString("Error"),
							JOptionPane.ERROR_MESSAGE);
			return null ;
		}
		return (block);
	}
	private void placeEastBound() {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		Point2D p = boundary.getCoords();
		if (trackHorizontal) {
			setSignalHeadOnPanel(2,eastBoundField.getText().trim(),
				(int)(p.getX()-testIcon.getIconWidth()),
				(int)(p.getY()+4) );
		}
		else if (trackVertical) {
			setSignalHeadOnPanel(1,eastBoundField.getText().trim(),
				(int)(p.getX()-2-testIcon.getIconWidth()),
				(int)(p.getY()-testIcon.getIconHeight()) );
		}
	}
	private void placeWestBound() {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		Point2D p = boundary.getCoords();
		if (trackHorizontal) {
			setSignalHeadOnPanel(0,westBoundField.getText().trim(),
				(int)(p.getX()),
				(int)(p.getY()-4-testIcon.getIconHeight()) );
		}
		else if (trackVertical) {
			setSignalHeadOnPanel(3,westBoundField.getText().trim(),
				(int)(p.getX()+4),
				(int)(p.getY()) );
		}	
	}
	private void setLogicEastBound() {
		LayoutBlock eastBlock = eastTrack.getLayoutBlock();
		Sensor eastBlockOccupancy = eastBlock.getOccupancySensor();
		if (eastBlockOccupancy==null) {
			JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage4"),
					new String[]{eastBlock.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);						
			return;
		}
		SignalHead nextHead = getNextSignalFromObject(eastTrack,
													(Object)boundary);
		if ( (nextHead==null) && (!reachedEndBumper()) ) {
			JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage5"),
					new String[]{eastBlock.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (!initializeBlockBossLogic(eastBoundField.getText().trim())) return;
		logic.setMode(BlockBossLogic.SINGLEBLOCK);
		logic.setSensor1(eastBlockOccupancy.getSystemName());
		if (nextHead!=null) {
			logic.setWatchedSignal1(nextHead.getSystemName(),false);
		}
		if (auxSignal!=null) {
			logic.setWatchedSignal1Alt(auxSignal.getSystemName());
		}
		finalizeBlockBossLogic();
	}
	private void setLogicWestBound() {
		LayoutBlock westBlock = westTrack.getLayoutBlock();
		Sensor westBlockOccupancy = westBlock.getOccupancySensor();
		if (westBlockOccupancy==null) {
			JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage4"),
					new String[]{westBlock.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);			
			return;
		}
		SignalHead nextHead = getNextSignalFromObject(westTrack,
													(Object)boundary);
		if ( (nextHead==null) && (!reachedEndBumper()) ) {
			JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage5"),
					new String[]{westBlock.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (!initializeBlockBossLogic(westBoundField.getText().trim())) return;
		logic.setMode(BlockBossLogic.SINGLEBLOCK);
		logic.setSensor1(westBlockOccupancy.getSystemName());
		if (nextHead!=null) {
			logic.setWatchedSignal1(nextHead.getSystemName(),false);
		}
		if (auxSignal!=null) {
			logic.setWatchedSignal1Alt(auxSignal.getSystemName());
		}
		finalizeBlockBossLogic();
	}

	/**
	 * Tool to set signals at a double crossover turnout, including placing the 
	 *		signal icons and setup of Simple Signal Logic for each signal head
	 * <P>
	 * This tool assumes left facing signal head icons have been selected, and 
	 *		will rotate the signal head icons accordingly.
	 * <P>
	 * This tool will place icons on the outside edge of the turnout.
	 * <P>
	 * At least one signal at each of the four connection points is 
	 *		required. A second signal at each is optional.
	 * <P>
	 * This tool only places signal icons if the turnout is either mostly vertical 
	 *		or mostly horizontal. Some user adjustment may be needed.
	 */	
	
	// operational variables for Set Signals at Double Crossover Turnout tool
	private JmriJFrame setSignalsAtXoverFrame = null;
	private boolean setSignalsAtXoverOpen = false;
	private JTextField xoverTurnoutNameField = new JTextField(16);
	private JTextField a1Field = new JTextField(16);
	private JTextField a2Field = new JTextField(16);
	private JTextField b1Field = new JTextField(16);
	private JTextField b2Field = new JTextField(16);
	private JTextField c1Field = new JTextField(16);
	private JTextField c2Field = new JTextField(16);
	private JTextField d1Field = new JTextField(16);
	private JTextField d2Field = new JTextField(16);
	private JCheckBox setA1Head = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupA1Logic = new JCheckBox(rb.getString("SetLogic"));
	private JCheckBox setA2Head = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupA2Logic = new JCheckBox(rb.getString("SetLogic"));
	private JCheckBox setB1Head = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupB1Logic = new JCheckBox(rb.getString("SetLogic"));
	private JCheckBox setB2Head = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupB2Logic = new JCheckBox(rb.getString("SetLogic"));
	private JCheckBox setC1Head = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupC1Logic = new JCheckBox(rb.getString("SetLogic"));
	private JCheckBox setC2Head = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupC2Logic = new JCheckBox(rb.getString("SetLogic"));
	private JCheckBox setD1Head = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupD1Logic = new JCheckBox(rb.getString("SetLogic"));
	private JCheckBox setD2Head = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupD2Logic = new JCheckBox(rb.getString("SetLogic"));
	private JButton getSavedXoverSignalHeads = null;
	private JButton changeXoverSignalIcon = null;
	private JButton setXoverSignalsDone = null;
	private JButton setXoverSignalsCancel = null;
	private boolean xoverFromMenu = false;
	private SignalHead a1Head = null;
	private SignalHead a2Head = null;
	private SignalHead b1Head = null;
	private SignalHead b2Head = null;
	private SignalHead c1Head = null;
	private SignalHead c2Head = null;
	private SignalHead d1Head = null;
	private SignalHead d2Head = null;

	// display dialog for Set Signals at Crossover Turnout tool
	public void setSignalsAtXoverTurnoutFromMenu( LayoutTurnout to, 
					MultiIconEditor theEditor, JFrame theFrame ) {
		xoverFromMenu = true;
		layoutTurnout = to;
		turnout = to.getTurnout();
		xoverTurnoutNameField.setText(to.getTurnoutName());
		setSignalsAtXoverTurnout(theEditor,theFrame);
	}
	public void setSignalsAtXoverTurnout( MultiIconEditor theEditor, JFrame theFrame ) {
		signalIconEditor = theEditor;
		signalFrame = theFrame;
		if (setSignalsAtXoverOpen) {
			setSignalsAtXoverFrame.setVisible(true);
			return;
		}
		// Initialize if needed
		if (setSignalsAtXoverFrame == null) {
            setSignalsAtXoverFrame = new JmriJFrame( rb.getString("SignalsAtXoverTurnout") );
            setSignalsAtXoverFrame.addHelpMenu("package.jmri.jmrit.display.SetSignalsAtXoverTurnout", true);
            setSignalsAtXoverFrame.setLocation(70,30);
            Container theContentPane = setSignalsAtXoverFrame.getContentPane();        
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));
			JPanel panel1 = new JPanel(); 
            panel1.setLayout(new FlowLayout());
			if (xoverFromMenu) {
				JLabel turnoutNameLabel = new JLabel( rb.getString("Turnout")+" "+
					rb.getString("Name")+" : "+layoutTurnout.getTurnoutName());
				panel1.add(turnoutNameLabel);
			}
			else {
				JLabel turnoutNameLabel = new JLabel( rb.getString("Turnout")+" "+
																rb.getString("Name") );
				panel1.add(turnoutNameLabel);
				panel1.add(turnoutNameField);
				turnoutNameField.setToolTipText(rb.getString("SignalsTurnoutNameHint"));
			}
            theContentPane.add(panel1);
			theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
			JLabel shTitle = new JLabel(rb.getString("SignalHeads"));
			panel2.add(shTitle);
			panel2.add(new JLabel("   "));
            panel2.add(getSavedXoverSignalHeads = new JButton(rb.getString("GetSaved")));
            getSavedXoverSignalHeads.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						xoverTurnoutSignalsGetSaved(e);
					}
				});
            getSavedXoverSignalHeads.setToolTipText( rb.getString("GetSavedHint") );			
			theContentPane.add(panel2);
            JPanel panel21 = new JPanel();
            panel21.setLayout(new FlowLayout());
			JLabel a1Label = new JLabel(rb.getString("AContinuing")+" : ");
			panel21.add(a1Label);
			panel21.add(a1Field);
			theContentPane.add(panel21);
			a1Field.setToolTipText(rb.getString("SignalHeadNameHint"));
            JPanel panel22 = new JPanel();
            panel22.setLayout(new FlowLayout());
			panel22.add(new JLabel("   "));
			panel22.add(setA1Head);
			setA1Head.setToolTipText(rb.getString("PlaceHeadHint"));
			panel22.add(new JLabel("  "));
			panel22.add(setupA1Logic);
			setupA1Logic.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel22);
            JPanel panel23 = new JPanel();
            panel23.setLayout(new FlowLayout());
			JLabel a2Label = new JLabel(rb.getString("ADiverging")+" : ");
			panel23.add(a2Label);
			panel23.add(a2Field);
			theContentPane.add(panel23);
			a2Field.setToolTipText(rb.getString("SignalHeadNameHint"));
            JPanel panel24 = new JPanel();
            panel24.setLayout(new FlowLayout());
			panel24.add(new JLabel("   "));
			panel24.add(setA2Head);
			setA2Head.setToolTipText(rb.getString("PlaceHeadHint"));
			panel24.add(new JLabel("  "));
			panel24.add(setupA2Logic);
			setupA2Logic.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel24);
            JPanel panel31 = new JPanel();
            panel31.setLayout(new FlowLayout());
			JLabel b1Label = new JLabel(rb.getString("BContinuing")+" : ");
			panel31.add(b1Label);
			panel31.add(b1Field);
			theContentPane.add(panel31);
			b1Field.setToolTipText(rb.getString("SignalHeadNameHint"));
            JPanel panel32 = new JPanel();
            panel32.setLayout(new FlowLayout());
			panel32.add(new JLabel("   "));
			panel32.add(setB1Head);
			setB1Head.setToolTipText(rb.getString("PlaceHeadHint"));
			panel32.add(new JLabel("  "));
			panel32.add(setupB1Logic);
			setupB1Logic.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel32);
            JPanel panel33 = new JPanel();
            panel33.setLayout(new FlowLayout());
			JLabel b2Label = new JLabel(rb.getString("BDiverging")+" : ");
			panel33.add(b2Label);
			panel33.add(b2Field);
			theContentPane.add(panel33);
			b2Field.setToolTipText(rb.getString("SignalHeadNameHint"));
            JPanel panel34 = new JPanel();
            panel34.setLayout(new FlowLayout());
			panel34.add(new JLabel("   "));
			panel34.add(setB2Head);
			setB2Head.setToolTipText(rb.getString("PlaceHeadHint"));
			panel34.add(new JLabel("  "));
			panel34.add(setupB2Logic);
			setupB2Logic.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel34);
            JPanel panel41 = new JPanel();
            panel41.setLayout(new FlowLayout());
			JLabel c1Label = new JLabel(rb.getString("CContinuing")+" : ");
			panel41.add(c1Label);
			panel41.add(c1Field);
			theContentPane.add(panel41);
			c1Field.setToolTipText(rb.getString("SignalHeadNameHint"));
            JPanel panel42 = new JPanel();
            panel42.setLayout(new FlowLayout());
			panel42.add(new JLabel("   "));
			panel42.add(setC1Head);
			setC1Head.setToolTipText(rb.getString("PlaceHeadHint"));
			panel42.add(new JLabel("  "));
			panel42.add(setupC1Logic);
			setupC1Logic.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel42);
            JPanel panel43 = new JPanel();
            panel43.setLayout(new FlowLayout());
			JLabel c2Label = new JLabel(rb.getString("CDiverging")+" : ");
			panel43.add(c2Label);
			panel43.add(c2Field);
			theContentPane.add(panel43);
			c2Field.setToolTipText(rb.getString("SignalHeadNameHint"));
            JPanel panel44 = new JPanel();
            panel44.setLayout(new FlowLayout());
			panel44.add(new JLabel("   "));
			panel44.add(setC2Head);
			setC2Head.setToolTipText(rb.getString("PlaceHeadHint"));
			panel44.add(new JLabel("  "));
			panel44.add(setupC2Logic);
			setupC2Logic.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel44);
            JPanel panel51 = new JPanel();
            panel51.setLayout(new FlowLayout());
			JLabel d1Label = new JLabel(rb.getString("DContinuing")+" : ");
			panel51.add(d1Label);
			panel51.add(d1Field);
			theContentPane.add(panel51);
			d1Field.setToolTipText(rb.getString("SignalHeadNameHint"));
            JPanel panel52 = new JPanel();
            panel52.setLayout(new FlowLayout());
			panel52.add(new JLabel("   "));
			panel52.add(setD1Head);
			setD1Head.setToolTipText(rb.getString("PlaceHeadHint"));
			panel52.add(new JLabel("  "));
			panel52.add(setupD1Logic);
			setupD1Logic.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel52);
            JPanel panel53 = new JPanel();
            panel53.setLayout(new FlowLayout());
			JLabel d2Label = new JLabel(rb.getString("DDiverging")+" : ");
			panel53.add(d2Label);
			panel53.add(d2Field);
			theContentPane.add(panel53);
			d2Field.setToolTipText(rb.getString("SignalHeadNameHint"));
            JPanel panel54 = new JPanel();
            panel54.setLayout(new FlowLayout());
			panel54.add(new JLabel("   "));
			panel54.add(setD2Head);
			setD2Head.setToolTipText(rb.getString("PlaceHeadHint"));
			panel54.add(new JLabel("  "));
			panel54.add(setupD2Logic);
			setupD2Logic.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel54);
			theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
            JPanel panel6 = new JPanel();
            panel6.setLayout(new FlowLayout());
            panel6.add(changeXoverSignalIcon = new JButton(rb.getString("ChangeSignalIcon")));
            changeXoverSignalIcon.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						signalFrame.show();
					}
				});
            changeXoverSignalIcon.setToolTipText( rb.getString("ChangeSignalIconHint") );
			panel6.add(new JLabel("  "));
            panel6.add(setXoverSignalsDone = new JButton(rb.getString("Done")));
            setXoverSignalsDone.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setXoverSignalsDonePressed(e);
                }
            });
            setXoverSignalsDone.setToolTipText( rb.getString("SignalDoneHint") );
            panel6.add(setXoverSignalsCancel = new JButton(rb.getString("Cancel")));
            setXoverSignalsCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setXoverSignalsCancelPressed(e);
                }
            });
            setXoverSignalsCancel.setToolTipText( rb.getString("CancelHint") );
            theContentPane.add(panel6);
			setSignalsAtXoverFrame.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					setXoverSignalsCancelPressed(null);
				}
			});
		}
        setSignalsAtXoverFrame.pack();
        setSignalsAtXoverFrame.setVisible(true);		
		setSignalsAtXoverOpen = true;
	}	
	private void xoverTurnoutSignalsGetSaved (ActionEvent a) {
		if ( !getTurnoutInformation(true) ) return;
		a1Field.setText(layoutTurnout.getSignalA1Name());	
		a2Field.setText(layoutTurnout.getSignalA2Name());
		b1Field.setText(layoutTurnout.getSignalB1Name());
		b2Field.setText(layoutTurnout.getSignalB2Name());
		c1Field.setText(layoutTurnout.getSignalC1Name());	
		c2Field.setText(layoutTurnout.getSignalC2Name());	
		d1Field.setText(layoutTurnout.getSignalD1Name());	
		d2Field.setText(layoutTurnout.getSignalD2Name());	
	}
	private void setXoverSignalsCancelPressed (ActionEvent a) {
		setSignalsAtXoverOpen = false;
		xoverFromMenu = false;
		setSignalsAtXoverFrame.setVisible(false);
	}
	private void setXoverSignalsDonePressed (ActionEvent a) {
		if ( !getTurnoutInformation(true) ) return;
		if ( !getXoverSignalHeadInformation() ) return;
		// place signal icons if requested, and assign signal heads to this turnout
		if (setA1Head.isSelected()) {
			if (isHeadOnPanel(a1Head) &&
				(a1Head!=getHeadFromName(layoutTurnout.getSignalA1Name()))) {
				JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new String[]{a1Field.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!layoutTurnoutHorizontal) && (!layoutTurnoutVertical) ) {
				JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
					rb.getString("InfoMessage2"),"",JOptionPane.INFORMATION_MESSAGE);
				if (a1Head!=getHeadFromName(layoutTurnout.getSignalA1Name())) {				
					removeSignalHeadFromPanel(layoutTurnout.getSignalA1Name());
					removeAssignment(a1Head);
					layoutTurnout.setSignalA1Name(a1Field.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(layoutTurnout.getSignalA1Name());
				placeA1();
				removeAssignment(a1Head);
				layoutTurnout.setSignalA1Name(a1Field.getText().trim());
				needRedraw = true;
			}		
		}
		else {
			int assigned = isHeadAssignedHere(a1Head);
			if (assigned == NONE) {
				if ( isHeadOnPanel(a1Head) && 
									isHeadAssignedAnywhere(a1Head) ) {
					JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError8"),
							new String[]{a1Field.getText().trim()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSignalHeadFromPanel(layoutTurnout.getSignalA1Name());
					removeAssignment(a1Head);
					layoutTurnout.setSignalA1Name(a1Field.getText().trim());
				}
			}
			else if (assigned!=A1) {
// need to figure out what to do in this case.			
			}
		}
		if ( (a2Head!=null) && setA2Head.isSelected() ) {
			if (isHeadOnPanel(a2Head) &&
				(a2Head!=getHeadFromName(layoutTurnout.getSignalA2Name()))) {
				JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new String[]{a2Field.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!layoutTurnoutHorizontal) && (!layoutTurnoutVertical) ) {
				JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
					rb.getString("InfoMessage2"),"",JOptionPane.INFORMATION_MESSAGE);
				if (a2Head!=getHeadFromName(layoutTurnout.getSignalA2Name())) {				
					removeSignalHeadFromPanel(layoutTurnout.getSignalA2Name());
					removeAssignment(a2Head);
					layoutTurnout.setSignalA2Name(a2Field.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(layoutTurnout.getSignalA2Name());
				placeA2();
				removeAssignment(a2Head);
				layoutTurnout.setSignalA2Name(a2Field.getText().trim());
				needRedraw = true;
			}		
		}
		else if (a2Head!=null) {
			int assigned = isHeadAssignedHere(a2Head);
			if (assigned == NONE) {
				if ( isHeadOnPanel(a2Head) && 
									isHeadAssignedAnywhere(a2Head) ) {
					JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError8"),
							new String[]{a2Field.getText().trim()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSignalHeadFromPanel(layoutTurnout.getSignalA2Name());
					removeAssignment(a2Head);
					layoutTurnout.setSignalA2Name(a2Field.getText().trim());
				}
			}
			else if (assigned!=A2) {
// need to figure out what to do in this case.			
			}
		}
		else if (a2Head==null) {
			removeSignalHeadFromPanel(layoutTurnout.getSignalA2Name());
			layoutTurnout.setSignalA2Name("");
		}
		if (setB1Head.isSelected()) {
			if (isHeadOnPanel(b1Head) &&
				(b1Head!=getHeadFromName(layoutTurnout.getSignalB1Name()))) {
				JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new String[]{b1Field.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!layoutTurnoutHorizontal) && (!layoutTurnoutVertical) ) {
				JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
					rb.getString("InfoMessage2"),"",JOptionPane.INFORMATION_MESSAGE);
				if (b1Head!=getHeadFromName(layoutTurnout.getSignalB1Name())) {				
					removeSignalHeadFromPanel(layoutTurnout.getSignalB1Name());
					removeAssignment(b1Head);
					layoutTurnout.setSignalB1Name(b1Field.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(layoutTurnout.getSignalB1Name());
				placeB1();
				removeAssignment(b1Head);
				layoutTurnout.setSignalB1Name(b1Field.getText().trim());
				needRedraw = true;
			}		
		}
		else {
			int assigned = isHeadAssignedHere(b1Head);
			if (assigned == NONE) {
				if ( isHeadOnPanel(b1Head) && 
									isHeadAssignedAnywhere(b1Head) ) {
					JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError8"),
							new String[]{b1Field.getText().trim()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSignalHeadFromPanel(layoutTurnout.getSignalB1Name());
					removeAssignment(b1Head);
					layoutTurnout.setSignalB1Name(b1Field.getText().trim());
				}
			}
			else if (assigned!=B1) {
// need to figure out what to do in this case.			
			}
		}
		if ( (b2Head!=null) && setB2Head.isSelected() ) {
			if (isHeadOnPanel(b2Head) &&
				(b2Head!=getHeadFromName(layoutTurnout.getSignalB2Name()))) {
				JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new String[]{b2Field.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!layoutTurnoutHorizontal) && (!layoutTurnoutVertical) ) {
				JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
					rb.getString("InfoMessage2"),"",JOptionPane.INFORMATION_MESSAGE);
				if (b2Head!=getHeadFromName(layoutTurnout.getSignalB2Name())) {				
					removeSignalHeadFromPanel(layoutTurnout.getSignalB2Name());
					removeAssignment(b2Head);
					layoutTurnout.setSignalB2Name(b2Field.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(layoutTurnout.getSignalB2Name());
				placeB2();
				removeAssignment(b2Head);
				layoutTurnout.setSignalB2Name(b2Field.getText().trim());
				needRedraw = true;
			}		
		}
		else if (b2Head!=null) {
			int assigned = isHeadAssignedHere(b2Head);
			if (assigned == NONE) {
				if ( isHeadOnPanel(b2Head) && 
									isHeadAssignedAnywhere(b2Head) ) {
					JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError8"),
							new String[]{b2Field.getText().trim()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSignalHeadFromPanel(layoutTurnout.getSignalB2Name());
					removeAssignment(b2Head);
					layoutTurnout.setSignalB2Name(b2Field.getText().trim());
				}
			}
			else if (assigned!=B2) {
// need to figure out what to do in this case.			
			}
		}
		else if (b2Head==null) {
			removeSignalHeadFromPanel(layoutTurnout.getSignalB2Name());
			layoutTurnout.setSignalB2Name("");
		}
		if (setC1Head.isSelected()) {
			if (isHeadOnPanel(c1Head) &&
				(c1Head!=getHeadFromName(layoutTurnout.getSignalC1Name()))) {
				JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new String[]{c1Field.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!layoutTurnoutHorizontal) && (!layoutTurnoutVertical) ) {
				JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
					rb.getString("InfoMessage2"),"",JOptionPane.INFORMATION_MESSAGE);
				if (c1Head!=getHeadFromName(layoutTurnout.getSignalC1Name())) {				
					removeSignalHeadFromPanel(layoutTurnout.getSignalC1Name());
					removeAssignment(c1Head);
					layoutTurnout.setSignalC1Name(c1Field.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(layoutTurnout.getSignalC1Name());
				placeC1();
				removeAssignment(c1Head);
				layoutTurnout.setSignalC1Name(c1Field.getText().trim());
				needRedraw = true;
			}		
		}
		else {
			int assigned = isHeadAssignedHere(c1Head);
			if (assigned == NONE) {
				if ( isHeadOnPanel(c1Head) && 
									isHeadAssignedAnywhere(c1Head) ) {
					JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError8"),
							new String[]{c1Field.getText().trim()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSignalHeadFromPanel(layoutTurnout.getSignalC1Name());
					removeAssignment(c1Head);
					layoutTurnout.setSignalC1Name(c1Field.getText().trim());
				}
			}
			else if (assigned!=C1) {
// need to figure out what to do in this case.			
			}
		}
		if ( (c2Head!=null) && setC2Head.isSelected() ) {
			if (isHeadOnPanel(c2Head) &&
				(c2Head!=getHeadFromName(layoutTurnout.getSignalC2Name()))) {
				JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new String[]{c2Field.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!layoutTurnoutHorizontal) && (!layoutTurnoutVertical) ) {
				JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
					rb.getString("InfoMessage2"),"",JOptionPane.INFORMATION_MESSAGE);
				if (c2Head!=getHeadFromName(layoutTurnout.getSignalC2Name())) {				
					removeSignalHeadFromPanel(layoutTurnout.getSignalC2Name());
					removeAssignment(c2Head);
					layoutTurnout.setSignalC2Name(c2Field.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(layoutTurnout.getSignalC2Name());
				placeC2();
				removeAssignment(c2Head);
				layoutTurnout.setSignalC2Name(c2Field.getText().trim());
				needRedraw = true;
			}		
		}
		else if (c2Head!=null) {
			int assigned = isHeadAssignedHere(c2Head);
			if (assigned == NONE) {
				if ( isHeadOnPanel(c2Head) && 
									isHeadAssignedAnywhere(c2Head) ) {
					JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError8"),
							new String[]{c2Field.getText().trim()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSignalHeadFromPanel(layoutTurnout.getSignalC2Name());
					removeAssignment(c2Head);
					layoutTurnout.setSignalC2Name(c2Field.getText().trim());
				}
			}
			else if (assigned!=C2) {
// need to figure out what to do in this case.			
			}
		}
		else if (c2Head==null) {
			removeSignalHeadFromPanel(layoutTurnout.getSignalC2Name());
			layoutTurnout.setSignalC2Name("");
		}
		if (setD1Head.isSelected()) {
			if (isHeadOnPanel(d1Head) &&
				(d1Head!=getHeadFromName(layoutTurnout.getSignalD1Name()))) {
				JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new String[]{d1Field.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!layoutTurnoutHorizontal) && (!layoutTurnoutVertical) ) {
				JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
					rb.getString("InfoMessage2"),"",JOptionPane.INFORMATION_MESSAGE);
				if (d1Head!=getHeadFromName(layoutTurnout.getSignalD1Name())) {				
					removeSignalHeadFromPanel(layoutTurnout.getSignalD1Name());
					removeAssignment(d1Head);
					layoutTurnout.setSignalD1Name(d1Field.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(layoutTurnout.getSignalD1Name());
				placeD1();
				removeAssignment(d1Head);
				layoutTurnout.setSignalD1Name(d1Field.getText().trim());
				needRedraw = true;
			}		
		}
		else {
			int assigned = isHeadAssignedHere(d1Head);
			if (assigned == NONE) {
				if ( isHeadOnPanel(d1Head) && 
									isHeadAssignedAnywhere(d1Head) ) {
					JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError8"),
							new String[]{d1Field.getText().trim()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSignalHeadFromPanel(layoutTurnout.getSignalD1Name());
					removeAssignment(d1Head);
					layoutTurnout.setSignalD1Name(d1Field.getText().trim());
				}
			}
			else if (assigned!=D1) {
// need to figure out what to do in this case.			
			}
		}
		if ( (d2Head!=null) && setD2Head.isSelected() ) {
			if (isHeadOnPanel(d2Head) &&
				(d2Head!=getHeadFromName(layoutTurnout.getSignalD2Name()))) {
				JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new String[]{d2Field.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!layoutTurnoutHorizontal) && (!layoutTurnoutVertical) ) {
				JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
					rb.getString("InfoMessage2"),"",JOptionPane.INFORMATION_MESSAGE);
				if (d2Head!=getHeadFromName(layoutTurnout.getSignalD2Name())) {				
					removeSignalHeadFromPanel(layoutTurnout.getSignalD2Name());
					removeAssignment(d2Head);
					layoutTurnout.setSignalD2Name(d2Field.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(layoutTurnout.getSignalD2Name());
				placeD2();
				removeAssignment(d2Head);
				layoutTurnout.setSignalD2Name(d2Field.getText().trim());
				needRedraw = true;
			}		
		}
		else if (d2Head!=null) {
			int assigned = isHeadAssignedHere(d2Head);
			if (assigned == NONE) {
				if ( isHeadOnPanel(d2Head) && 
									isHeadAssignedAnywhere(d2Head) ) {
					JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
						java.text.MessageFormat.format(rb.getString("SignalsError8"),
							new String[]{d2Field.getText().trim()}), 
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return;
				}		
				else {
					removeSignalHeadFromPanel(layoutTurnout.getSignalD2Name());
					removeAssignment(d2Head);
					layoutTurnout.setSignalD2Name(d2Field.getText().trim());
				}
			}
			else if (assigned!=A2) {
// need to figure out what to do in this case.			
			}
		}
		else if (d2Head==null) {
			removeSignalHeadFromPanel(layoutTurnout.getSignalD2Name());
			layoutTurnout.setSignalD2Name("");
		}
		// setup logic if requested
		if (setupA1Logic.isSelected() || setupA2Logic.isSelected()) {
			setLogicXover(a1Head,(TrackSegment)layoutTurnout.getConnectB(),a2Head,
					(TrackSegment)layoutTurnout.getConnectC(),setupA1Logic.isSelected(),
								setupA2Logic.isSelected());
		}
		if (setupB1Logic.isSelected() || setupB2Logic.isSelected()) {
			setLogicXover(b1Head,(TrackSegment)layoutTurnout.getConnectA(),b2Head,
					(TrackSegment)layoutTurnout.getConnectD(),setupB1Logic.isSelected(),
								setupB2Logic.isSelected());
		}
		if (setupC1Logic.isSelected() || setupC2Logic.isSelected()) {
			setLogicXover(c1Head,(TrackSegment)layoutTurnout.getConnectD(),c2Head,
					(TrackSegment)layoutTurnout.getConnectA(),setupC1Logic.isSelected(),
								setupC2Logic.isSelected());
		}
		if (setupD1Logic.isSelected() || setupD2Logic.isSelected()) {
			setLogicXover(d1Head,(TrackSegment)layoutTurnout.getConnectC(),d2Head,
					(TrackSegment)layoutTurnout.getConnectB(),setupD1Logic.isSelected(),
								setupD2Logic.isSelected());
		}
		// finish up
		setSignalsAtXoverOpen = false;
		xoverFromMenu = false;
		setSignalsAtXoverFrame.setVisible(false);
		if (needRedraw) {
			layoutEditor.redrawPanel();
			needRedraw = false;
			layoutEditor.setDirty();
		}		
	}
	private boolean getXoverSignalHeadInformation() {
		a1Head = getSignalHeadFromEntry(a1Field,true,setSignalsAtXoverFrame);
		if (a1Head==null) return false;
		a2Head = getSignalHeadFromEntry(a2Field,false,setSignalsAtXoverFrame);
		b1Head = getSignalHeadFromEntry(b1Field,true,setSignalsAtXoverFrame);
		if (b1Head==null) return false;
		b2Head = getSignalHeadFromEntry(b2Field,false,setSignalsAtXoverFrame);
		c1Head = getSignalHeadFromEntry(c1Field,true,setSignalsAtXoverFrame);
		if (c1Head==null) return false;
		c2Head = getSignalHeadFromEntry(c2Field,false,setSignalsAtXoverFrame);
		d1Head = getSignalHeadFromEntry(d1Field,true,setSignalsAtXoverFrame);
		if (d1Head==null) return false;
		d2Head = getSignalHeadFromEntry(d2Field,false,setSignalsAtXoverFrame);
		return true;
	}
	private void placeA1() {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		if( layoutTurnoutHorizontal && layoutTurnoutThroatLeft ) {
			setSignalHeadOnPanel(2,a1Field.getText().trim(),
				(int)(layoutTurnout.getCoordsA().getX()-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsA().getY()+4) );
		}
		else if( layoutTurnoutHorizontal && (!layoutTurnoutThroatLeft) ) {
			setSignalHeadOnPanel(0,a1Field.getText().trim(),
				(int)(layoutTurnout.getCoordsA().getX()),
				(int)(layoutTurnout.getCoordsA().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutVertical && layoutTurnoutThroatUp ) {
			setSignalHeadOnPanel(1,a1Field.getText().trim(),
				(int)(layoutTurnout.getCoordsA().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsA().getY()-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutVertical && (!layoutTurnoutThroatUp) ) {
			setSignalHeadOnPanel(3,a1Field.getText().trim(),
				(int)(layoutTurnout.getCoordsA().getX()+4),
				(int)(layoutTurnout.getCoordsA().getY()) );
		}
	}
	private void placeA2() {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		if( layoutTurnoutHorizontal && layoutTurnoutThroatLeft ) {
			setSignalHeadOnPanel(2,a2Field.getText().trim(),
				(int)(layoutTurnout.getCoordsA().getX()-4-(2*testIcon.getIconWidth())),
				(int)(layoutTurnout.getCoordsA().getY()+4) );
		}
		else if( layoutTurnoutHorizontal && (!layoutTurnoutThroatLeft) ) {
			setSignalHeadOnPanel(0,a2Field.getText().trim(),
				(int)(layoutTurnout.getCoordsA().getX()+4+testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsA().getY()-4-testIcon.getIconHeight()));
		}
		else if( layoutTurnoutVertical && layoutTurnoutThroatUp ) {
			setSignalHeadOnPanel(1,a2Field.getText().trim(),
				(int)(layoutTurnout.getCoordsA().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsA().getY()-4-(2*testIcon.getIconHeight())));
		}
		else if( layoutTurnoutVertical && (!layoutTurnoutThroatUp) ) {
			setSignalHeadOnPanel(3,a2Field.getText().trim(),
				(int)(layoutTurnout.getCoordsA().getX()+4),
				(int)(layoutTurnout.getCoordsA().getY()+4+testIcon.getIconHeight()));
		}
	}
	private void placeB1() {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		if( layoutTurnoutHorizontal && layoutTurnoutThroatLeft ) {
			setSignalHeadOnPanel(0,b1Field.getText().trim(),
				(int)(layoutTurnout.getCoordsB().getX()),
				(int)(layoutTurnout.getCoordsB().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutHorizontal && (!layoutTurnoutThroatLeft) ) {
			setSignalHeadOnPanel(2,b1Field.getText().trim(),
				(int)(layoutTurnout.getCoordsB().getX()-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsB().getY()+4) );
		}
		else if( layoutTurnoutVertical && layoutTurnoutThroatUp ) {
			setSignalHeadOnPanel(3,b1Field.getText().trim(),
				(int)(layoutTurnout.getCoordsB().getX()+4),
				(int)(layoutTurnout.getCoordsB().getY()) );
		}
		else if( layoutTurnoutVertical && (!layoutTurnoutThroatUp) ) {
			setSignalHeadOnPanel(1,b1Field.getText().trim(),
				(int)(layoutTurnout.getCoordsB().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsB().getY()-testIcon.getIconHeight()) );
		}
	}
	private void placeB2() {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		if( layoutTurnoutHorizontal && layoutTurnoutThroatLeft ) {
			setSignalHeadOnPanel(0,b2Field.getText().trim(),
				(int)(layoutTurnout.getCoordsB().getX()+4+testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsB().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutHorizontal && (!layoutTurnoutThroatLeft) ) {
			setSignalHeadOnPanel(2,b2Field.getText().trim(),
				(int)(layoutTurnout.getCoordsB().getX()-4-(2*testIcon.getIconWidth())),
				(int)(layoutTurnout.getCoordsB().getY()+4) );
		}
		else if( layoutTurnoutVertical && layoutTurnoutThroatUp ) {
			setSignalHeadOnPanel(3,b2Field.getText().trim(),
				(int)(layoutTurnout.getCoordsB().getX()+4),
				(int)(layoutTurnout.getCoordsB().getY()+4+testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutVertical && (!layoutTurnoutThroatUp) ) {
			setSignalHeadOnPanel(1,b2Field.getText().trim(),
				(int)(layoutTurnout.getCoordsB().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsB().getY()-4-(2*testIcon.getIconHeight())) );
		}
	}
	private void placeC1() {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		if( layoutTurnoutHorizontal && layoutTurnoutThroatLeft ) {
			setSignalHeadOnPanel(0,c1Field.getText().trim(),
				(int)(layoutTurnout.getCoordsC().getX()),
				(int)(layoutTurnout.getCoordsC().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutHorizontal && (!layoutTurnoutThroatLeft) ) {
			setSignalHeadOnPanel(2,c1Field.getText().trim(),
				(int)(layoutTurnout.getCoordsC().getX()-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsC().getY()+4) );
		}
		else if( layoutTurnoutVertical && layoutTurnoutThroatUp ) {
			setSignalHeadOnPanel(3,c1Field.getText().trim(),
				(int)(layoutTurnout.getCoordsC().getX()+4),
				(int)(layoutTurnout.getCoordsC().getY()) );
		}
		else if( layoutTurnoutVertical && (!layoutTurnoutThroatUp) ) {
			setSignalHeadOnPanel(1,c1Field.getText().trim(),
				(int)(layoutTurnout.getCoordsC().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsC().getY()-testIcon.getIconHeight()) );
		}
	}
	private void placeC2() {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		if( layoutTurnoutHorizontal && layoutTurnoutThroatLeft ) {
			setSignalHeadOnPanel(0,c2Field.getText().trim(),
				(int)(layoutTurnout.getCoordsC().getX()+4+testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsC().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutHorizontal && (!layoutTurnoutThroatLeft) ) {
			setSignalHeadOnPanel(2,c2Field.getText().trim(),
				(int)(layoutTurnout.getCoordsC().getX()-4-(2*testIcon.getIconWidth())),
				(int)(layoutTurnout.getCoordsC().getY()+4) );
		}
		else if( layoutTurnoutVertical && layoutTurnoutThroatUp ) {
			setSignalHeadOnPanel(3,c2Field.getText().trim(),
				(int)(layoutTurnout.getCoordsC().getX()+4),
				(int)(layoutTurnout.getCoordsC().getY()+4+testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutVertical && (!layoutTurnoutThroatUp) ) {
			setSignalHeadOnPanel(1,c2Field.getText().trim(),
				(int)(layoutTurnout.getCoordsC().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsC().getY()-4-(2*testIcon.getIconHeight())) );
		}
	}
	private void placeD1() {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		if( layoutTurnoutHorizontal && layoutTurnoutThroatLeft ) {
			setSignalHeadOnPanel(2,d1Field.getText().trim(),
				(int)(layoutTurnout.getCoordsD().getX()-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsD().getY()+4) );
		}
		else if( layoutTurnoutHorizontal && (!layoutTurnoutThroatLeft) ) {
			setSignalHeadOnPanel(0,d1Field.getText().trim(),
				(int)(layoutTurnout.getCoordsD().getX()),
				(int)(layoutTurnout.getCoordsD().getY()-4-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutVertical && layoutTurnoutThroatUp ) {
			setSignalHeadOnPanel(1,d1Field.getText().trim(),
				(int)(layoutTurnout.getCoordsD().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsD().getY()-testIcon.getIconHeight()) );
		}
		else if( layoutTurnoutVertical && (!layoutTurnoutThroatUp) ) {
			setSignalHeadOnPanel(3,d1Field.getText().trim(),
				(int)(layoutTurnout.getCoordsD().getX()+4),
				(int)(layoutTurnout.getCoordsD().getY()) );
		}
	}
	private void placeD2() {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		if( layoutTurnoutHorizontal && layoutTurnoutThroatLeft ) {
			setSignalHeadOnPanel(2,d2Field.getText().trim(),
				(int)(layoutTurnout.getCoordsD().getX()-4-(2*testIcon.getIconWidth())),
				(int)(layoutTurnout.getCoordsD().getY()+4) );
		}
		else if( layoutTurnoutHorizontal && (!layoutTurnoutThroatLeft) ) {
			setSignalHeadOnPanel(0,d2Field.getText().trim(),
				(int)(layoutTurnout.getCoordsD().getX()+4+testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsD().getY()-4-testIcon.getIconHeight()));
		}
		else if( layoutTurnoutVertical && layoutTurnoutThroatUp ) {
			setSignalHeadOnPanel(1,d2Field.getText().trim(),
				(int)(layoutTurnout.getCoordsD().getX()-4-testIcon.getIconWidth()),
				(int)(layoutTurnout.getCoordsD().getY()-4-(2*testIcon.getIconHeight())));
		}
		else if( layoutTurnoutVertical && (!layoutTurnoutThroatUp) ) {
			setSignalHeadOnPanel(3,d2Field.getText().trim(),
				(int)(layoutTurnout.getCoordsD().getX()+4),
				(int)(layoutTurnout.getCoordsD().getY()+4+testIcon.getIconHeight()));
		}
	}
	private void setLogicXover(SignalHead head,TrackSegment track,SignalHead secondHead,TrackSegment track2,
				boolean setup1, boolean setup2) {
		if ( (track==null) && setup1 ) {
			JOptionPane.showMessageDialog(setSignalsFrame,
					rb.getString("InfoMessage7"),"",JOptionPane.INFORMATION_MESSAGE);			
			return;
		}
		Sensor occupancy = null;
		SignalHead nextHead = null;
		if ( (track!=null) && setup1) {
			LayoutBlock block = track.getLayoutBlock();
			if (block==null) {
				JOptionPane.showMessageDialog(setSignalsFrame,
					rb.getString("InfoMessage6"),"",JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			occupancy = block.getOccupancySensor();
			if (occupancy==null) {
				JOptionPane.showMessageDialog(setSignalsFrame,
					java.text.MessageFormat.format(rb.getString("InfoMessage4"),
						new String[]{block.getUserName()}), 
							null,JOptionPane.INFORMATION_MESSAGE);						
				return;
			}
			nextHead = getNextSignalFromObject(track,
													(Object)layoutTurnout);
			if ( (nextHead==null) && (!reachedEndBumper()) ) {
				JOptionPane.showMessageDialog(setSignalsFrame,
					java.text.MessageFormat.format(rb.getString("InfoMessage5"),
						new String[]{block.getUserName()}), 
							null,JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			if (secondHead!=null) {
				if (!initializeBlockBossLogic(head.getSystemName())) return;
				logic.setMode(BlockBossLogic.TRAILINGMAIN);
				logic.setTurnout(turnout.getSystemName());
				logic.setSensor1(occupancy.getSystemName());
				if (nextHead!=null) {
					logic.setWatchedSignal1(nextHead.getSystemName(),false);
				}
				if (auxSignal!=null) {
					logic.setWatchedSignal1Alt(auxSignal.getSystemName());
				}
				finalizeBlockBossLogic();
			}
		}
		if ( (secondHead!=null) && !setup2 ) return;
		SignalHead savedAuxSignal = auxSignal;
		if (track2==null) {
			JOptionPane.showMessageDialog(setSignalsFrame,
					rb.getString("InfoMessage7"),"",JOptionPane.INFORMATION_MESSAGE);			
			return;
		}
		LayoutBlock block2 = track2.getLayoutBlock();
		if (block2==null) {
			JOptionPane.showMessageDialog(setSignalsFrame,
					rb.getString("InfoMessage6"),"",JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		Sensor occupancy2 = block2.getOccupancySensor();
		if (occupancy2==null) {
			JOptionPane.showMessageDialog(setSignalsFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage4"),
					new String[]{block2.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);						
			return;
		}
		SignalHead nextHead2 = getNextSignalFromObject(track2,
													(Object)layoutTurnout);
		if ( (nextHead2==null) && (!reachedEndBumper()) ) {
			JOptionPane.showMessageDialog(setSignalsFrame,
				java.text.MessageFormat.format(rb.getString("InfoMessage5"),
					new String[]{block2.getUserName()}), 
						null,JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if ( (secondHead==null) && setup1 ) {
			if (!initializeBlockBossLogic(head.getSystemName())) return;
			logic.setMode(BlockBossLogic.FACING);
			logic.setTurnout(turnout.getSystemName());
			logic.setWatchedSensor1(occupancy.getSystemName());
			logic.setWatchedSensor2(occupancy2.getSystemName());
			if (nextHead!=null) {
				logic.setWatchedSignal1(nextHead.getSystemName(),false);
			}
			if (savedAuxSignal!=null) {
				logic.setWatchedSignal1Alt(savedAuxSignal.getSystemName());
			}
			if (nextHead2!=null) {
				logic.setWatchedSignal2(nextHead2.getSystemName());
			}
			if (auxSignal!=null) {
				logic.setWatchedSignal2Alt(auxSignal.getSystemName());
			}		
			logic.setLimitSpeed2(true);
			finalizeBlockBossLogic();
		}
		else if ( (secondHead!=null) && setup2) {
			if (!initializeBlockBossLogic(secondHead.getSystemName())) return;
			logic.setMode(BlockBossLogic.TRAILINGDIVERGING);
			logic.setTurnout(turnout.getSystemName());
			logic.setSensor1(occupancy2.getSystemName());
			logic.setWatchedSignal1(nextHead2.getSystemName(),false);
			if (auxSignal!=null) {
				logic.setWatchedSignal1Alt(auxSignal.getSystemName());
			}
			logic.setLimitSpeed2(true);
			finalizeBlockBossLogic();			
		}
	}
	/**
	 * Tool to set signals at a level crossing, including placing the 
	 *		signal icons and setup of Simple Signal Logic for each signal head
	 * <P>
	 * This tool assumes left facing signal head icons have been selected, and 
	 *		will rotate the signal head icons accordingly.
	 * <P>
	 * This tool will place icons on the right side of each track.
	 * <P>
	 * Both tracks do not need to be signalled. If one signal for a track,
	 *		A-C or B-D, the other must also be present.
	 * <P>
	 * Some user adjustment of turnout positions may be needed.
	 */	
	
	// operational variables for Set Signals at Level Crossing tool
	private JmriJFrame setSignalsAtXingFrame = null;
	private boolean setSignalsAtXingOpen = false;
	private JTextField blockANameField = new JTextField(16);
	private JTextField blockCNameField = new JTextField(16);
	private JTextField aField = new JTextField(16);
	private JTextField bField = new JTextField(16);
	private JTextField cField = new JTextField(16);
	private JTextField dField = new JTextField(16);
	private JCheckBox setAHead = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupALogic = new JCheckBox(rb.getString("SetLogic"));
	private JCheckBox setBHead = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupBLogic = new JCheckBox(rb.getString("SetLogic"));
	private JCheckBox setCHead = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupCLogic = new JCheckBox(rb.getString("SetLogic"));
	private JCheckBox setDHead = new JCheckBox(rb.getString("PlaceHead"));
	private JCheckBox setupDLogic = new JCheckBox(rb.getString("SetLogic"));
	private JButton getSavedXingSignalHeads = null;
	private JButton changeXingSignalIcon = null;
	private JButton setXingSignalsDone = null;
	private JButton setXingSignalsCancel = null;
	private TrackSegment xingTrackA = null;
	private TrackSegment xingTrackB = null;
	private TrackSegment xingTrackC = null;
	private TrackSegment xingTrackD = null;
	private boolean levelXingACHorizontal = false;	
	private boolean levelXingACVertical = false;	
	private boolean levelXingALeft = false;
	private boolean levelXingAUp = false;
	private boolean levelXingBUp = false;
	private boolean levelXingBLeft = false;
	private boolean xingFromMenu = false;
	private LevelXing levelXing = null;
	private SignalHead aHead = null;
	private SignalHead bHead = null;
	private SignalHead cHead = null;
	private SignalHead dHead = null;

	// display dialog for Set Signals at Level Crossing tool
	public void setSignalsAtLevelXingFromMenu (LevelXing xing, MultiIconEditor theEditor, 
			JFrame theFrame ) {
		xingFromMenu = true;
		levelXing = xing;
		blockANameField.setText(levelXing.getBlockNameAC());
		blockCNameField.setText(levelXing.getBlockNameBD());
		setSignalsAtLevelXing(theEditor,theFrame);
		return;
	}		
	public void setSignalsAtLevelXing( MultiIconEditor theEditor, JFrame theFrame ) {
		signalIconEditor = theEditor;
		signalFrame = theFrame;
		if (setSignalsAtXingOpen) {
			setSignalsAtXingFrame.setVisible(true);
			return;
		}
		aField.setText("");
		bField.setText("");
		cField.setText("");
		dField.setText("");
		// Initialize if needed
		if (setSignalsAtXingFrame == null) {
            setSignalsAtXingFrame = new JmriJFrame( rb.getString("SignalsAtLevelXing") );
            setSignalsAtXingFrame.addHelpMenu("package.jmri.jmrit.display.SetSignalsAtLevelXing", true);
            setSignalsAtXingFrame.setLocation(70,30);
            Container theContentPane = setSignalsAtXingFrame.getContentPane();        
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));
			JPanel panel11 = new JPanel(); 
            panel11.setLayout(new FlowLayout());
			if (xingFromMenu) {
				JLabel blockANameLabel = new JLabel( rb.getString("BlockAtA")+" "+
							rb.getString("Name")+" : "+levelXing.getBlockNameAC());
			
				panel11.add(blockANameLabel);
			}
			else {
				JLabel blockANameLabel = new JLabel( rb.getString("BlockAtA")+" "+
																rb.getString("Name")+" : ");
				panel11.add(blockANameLabel);
				panel11.add(blockANameField);
				blockANameField.setToolTipText(rb.getString("SignalsBlockNameHint"));
			}
            theContentPane.add(panel11);
			JPanel panel12 = new JPanel(); 
            panel12.setLayout(new FlowLayout());
			if (xingFromMenu) {
				JLabel blockCNameLabel = new JLabel( rb.getString("BlockAtC")+" "+
							rb.getString("Name")+" : "+levelXing.getBlockNameBD());
			
				panel12.add(blockCNameLabel);
			}
			else {
				JLabel blockCNameLabel = new JLabel( rb.getString("BlockAtC")+" "+
																rb.getString("Name")+" : ");
				panel12.add(blockCNameLabel);
				panel12.add(blockCNameField);
				blockCNameField.setToolTipText(rb.getString("SignalsBlockNameHint"));
			}
            theContentPane.add(panel12);
			theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
			JLabel shTitle = new JLabel(rb.getString("SignalHeads"));
			panel2.add(shTitle);
			panel2.add(new JLabel("   "));
            panel2.add(getSavedXingSignalHeads = new JButton(rb.getString("GetSaved")));
            getSavedXingSignalHeads.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						xingSignalsGetSaved(e);
					}
				});
            getSavedXingSignalHeads.setToolTipText( rb.getString("GetSavedHint") );			
			theContentPane.add(panel2);
            JPanel panel21 = new JPanel();
            panel21.setLayout(new FlowLayout());
			JLabel aLabel = new JLabel(rb.getString("ATrack")+" : ");
			panel21.add(aLabel);
			panel21.add(aField);
			theContentPane.add(panel21);
			aField.setToolTipText(rb.getString("SignalHeadNameHint"));
            JPanel panel22 = new JPanel();
            panel22.setLayout(new FlowLayout());
			panel22.add(new JLabel("   "));
			panel22.add(setAHead);
			setAHead.setToolTipText(rb.getString("PlaceHeadHint"));
			panel22.add(new JLabel("  "));
			panel22.add(setupALogic);
			setupALogic.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel22);
            JPanel panel31 = new JPanel();
            panel31.setLayout(new FlowLayout());
			JLabel bLabel = new JLabel(rb.getString("BTrack")+" : ");
			panel31.add(bLabel);
			panel31.add(bField);
			theContentPane.add(panel31);
			bField.setToolTipText(rb.getString("SignalHeadNameHint"));
            JPanel panel32 = new JPanel();
            panel32.setLayout(new FlowLayout());
			panel32.add(new JLabel("   "));
			panel32.add(setBHead);
			setBHead.setToolTipText(rb.getString("PlaceHeadHint"));
			panel32.add(new JLabel("  "));
			panel32.add(setupBLogic);
			setupBLogic.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel32);
            JPanel panel41 = new JPanel();
            panel41.setLayout(new FlowLayout());
			JLabel cLabel = new JLabel(rb.getString("CTrack")+" : ");
			panel41.add(cLabel);
			panel41.add(cField);
			theContentPane.add(panel41);
			cField.setToolTipText(rb.getString("SignalHeadNameHint"));
            JPanel panel42 = new JPanel();
            panel42.setLayout(new FlowLayout());
			panel42.add(new JLabel("   "));
			panel42.add(setCHead);
			setCHead.setToolTipText(rb.getString("PlaceHeadHint"));
			panel42.add(new JLabel("  "));
			panel42.add(setupCLogic);
			setupCLogic.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel42);
            JPanel panel51 = new JPanel();
            panel51.setLayout(new FlowLayout());
			JLabel dLabel = new JLabel(rb.getString("DTrack")+" : ");
			panel51.add(dLabel);
			panel51.add(dField);
			theContentPane.add(panel51);
			dField.setToolTipText(rb.getString("SignalHeadNameHint"));
            JPanel panel52 = new JPanel();
            panel52.setLayout(new FlowLayout());
			panel52.add(new JLabel("   "));
			panel52.add(setDHead);
			setDHead.setToolTipText(rb.getString("PlaceHeadHint"));
			panel52.add(new JLabel("  "));
			panel52.add(setupDLogic);
			setupDLogic.setToolTipText(rb.getString("SetLogicHint"));
			theContentPane.add(panel52);
			theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
            JPanel panel6 = new JPanel();
            panel6.setLayout(new FlowLayout());
            panel6.add(changeXingSignalIcon = new JButton(rb.getString("ChangeSignalIcon")));
            changeXingSignalIcon.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						signalFrame.show();
					}
				});
            changeXingSignalIcon.setToolTipText( rb.getString("ChangeSignalIconHint") );
			panel6.add(new JLabel("  "));
            panel6.add(setXingSignalsDone = new JButton(rb.getString("Done")));
            setXingSignalsDone.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setXingSignalsDonePressed(e);
                }
            });
            setXingSignalsDone.setToolTipText( rb.getString("SignalDoneHint") );
            panel6.add(setXingSignalsCancel = new JButton(rb.getString("Cancel")));
            setXingSignalsCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setXingSignalsCancelPressed(e);
                }
            });
            setXingSignalsCancel.setToolTipText( rb.getString("CancelHint") );
            theContentPane.add(panel6);
			setSignalsAtXingFrame.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					setXingSignalsCancelPressed(null);
				}
			});
		}
        setSignalsAtXingFrame.pack();
        setSignalsAtXingFrame.setVisible(true);		
		setSignalsAtXingOpen = true;
	}	
	private void xingSignalsGetSaved (ActionEvent a) {
		if ( !getLevelCrossingInformation() ) return;
		aField.setText(levelXing.getSignalAName());	
		bField.setText(levelXing.getSignalBName());
		cField.setText(levelXing.getSignalCName());	
		dField.setText(levelXing.getSignalDName());	
	}
	private void setXingSignalsCancelPressed (ActionEvent a) {
		setSignalsAtXingOpen = false;
		setSignalsAtXingFrame.setVisible(false);
		xingFromMenu = false;
	}
	private void setXingSignalsDonePressed (ActionEvent a) {
		if ( !getLevelCrossingInformation() ) return;
		if ( !getXingSignalHeadInformation() ) return;
		// place or update signals as requested
		if ( (aHead!=null) && setAHead.isSelected() ) {
			if (isHeadOnPanel(aHead) && 
					(aHead!=getHeadFromName(levelXing.getSignalAName())) ) { 
				JOptionPane.showMessageDialog(setSignalsAtXingFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new String[]{aField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!levelXingACHorizontal) && (!levelXingACVertical) ) {
				JOptionPane.showMessageDialog(setSignalsAtXingFrame,
					rb.getString("InfoMessage3"),"",JOptionPane.INFORMATION_MESSAGE);
				if ( aHead!=getHeadFromName(levelXing.getSignalAName()) ) {				
					removeSignalHeadFromPanel(levelXing.getSignalAName());
					removeAssignment(aHead);
					levelXing.setSignalAName(aField.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(levelXing.getSignalAName());
				placeXingA();
				removeAssignment(aHead);
				levelXing.setSignalAName(aField.getText().trim());
				needRedraw = true;
			}		
		}
		else if ( (aHead!=null) && 
				(aHead!=getHeadFromName(levelXing.getSignalAName())) &&
				(aHead!=getHeadFromName(levelXing.getSignalBName())) &&
				(aHead!=getHeadFromName(levelXing.getSignalCName())) &&
				(aHead!=getHeadFromName(levelXing.getSignalDName())) ) {
			if (isHeadOnPanel(aHead)) {
				JOptionPane.showMessageDialog(setSignalsAtXingFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError13"),
						new String[]{aField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}		
			else {
				removeSignalHeadFromPanel(levelXing.getSignalAName());
				removeAssignment(aHead);
				levelXing.setSignalAName(aField.getText().trim());
			}
		}
		else if ( (aHead!=null) &&  
				( (aHead==getHeadFromName(levelXing.getSignalBName())) ||
					(aHead==getHeadFromName(levelXing.getSignalCName())) ||
					(aHead==getHeadFromName(levelXing.getSignalDName())) ) ) {
// need to figure out what to do in this case.			
		}
		else if (aHead==null) {
			removeSignalHeadFromPanel(levelXing.getSignalAName());
			levelXing.setSignalAName("");		
		}
		if ( (bHead!=null) && setBHead.isSelected() ) {
			if (isHeadOnPanel(bHead) && 
					(bHead!=getHeadFromName(levelXing.getSignalBName()))) { 
				JOptionPane.showMessageDialog(setSignalsAtXingFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new String[]{bField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!levelXingACHorizontal) && (!levelXingACVertical) ) {
				JOptionPane.showMessageDialog(setSignalsAtXingFrame,
					rb.getString("InfoMessage3"),"",JOptionPane.INFORMATION_MESSAGE);
				if (bHead!=getHeadFromName(levelXing.getSignalBName())) {				
					removeSignalHeadFromPanel(levelXing.getSignalBName());
					removeAssignment(bHead);
					levelXing.setSignalBName(bField.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(levelXing.getSignalBName());
				placeXingB();
				removeAssignment(bHead);
				levelXing.setSignalBName(bField.getText().trim());
				needRedraw = true;
			}		
		}
		else if ( (bHead!=null) && 
				(bHead!=getHeadFromName(levelXing.getSignalAName())) &&
				(bHead!=getHeadFromName(levelXing.getSignalBName())) &&
				(bHead!=getHeadFromName(levelXing.getSignalCName())) &&
				(bHead!=getHeadFromName(levelXing.getSignalDName())) ) {
			if (isHeadOnPanel(bHead)) {
				JOptionPane.showMessageDialog(setSignalsAtXingFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError13"),
						new String[]{bField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}		
			else {
				removeSignalHeadFromPanel(levelXing.getSignalBName());
				removeAssignment(bHead);
				levelXing.setSignalBName(bField.getText().trim());
			}
		}
		else if ( (bHead!=null) &&  
				( (bHead==getHeadFromName(levelXing.getSignalAName())) ||
					(bHead==getHeadFromName(levelXing.getSignalCName())) ||
					(bHead==getHeadFromName(levelXing.getSignalDName())) ) ) {
// need to figure out what to do in this case.			
		}
		else if (bHead==null) {
			removeSignalHeadFromPanel(levelXing.getSignalBName());
			levelXing.setSignalBName("");		
		}
		if ( (cHead!=null) && setCHead.isSelected() ) {
			if (isHeadOnPanel(cHead) && 
					(cHead!=getHeadFromName(levelXing.getSignalCName())) ) { 
				JOptionPane.showMessageDialog(setSignalsAtXingFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new String[]{cField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!levelXingACHorizontal) && (!levelXingACVertical) ) {
				JOptionPane.showMessageDialog(setSignalsAtXingFrame,
					rb.getString("InfoMessage3"),"",JOptionPane.INFORMATION_MESSAGE);
				if ( cHead!=getHeadFromName(levelXing.getSignalCName()) ) {				
					removeSignalHeadFromPanel(levelXing.getSignalCName());
					removeAssignment(cHead);
					levelXing.setSignalCName(cField.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(levelXing.getSignalCName());
				placeXingC();
				removeAssignment(cHead);
				levelXing.setSignalCName(cField.getText().trim());
				needRedraw = true;
			}		
		}
		else if ( (cHead!=null) && 
				(cHead!=getHeadFromName(levelXing.getSignalAName())) &&
				(cHead!=getHeadFromName(levelXing.getSignalBName())) &&
				(cHead!=getHeadFromName(levelXing.getSignalCName())) &&
				(cHead!=getHeadFromName(levelXing.getSignalDName())) ) {
			if (isHeadOnPanel(cHead)) {
				JOptionPane.showMessageDialog(setSignalsAtXingFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError13"),
						new String[]{cField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}		
			else {
				removeSignalHeadFromPanel(levelXing.getSignalCName());
				removeAssignment(cHead);
				levelXing.setSignalCName(cField.getText().trim());
			}
		}
		else if ( (cHead!=null) &&  
				( (cHead==getHeadFromName(levelXing.getSignalBName())) ||
					(cHead==getHeadFromName(levelXing.getSignalAName())) ||
					(cHead==getHeadFromName(levelXing.getSignalDName())) ) ) {
// need to figure out what to do in this case.			
		}
		else if (cHead==null) {
			removeSignalHeadFromPanel(levelXing.getSignalCName());
			levelXing.setSignalCName("");		
		}
		if ( (dHead!=null) && setDHead.isSelected() ) {
			if (isHeadOnPanel(dHead) && 
					(dHead!=getHeadFromName(levelXing.getSignalDName())) ) { 
				JOptionPane.showMessageDialog(setSignalsAtXingFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError6"),
						new String[]{dField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( (!levelXingACHorizontal) && (!levelXingACVertical) ) {
				JOptionPane.showMessageDialog(setSignalsAtXingFrame,
					rb.getString("InfoMessage3"),"",JOptionPane.INFORMATION_MESSAGE);
				if ( dHead!=getHeadFromName(levelXing.getSignalDName()) ) {				
					removeSignalHeadFromPanel(levelXing.getSignalDName());
					removeAssignment(dHead);
					levelXing.setSignalDName(dField.getText().trim());
				}
			}				
			else {
				removeSignalHeadFromPanel(levelXing.getSignalDName());
				placeXingD();
				removeAssignment(dHead);
				levelXing.setSignalDName(dField.getText().trim());
				needRedraw = true;
			}		
		}
		else if ( (dHead!=null) && 
				(dHead!=getHeadFromName(levelXing.getSignalAName())) &&
				(dHead!=getHeadFromName(levelXing.getSignalBName())) &&
				(dHead!=getHeadFromName(levelXing.getSignalCName())) &&
				(dHead!=getHeadFromName(levelXing.getSignalDName())) ) {
			if (isHeadOnPanel(dHead)) {
				JOptionPane.showMessageDialog(setSignalsAtXingFrame,
					java.text.MessageFormat.format(rb.getString("SignalsError13"),
						new String[]{dField.getText().trim()}), 
							rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return;
			}		
			else {
				removeSignalHeadFromPanel(levelXing.getSignalDName());
				removeAssignment(dHead);
				levelXing.setSignalDName(dField.getText().trim());
			}
		}
		else if ( (dHead!=null) &&  
				( (dHead==getHeadFromName(levelXing.getSignalBName())) ||
					(dHead==getHeadFromName(levelXing.getSignalCName())) ||
					(dHead==getHeadFromName(levelXing.getSignalAName())) ) ) {
// need to figure out what to do in this case.			
		}
		else if (dHead==null) {
			removeSignalHeadFromPanel(levelXing.getSignalDName());
			levelXing.setSignalDName("");		
		}
		// setup logic if requested
		if ( setupALogic.isSelected() && (aHead!=null) ) {
			setLogicXing( aHead, (TrackSegment)levelXing.getConnectC(),
				levelXing.getLayoutBlockBD(), (TrackSegment)levelXing.getConnectB(),
					(TrackSegment)levelXing.getConnectD(), aField.getText());
		}
		if ( setupBLogic.isSelected() && (bHead!=null) ) {
			setLogicXing( bHead, (TrackSegment)levelXing.getConnectD(),
				levelXing.getLayoutBlockAC(), (TrackSegment)levelXing.getConnectA(),
					(TrackSegment)levelXing.getConnectC(), bField.getText());
		}
		if ( setupCLogic.isSelected() && (cHead!=null) ) {
			setLogicXing( cHead, (TrackSegment)levelXing.getConnectA(),
				levelXing.getLayoutBlockBD(), (TrackSegment)levelXing.getConnectB(),
					(TrackSegment)levelXing.getConnectD(), cField.getText());
		}
		if ( setupDLogic.isSelected() && (dHead!=null) ) {
			setLogicXing( dHead, (TrackSegment)levelXing.getConnectB(),
				levelXing.getLayoutBlockAC(), (TrackSegment)levelXing.getConnectA(),
					(TrackSegment)levelXing.getConnectC(), dField.getText());
		}
		// finish up
		setSignalsAtXingOpen = false;
		setSignalsAtXingFrame.setVisible(false);
		xingFromMenu = false;	
		if (needRedraw) {
			layoutEditor.redrawPanel();
			needRedraw = false;
			layoutEditor.setDirty();
		}		
	}
	private boolean getLevelCrossingInformation() {
		if (!xingFromMenu) {
			levelXing = null;
			if (layoutEditor.xingList.size()<=0) {
				JOptionPane.showMessageDialog(setSignalsAtXingFrame,
					rb.getString("SignalsError15"),
								rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
				return false;
			}
			else if (layoutEditor.xingList.size()==1) {
				levelXing = (LevelXing)layoutEditor.xingList.get(0);
			}
			else {
				LayoutBlock xingBlockA = null;
				LayoutBlock xingBlockC = null;
				xingBlockA = getBlockFromEntry(blockANameField);
				if (xingBlockA==null) return false;
				if (blockCNameField.getText().trim().length()>0) {
					xingBlockC = getBlockFromEntry(blockCNameField);
					if (xingBlockC==null) return false;
				}
				LevelXing x = null;
				int foundCount = 0;
				// make two block tests first
				if (xingBlockC!=null) {
					for (int i = 0;(i<layoutEditor.xingList.size());i++) {
						x = (LevelXing)layoutEditor.xingList.get(i);
						LayoutBlock xA = null;
						LayoutBlock xB = null;
						LayoutBlock xC = null;
						LayoutBlock xD = null;
						LayoutBlock xAC = x.getLayoutBlockAC();
						LayoutBlock xBD = x.getLayoutBlockBD();
						if (x.getConnectA()!=null) xA = ((TrackSegment)x.getConnectA()).getLayoutBlock();
						if (x.getConnectB()!=null) xB = ((TrackSegment)x.getConnectB()).getLayoutBlock();
						if (x.getConnectC()!=null) xC = ((TrackSegment)x.getConnectC()).getLayoutBlock();
						if (x.getConnectD()!=null) xD = ((TrackSegment)x.getConnectD()).getLayoutBlock();
						if ( ( (xA!=null) && (xC!=null) && ( ((xA==xingBlockA)&&(xC==xingBlockC)) ||
								((xA==xingBlockC)&&(xC==xingBlockA)) ) ) ||
								( (xB!=null) && (xD!=null) && ( ((xB==xingBlockA)&&(xD==xingBlockC)) ||
								((xB==xingBlockC)&&(xD==xingBlockA)) ) ) ) {
							levelXing = x;
							foundCount ++;
						}
						else if ( (xAC!=null) && (xBD!=null) && ( ((xAC==xingBlockA) && (xBD==xingBlockC)) ||
									((xAC==xingBlockC) && (xBD==xingBlockA)) ) ) {
							levelXing = x;
							foundCount ++;
						}				
					}
				}
				if (foundCount==0) {
					// try one block test
					for (int i = 0;(i<layoutEditor.xingList.size());i++) {
						x = (LevelXing)layoutEditor.xingList.get(i);
						if ((xingBlockA == x.getLayoutBlockAC()) || (xingBlockA == x.getLayoutBlockBD())) {
							levelXing = x;
							foundCount ++;
						}				
					}
				}
				if (foundCount>1) {
					JOptionPane.showMessageDialog(setSignalsAtXingFrame,
							java.text.MessageFormat.format(rb.getString("SignalsError16"),
								new String[]{" "+foundCount+" "}), 
									rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return false;
				}
				if (levelXing==null) {
					JOptionPane.showMessageDialog(setSignalsAtXingFrame,
								rb.getString("SignalsError17"),
										rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
		}
		if (levelXing.getConnectA()!=null) xingTrackA = ((TrackSegment)levelXing.getConnectA());
		if (levelXing.getConnectB()!=null) xingTrackB = ((TrackSegment)levelXing.getConnectB());
		if (levelXing.getConnectC()!=null) xingTrackC = ((TrackSegment)levelXing.getConnectC());
		if (levelXing.getConnectD()!=null) xingTrackD = ((TrackSegment)levelXing.getConnectD());
		double delX = levelXing.getCoordsA().getX() - levelXing.getCoordsC().getX();
		double delY = levelXing.getCoordsA().getY() - levelXing.getCoordsC().getY();
		levelXingACHorizontal = false;
		levelXingACVertical = false;
		levelXingALeft = false;
		levelXingAUp = false;
		levelXingBUp = false;
		levelXingBLeft = false;
		if (java.lang.Math.abs(delX) > 2.0*java.lang.Math.abs(delY)) {
			levelXingACHorizontal = true;
			if (delX < 0.0) levelXingALeft = true;
			if (levelXing.getCoordsB().getY() < levelXing.getCoordsD().getY())
				levelXingBUp = true;
		}
		else if (java.lang.Math.abs(delY) > 2.0*java.lang.Math.abs(delX)) { 
			levelXingACVertical = true;
			if (delY <0.0) levelXingAUp = true;
			if (levelXing.getCoordsB().getX() < levelXing.getCoordsD().getX())
				levelXingBLeft = true;
		}
		return true;
	}
	private boolean getXingSignalHeadInformation() {
		// note that all heads are optional, but pairs must be present
		aHead = getSignalHeadFromEntry(aField,false,setSignalsAtXingFrame);
		bHead = getSignalHeadFromEntry(bField,false,setSignalsAtXingFrame);
		cHead = getSignalHeadFromEntry(cField,false,setSignalsAtXingFrame);
		dHead = getSignalHeadFromEntry(dField,false,setSignalsAtXingFrame);
		if ( ((aHead!=null) && (cHead==null)) || ((aHead==null) && (cHead!=null)) ||
				((bHead!=null) && (dHead==null)) || ((bHead==null) && (dHead!=null)) ) {
			JOptionPane.showMessageDialog(setSignalsAtXingFrame,
					rb.getString("SignalsError14"),
						rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if ( (aHead==null) && (bHead==null) && (cHead==null) && (dHead==null) ) {
			JOptionPane.showMessageDialog(setSignalsAtXingFrame,
					rb.getString("SignalsError12"),
						rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	private void placeXingA() {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		Point2D p = levelXing.getCoordsA();
		if (levelXingACHorizontal && levelXingALeft) {
			setSignalHeadOnPanel(2,aField.getText().trim(),
				(int)(p.getX()-testIcon.getIconWidth()),
				(int)(p.getY()+4) );
		}
		else if (levelXingACHorizontal && (!levelXingALeft) ) {
			setSignalHeadOnPanel(0,aField.getText().trim(),
				(int)(p.getX()),
				(int)(p.getY()-4-testIcon.getIconHeight()) );
		}
		else if (levelXingACVertical && levelXingAUp) {
			setSignalHeadOnPanel(1,aField.getText().trim(),
				(int)(p.getX()-2-testIcon.getIconWidth()),
				(int)(p.getY()-testIcon.getIconHeight()) );
		}
		else if (levelXingACVertical && (!levelXingAUp) ) {
			setSignalHeadOnPanel(3,aField.getText().trim(),
				(int)(p.getX()+4),
				(int)(p.getY()+2) );
		}
	}
	private void placeXingB() {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		Point2D p = levelXing.getCoordsB();
		if (levelXingACVertical && levelXingBLeft) {
			setSignalHeadOnPanel(2,bField.getText().trim(),
				(int)(p.getX()-testIcon.getIconWidth()),
				(int)(p.getY()+4) );
		}
		else if (levelXingACVertical && (!levelXingBLeft) ) {
			setSignalHeadOnPanel(0,bField.getText().trim(),
				(int)(p.getX()),
				(int)(p.getY()-4-testIcon.getIconHeight()) );
		}
		else if (levelXingACHorizontal && levelXingBUp) {
			setSignalHeadOnPanel(1,bField.getText().trim(),
				(int)(p.getX()-2-testIcon.getIconWidth()),
				(int)(p.getY()-testIcon.getIconHeight()) );
		}
		else if (levelXingACHorizontal && (!levelXingBUp) ) {
			setSignalHeadOnPanel(3,bField.getText().trim(),
				(int)(p.getX()+4),
				(int)(p.getY()+2) );
		}
	}
	private void placeXingC() {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		Point2D p = levelXing.getCoordsC();
		if (levelXingACHorizontal && (!levelXingALeft) ) {
			setSignalHeadOnPanel(2,cField.getText().trim(),
				(int)(p.getX()-testIcon.getIconWidth()),
				(int)(p.getY()+4) );
		}
		else if (levelXingACHorizontal && levelXingALeft) {
			setSignalHeadOnPanel(0,cField.getText().trim(),
				(int)(p.getX()),
				(int)(p.getY()-4-testIcon.getIconHeight()) );
		}
		else if (levelXingACVertical && (!levelXingAUp) ) {
			setSignalHeadOnPanel(1,cField.getText().trim(),
				(int)(p.getX()-2-testIcon.getIconWidth()),
				(int)(p.getY()-testIcon.getIconHeight()) );
		}
		else if (levelXingACVertical && levelXingAUp) {
			setSignalHeadOnPanel(3,cField.getText().trim(),
				(int)(p.getX()+4),
				(int)(p.getY()+2) );
		}
	}
	private void placeXingD() {
		if (testIcon == null)
			testIcon = signalIconEditor.getIcon(0);
		Point2D p = levelXing.getCoordsD();
		if (levelXingACVertical && (!levelXingBLeft) ) {
			setSignalHeadOnPanel(2,dField.getText().trim(),
				(int)(p.getX()-testIcon.getIconWidth()),
				(int)(p.getY()+4) );
		}
		else if (levelXingACVertical && levelXingBLeft) {
			setSignalHeadOnPanel(0,dField.getText().trim(),
				(int)(p.getX()),
				(int)(p.getY()-4-testIcon.getIconHeight()) );
		}
		else if (levelXingACHorizontal && (!levelXingBUp) ) {
			setSignalHeadOnPanel(1,dField.getText().trim(),
				(int)(p.getX()-2-testIcon.getIconWidth()),
				(int)(p.getY()-testIcon.getIconHeight()) );
		}
		else if (levelXingACHorizontal && levelXingBUp) {
			setSignalHeadOnPanel(3,dField.getText().trim(),
				(int)(p.getX()+4),
				(int)(p.getY()+2) );
		}
	}
	private void setLogicXing(SignalHead head, TrackSegment track, LayoutBlock crossBlock,
				TrackSegment crossTrack1, TrackSegment crossTrack2, String headName) {
		if (track==null) {
			JOptionPane.showMessageDialog(setSignalsAtXingFrame,
					rb.getString("InfoMessage7"),"",JOptionPane.INFORMATION_MESSAGE);			
			return;
		}
		Sensor occupancy = null;
		Sensor crossOccupancy = null;
		Sensor track1Occupancy = null;
		Sensor track2Occupancy = null;
		SignalHead nextHead = null;
		LayoutBlock block = track.getLayoutBlock();
		if (block==null) {
			JOptionPane.showMessageDialog(setSignalsAtXingFrame,
					rb.getString("InfoMessage6"),"",JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		occupancy = block.getOccupancySensor();
		if (occupancy==null) {
			JOptionPane.showMessageDialog(setSignalsAtXingFrame,
					java.text.MessageFormat.format(rb.getString("InfoMessage4"),
						new String[]{block.getUserName()}), 
							null,JOptionPane.INFORMATION_MESSAGE);						
			return;
		}
		if (crossBlock!=null) {
			crossOccupancy = crossBlock.getOccupancySensor();
		}
		if (crossTrack1!=null) {
			block = crossTrack1.getLayoutBlock();
			if (block!=null) {
				track1Occupancy = block.getOccupancySensor();
				if (track1Occupancy==crossOccupancy) track1Occupancy = null;
			}
		}
		if (crossTrack2!=null) {
			block = crossTrack2.getLayoutBlock();
			if (block!=null) {
				track2Occupancy = block.getOccupancySensor();
				if ( (track2Occupancy==crossOccupancy) ||
				     (track2Occupancy==track1Occupancy) ) track2Occupancy = null;
			}
		}			
		nextHead = getNextSignalFromObject(track,(Object)levelXing);
		if ( (nextHead==null) && (!reachedEndBumper()) ) {
			JOptionPane.showMessageDialog(setSignalsAtXingFrame,
					java.text.MessageFormat.format(rb.getString("InfoMessage5"),
						new String[]{block.getUserName()}), 
							null,JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if ( (crossOccupancy==null) && (track1Occupancy==null) && (track2Occupancy==null) ) {
			JOptionPane.showMessageDialog(setSignalsAtXingFrame,
					java.text.MessageFormat.format(rb.getString("SignalsWarn1"),
						new String[]{headName}), 
							null,JOptionPane.WARNING_MESSAGE);
		}
		if (!initializeBlockBossLogic(head.getSystemName())) return;
		logic.setMode(BlockBossLogic.SINGLEBLOCK);
		logic.setSensor1(occupancy.getSystemName());
		if (nextHead!=null) {
			logic.setWatchedSignal1(nextHead.getSystemName(),false);
		}
		if (auxSignal!=null) {
			logic.setWatchedSignal1Alt(auxSignal.getSystemName());
		}
		if (crossOccupancy!=null) {
			logic.setSensor2(crossOccupancy.getSystemName());
			if (track1Occupancy!=null) {
				logic.setSensor3(track1Occupancy.getSystemName());
				if (track2Occupancy!=null) 
					logic.setSensor4(track2Occupancy.getSystemName());
			}
			else if (track2Occupancy!=null)
				logic.setSensor3(track2Occupancy.getSystemName());
		}
		else if (track1Occupancy!=null) {
			logic.setSensor2(track1Occupancy.getSystemName());
			if (track2Occupancy!=null)
				logic.setSensor3(track2Occupancy.getSystemName());
		}
		else if (track2Occupancy!=null)
			logic.setSensor2(track2Occupancy.getSystemName());
		finalizeBlockBossLogic();
	}

    /**
     * Clean up when this object is no longer needed.  Should not
     * be called while the object is still displayed; see remove()
     */
    public void dispose() {
    }

	// initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LayoutEditorTools.class.getName());
}
