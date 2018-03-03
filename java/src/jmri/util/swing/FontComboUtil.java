package jmri.util.swing;

import java.util.List;
import javax.swing.JComboBox;
import jmri.InstanceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This utility class provides methods that initialize and return a JComboBox
 * containing a specific sub-set of fonts installed on a users system.
 * <p>
 * Optionally, the JComboBox can be displayed with a preview of the specific
 * font in the drop-down list itself.
 * <p>
 * Note this is a static API implementing static access to
 * {@link FontComboUtilSupport}.
 *
 * @author Matthew Harris Copyright (C) 2011
 * @author Randall Wood Copyright 2018
 * @since 2.13.1
 */
public class FontComboUtil {

    public static final int ALL = 0;
    public static final int MONOSPACED = 1;
    public static final int PROPORTIONAL = 2;
    public static final int CHARACTER = 3;
    public static final int SYMBOL = 4;

    public static List<String> getFonts(int which) {
        return InstanceManager.getDefault(FontComboUtilSupport.class).getFonts(which);
    }

    /**
     * Determine if the specified font family is a symbol font
     *
     * @param font the font family to check
     * @return true if a symbol font; false if not
     */
    public static boolean isSymbolFont(String font) {
        return InstanceManager.getDefault(FontComboUtilSupport.class).isSymbolFont(font);
    }

    /**
     * Return a JComboBox containing all available font families. The list is
     * displayed using a preview of the font at the standard size.
     *
     * @see #getFontCombo(int, int, boolean)
     * @return List of all available font families as a {@link JComboBox}
     */
    public static JComboBox<String> getFontCombo() {
        return InstanceManager.getDefault(FontComboUtilSupport.class).getFontCombo();
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
        return InstanceManager.getDefault(FontComboUtilSupport.class).getFontCombo(previewOnly);
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
        return InstanceManager.getDefault(FontComboUtilSupport.class).getFontCombo(which);
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
        return InstanceManager.getDefault(FontComboUtilSupport.class).getFontCombo(which, previewOnly);
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
        return InstanceManager.getDefault(FontComboUtilSupport.class).getFontCombo(which, size);
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
        return InstanceManager.getDefault(FontComboUtilSupport.class).getFontCombo(which, size, previewOnly);
    }

    /**
     * Determine if usable.
     *
     * @return true if ready for use; false otherwise
     */
    public static boolean isReady() {
        return InstanceManager.getDefault(FontComboUtilSupport.class).isReady();
    }

    /**
     * Prepare for use.
     */
    public static void initialize() {
        InstanceManager.getDefault(FontComboUtilSupport.class).initialize();
    }

    private static final Logger log = LoggerFactory.getLogger(FontComboUtil.class);

}
