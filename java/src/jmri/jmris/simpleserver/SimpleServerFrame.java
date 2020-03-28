package jmri.jmris.simpleserver;

import jmri.InstanceManagerDelegate;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

/**
 * Frame displaying start/stop buttons for the JMRI Simple Server.
 *
 * @author Paul Bender Copyright (C) 2009
 */
public class SimpleServerFrame extends jmri.util.JmriJFrame {

    private final InstanceManagerDelegate instanceManagerDelegate;

    public SimpleServerFrame() {
        this("Jmri Simple Server Starter");
    }

    public SimpleServerFrame(String FrameName){
        this(FrameName,new InstanceManagerDelegate());
    }

    public SimpleServerFrame(String FrameName, InstanceManagerDelegate instanceManagerDelegate) {
        super(FrameName);
        this.instanceManagerDelegate = instanceManagerDelegate;
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
        instanceManagerDelegate.getDefault(SimpleServer.class).start();
    }

    public void stopSimpleServer() {
        instanceManagerDelegate.getDefault(SimpleServer.class).stop();
    }

}
