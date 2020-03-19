package jmri.jmris.srcp;

//import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import jmri.InstanceManagerDelegate;
import jmri.jmris.JmriServer;

/**
 * Frame displaying start/stop buttons for the JMRI SRCP server.
 *
 * @author Paul Bender Copyright (C) 2009
 */
public class JmriSRCPServerFrame extends jmri.util.JmriJFrame {

    private InstanceManagerDelegate instanceManager;

    public JmriSRCPServerFrame() {
        this("Jmri SRCP Server Starter");
    }

    public JmriSRCPServerFrame(String FrameName) {
        this(FrameName,new InstanceManagerDelegate());
    }

    public JmriSRCPServerFrame(String FrameName,InstanceManagerDelegate instanceManager) {
        super(FrameName);
        this.instanceManager = instanceManager;
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
            @Override
            public void actionPerformed(ActionEvent a) {
                startSRCPServer();
            }
        }
        );

        // install stop button handler
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                stopSRCPServer();
            }
        }
        );

        // install close button handler
        closeButton.addActionListener(new ActionListener() {
            @Override
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

    @Override
    public void dispose() {
        // take apart the JFrame
        super.dispose();
    }

    public void startSRCPServer() {
        instanceManager.getDefault(JmriServer.class).start();
    }

    public void stopSRCPServer() {
        instanceManager.getDefault(JmriServer.class).stop();
    }

}
