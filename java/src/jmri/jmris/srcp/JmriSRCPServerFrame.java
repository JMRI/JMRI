// JmriSRCPServerFrame.java

package jmri.jmris.srcp;

//import java.awt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.event.*;
import javax.swing.*;

/**
 * Frame displaying start/stop buttons for the JMRI server.
 *
 * @author			Paul Bender  Copyright (C) 2009
 * @version			$Revision$
 */
public class JmriSRCPServerFrame extends jmri.util.JmriJFrame {

    public JmriSRCPServerFrame() {
        this("Jmri SRCP Server Starter");
    }

    public JmriSRCPServerFrame(String FrameName) {
        super(FrameName);
        getContentPane().setLayout(new BoxLayout(getContentPane(), 
				   BoxLayout.Y_AXIS));

        JPanel pane0 = new JPanel();
        pane0.add(startButton);
        pane0.add(stopButton);
        pane0.add(closeButton);
        getContentPane().add(pane0);

        // and prep for display
        pack();

        // install start button handler
        startButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
			startSRCPServer();
                }
            }
        );

        // install stop button handler
        stopButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
			stopSRCPServer();
                }
            }
        );

        // install close button handler
        closeButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	setVisible(false);
        		dispose();
                }
            }
        );

    }

    JToggleButton startButton = new JToggleButton("Start SRCP Server");
    JToggleButton stopButton = new JToggleButton("Stop SRCP Server");
    JToggleButton closeButton = new JToggleButton("Close SRCP Server");

    // Close the window when the close box is clicked
    void thisWindowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
        dispose();
    }

    public void dispose() {
        // take apart the JFrame
        super.dispose();
    }

    public void startSRCPServer() {
	JmriSRCPServer.instance().start();
    }

    public void stopSRCPServer() {
	JmriSRCPServer.instance().stop();
    }

    static Logger log = LoggerFactory.getLogger(JmriSRCPServerFrame.class.getName());

}
