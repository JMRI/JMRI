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

import ErrLoggerJ.ErrLog;

public class LocoMonFrame extends javax.swing.JFrame implements LocoNetListener {

// IMPORTANT: Source code between BEGIN/END comment pair will be regenerated
// every time the form is saved. All manual changes will be overwritten.
// BEGIN GENERATED CODE
	// member declarations
	javax.swing.JButton clearButton = new javax.swing.JButton();
	javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
	javax.swing.JTextPane locoMonTextPane = new javax.swing.JTextPane();
	javax.swing.JTextField logFileTextField = new javax.swing.JTextField();
	javax.swing.JButton startLogButton = new javax.swing.JButton();
	javax.swing.JButton stopLogButton = new javax.swing.JButton();
	javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
	javax.swing.JCheckBox hexCheckBox = new javax.swing.JCheckBox();
	javax.swing.JButton openFileChooserButton = new javax.swing.JButton();
// END GENERATED CODE

	public LocoMonFrame() {
	}

	public void initComponents() throws Exception {
// IMPORTANT: Source code between BEGIN/END comment pair will be regenerated
// every time the form is saved. All manual changes will be overwritten.
// BEGIN GENERATED CODE
		// the following code sets the frame's initial state

		clearButton.setText("Clear");
		clearButton.setLocation(new java.awt.Point(170, 280));
		clearButton.setVisible(true);
		clearButton.setToolTipText("Clear to clear monitoring history");
		clearButton.setSize(new java.awt.Dimension(80, 30));

		jScrollPane1.setLocation(new java.awt.Point(10, 10));
		jScrollPane1.setVisible(true);
		jScrollPane1.setSize(new java.awt.Dimension(550, 260));
		jScrollPane1.getViewport().add(locoMonTextPane);

		locoMonTextPane.setVisible(true);
		locoMonTextPane.setToolTipText("LocoNet monitoring information appears here");
		locoMonTextPane.setEditable(false);

		logFileTextField.setText("loconetLog.txt");
		logFileTextField.setLocation(new java.awt.Point(360, 280));
		logFileTextField.setVisible(true);
		logFileTextField.setToolTipText("Enter log file name");
		logFileTextField.setSize(new java.awt.Dimension(150, 30));

		startLogButton.setText("start");
		startLogButton.setLocation(new java.awt.Point(360, 320));
		startLogButton.setVisible(true);
		startLogButton.setToolTipText("start logging");
		startLogButton.setSize(new java.awt.Dimension(70, 20));

		stopLogButton.setText("stop");
		stopLogButton.setLocation(new java.awt.Point(440, 320));
		stopLogButton.setVisible(true);
		stopLogButton.setToolTipText("stop logging");
		stopLogButton.setSize(new java.awt.Dimension(70, 20));

		jLabel1.setText("Log to file:");
		jLabel1.setLocation(new java.awt.Point(290, 280));
		jLabel1.setVisible(true);
		jLabel1.setSize(new java.awt.Dimension(70, 30));

		hexCheckBox.setText("show raw data");
		hexCheckBox.setLocation(new java.awt.Point(26, 298));
		hexCheckBox.setVisible(true);
		hexCheckBox.setToolTipText("if checked, show the raw LocoNet packets in hex");
		hexCheckBox.setSize(new java.awt.Dimension(104, 21));

		openFileChooserButton.setText("Log file:");
		openFileChooserButton.setLocation(new java.awt.Point(260, 320));
		openFileChooserButton.setVisible(true);
		openFileChooserButton.setToolTipText("Click here to select a new output log file");
		openFileChooserButton.setSize(new java.awt.Dimension(80, 20));

		setLocation(new java.awt.Point(400, 10));
		setTitle("LocoNet monitoring");
		getContentPane().setLayout(null);
		setSize(new java.awt.Dimension(567, 352));
		getContentPane().add(clearButton);
		getContentPane().add(jScrollPane1);
		getContentPane().add(logFileTextField);
		getContentPane().add(startLogButton);
		getContentPane().add(stopLogButton);
		getContentPane().add(jLabel1);
		getContentPane().add(hexCheckBox);
		getContentPane().add(openFileChooserButton);


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

// END GENERATED CODE
		
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
			Dimension dimension = getSize();
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
				logStream = new PrintStream (new FileOutputStream(logFileTextField.getText()));
			} catch (Exception ex) {
				ErrLog.msg(ErrLog.error, "LocMonFrame", "logToFileToggleButtonStateChanged",""+ex);
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
		fc.setSelectedFile(new File(logFileTextField.getText()));
		int retVal = fc.showSaveDialog(this);

		// handle selection or cancel
		if (retVal == JFileChooser.APPROVE_OPTION) {
			boolean loggingNow = (logStream != null);
			stopLogButtonActionPerformed(e);  // stop before changing file
			File file = fc.getSelectedFile();
			logFileTextField.setText(file.getPath());
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
	
	// to find and remember the log file
	final JFileChooser fc = new JFileChooser("logonetLog.txt");

}
