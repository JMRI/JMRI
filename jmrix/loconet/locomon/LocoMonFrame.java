/** 
 * LocoMonFrame.java
 *
 * Description:		Frame displaying (and logging) LocoNet messages
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */

package jmri.jmrix.loconet.locomon;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Date;
import java.text.DateFormat;
import java.io.File;
import java.io.PrintStream;
import java.io.FileOutputStream;

import jmri.jmrix.loconet.LocoNetListener;
import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetMessage;

public class LocoMonFrame extends javax.swing.JFrame implements LocoNetListener {

	// member declarations
	javax.swing.JButton clearButton = new javax.swing.JButton();
	javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
	javax.swing.JTextPane locoMonTextPane = new javax.swing.JTextPane();
	javax.swing.JButton startLogButton = new javax.swing.JButton();
	javax.swing.JButton stopLogButton = new javax.swing.JButton();
	javax.swing.JCheckBox hexCheckBox = new javax.swing.JCheckBox();
	javax.swing.JButton openFileChooserButton = new javax.swing.JButton();
	
	// to find and remember the log file
	final javax.swing.JFileChooser logFileChooser = new JFileChooser();

	public LocoMonFrame() {
	}

	public void initComponents() throws Exception {
		// the following code sets the frame's initial state

		clearButton.setText("Clear screen");
		clearButton.setVisible(true);
		clearButton.setToolTipText("Clear monitoring history");

		jScrollPane1.setVisible(true);
		jScrollPane1.getViewport().add(locoMonTextPane);

		locoMonTextPane.setVisible(true);
		locoMonTextPane.setPreferredSize(new java.awt.Dimension(40,100));
		locoMonTextPane.setToolTipText("LocoNet monitoring information appears here");
		locoMonTextPane.setEditable(false);

		startLogButton.setText("Start log file");
		startLogButton.setVisible(true);
		startLogButton.setToolTipText("start logging to file");

		stopLogButton.setText("Stop log file");
		stopLogButton.setVisible(true);
		stopLogButton.setToolTipText("Stop logging to file");

		hexCheckBox.setText("Show raw data");
		hexCheckBox.setVisible(true);
		hexCheckBox.setToolTipText("If checked, show the raw LocoNet packets in hex");

		openFileChooserButton.setText("Choose log file");
		openFileChooserButton.setVisible(true);
		openFileChooserButton.setToolTipText("Click here to select a new output log file");

		// set sizes to same
		clearButton.setMaximumSize(openFileChooserButton.getMaximumSize());
		hexCheckBox.setMaximumSize(openFileChooserButton.getMaximumSize());
		startLogButton.setMaximumSize(openFileChooserButton.getMaximumSize());
		stopLogButton.setMaximumSize(openFileChooserButton.getMaximumSize());

		setTitle("LocoNet monitoring");
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		
		// add items to GUI
		getContentPane().add(jScrollPane1);
		
		JPanel paneA = new JPanel();
			paneA.setLayout(new BoxLayout(paneA, BoxLayout.X_AXIS));
			JPanel pane1 = new JPanel();
				pane1.setLayout(new BoxLayout(pane1, BoxLayout.Y_AXIS));
				pane1.add(clearButton);
				pane1.add(hexCheckBox);
			paneA.add(pane1);
		
			JPanel pane2 = new JPanel();
				pane2.setLayout(new BoxLayout(pane2, BoxLayout.Y_AXIS));
				pane2.add(startLogButton);
				pane2.add(stopLogButton);
				pane2.add(openFileChooserButton);
			paneA.add(pane2);
		getContentPane().add(paneA);

		// connect actions to buttons
		clearButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				clearButtonActionPerformed(e);
			}
		});
		startLogButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				startLogButtonActionPerformed(e);
			}
		});
		stopLogButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				stopLogButtonActionPerformed(e);
			}
		});
		openFileChooserButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				openFileChooserButtonActionPerformed(e);
			}
		});
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				thisWindowClosing(e);
			}
		});
		
		// set file chooser to a default
		logFileChooser.setSelectedFile(new File("LocoNetLog.txt"));
	}
  
  	private boolean mShown = false;
  	
	public void addNotify() {
		super.addNotify();
		
		if (mShown)
			return;
			
		// resize frame to account for menubar
		JMenuBar jMenuBar = getJMenuBar();
		if (jMenuBar != null) {
			int jMenuBarHeight = jMenuBar.getPreferredSize().height;
			java.awt.Dimension dimension = getSize();
			dimension.height += jMenuBarHeight;
			setSize(dimension);
		}

		mShown = true;
	}

	// Close the window when the close box is clicked
	void thisWindowClosing(java.awt.event.WindowEvent e) {
		setVisible(false);
		dispose();
	// and disconnect from the LnTrafficController
	LnTrafficController.instance().removeLocoNetListener(~0,this);
	}
	
	public synchronized void message(LocoNetMessage l) {  // receive a LocoNet message and log it
		// !! limit length; without that, Swing eventually 
		// slows down	
	
		// !! ugly manual way of handling N lines _only_
		s20 = s19;
		s19 = s18;
		s18 = s17;
		s17 = s16;
		s16 = s15;
		s15 = s14;
		s14 = s13;
		s13 = s12;
		s12 = s11;
		s11 = s10;
		s10 = s9;
		s9 = s8;
		s8 = s7;
		s7 = s6;
		s6 = s5;
		s5 = s4;
		s4 = s3;
		s3 = s2;
		s2 = s1;
		s1 = "";
	
		// String sOld = locoMonTextPane.getText();
	
		// display the raw data if requested
		if ( hexCheckBox.isSelected() ) {
			s1 = "time: "+df.format(new Date())+"\tpacket: ";
			int len = l.getNumDataElements();
			for (int i=0; i<len; i++)
				s1 += Integer.toHexString(l.getElement(i))+" ";
			s1 += "\n";
		}

		// display the decoded data
		// we use Llnmon to format, expect it to provide consistent \n after each line
		s1 += llnmon.displayMessage(l);

		// display it in the Swing thread
		Runnable r = new Runnable() {
			public void run() { locoMonTextPane.setText(getFrameText()); }
			};
		javax.swing.SwingUtilities.invokeLater(r);
		
		// if requested, log to a file.
		if (logStream != null) {
			logStream.print(s1);
		}
	}
	
	public synchronized String getFrameText() {
		return 	s20+s19+s18+s17+s16
				+s15+s14+s13+s12+s11
				+s10+s9+s8+s7+s6
				+s5+s4+s3+s2+s1;
	}

	public synchronized void clearButtonActionPerformed(java.awt.event.ActionEvent e) {
		// clear the monitoring history
		locoMonTextPane.setText("");
		s1=s2=s3=s4=s5=s6=s7=s8=s9=s10 = "";
		s11=s12=s13=s14=s15=s16=s17=s18=s19=s20 = "";
	}
		
	public synchronized void startLogButtonActionPerformed(java.awt.event.ActionEvent e) {
		// start logging by creating the stream
		if ( logStream==null) {  // successive clicks don't restart the file
			// start logging
			try {
				logStream = new PrintStream (new FileOutputStream(logFileChooser.getSelectedFile()));
			} catch (Exception ex) {
				log.error("exception "+ex);
			}
		}
	}
	
	public synchronized void stopLogButtonActionPerformed(java.awt.event.ActionEvent e) {
		// stop logging by removing the stream
		if (logStream!=null) {
			logStream.flush();
			logStream.close();
			}
		logStream = null;
	}
	
	public void openFileChooserButtonActionPerformed(java.awt.event.ActionEvent e) {
		// start at current file, show dialog
		int retVal = logFileChooser.showSaveDialog(this);

		// handle selection or cancel
		if (retVal == JFileChooser.APPROVE_OPTION) {
			boolean loggingNow = (logStream != null);
			stopLogButtonActionPerformed(e);  // stop before changing file
			File file = logFileChooser.getSelectedFile();
			// if we were currently logging, start the new file
			if (loggingNow) startLogButtonActionPerformed(e); 
		}
	}
	
	
	
	
	PrintStream logStream = null;
	Llnmon llnmon = new Llnmon();

	// remember old messages
	String s1 = "";
	String s2 = "";
	String s3 = "";
	String s4 = "";
	String s5 = "";
	String s6 = "";
	String s7 = "";
	String s8 = "";
	String s9 = "";
	String s10 = "";
	String s11 = "";
	String s12 = "";
	String s13 = "";
	String s14 = "";
	String s15 = "";
	String s16 = "";
	String s17 = "";
	String s18 = "";
	String s19 = "";
	String s20 = "";
	
	// to get a time string
	DateFormat df = DateFormat.getTimeInstance();
	
   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoMonFrame.class.getName());

}
