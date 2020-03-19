package jmri.jmris;

import jmri.InstanceManagerDelegate;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

/**
 * Frame displaying start/stop buttons for the JMRI server.
 *
 * @author Paul Bender Copyright (C) 2010
 */
public class JmriServerFrame extends jmri.util.JmriJFrame {

    private InstanceManagerDelegate instanceManager;

    public JmriServerFrame() {
        this("Jmri Server Starter");
    }

    public JmriServerFrame(String frameName){
        this(frameName, new InstanceManagerDelegate());
    }

    public JmriServerFrame(String FrameName,InstanceManagerDelegate instanceManager) {
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
                startServer();
            }
        }
        );

        // install stop button handler
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                stopServer();
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

    JToggleButton startButton = new JToggleButton("Start Server");
    JToggleButton stopButton = new JToggleButton("Stop Server");
    JToggleButton closeButton = new JToggleButton("Close Server");

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

    public void startServer() {
        instanceManager.getDefault(JmriServer.class).start();
    }

    public void stopServer() {
        instanceManager.getDefault(JmriServer.class).stop();
    }

}
