/** 
 * ErrLogFrame.java
 *
 * Description:		Frame displaying (and logging) ErrLogger
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */

package ErrLoggerJ;

import java.io.PrintStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.awt.Dimension;
import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.BoxLayout;


import ErrLoggerJ.ErrLog;

public class ErrLogFrame extends javax.swing.JFrame implements ErrLogBase {

	// member declarations
	javax.swing.JButton clearButton = new javax.swing.JButton();
	javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
	javax.swing.JTextPane logTextPane = new javax.swing.JTextPane();
	javax.swing.JButton startLogButton = new javax.swing.JButton();
	javax.swing.JButton stopLogButton = new javax.swing.JButton();
	javax.swing.JButton openFileChooserButton = new javax.swing.JButton();

	public ErrLogFrame() { super(); }
	
	public void initComponents() {

		// configure GUI objects

		clearButton.setText("Clear");
		clearButton.setLocation(new java.awt.Point(170, 280));
		clearButton.setVisible(true);
		clearButton.setToolTipText("Clear to clear monitoring history");
		clearButton.setSize(new java.awt.Dimension(80, 30));

		jScrollPane1.setLocation(new java.awt.Point(10, 10));
		jScrollPane1.setVisible(true);
		jScrollPane1.setSize(new java.awt.Dimension(550, 260));
		jScrollPane1.getViewport().add(logTextPane);

		logTextPane.setVisible(true);
		logTextPane.setToolTipText("Message log appears here");
		logTextPane.setEditable(false);

		startLogButton.setText("Start log file");
		startLogButton.setVisible(true);
		startLogButton.setToolTipText("start logging");

		stopLogButton.setText("Stop log file");
		stopLogButton.setVisible(true);
		stopLogButton.setToolTipText("stop logging");
		stopLogButton.setSize(new java.awt.Dimension(70, 20));

		openFileChooserButton.setText("Select log file");
		openFileChooserButton.setVisible(true);
		openFileChooserButton.setToolTipText("Click here to select a new output log file");

		setLocation(new java.awt.Point(400, 10));
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		setTitle("ErrLogger control");
		getContentPane().add(clearButton);
		getContentPane().add(jScrollPane1);
		getContentPane().add(startLogButton);
		getContentPane().add(stopLogButton);
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

		// install as the default ErrLogger implementation
		ErrLog.installHandler(this);
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
	}
	
	public void msg(int severity, 
                String facility, 
                String code,
                String text) {
          message(labels[severity-ErrLog.debugging]+":"+facility+":"+code+":"+text);
          }

    public void msg(int severity, 
                String facility, 
                int code,
                String text) {
          this.msg(severity, facility, String.valueOf(code), text);
          }

    public boolean logging(int severity, 
                String facility, 
                String code) {
          return true;   // !!
          }

    public boolean logging(int severity, 
                String facility, 
                int code){
          return true;   // !!
          }

	
	public synchronized void message(String l) {  // receive a string
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
		
		// display the new message
		s1 = l+"\n";

		// display it in the Swing thread
		Runnable r = new Runnable() {
			public void run() { logTextPane.setText(getFrameText()); }
			};
		javax.swing.SwingUtilities.invokeLater(r);
		
		// if requested, log to a file.
		if (logStream != null) {
			System.out.println("logging");
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
		logTextPane.setText("");
		s1=s2=s3=s4=s5=s6=s7=s8=s9=s10 = "";
		s11=s12=s13=s14=s15=s16=s17=s18=s19=s20 = "";
	}
		
	public synchronized void startLogButtonActionPerformed(java.awt.event.ActionEvent e) {
		// start logging by creating the stream
		if ( logStream==null) {  // successive clicks don't restart the file
			// start logging
			try {
				logStream = new PrintStream (new FileOutputStream(fc.getSelectedFile().getPath()));
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
		fc.setSelectedFile(new File("log.txt"));
		int retVal = fc.showSaveDialog(this);

		// handle selection or cancel
		if (retVal == JFileChooser.APPROVE_OPTION) {
			boolean loggingNow = (logStream != null);
			stopLogButtonActionPerformed(e);  // stop before changing file
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
	
	// to find and remember the log file
	final JFileChooser fc = new JFileChooser("logonetLog.txt");

}
