/** 
 * AbstractMonFrame.java
 *
 * Description:		Abstact base class for Frames displaying communications monitor information
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */

package jmri.jmrix;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Date;
import java.text.DateFormat;
import java.io.File;
import java.io.PrintStream;
import java.io.FileOutputStream;

public abstract class AbstractMonFrame extends javax.swing.JFrame  {

	// template functions to fill in
	protected abstract String title();    // provide the title for the frame
	protected abstract void init();       // last part of configuration, connect to data source
	
	// the subclass also needs a dispose() method to close any specific communications; call super.dispose()
	public void dispose() { super.dispose();}
	// you'll also have to add the message(Foo) members to handle info to be logged.
	// these should call nextLine(String line, String raw) with their updates
	
	// member declarations
	protected javax.swing.JButton clearButton = new javax.swing.JButton();
	protected javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
	protected javax.swing.JTextPane monTextPane = new javax.swing.JTextPane();
	protected javax.swing.JButton startLogButton = new javax.swing.JButton();
	protected javax.swing.JButton stopLogButton = new javax.swing.JButton();
	protected javax.swing.JCheckBox rawCheckBox = new javax.swing.JCheckBox();
	protected javax.swing.JCheckBox timeCheckBox = new javax.swing.JCheckBox();
	protected javax.swing.JButton openFileChooserButton = new javax.swing.JButton();
	
	// to find and remember the log file
	final javax.swing.JFileChooser logFileChooser = new JFileChooser(".");

	public AbstractMonFrame() {
	}

	public void initComponents() throws Exception {
		// the following code sets the frame's initial state

		clearButton.setText("Clear screen");
		clearButton.setVisible(true);
		clearButton.setToolTipText("Clear monitoring history");

		monTextPane.setVisible(true);
		monTextPane.setToolTipText("Command and reply monitoring information appears here");
		monTextPane.setEditable(false);

		// fix a width for current character set
		JTextField t = new JTextField("                                                  ");		
		// force a minimum size - 5 is just an arbitrary constant!
		int x = jScrollPane1.getPreferredSize().width+t.getPreferredSize().width;
		int y = jScrollPane1.getPreferredSize().height+5*t.getPreferredSize().height;	

		jScrollPane1.getViewport().add(monTextPane);
		jScrollPane1.setMinimumSize(new Dimension(x, y));
		jScrollPane1.setPreferredSize(new Dimension(x, y));
		jScrollPane1.setVisible(true);

		startLogButton.setText("Start logging");
		startLogButton.setVisible(true);
		startLogButton.setToolTipText("start logging to file");

		stopLogButton.setText("Stop logging");
		stopLogButton.setVisible(true);
		stopLogButton.setToolTipText("Stop logging to file");

		rawCheckBox.setText("Show raw data");
		rawCheckBox.setVisible(true);
		rawCheckBox.setToolTipText("If checked, show the raw traffic in hex");

		timeCheckBox.setText("Show timestamps");
		timeCheckBox.setVisible(true);
		timeCheckBox.setToolTipText("If checked, show timestamps before each message");

		openFileChooserButton.setText("Choose log file");
		openFileChooserButton.setVisible(true);
		openFileChooserButton.setToolTipText("Click here to select a new output log file");

		// set sizes to same as largest
		Dimension big = openFileChooserButton.getMaximumSize();
		if (timeCheckBox.getMaximumSize().width > big.width) big.width = timeCheckBox.getMaximumSize().width;
		clearButton.setMaximumSize(big);
		timeCheckBox.setMaximumSize(big);
		rawCheckBox.setMaximumSize(big);
		startLogButton.setMaximumSize(big);
		stopLogButton.setMaximumSize(big);
		
		setTitle(title());
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		
		// add items to GUI
		getContentPane().add(jScrollPane1);
		
		JPanel paneA = new JPanel();
			paneA.setLayout(new BoxLayout(paneA, BoxLayout.X_AXIS));
			JPanel pane1 = new JPanel();
				pane1.setLayout(new BoxLayout(pane1, BoxLayout.Y_AXIS));
				pane1.add(clearButton);
				pane1.add(rawCheckBox);
				pane1.add(timeCheckBox);
			paneA.add(pane1);
		
			JPanel pane2 = new JPanel();
				pane2.setLayout(new BoxLayout(pane2, BoxLayout.Y_AXIS));
				pane2.add(openFileChooserButton);
				pane2.add(startLogButton);
				pane2.add(stopLogButton);
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
		logFileChooser.setSelectedFile(new File("monitorLog.txt"));
		
		// connect to data source
		init();
		
		// and start displaying
		pack();
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
	}
	
	protected void nextLine(String line, String raw) {
		// handle display of traffic
		// line is the traffic in 'normal form', raw is the "raw form"
		// Both should be one or more well-formed lines, e.g. end with \n
		
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
		
		// space if multipart form
		if ( timeCheckBox.isSelected() || rawCheckBox.isSelected()) s1+="\n";
		
		// display the timestamp if requested
		if ( timeCheckBox.isSelected() ) {
			s1 += "time: "+df.format(new Date())+"\n";
		}

		// display the raw data if requested
		if ( rawCheckBox.isSelected() ) {
			s1 += raw;
		}

		// display decoded data
		s1 += line;
		
		// display it in the Swing thread
		Runnable r = new Runnable() {
			public void run() { monTextPane.setText(getFrameText()); }
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
		monTextPane.setText("");
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
	
   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractMonFrame.class.getName());

}
