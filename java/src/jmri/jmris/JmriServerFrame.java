// JmriServerFrame.java
package jmri.jmris;

//import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame displaying start/stop buttons for the JMRI server.
 *
 * @author	Paul Bender Copyright (C) 2010
 * @version	$Revision$
 */
public class JmriServerFrame extends jmri.util.JmriJFrame {

    /**
     *
     */
    private static final long serialVersionUID = -412445239269856582L;

    public JmriServerFrame() {
        this("Jmri Server Starter");
    }

    public JmriServerFrame(String FrameName) {
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
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                startServer();
            }
        }
        );

        // install stop button handler
        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                stopServer();
            }
        }
        );

        // install close button handler
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                setVisible(false);
                dispose();
            }
        }
        );

    }

    JToggleButton startButton = new JToggleButton("Start Server");
    JToggleButton stopButton = new JToggleButton("Stop Server");
    JToggleButton closeButton = new JToggleButton("Close Server");

    // Close the window when the close box is clicked
    void thisWindowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
        dispose();
    }

    public void dispose() {
        // take apart the JFrame
        super.dispose();
    }

    public void startServer() {
        JmriServer.instance().start();
    }

    public void stopServer() {
        JmriServer.instance().stop();
    }

    private final static Logger log = LoggerFactory.getLogger(JmriServerFrame.class.getName());

}
