package jmri.util.swing;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.font.FontRenderContext;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.UIManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This utility class provides methods that initialise and return a JComboBox
 * containing a specific sub-set of fonts installed on a users system.
 * <P>
 * Optionally, the JComboBox can be displayed with a preview of the specific
 * font in the drop-down list itself.
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 *
 * @author Matthew Harris Copyright (C) 2011
 * @since 2.13.1
 */
public class FontComboUtil {

    public static final int ALL = 0;
    public static final int MONOSPACED = 1;
    public static final int PROPORTIONAL = 2;
    public static final int CHARACTER = 3;
    public static final int SYMBOL = 4;

    private static List<String> all = null;
    private static List<String> monospaced = null;
    private static List<String> proportional = null;
    private static List<String> character = null;
    private static List<String> symbol = null;

    private static boolean prepared = false;

    public static List<String> getFonts(int which) {
        if (!prepared) {
            prepareFontLists();
        }

        switch (which) {
            case MONOSPACED:
                return monospaced;
            case PROPORTIONAL:
                return proportional;
            case CHARACTER:
                return character;
            case SYMBOL:
                return symbol;
            default:
                return all;
        }

    }

    /**
     * Determine if the specified font family is a symbol font
     *
     * @param font the font family to check
     * @return true if a symbol font; false if not
     */
    public static boolean isSymbolFont(String font) {
        if (!prepared) {
            prepareFontLists();
        }
        return symbol.contains(font);
    }

    /**
     * Method to initialise the font lists on first access
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value="FE_FLOATING_POINT_EQUALITY", justification="font sizes are really quantized")
    public static synchronized void prepareFontLists() {

        if (prepared) {
            // Normally we shouldn't get here except when the initialisation
            // thread has taken a bit longer than normal.
            log.debug("Subsequent call - no need to prepare");
            return;
        }

        log.debug("Prepare font lists...");

        // Initialise the font lists
        monospaced = new ArrayList<>();
        proportional = new ArrayList<>();
        character = new ArrayList<>();
        symbol = new ArrayList<>();
        all = new ArrayList<>();

        // Create a font render context to use for the comparison
        FontRenderContext frc = new FontRenderContext(null, false, false);

        // Loop through all available font families
        for (String s : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {

            // Add to the 'all' fonts list
            all.add(s);

            // Retrieve a plain version of the current font family
            Font f = new Font(s, Font.PLAIN, 12);

            // Fairly naive test if this is a symbol font
//            if (f.canDisplayUpTo("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789")==-1) {
            // Check that a few different characters can be displayed
            if (f.canDisplay('F') && f.canDisplay('b') && f.canDisplay('8')) {
                // It's not a symbol font - add to the character font list
                character.add(s);

                // Check if the widths of a 'narrow' letter (I)
                // a 'wide' letter (W) and a 'space' ( ) are the same.
                double w;
                // next line is the FE_FLOATING_POINT_EQUALITY annotated above
                if (f.getStringBounds("I", frc).getWidth()
                        == (w = f.getStringBounds("W", frc).getWidth())
                        && w == f.getStringBounds(" ", frc).getWidth()) {
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
    }

    /**
     * Return a JComboBox containing all available font families. The list is
     * displayed using a preview of the font at the standard size.
     *
     * @see #getFontCombo(int, int, boolean)
     * @return List of all available font families as a {@link JComboBox}
     */
    public static JComboBox<String> getFontCombo() {
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
    public static JComboBox<String> getFontCombo(boolean previewOnly) {
        return getFontCombo(ALL, previewOnly);
    }

    /**
     * Return a JComboBox containing the specified set of font families. The
     * list is displayed using a preview of the font at the standard size.
     *
     * @see #getFontCombo(int, int, boolean)
     * @param which the set of fonts to return; {@link #MONOSPACED},
     * {@link #PROPORTIONAL}, {@link #CHARACTER}, {@link #SYMBOL} or
     *              {@link #ALL}
     * @return List of specified font families as a {@link JComboBox}
     */
    public static JComboBox<String> getFontCombo(int which) {
        return getFontCombo(which, true);
    }

    /**
     * Return a JComboBox containing the specified set of font families. The
     * list is displayed using a preview of the font at the standard size and
     * with the option of the name alongside in the regular dialog font.
     *
     * @see #getFontCombo(int, int, boolean)
     * @param which       the set of fonts to return; {@link #MONOSPACED},
     * {@link #PROPORTIONAL}, {@link #CHARACTER}, {@link #SYMBOL} or
     *                    {@link #ALL}
     * @param previewOnly set to True to show only a preview in the list; False
     *                    to show both name and preview
     * @return List of specified font families as a {@link JComboBox}
     */
    public static JComboBox<String> getFontCombo(int which, boolean previewOnly) {
        return getFontCombo(which, 0, previewOnly);
    }

    /**
     * Return a JComboBox containing the specified set of font families. The
     * list is displayed using a preview of the font at the specified point
     * size.
     *
     * @see #getFontCombo(int, int, boolean)
     * @param which the set of fonts to return; {@link #MONOSPACED},
     * {@link #PROPORTIONAL}, {@link #CHARACTER}, {@link #SYMBOL} or
     *              {@link #ALL}
     * @param size  point size for the preview
     * @return List of specified font families as a {@link JComboBox}
     */
    public static JComboBox<String> getFontCombo(int which, int size) {
        return getFontCombo(which, size, true);
    }

    /**
     * Return a JComboBox containing the specified set of font families. The
     * list is displayed using a preview of the font at the specified point size
     * and with the option of the name alongside in the regular dialog font.
     * <p>
     * Available font sets:
     * <ul>
     * <li>Monospaced fonts {@link #MONOSPACED}
     * <li>Proportional fonts {@link #PROPORTIONAL}
     * <li>Character fonts {@link #CHARACTER}
     * <li>Symbol fonts {@link #SYMBOL}
     * <li>All available fonts {@link #ALL}
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
     * @param which       the set of fonts to return; {@link #MONOSPACED},
     * {@link #PROPORTIONAL}, {@link #CHARACTER}, {@link #SYMBOL} or
     *                    {@link #ALL}
     * @param size        point size for the preview
     * @param previewOnly true to show only a preview in the list; false to show
     *                    both name and preview
     * @return List of specified font families as a {@link JComboBox}
     */
    public static JComboBox<String> getFontCombo(int which, final int size, final boolean previewOnly) {
        // Create a JComboBox containing the specified list of font families
        JComboBox<String> fontList = new JComboBox<>(getFonts(which).toArray(new String[0]));

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

    private static final Logger log = LoggerFactory.getLogger(FontComboUtil.class.getName());

}
