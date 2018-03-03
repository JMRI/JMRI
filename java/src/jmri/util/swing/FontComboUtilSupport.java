package jmri.util.swing;

import static jmri.util.swing.FontComboUtil.ALL;
import static jmri.util.swing.FontComboUtil.CHARACTER;
import static jmri.util.swing.FontComboUtil.MONOSPACED;
import static jmri.util.swing.FontComboUtil.PROPORTIONAL;
import static jmri.util.swing.FontComboUtil.SYMBOL;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.UIManager;
import jmri.InstanceManagerAutoDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This utility class provides methods that initialise and return a JComboBox
 * containing a specific sub-set of fonts installed on a users system.
 * <p>
 * Optionally, the JComboBox can be displayed with a preview of the specific
 * font in the drop-down list itself.
 * <p>
 * Note this class should not be accessed directly, but should be accessed
 * through the static methods of {@link FontComboUtil}.
 *
 * @author Matthew Harris Copyright (C) 2011
 * @author Randall Wood Copyright 2018
 * @since 2.13.1
 */
public class FontComboUtilSupport implements InstanceManagerAutoDefault {

    private final List<String> all = new ArrayList<>();
    private final List<String> monospaced = new ArrayList<>();
    private final List<String> proportional = new ArrayList<>();
    private final List<String> character = new ArrayList<>();
    private final List<String> symbol = new ArrayList<>();

    private boolean prepared = false;
    private boolean preparing = false;
    private Thread prepareThread;

    public List<String> getFonts(int which) {
        if (!prepared) {
            prepareFontLists();
        }

        synchronized (this) {
            switch (which) {
                case MONOSPACED:
                    return new ArrayList<>(monospaced);
                case PROPORTIONAL:
                    return new ArrayList<>(proportional);
                case CHARACTER:
                    return new ArrayList<>(character);
                case SYMBOL:
                    return new ArrayList<>(symbol);
                default:
                    return new ArrayList<>(all);
            }
        }

    }

    /**
     * Determine if the specified font family is a symbol font
     *
     * @param font the font family to check
     * @return true if a symbol font; false if not
     */
    public boolean isSymbolFont(String font) {
        if (!prepared) {
            prepareFontLists();
        }
        synchronized (this) {
            return symbol.contains(font);
        }
    }

    /**
     * Method to initialise the font lists on first access
     */
    private synchronized void prepareFontLists() {
        if (prepared || preparing) {
            // Normally we shouldn't get here except when the initialisation
            // thread has taken a bit longer than normal.
            log.debug("Subsequent call - no need to prepare");
            return;
        }
        preparing = true;

        log.debug("Prepare font lists...");

        // Initialise the font lists
        monospaced.clear();
        proportional.clear();
        character.clear();
        symbol.clear();
        all.clear();

        // Create a font render context to use for the comparison
        Canvas c = new Canvas();
        // Loop through all available font families
        for (String s : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {

            // Add to the 'all' fonts list
            all.add(s);

            // Retrieve a plain version of the current font family
            Font f = new Font(s, Font.PLAIN, 12);
            FontMetrics fm = c.getFontMetrics(f);

            // Fairly naive test if this is a symbol font
//            if (f.canDisplayUpTo("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789")==-1) {
            // Check that a few different characters can be displayed
            if (f.canDisplay('F') && f.canDisplay('b') && f.canDisplay('8')) {
                // It's not a symbol font - add to the character font list
                character.add(s);

                // Check if the widths of a 'narrow' letter (I)
                // a 'wide' letter (W) and a 'space' ( ) are the same.
                int w = fm.charWidth('I');
                if (fm.charWidth('W') == w && fm.charWidth(' ') == w) {
                    // Yes, they're all the same width - add to the monospaced list
                    monospaced.add(s);
                } else {
                    // No, they're different widths - add to the proportional list
                    proportional.add(s);
                }
            } else {
                // It's a symbol font - add to the symbol font list
                symbol.add(s);
            }
        }

        log.debug("...font lists built");
        prepared = true;
        if (prepareThread != null) {
            prepareThread = null;
        }
    }

    /**
     * Return a JComboBox containing all available font families. The list is
     * displayed using a preview of the font at the standard size.
     *
     * @see #getFontCombo(int, int, boolean)
     * @return List of all available font families as a {@link JComboBox}
     */
    public JComboBox<String> getFontCombo() {
        return getFontCombo(ALL);
    }

    /**
     * Return a JComboBox containing all available font families. The list is
     * displayed using a preview of the font at the standard size and with the
     * option of the name alongside in the regular dialog font.
     *
     * @see #getFontCombo(int, int, boolean)
     * @param previewOnly set to True to show only a preview in the list; False
     *                    to show both name and preview
     * @return List of specified font families as a {@link JComboBox}
     */
    public JComboBox<String> getFontCombo(boolean previewOnly) {
        return getFontCombo(ALL, previewOnly);
    }

    /**
     * Return a JComboBox containing the specified set of font families. The
     * list is displayed using a preview of the font at the standard size.
     *
     * @see #getFontCombo(int, int, boolean)
     * @param which the set of fonts to return; {@link FontComboUtil#MONOSPACED},
     *              {@link FontComboUtil#PROPORTIONAL}, {@link FontComboUtil#CHARACTER},
     *              {@link FontComboUtil#SYMBOL} or {@link FontComboUtil#ALL}
     * @return List of specified font families as a {@link JComboBox}
     */
    public JComboBox<String> getFontCombo(int which) {
        return getFontCombo(which, true);
    }

    /**
     * Return a JComboBox containing the specified set of font families. The
     * list is displayed using a preview of the font at the standard size and
     * with the option of the name alongside in the regular dialog font.
     *
     * @see #getFontCombo(int, int, boolean)
     * @param which       the set of fonts to return; {@link FontComboUtil#MONOSPACED},
     *                    {@link FontComboUtil#PROPORTIONAL}, {@link FontComboUtil#CHARACTER},
     *                    {@link FontComboUtil#SYMBOL} or {@link FontComboUtil#ALL}
     * @param previewOnly set to True to show only a preview in the list; False
     *                    to show both name and preview
     * @return List of specified font families as a {@link JComboBox}
     */
    public JComboBox<String> getFontCombo(int which, boolean previewOnly) {
        return getFontCombo(which, 0, previewOnly);
    }

    /**
     * Return a JComboBox containing the specified set of font families. The
     * list is displayed using a preview of the font at the specified point
     * size.
     *
     * @see #getFontCombo(int, int, boolean)
     * @param which the set of fonts to return; {@link FontComboUtil#MONOSPACED},
     * {@link FontComboUtil#PROPORTIONAL}, {@link FontComboUtil#CHARACTER}, {@link FontComboUtil#SYMBOL}
     *              or {@link FontComboUtil#ALL}
     * @param size  point size for the preview
     * @return List of specified font families as a {@link JComboBox}
     */
    public JComboBox<String> getFontCombo(int which, int size) {
        return getFontCombo(which, size, true);
    }

    /**
     * Return a JComboBox containing the specified set of font families. The
     * list is displayed using a preview of the font at the specified point size
     * and with the option of the name alongside in the regular dialog font.
     * <p>
     * Available font sets:
     * <ul>
     * <li>Monospaced fonts {@link FontComboUtil#MONOSPACED}
     * <li>Proportional fonts {@link FontComboUtil#PROPORTIONAL}
     * <li>Character fonts {@link FontComboUtil#CHARACTER}
     * <li>Symbol fonts {@link FontComboUtil#SYMBOL}
     * <li>All available fonts {@link FontComboUtil#ALL}
     * </ul>
     * <p>
     * Typical usage:
     * <pre>
     * JComboBox fontFamily = FontUtil.getFontCombo(FontUtil.MONOSPACED);
     * fontFamily.addActionListener(new ActionListener() {
     *      public void actionPerformed(ActionEvent e) {
     *          myObject.setFontFamily((String) ((JComboBox)e.getSource()).getSelectedItem());
     *      }
     *  });
     *  fontFamily.setSelectedItem(myObject.getFontFamily());
     * </pre>
     *
     * @param which       the set of fonts to return; {@link FontComboUtil#MONOSPACED},
     *                    {@link FontComboUtil#PROPORTIONAL}, {@link FontComboUtil#CHARACTER},
     *                    {@link FontComboUtil#SYMBOL}, or {@link FontComboUtil#ALL}
     * @param size        point size for the preview
     * @param previewOnly true to show only a preview in the list; false to show
     *                    both name and preview
     * @return List of specified font families as a {@link JComboBox}
     */
    public JComboBox<String> getFontCombo(int which, final int size, final boolean previewOnly) {
        // Create a JComboBox containing the specified list of font families
        List<String> fonts = getFonts(which);
        JComboBox<String> fontList = new JComboBox<>(fonts.toArray(new String[fonts.size()]));

        // Assign a custom renderer
        fontList.setRenderer((JList<? extends String> list, String family, // name of the current font family
                int index, boolean isSelected, boolean hasFocus) -> {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));

            // Opaque only when rendering the actual list items
            p.setOpaque(index > -1);

            // Invert colours when item selected in the list
            if (isSelected && index > -1) {
                p.setBackground(list.getSelectionBackground());
                p.setForeground(list.getSelectionForeground());
            } else {
                p.setBackground(list.getBackground());
                p.setForeground(list.getForeground());
            }

            // Setup two labels:
            // - one for the font name in regular dialog font
            // - one for the font name in the font itself
            JLabel name = new JLabel(family + (previewOnly || index == -1 ? "" : ": "));
            JLabel preview = new JLabel(family);

            // Set the font of the labels
            // Regular dialog font for the name
            // Actual font for the preview (unless a symbol font)
            name.setFont(list.getFont());
            if (isSymbolFont(family)) {
                preview.setFont(list.getFont());
                preview.setText(family + " " + Bundle.getMessage("FontSymbol"));
            } else {
                preview.setFont(new Font(family, Font.PLAIN, size == 0 ? list.getFont().getSize() : size));
            }

            // Set the size of the labels
            name.setPreferredSize(new Dimension((index == -1 && !previewOnly ? name.getMaximumSize().width * 2 : name.getMaximumSize().width), name.getMaximumSize().height + 4));
            preview.setPreferredSize(new Dimension(name.getMaximumSize().width, preview.getMaximumSize().height));

            // Centre align both labels vertically
            name.setAlignmentY(JLabel.CENTER_ALIGNMENT);
            preview.setAlignmentY(JLabel.CENTER_ALIGNMENT);

            // Ensure text colours align with that of the underlying panel
            name.setForeground(p.getForeground());
            preview.setForeground(p.getForeground());

            // Determine which label(s) to show
            // Always display the dialog font version as the list header
            if (!previewOnly && index > -1) {
                p.add(name);
                p.add(preview);
            } else if (index == -1) {
                name.setPreferredSize(new Dimension(name.getPreferredSize().width + 20, name.getPreferredSize().height - 2));
                p.add(name);
            } else {
                p.add(preview);
            }

            // 'Oribble hack as CDE/Motif JComboBox doesn't seem to like
            // displaying JPanels in the JComboBox header
            if (UIManager.getLookAndFeel().getName().equals("CDE/Motif") && index == -1) {
                return name;
            }
            return p;

        });
        return fontList;
    }

    /**
     * Determine if usable.
     *
     * @return true if ready for use; false otherwise
     */
    public boolean isReady() {
        if (!prepared) {
            initialize();
        }
        return prepared;
    }

    /**
     * Prepare for use. Cannot be used in conjunction with
     * {@link jmri.InstanceManagerAutoInitialize} since the synchronization in
     * {@link jmri.InstanceManager#getInstance(java.lang.Class)} forces this
     * method to wait on the thread it creates to run
     * {@link #prepareFontLists()}.
     */
    public void initialize() {
        synchronized (this) {
            if (!prepared && prepareThread == null) {
                prepareThread = new Thread(() -> {
                    prepareFontLists();
                }, "FontComboUtil Prepare");
            }
        }
        if (prepareThread != null && !prepareThread.isAlive() && !prepared && !preparing) {
            prepareThread.start();
        }
    }

    private final Logger log = LoggerFactory.getLogger(FontComboUtilSupport.class);

}
