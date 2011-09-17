// SystemConsole.java

package apps;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
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
 * @author Matthew Harris  copyright (c) 2010, 2011
 * @version $Revision$
 */
public class SystemConsole extends JTextArea {

    static final ResourceBundle rb = ResourceBundle.getBundle("apps.AppsBundle");
    static final ResourceBundle rbc = ResourceBundle.getBundle("apps.AppsConfigBundle");

    private static final int STD_ERR = 1;
    private static final int STD_OUT = 2;

    private static JTextArea console = null;

    private static PrintStream originalOut;
    private static PrintStream originalErr;

    private static JmriJFrame frame = null;

    private static JPopupMenu popup = new JPopupMenu();

    private static JMenu wrapMenu = null;
    private static ButtonGroup wrapGroup = null;

    private static JMenu schemeMenu = null;
    private static ButtonGroup schemeGroup = null;

    static ArrayList<Schemes> schemes;

    private static int scheme = 0; // Green on Black

    private static int fontSize = 12;

    private static int fontStyle = Font.PLAIN;

    private static String fontFamily = "Monospaced";

    public static final int WRAP_STYLE_NONE = 0x00;
    public static final int WRAP_STYLE_LINE = 0x01;
    public static final int WRAP_STYLE_WORD = 0x02;

    private static int wrapStyle = WRAP_STYLE_WORD;

    /**
     * Initialise the system console ensuring both System.out and System.err
     * streams are re-directed to the consoles JTextArea
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
            console.setFont(new Font(fontFamily, fontStyle, fontSize));
            console.setEditable(false);
            setScheme(scheme);
            setWrapStyle(wrapStyle);

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

        // Check if we've created the frame and do so if not
        log.debug("Check if frame created");
        if (frame==null) {
            log.debug("No, do frame layout");
            // To avoid possible locks, frame layout should be
            // performed on the Swing thread
            if (SwingUtilities.isEventDispatchThread()) {
                layoutFrame();
            } else {
                try {
                    // Use invokeAndWait method as we don't want to
                    // return until the frame layout is completed
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            layoutFrame();
                        }
                    });
                } catch (Exception ex) {
                    log.error("Exception creating system console frame: "+ex);
                }
            }
            log.debug("Layout done");
        }

        return frame;
    }

    /**
     * Layout the console frame
     */
    private static void layoutFrame() {
        // Use a JmriJFrame to ensure that we fit on the screen
        frame = new JmriJFrame(rb.getString("TitleConsole"));

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

         // Define the pop-up menu
        JMenuItem menuItem = new JMenuItem(rb.getString("ButtonCopyClip"));
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                StringSelection text = new StringSelection(console.getText());
                clipboard.setContents(text, text);
            }
        });
        popup.add(menuItem);

        popup.add(new JSeparator());

        JRadioButtonMenuItem rbMenuItem;

        // Define the colour scheme sub-menu
        schemeMenu = new JMenu("Colour scheme");
        schemeGroup = new ButtonGroup();
        for (final Schemes s: schemes) {
            rbMenuItem = new JRadioButtonMenuItem(s.description);
            rbMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    setScheme(schemes.indexOf(s));
                }
            });
            rbMenuItem.setSelected(getScheme()==schemes.indexOf(s));
            schemeMenu.add(rbMenuItem);
            schemeGroup.add(rbMenuItem);
        }
        popup.add(schemeMenu);

        // Define the wrap style sub-menu
        wrapMenu = new JMenu("Wrap style");
        wrapGroup = new ButtonGroup();
        rbMenuItem = new JRadioButtonMenuItem(rbc.getString("ConsoleWrapStyleNone"));
        rbMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                setWrapStyle(WRAP_STYLE_NONE);
            }
        });
        rbMenuItem.setSelected(getWrapStyle()==WRAP_STYLE_NONE);
        wrapMenu.add(rbMenuItem);
        wrapGroup.add(rbMenuItem);

        rbMenuItem = new JRadioButtonMenuItem(rbc.getString("ConsoleWrapStyleLine"));
        rbMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                setWrapStyle(WRAP_STYLE_LINE);
            }
        });
        rbMenuItem.setSelected(getWrapStyle()==WRAP_STYLE_LINE);
        wrapMenu.add(rbMenuItem);
        wrapGroup.add(rbMenuItem);

        rbMenuItem = new JRadioButtonMenuItem(rbc.getString("ConsoleWrapStyleWord"));
        rbMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                setWrapStyle(WRAP_STYLE_WORD);
            }
        });
        rbMenuItem.setSelected(getWrapStyle()==WRAP_STYLE_WORD);
        wrapMenu.add(rbMenuItem);
        wrapGroup.add(rbMenuItem);

        popup.add(wrapMenu);

        // Bind pop-up to objects
        MouseListener popupListener = new PopupListener();
        console.addMouseListener(popupListener);
        frame.addMouseListener(popupListener);

        // Add the button panel to the frame & then arrange everything
        frame.add(p, BorderLayout.SOUTH);
        frame.pack();
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
        if (SwingUtilities.isEventDispatchThread()) {
            console.append(text);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    console.append(text);
                }
            });
        }
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

    /**
     * Set the console wrapping style to one of the following:
     * @param style one of the defined style attributes - one of
     * <ul>
     * <li>{@link #WRAP_STYLE_NONE} No wrapping
     * <li>{@link #WRAP_STYLE_LINE} Wrap at end of line
     * <li>{@link #WRAP_STYLE_WORD} Wrap by word boundaries
     * </ul>
     */
    public static void setWrapStyle(int style) {
        wrapStyle = style;
        console.setLineWrap(style!=WRAP_STYLE_NONE);
        console.setWrapStyleWord(style==WRAP_STYLE_WORD);

        if (wrapGroup!=null) {
            wrapGroup.setSelected(wrapMenu.getItem(style).getModel(), true);
        }
    }

    /**
     * Retrieve the current console wrapping style
     * @return current wrapping style - one of
     * <ul>
     * <li>{@link #WRAP_STYLE_NONE} No wrapping
     * <li>{@link #WRAP_STYLE_LINE} Wrap at end of line
     * <li>{@link #WRAP_STYLE_WORD} Wrap by word boundaries (default)
     * </ul>
     */
    public static int getWrapStyle() {
        return wrapStyle;
    }

    /**
     * Set the console font size
     * @param size point size of font between 6 and 24 point
     */
    public static void setFontSize(int size) {
        updateFont(fontFamily, fontStyle, (fontSize = size<6?6:size>24?24:size));
    }

    /**
     * Retrieve the current console font size (default 12 point)
     * @return selected font size in points
     */
    public static int getFontSize() {
        return fontSize;
    }

    /**
     * Set the console font style
     * @param style one of {@link Font#BOLD}, {@link Font#ITALIC}, {@link Font#PLAIN} (default)
     */
    public static void setFontStyle(int style) {

        if (style==Font.BOLD || style==Font.ITALIC || style==Font.PLAIN || style==(Font.BOLD|Font.ITALIC)) {
            fontStyle = style;
        } else {
            fontStyle = Font.PLAIN;
        }
        updateFont(fontFamily, fontStyle, fontSize);
    }

    public static void setFontFamily(String family) {
        updateFont((fontFamily = family), fontStyle, fontSize);
    }

    public static String getFontFamily() {
        return fontFamily;
    }

    /**
     * Retrieve the current console font style
     * @return selected font style - one of {@link Font#BOLD}, {@link Font#ITALIC}, {@link Font#PLAIN} (default)
     */
    public static int getFontStyle() {
        return fontStyle;
    }

    /**
     * Update the system console font with the specified parameters
     * @param style font style
     * @param size font size
     */
    private static void updateFont(String family, int style, int size) {
        console.setFont(new Font(family, style, size));
    }

    /**
     * Method to define console colour schemes
     */
    private static void defineSchemes() {
        schemes = new ArrayList<Schemes>();
        schemes.add(new Schemes(rbc.getString("ConsoleSchemeGreenOnBlack"), Color.GREEN, Color.BLACK));
        schemes.add(new Schemes(rbc.getString("ConsoleSchemeOrangeOnBlack"), Color.ORANGE, Color.BLACK));
        schemes.add(new Schemes(rbc.getString("ConsoleSchemeWhiteOnBlack"), Color.WHITE, Color.BLACK));
        schemes.add(new Schemes(rbc.getString("ConsoleSchemeBlackOnWhite"), Color.BLACK, Color.WHITE));
        schemes.add(new Schemes(rbc.getString("ConsoleSchemeWhiteOnBlue"), Color.WHITE, Color.BLUE));
        schemes.add(new Schemes(rbc.getString("ConsoleSchemeBlackOnLightGray"), Color.BLACK, Color.LIGHT_GRAY));
        schemes.add(new Schemes(rbc.getString("ConsoleSchemeBlackOnGray"), Color.BLACK, Color.GRAY));
        schemes.add(new Schemes(rbc.getString("ConsoleSchemeWhiteOnGray"), Color.WHITE, Color.GRAY));
        schemes.add(new Schemes(rbc.getString("ConsoleSchemeWhiteOnDarkGray"), Color.WHITE, Color.DARK_GRAY));
        schemes.add(new Schemes(rbc.getString("ConsoleSchemeGreenOnDarkGray"), Color.GREEN, Color.DARK_GRAY));
        schemes.add(new Schemes(rbc.getString("ConsoleSchemeOrangeOnDarkGray"), Color.ORANGE, Color.DARK_GRAY));
    }

    /**
     * Set the console colour scheme
     * @param which the scheme to use
     */
    public static void setScheme(int which) {
        scheme = which;

        if (schemes == null) {
            defineSchemes();
        }

        Schemes s;

        try {
            s = schemes.get(which);
        } catch (IndexOutOfBoundsException ex) {
            s = schemes.get(0);
            scheme = 0;
        }

        console.setForeground(s.foreground);
        console.setBackground(s.background);

        if (schemeGroup!=null) {
            schemeGroup.setSelected(schemeMenu.getItem(scheme).getModel(), true);
        }
    }

    /**
     * Retrieve the current console colour scheme
     * @return selected colour scheme
     */
    public static int getScheme() {
        return scheme;
    }

    /**
     * Class holding details of each scheme
     */
    public static final class Schemes {
        public Color foreground;
        public Color background;
        public String description;

        Schemes(String description, Color foreground, Color background) {
            this.foreground = foreground;
            this.background = background;
            this.description = description;
        }
    }

    /**
     * Class to deal with handling popup menu
     */
    public static final class PopupListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SystemConsole.class.getName());

}

/* @(#)SystemConsole.java */