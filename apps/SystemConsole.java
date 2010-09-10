// SystemConsole.java

package apps;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import jmri.util.JmriJFrame;

/**
 * Class to direct standard output and standard error to a JTextArea.
 * This allows for easier clipboard operations etc.
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 * <P>
 *
 * @author Matthew Harris  copyright (c) 2010
 * @version $Revision: 1.2 $
 */
public class SystemConsole extends JTextArea {

    static final ResourceBundle rb = ResourceBundle.getBundle("apps.AppsBundle");

    private static final int STD_ERR = 1;
    private static final int STD_OUT = 2;

    private static JTextArea console = null;

    private static PrintStream originalOut;
    private static PrintStream originalErr;

    /**
     * Initialise the system console ensuring both System.out and System.err
     * streams are re-directed to the console's JTextArea
     */
    public static void init() {

        if (console == null) {
            
            // Record current System.out and System.err
            // so that we can still send to them
            originalOut = System.out;
            originalErr = System.err;

            // Create the console text area
            console = new JTextArea();

            // Setup the console text area
            console.setRows(20);
            console.setColumns(120);
            console.setLineWrap(true);
            console.setWrapStyleWord(true);
            console.setFont(new Font("Monospaced", Font.PLAIN, 12));
            console.setEditable(false);
            console.setBackground(Color.BLACK);
            console.setForeground(Color.GREEN);

            // Then redirect to it
            redirectSystemStreams();

        }
    }

    /**
     * Return the JFrame containing the console
     * @return console JFrame
     */
    public static JFrame getConsole() {
        // Check if we've already been initialised and do so if not
        if (console==null) {
            init();
        }

        // Use a JmriJFrame to ensure that we fit on the screen
        final JFrame frame = new JmriJFrame(rb.getString("TitleConsole"));

        // Grab a reference to the system clipboard
        final Clipboard clipboard = frame.getToolkit().getSystemClipboard();

        // Setup the scroll pane
        JScrollPane scroll = new JScrollPane(console);
        frame.add(scroll, BorderLayout.CENTER);

        // Add button to allow copy to clipboard
        JPanel p = new JPanel();
        JButton copy = new JButton(rb.getString("ButtonCopyClip"));
        copy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                StringSelection text = new StringSelection(console.getText());
                clipboard.setContents(text, text);
            }
        });
        p.add(copy);

        // Add button to allow console window to be closed
        JButton close = new JButton(rb.getString("ButtonClose"));
        close.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                frame.setVisible(false);
                frame.dispose();
            }
        });
        p.add(close);

//        // Add button to allow console text size to be decreased
//        JButton decSize = new JButton("-");
//        decSize.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent event) {
//                Font font = console.getFont();
//                if (font.getSize()>=6) {
//                    console.setFont(new Font(font.getName(),font.getStyle(),font.getSize()-1));
//                }
//            }
//        });
//        p.add(decSize);
//
//        // Add button to allow console text size to be increased
//        JButton incSize = new JButton("+");
//        incSize.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent event) {
//                Font font = console.getFont();
//                if (font.getSize()<=24) {
//                    console.setFont(new Font(font.getName(),font.getStyle(),font.getSize()+1));
//                }
//            }
//        });
//        p.add(incSize);

        // Add the button panel to the frame & then arrange everything
        frame.add(p, BorderLayout.SOUTH);
        frame.pack();

        return frame;
    }

    /**
     * Add text to the console
     * @param text the text to add
     * @param which the stream that this text is for
     */
    private static void updateTextArea(final String text, final int which) {

        // Append message to the original System.out / System.err streams
        if (which == STD_OUT) {
            originalOut.append(text);
        } else if (which==STD_ERR) {
            originalErr.append(text);
        }

        // Now append to the JTextArea
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                console.append(text);
            }
        });
    }

    /**
     * Creates a new OutputStream for the specified stream
     * @param which the stream, either STD_OUT or STD_ERR
     * @return the new OutputStream
     */
    private static OutputStream outStream(final int which) {
        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                updateTextArea(String.valueOf((char)b), which);
            }
            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                updateTextArea(new String(b, off, len), which);
            }
            @Override
            public void write(byte[] b) throws IOException {
                write(b, 0, b.length);
            }
        };
    }

    /**
     * Method to redirect the system streams to the console
     */
    private static void redirectSystemStreams() {
        System.setOut(new PrintStream(outStream(STD_OUT), true));
        System.setErr(new PrintStream(outStream(STD_ERR), true));
    }
}

/* @(#)SystemConsole.java */