// InputWindow.java

package jmri.jmrit.jython;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextArea;
import java.awt.Font;
import jmri.util.PythonInterp;

/**
 * This Action runs creates a JFrame for sending input to the
 * global jython interpreter
 *
 * @author	Bob Jacobsen    Copyright (C) 2004
 * @version     $Revision: 1.3 $
 */
public class InputWindow extends AbstractAction {

    /**
     * Constructor just initializes parent class.
     * @param name Action name
     */
    public InputWindow(String name) {
        super(name);
    }

    JTextArea area;
    JButton button;

    /**
     * Invoking this action via an event triggers
     * display of a file dialog. If a file is selected,
     * it's then invoked as a script.
     * @param e
     */
    public void actionPerformed(ActionEvent e) {
        PythonInterp.getPythonInterpreter();

        java.util.ResourceBundle rb = java.util.ResourceBundle.getBundle("jmri.jmrit.jython.JythonBundle");

        JFrame f = new JFrame(rb.getString("TitleInputFrame"));
        f.getContentPane().setLayout(new javax.swing.BoxLayout(f.getContentPane(), javax.swing.BoxLayout.Y_AXIS));
        f.getContentPane().add(area = new JTextArea(12, 50));

        JPanel p = new JPanel();
        p.add(button = new JButton(rb.getString("ButtonExecute")));
        f.getContentPane().add(p);

        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                buttonPressed();
            }
        });

        // set a monospaced font
        int size = area.getFont().getSize();
        area.setFont(new Font("Monospaced", Font.PLAIN, size));

        f.pack();
        f.show();

    }

    void buttonPressed() {
        String cmd = area.getText()+"\n";

        // The command must end with exactly one \n
        while ((cmd.length()>1) && cmd.charAt(cmd.length()-2)=='\n')
            cmd = cmd.substring(0, cmd.length()-1);

        // add the text to the output frame
        String echo = ">>> "+cmd;
        // intermediate \n characters need to be prefixed
        echo = jmri.util.StringUtil.replaceAll(echo,"\n", "\n... ");
        echo = echo.substring(0, echo.length()-4);
        PythonInterp.outputlog.append(echo);

        // and execute
        PythonInterp.execCommand(cmd);
    }

}

/* @(#)InputWindow.java */
