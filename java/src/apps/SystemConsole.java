package apps;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import jmri.UserPreferencesManager;
import jmri.util.JmriJFrame;
import jmri.util.swing.TextAreaFIFO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to direct standard output and standard error to a ( JTextArea ) TextAreaFIFO . 
 * This allows for easier clipboard operations etc.
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 *
 * @author Matthew Harris copyright (c) 2010, 2011, 2012
 */
public final class SystemConsole extends JTextArea {

    static final ResourceBundle rbc = ResourceBundle.getBundle("apps.AppsConfigBundle"); // NOI18N

    private static final int STD_ERR = 1;
    private static final int STD_OUT = 2;

    private final TextAreaFIFO console;

    private final PrintStream originalOut;
    private final PrintStream originalErr;

    private final PrintStream outputStream;
    private final PrintStream errorStream;

    private JmriJFrame frame = null;

    private final JPopupMenu popup = new JPopupMenu();

    private JMenuItem copySelection = null;

    private JMenu wrapMenu = null;
    private ButtonGroup wrapGroup = null;

    private JMenu schemeMenu = null;
    private ButtonGroup schemeGroup = null;

    private ArrayList<Scheme> schemes;

    private int scheme = 0; // Green on Black

    private int fontSize = 12;

    private int fontStyle = Font.PLAIN;

    private String fontFamily = "Monospaced";  //NOI18N

    public static final int WRAP_STYLE_NONE = 0x00;
    public static final int WRAP_STYLE_LINE = 0x01;
    public static final int WRAP_STYLE_WORD = 0x02;

    private int wrapStyle = WRAP_STYLE_WORD;

    private static SystemConsole instance;

    private UserPreferencesManager pref;

    private JCheckBox autoScroll;
    private JCheckBox alwaysOnTop;

    private final String alwaysScrollCheck = this.getClass().getName() + ".alwaysScroll"; //NOI18N
    private final String alwaysOnTopCheck = this.getClass().getName() + ".alwaysOnTop";   //NOI18N

    final public int MAX_CONSOLE_LINES = 5000;  // public, not static so can be modified via a script

    /**
     * Initialise the system console ensuring both System.out and System.err
     * streams are re-directed to the consoles JTextArea
     */
    public static void create() {

        if (instance == null) {
            try {
                instance = new SystemConsole();
            } catch (RuntimeException ex) {
                log.error("failed to complete Console redirection", ex);
            }
        }
    }

    @SuppressFBWarnings(value = "DM_DEFAULT_ENCODING",
            justification = "Can only be called from the same instance so default encoding OK")
    private SystemConsole() {
        // Record current System.out and System.err
        // so that we can still send to them
        originalOut = System.out;
        originalErr = System.err;

        // Create the console text area
        console = new TextAreaFIFO(MAX_CONSOLE_LINES);

        // Setup the console text area
        console.setRows(20);
        console.setColumns(120);
        console.setFont(new Font(fontFamily, fontStyle, fontSize));
        console.setEditable(false);
        setScheme(scheme);
        setWrapStyle(wrapStyle);

        this.outputStream = new PrintStream(outStream(STD_OUT), true);
        this.errorStream = new PrintStream(outStream(STD_ERR), true);

        // Then redirect to it
        redirectSystemStreams();
    }

    /**
     * Get current SystemConsole instance.
     * If one doesn't yet exist, create it.
     * @return current SystemConsole instance
     */
    public static SystemConsole getInstance() {
        if (instance == null) {
            SystemConsole.create();
        }
        return instance;
    }

    /**
     * Return the JFrame containing the console
     *
     * @return console JFrame
     */
    public static JFrame getConsole() {
        return SystemConsole.getInstance().getFrame();
    }

    public JFrame getFrame() {

        // Check if we've created the frame and do so if not
        if (frame == null) {
            log.debug("Creating frame for console");
            // To avoid possible locks, frame layout should be
            // performed on the Swing thread
            if (SwingUtilities.isEventDispatchThread()) {
                createFrame();
            } else {
                try {
                    // Use invokeAndWait method as we don't want to
                    // return until the frame layout is completed
                    SwingUtilities.invokeAndWait(this::createFrame);
                } catch (InterruptedException | InvocationTargetException ex) {
                    log.error("Exception creating system console frame: " + ex);
                }
            }
            log.debug("Frame created");
        }

        return frame;
    }

    /**
     * Layout the console frame
     */
    private void createFrame() {
        // Use a JmriJFrame to ensure that we fit on the screen
        frame = new JmriJFrame(Bundle.getMessage("TitleConsole"));

        pref = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);

        // Add Help menu (Windows menu automaitically added)
        frame.addHelpMenu("package.apps.SystemConsole", true); // NOI18N

        // Grab a reference to the system clipboard
        final Clipboard clipboard = frame.getToolkit().getSystemClipboard();

        // Setup the scroll pane
        JScrollPane scroll = new JScrollPane(console);
        frame.add(scroll, BorderLayout.CENTER);


        JPanel p = new JPanel();
        
        // Add button to clear display
        JButton clear = new JButton(Bundle.getMessage("ButtonClear"));
        clear.addActionListener((ActionEvent event) -> {
            console.setText("");
        });
        clear.setToolTipText(Bundle.getMessage("ButtonClearTip"));
        p.add(clear);        
        
        // Add button to allow copy to clipboard        
        JButton copy = new JButton(Bundle.getMessage("ButtonCopyClip"));
        copy.addActionListener((ActionEvent event) -> {
            StringSelection text = new StringSelection(console.getText());
            clipboard.setContents(text, text);
        });
        p.add(copy);

        // Add button to allow console window to be closed
        JButton close = new JButton(Bundle.getMessage("ButtonClose"));
        close.addActionListener((ActionEvent event) -> {
            frame.setVisible(false);
            console.dispose();
            frame.dispose();
        });
        p.add(close);

        JButton stackTrace = new JButton(Bundle.getMessage("ButtonStackTrace"));
        stackTrace.addActionListener((ActionEvent event) -> {
            performStackTrace();
        });
        p.add(stackTrace);

        // Add checkbox to enable/disable auto-scrolling
        // Use the inverted SimplePreferenceState to default as enabled
        p.add(autoScroll = new JCheckBox(Bundle.getMessage("CheckBoxAutoScroll"),
                !pref.getSimplePreferenceState(alwaysScrollCheck)));
        console.setAutoScroll(autoScroll.isSelected());
        autoScroll.addActionListener((ActionEvent event) -> {
            console.setAutoScroll(autoScroll.isSelected());
            pref.setSimplePreferenceState(alwaysScrollCheck, !autoScroll.isSelected());
        });

        // Add checkbox to enable/disable always on top
        p.add(alwaysOnTop = new JCheckBox(Bundle.getMessage("CheckBoxOnTop"),
                pref.getSimplePreferenceState(alwaysOnTopCheck)));
        alwaysOnTop.setVisible(true);
        alwaysOnTop.setToolTipText(Bundle.getMessage("ToolTipOnTop"));
        alwaysOnTop.addActionListener((ActionEvent event) -> {
            frame.setAlwaysOnTop(alwaysOnTop.isSelected());
            pref.setSimplePreferenceState(alwaysOnTopCheck, alwaysOnTop.isSelected());
        });

        frame.setAlwaysOnTop(alwaysOnTop.isSelected());

        // Define the pop-up menu
        copySelection = new JMenuItem(Bundle.getMessage("MenuItemCopy"));
        copySelection.addActionListener((ActionEvent event) -> {
            StringSelection text = new StringSelection(console.getSelectedText());
            clipboard.setContents(text, text);
        });
        popup.add(copySelection);

        JMenuItem menuItem = new JMenuItem(Bundle.getMessage("ButtonCopyClip"));
        menuItem.addActionListener((ActionEvent event) -> {
            StringSelection text = new StringSelection(console.getText());
            clipboard.setContents(text, text);
        });
        popup.add(menuItem);

        popup.add(new JSeparator());

        JRadioButtonMenuItem rbMenuItem;

        // Define the colour scheme sub-menu
        schemeMenu = new JMenu(rbc.getString("ConsoleSchemeMenu"));
        schemeGroup = new ButtonGroup();
        for (final Scheme s : schemes) {
            rbMenuItem = new JRadioButtonMenuItem(s.description);
            rbMenuItem.addActionListener((ActionEvent event) -> {
                setScheme(schemes.indexOf(s));
            });
            rbMenuItem.setSelected(getScheme() == schemes.indexOf(s));
            schemeMenu.add(rbMenuItem);
            schemeGroup.add(rbMenuItem);
        }
        popup.add(schemeMenu);

        // Define the wrap style sub-menu
        wrapMenu = new JMenu(rbc.getString("ConsoleWrapStyleMenu"));
        wrapGroup = new ButtonGroup();
        rbMenuItem = new JRadioButtonMenuItem(rbc.getString("ConsoleWrapStyleNone"));
        rbMenuItem.addActionListener((ActionEvent event) -> {
            setWrapStyle(WRAP_STYLE_NONE);
        });
        rbMenuItem.setSelected(getWrapStyle() == WRAP_STYLE_NONE);
        wrapMenu.add(rbMenuItem);
        wrapGroup.add(rbMenuItem);

        rbMenuItem = new JRadioButtonMenuItem(rbc.getString("ConsoleWrapStyleLine"));
        rbMenuItem.addActionListener((ActionEvent event) -> {
            setWrapStyle(WRAP_STYLE_LINE);
        });
        rbMenuItem.setSelected(getWrapStyle() == WRAP_STYLE_LINE);
        wrapMenu.add(rbMenuItem);
        wrapGroup.add(rbMenuItem);

        rbMenuItem = new JRadioButtonMenuItem(rbc.getString("ConsoleWrapStyleWord"));
        rbMenuItem.addActionListener((ActionEvent event) -> {
            setWrapStyle(WRAP_STYLE_WORD);
        });
        rbMenuItem.setSelected(getWrapStyle() == WRAP_STYLE_WORD);
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
     *
     * @param text  the text to add
     * @param which the stream that this text is for
     */
    private void updateTextArea(final String text, final int which) {
        // Append message to the original System.out / System.err streams
        if (which == STD_OUT) {
            originalOut.append(text);
        } else if (which == STD_ERR) {
            originalErr.append(text);
        }

        // Now append to the JTextArea
        // As append method is thread safe, we don't need to run this on
        // the Swing dispatch thread
        console.append(text);
    }

    /**
     * Creates a new OutputStream for the specified stream
     *
     * @param which the stream, either STD_OUT or STD_ERR
     * @return the new OutputStream
     */
    private OutputStream outStream(final int which) {
        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                updateTextArea(String.valueOf((char) b), which);
            }

            @Override
            @SuppressFBWarnings(value = "DM_DEFAULT_ENCODING",
                    justification = "Can only be called from the same instance so default encoding OK")
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
    @SuppressFBWarnings(value = "DM_DEFAULT_ENCODING",
            justification = "Can only be called from the same instance so default encoding OK")
    private void redirectSystemStreams() {
        System.setOut(this.getOutputStream());
        System.setErr(this.getErrorStream());
    }

    /**
     * Set the console wrapping style to one of the following:
     *
     * @param style one of the defined style attributes - one of
     * <ul>
     * <li>{@link #WRAP_STYLE_NONE} No wrapping
     * <li>{@link #WRAP_STYLE_LINE} Wrap at end of line
     * <li>{@link #WRAP_STYLE_WORD} Wrap by word boundaries
     * </ul>
     */
    public void setWrapStyle(int style) {
        wrapStyle = style;
        console.setLineWrap(style != WRAP_STYLE_NONE);
        console.setWrapStyleWord(style == WRAP_STYLE_WORD);

        if (wrapGroup != null) {
            wrapGroup.setSelected(wrapMenu.getItem(style).getModel(), true);
        }
    }

    /**
     * Retrieve the current console wrapping style
     *
     * @return current wrapping style - one of
     * <ul>
     * <li>{@link #WRAP_STYLE_NONE} No wrapping
     * <li>{@link #WRAP_STYLE_LINE} Wrap at end of line
     * <li>{@link #WRAP_STYLE_WORD} Wrap by word boundaries (default)
     * </ul>
     */
    public int getWrapStyle() {
        return wrapStyle;
    }

    /**
     * Set the console font size
     *
     * @param size point size of font between 6 and 24 point
     */
    public void setFontSize(int size) {
        updateFont(fontFamily, fontStyle, (fontSize = size < 6 ? 6 : size > 24 ? 24 : size));
    }

    /**
     * Retrieve the current console font size (default 12 point)
     *
     * @return selected font size in points
     */
    public int getFontSize() {
        return fontSize;
    }

    /**
     * Set the console font style
     *
     * @param style one of
     *              {@link Font#BOLD}, {@link Font#ITALIC}, {@link Font#PLAIN}
     *              (default)
     */
    public void setFontStyle(int style) {

        if (style == Font.BOLD || style == Font.ITALIC || style == Font.PLAIN || style == (Font.BOLD | Font.ITALIC)) {
            fontStyle = style;
        } else {
            fontStyle = Font.PLAIN;
        }
        updateFont(fontFamily, fontStyle, fontSize);
    }

    public void setFontFamily(String family) {
        updateFont((fontFamily = family), fontStyle, fontSize);
    }

    public String getFontFamily() {
        return fontFamily;
    }

    /**
     * Retrieve the current console font style
     *
     * @return selected font style - one of
     *         {@link Font#BOLD}, {@link Font#ITALIC}, {@link Font#PLAIN}
     *         (default)
     */
    public int getFontStyle() {
        return fontStyle;
    }

    /**
     * Update the system console font with the specified parameters
     *
     * @param style font style
     * @param size  font size
     */
    private void updateFont(String family, int style, int size) {
        console.setFont(new Font(family, style, size));
    }

    /**
     * Method to define console colour schemes
     */
    private void defineSchemes() {
        schemes = new ArrayList<>();
        schemes.add(new Scheme(rbc.getString("ConsoleSchemeGreenOnBlack"), Color.GREEN, Color.BLACK));
        schemes.add(new Scheme(rbc.getString("ConsoleSchemeOrangeOnBlack"), Color.ORANGE, Color.BLACK));
        schemes.add(new Scheme(rbc.getString("ConsoleSchemeWhiteOnBlack"), Color.WHITE, Color.BLACK));
        schemes.add(new Scheme(rbc.getString("ConsoleSchemeBlackOnWhite"), Color.BLACK, Color.WHITE));
        schemes.add(new Scheme(rbc.getString("ConsoleSchemeWhiteOnBlue"), Color.WHITE, Color.BLUE));
        schemes.add(new Scheme(rbc.getString("ConsoleSchemeBlackOnLightGray"), Color.BLACK, Color.LIGHT_GRAY));
        schemes.add(new Scheme(rbc.getString("ConsoleSchemeBlackOnGray"), Color.BLACK, Color.GRAY));
        schemes.add(new Scheme(rbc.getString("ConsoleSchemeWhiteOnGray"), Color.WHITE, Color.GRAY));
        schemes.add(new Scheme(rbc.getString("ConsoleSchemeWhiteOnDarkGray"), Color.WHITE, Color.DARK_GRAY));
        schemes.add(new Scheme(rbc.getString("ConsoleSchemeGreenOnDarkGray"), Color.GREEN, Color.DARK_GRAY));
        schemes.add(new Scheme(rbc.getString("ConsoleSchemeOrangeOnDarkGray"), Color.ORANGE, Color.DARK_GRAY));
    }

    private Map<Thread, StackTraceElement[]> traces;

    private void performStackTrace() {
        System.out.println("----------- Begin Stack Trace -----------"); //NO18N
        System.out.println("-----------------------------------------"); //NO18N
        traces = new HashMap<>(Thread.getAllStackTraces());
        for (Thread thread : traces.keySet()) {
            System.out.println("[" + thread.getId() + "] " + thread.getName());
            for (StackTraceElement el : thread.getStackTrace()) {
                System.out.println("  " + el);
            }
            System.out.println("-----------------------------------------"); //NO18N
        }
        System.out.println("-----------  End Stack Trace  -----------"); //NO18N
    }

    /**
     * Set the console colour scheme
     *
     * @param which the scheme to use
     */
    public void setScheme(int which) {
        scheme = which;

        if (schemes == null) {
            defineSchemes();
        }

        Scheme s;

        try {
            s = schemes.get(which);
        } catch (IndexOutOfBoundsException ex) {
            s = schemes.get(0);
            scheme = 0;
        }

        console.setForeground(s.foreground);
        console.setBackground(s.background);

        if (schemeGroup != null) {
            schemeGroup.setSelected(schemeMenu.getItem(scheme).getModel(), true);
        }
    }

    public PrintStream getOutputStream() {
        return this.outputStream;
    }
    
    public PrintStream getErrorStream() {
        return this.errorStream;
    }
    
    /**
     * Retrieve the current console colour scheme
     *
     * @return selected colour scheme
     */
    public int getScheme() {
        return scheme;
    }

    public Scheme[] getSchemes() {
        return this.schemes.toArray(new Scheme[this.schemes.size()]);
    }

    /**
     * Class holding details of each scheme
     */
    public static final class Scheme {

        public Color foreground;
        public Color background;
        public String description;

        Scheme(String description, Color foreground, Color background) {
            this.foreground = foreground;
            this.background = background;
            this.description = description;
        }
    }

    /**
     * Class to deal with handling popup menu
     */
    public final class PopupListener extends MouseAdapter {

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
                copySelection.setEnabled(console.getSelectionStart() != console.getSelectionEnd());
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    private static final Logger log = LoggerFactory.getLogger(SystemConsole.class);

}
