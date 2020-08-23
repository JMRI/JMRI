package jmri.jmris.simpleserver;

import jmri.InstanceManager;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

/**
 * Frame displaying start/stop buttons for the JMRI Simple Server.
 *
 * @author Paul Bender Copyright (C) 2009
 */
public class SimpleServerFrame extends jmri.util.JmriJFrame {

    public SimpleServerFrame() {
        this("Jmri Simple Server Starter");
    }

    public SimpleServerFrame(String FrameName) {
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
        startButton.addActionListener(a -> startSimpleServer());

        // install stop button handler
        stopButton.addActionListener(a -> stopSimpleServer());

        // install close button handler
        closeButton.addActionListener(a -> {
            setVisible(false);
            dispose();
        });

    }

    JToggleButton startButton = new JToggleButton("Start Simple Server");
    JToggleButton stopButton = new JToggleButton("Stop Simple Server");
    JToggleButton closeButton = new JToggleButton("Close Simple Server");

    // Close the window when the close box is clicked
    void thisWindowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
        dispose();
    }

    public void startSimpleServer() {
        InstanceManager.getDefault(SimpleServer.class).start();
    }

    public void stopSimpleServer() {
        InstanceManager.getDefault(SimpleServer.class).stop();
    }

}
