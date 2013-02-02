//AbstractMonPane.java

package jmri.jmrix;

import org.apache.log4j.Logger;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.AbstractDocument;

import jmri.util.FileUtil;

import jmri.util.swing.JmriPanel;

/**
 * Abstract base class for JPanels displaying communications monitor information
 * @author	Bob Jacobsen   Copyright (C) 2001, 2003, 2010
 * @version	$Revision$
 */
public abstract class AbstractMonPane extends JmriPanel  {

    // template functions to fill in
    @Override
    public abstract String getTitle();    // provide the title for the frame

    /**
     * Initialize the data source.
     * <P>
     * This is invoked at the end of the GUI initialization phase.
     * Subclass implementations should connect to their data source here.
     */
    protected abstract void init();

    // the subclass also needs a dispose() method to close any specific communications; call super.dispose()
    @Override
    public void dispose() { 
        p.setSimplePreferenceState(timeStampCheck, timeCheckBox.isSelected());
        p.setSimplePreferenceState(rawDataCheck, rawCheckBox.isSelected());
        p.setSimplePreferenceState(alwaysOnTopCheck, alwaysOnTopCheckBox.isSelected());
        p.setSimplePreferenceState(autoScrollCheck, !autoScrollCheckBox.isSelected());
        p.setProperty(filterFieldCheck, filterFieldCheck, filterField.getText());
        super.dispose();
    }
    // you'll also have to add the message(Foo) members to handle info to be logged.
    // these should call nextLine(String line, String raw) with their updates

    // member declarations
    protected JButton clearButton = new JButton();
    protected JToggleButton freezeButton = new JToggleButton();
    protected JScrollPane jScrollPane1 = new JScrollPane();
    protected JTextArea monTextPane = new JTextArea();
    protected JButton startLogButton = new JButton();
    protected JButton stopLogButton = new JButton();
    protected JCheckBox rawCheckBox = new JCheckBox();
    protected JCheckBox timeCheckBox = new JCheckBox();
    protected JCheckBox alwaysOnTopCheckBox = new JCheckBox();
    protected JCheckBox autoScrollCheckBox = new JCheckBox();
    protected JTextField filterField = new JTextField();
    protected JLabel filterLabel = new JLabel("Filter Bytes:", JLabel.LEFT);
    protected JButton openFileChooserButton = new JButton();
    protected JTextField entryField = new JTextField();
    protected JButton enterButton = new JButton();
    String rawDataCheck = this.getClass().getName()+".RawData";
    String timeStampCheck = this.getClass().getName()+".TimeStamp";
    String alwaysOnTopCheck = this.getClass().getName()+".AlwaysOnTop";
    String autoScrollCheck = this.getClass().getName()+".AutoScroll";
    String filterFieldCheck = this.getClass().getName()+".FilterField";
    jmri.UserPreferencesManager p;

    // for locking
    AbstractMonPane self;

    // to find and remember the log file
    final javax.swing.JFileChooser logFileChooser = new JFileChooser(FileUtil.getUserFilesPath());

    @SuppressWarnings("LeakingThisInConstructor")
    public AbstractMonPane() {
        super();
        self = this;
    }

    @Override
    public void initComponents() throws Exception {
        p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        // the following code sets the frame's initial state
        clearButton.setText("Clear screen");
        clearButton.setVisible(true);
        clearButton.setToolTipText("Clear monitoring history");

        freezeButton.setText("Freeze screen");
        freezeButton.setVisible(true);
        freezeButton.setToolTipText("Stop display scrolling");

        enterButton.setText("Add Message");
        enterButton.setVisible(true);
        enterButton.setToolTipText("Add a text message to the log");

        monTextPane.setVisible(true);
        monTextPane.setToolTipText("Command and reply monitoring information appears here");
        monTextPane.setEditable(false);
        
        // Add document listener to scroll to end when modified if required
        monTextPane.getDocument().addDocumentListener(new DocumentListener() {

            // References to the JTextArea and JCheckBox
            // of this instantiation
            JTextArea ta = monTextPane;
            JCheckBox chk = autoScrollCheckBox;

            @Override
            public void insertUpdate(DocumentEvent e) {
                doAutoScroll(ta, chk.isSelected());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                doAutoScroll(ta, chk.isSelected());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                doAutoScroll(ta, chk.isSelected());
            }
        });
        
        entryField.setToolTipText("Enter text here, then click button to include it in log");
        // cap vertical size to avoid over-growth
        Dimension currentPreferredSize = entryField.getPreferredSize();
        Dimension currentMaximumSize = entryField.getMaximumSize();
        currentMaximumSize.height = currentPreferredSize.height;
        entryField.setMaximumSize(currentMaximumSize);

        //setup filterField
        filterField.setToolTipText("Enter byte values to hide, separated by spaces");
        filterField.setMaximumSize(currentMaximumSize);
        try {
			filterField.setText(p.getProperty(filterFieldCheck, filterFieldCheck).toString());  //restore prev values
		} catch (Exception e1) {  //leave blank if previous value not retrieved
		}
        //automatically uppercase input in filterField, and only accept spaces and valid hex characters
        ((AbstractDocument) filterField.getDocument()).setDocumentFilter(new DocumentFilter() {
        	public void insertString(DocumentFilter.FilterBypass fb, int offset, String text,
        			AttributeSet attr) throws BadLocationException {
    	        if (text.matches("[[0-9a-fA-F]{0,7}| ]")) {
        			fb.insertString(offset, text.toUpperCase(), attr);
        		}
        	}
        	public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text,
        			AttributeSet attrs) throws BadLocationException {
    	        if (text.matches("[[0-9a-fA-F]{0,7}| ]")) {
        			fb.replace(offset, length, text.toUpperCase(), attrs);
        		}
        	}
        }); 

        // fix a width for current character set
        JTextField t = new JTextField(80);
        int x = jScrollPane1.getPreferredSize().width+t.getPreferredSize().width;
        int y = jScrollPane1.getPreferredSize().height+10*t.getPreferredSize().height;

        jScrollPane1.getViewport().add(monTextPane);
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
        rawCheckBox.setSelected(p.getSimplePreferenceState(rawDataCheck));

        timeCheckBox.setText("Show timestamps");
        timeCheckBox.setVisible(true);
        timeCheckBox.setToolTipText("If checked, show timestamps before each message");
        timeCheckBox.setSelected(p.getSimplePreferenceState(timeStampCheck));

        alwaysOnTopCheckBox.setText("Window always on Top");
        alwaysOnTopCheckBox.setVisible(true);
        alwaysOnTopCheckBox.setToolTipText("If checked, this window be always be displayed in front of any other window");
        alwaysOnTopCheckBox.setSelected(p.getSimplePreferenceState(alwaysOnTopCheck));
        if (getTopLevelAncestor()!=null){
            ((jmri.util.JmriJFrame)getTopLevelAncestor()).setAlwaysOnTop(alwaysOnTopCheckBox.isSelected());
        }

        autoScrollCheckBox.setText("Auto scroll");
        autoScrollCheckBox.setVisible(true);
        autoScrollCheckBox.setToolTipText("If checked, always scroll to the latest log entry");
        autoScrollCheckBox.setSelected(!p.getSimplePreferenceState(autoScrollCheck));

        openFileChooserButton.setText("Choose log file");
        openFileChooserButton.setVisible(true);
        openFileChooserButton.setToolTipText("Click here to select a new output log file");

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // add items to GUI
        add(jScrollPane1);

        JPanel paneA = new JPanel();
        paneA.setLayout(new BoxLayout(paneA, BoxLayout.Y_AXIS));

        JPanel pane1 = new JPanel();
        pane1.setLayout(new BoxLayout(pane1, BoxLayout.X_AXIS));
        pane1.add(clearButton);
        pane1.add(freezeButton);
        pane1.add(rawCheckBox);
        pane1.add(timeCheckBox);
        pane1.add(alwaysOnTopCheckBox);
        pane1.add(autoScrollCheckBox);
        paneA.add(pane1);

        JPanel pane2 = new JPanel();
        pane2.setLayout(new BoxLayout(pane2, BoxLayout.X_AXIS));
        pane2.add(filterLabel);
        pane2.add(filterField);
        pane2.add(openFileChooserButton);
        pane2.add(startLogButton);
        pane2.add(stopLogButton);
        paneA.add(pane2);

        JPanel pane3 = new JPanel();
        pane3.setLayout(new BoxLayout(pane3, BoxLayout.X_AXIS));
        pane3.add(enterButton);
        pane3.add(entryField);
        paneA.add(pane3);

        add(paneA);

        // connect actions to buttons
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                clearButtonActionPerformed(e);
            }
        });
        startLogButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                startLogButtonActionPerformed(e);
            }
        });
        stopLogButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                stopLogButtonActionPerformed(e);
            }
        });
        openFileChooserButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                openFileChooserButtonActionPerformed(e);
            }
        });

        enterButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                enterButtonActionPerformed(e);
            }
        });

        alwaysOnTopCheckBox.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (getTopLevelAncestor()!=null){
                    ((jmri.util.JmriJFrame)getTopLevelAncestor()).setAlwaysOnTop(alwaysOnTopCheckBox.isSelected());
                }
            }
        });

        autoScrollCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doAutoScroll(monTextPane, autoScrollCheckBox.isSelected());
            }
        });

        // set file chooser to a default
        logFileChooser.setSelectedFile(new File("monitorLog.txt"));

        // connect to data source
        init();

    }

    /**
     * Sets the display window to fixed width font, so that 
     * e.g. columns line up
     */
    public void setFixedWidthFont() {
        monTextPane.setFont( new java.awt.Font( "Monospaced", java.awt.Font.PLAIN, monTextPane.getFont().getSize() ) );
    }
    
    /**
     * Define help menu for this window.
     * <p>
     * By default, provides a generic help page
     * that covers general features.  Specific
     * implementations can override this to 
     * show their own help page if desired.
     */
    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.AbstractMonFrame";
    }
    
    public void nextLine(String line, String raw) {
    	
    	// handle display of traffic
    	// line is the traffic in 'normal form', raw is the "raw form"
    	// Both should be one or more well-formed lines, e.g. end with \n
    	StringBuffer sb = new StringBuffer(120);

    	// display the timestamp if requested
    	if ( timeCheckBox.isSelected() ) {
    		sb.append(df.format(new Date())).append( ": " ) ;
    	}

    	// display the raw data if available and requested
    	if ( raw != null && rawCheckBox.isSelected() ) {
    		sb.append( '[' ).append(raw).append( "]  " );
    	}

    	// display decoded data
    	sb.append(line);
    	synchronized( self )
    	{
    		linesBuffer.append( sb.toString() );
    	}

    	// if requested, log to a file.
    	if (logStream != null) {
    		synchronized (logStream) {
    			String logLine = sb.toString();
    			if (!newline.equals("\n")) {
    				// have to massage the line-ends
    				int i = 0;
    				int lim = sb.length();
    				StringBuffer out = new StringBuffer(sb.length()+10);  // arbitrary guess at space
    				for ( i = 0; i<lim; i++) {
    					if (sb.charAt(i) == '\n')
    						out.append(newline);
    					else
    						out.append(sb.charAt(i));
    				}
    				logLine = out.toString();
    			}
    			logStream.print(logLine);
    		}
    	}

    	// if frozen, exit without adding to the Swing thread
    	if (freezeButton.isSelected()) {
    		return;
    	} 
    	//don't bother to check filter if no raw value passed
    	if (raw != null) {
    		// if first bytes are in the skip list,  exit without adding to the Swing thread
    		String[] filters = filterField.getText().toUpperCase().split(" ");
    		String checkRaw = raw.substring(0, 2);

    		for (String s : filters) {
    			if (s.equals(checkRaw)) {
    				linesBuffer.setLength(0) ;
    				return;
    			}
    		} 
    	}

    	Runnable r = new Runnable() {
    		@Override
    		public void run() {
    			synchronized( self )
    			{
    				monTextPane.append( linesBuffer.toString() );
    				int LineCount = monTextPane.getLineCount() ;
    				if( LineCount > MAX_LINES )
    				{
    					LineCount -= MAX_LINES ;
    					try {
    						int offset = monTextPane.getLineStartOffset(LineCount);
    						monTextPane.getDocument().remove(0, offset ) ;
    					}
    					catch (BadLocationException ex) {
    					}
    				}
    				linesBuffer.setLength(0) ;
    			}
    		}
    	};
    	javax.swing.SwingUtilities.invokeLater(r);
    }
    
 
    String newline = System.getProperty("line.separator");

    public synchronized void clearButtonActionPerformed(java.awt.event.ActionEvent e) {
        // clear the monitoring history
        synchronized( linesBuffer )
        {
            linesBuffer.setLength(0);
            monTextPane.setText("");
        }
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
            synchronized (logStream) {
                logStream.flush();
                logStream.close();
            }
            logStream = null;
        }
    }

    public void openFileChooserButtonActionPerformed(java.awt.event.ActionEvent e) {
        // start at current file, show dialog
        int retVal = logFileChooser.showSaveDialog(this);

        // handle selection or cancel
        if (retVal == JFileChooser.APPROVE_OPTION) {
            boolean loggingNow = (logStream != null);
            stopLogButtonActionPerformed(e);  // stop before changing file
            //File file = logFileChooser.getSelectedFile();
            // if we were currently logging, start the new file
            if (loggingNow) startLogButtonActionPerformed(e);
        }
    }

    public void enterButtonActionPerformed(java.awt.event.ActionEvent e) {
        nextLine(entryField.getText()+"\n", null);
    }

    public synchronized String getFrameText() {
        return linesBuffer.toString();
    }

    /**
     * Method to position caret at end of JTextArea ta when
     * scroll true.
     * @param ta Reference to JTextArea
     * @param scroll True to move to end
     */
    private void doAutoScroll(final JTextArea ta, final boolean scroll) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                int len = ta.getText().length();
                if (scroll) {
                    ta.setCaretPosition(len);
                } else if (ta.getCaretPosition()==len && len>0) {
                    ta.setCaretPosition(len-1);
                }        
            }
        });
    }

    volatile PrintStream logStream = null;

    // to get a time string
    DateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");

    StringBuffer linesBuffer = new StringBuffer();
    static private int MAX_LINES = 500 ;
    
    private static final Logger log = Logger.getLogger(AbstractMonFrame.class.getName());
}
