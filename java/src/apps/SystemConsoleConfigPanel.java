package apps;

import apps.SystemConsole.Scheme;
import apps.systemconsole.SystemConsolePreferencesManager;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import jmri.InstanceManager;
import jmri.swing.PreferencesPanel;
import jmri.util.swing.FontComboUtil;
import org.openide.util.lookup.ServiceProvider;

/**
 * Allow certain elements of the System Console to be configured.
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
 * @author Matthew Harris copyright (c) 2010, 2011
 */
@ServiceProvider(service = PreferencesPanel.class)
public class SystemConsoleConfigPanel extends JPanel implements PreferencesPanel {

    private static final ResourceBundle rbc = ResourceBundle.getBundle("apps.AppsConfigBundle");

    private static final Integer fontSizes[] = {
        8,
        9,
        10,
        11,
        12,
        13,
        14,
        16,
        18,
        20,
        24};

    private static final Integer wrapStyles[] = {
        SystemConsole.WRAP_STYLE_NONE,
        SystemConsole.WRAP_STYLE_LINE,
        SystemConsole.WRAP_STYLE_WORD};

    private static final String wrapStyleNames[] = {
        rbc.getString("ConsoleWrapStyleNone"),
        rbc.getString("ConsoleWrapStyleLine"),
        rbc.getString("ConsoleWrapStyleWord")};

    private static final JToggleButton fontStyleBold = new JToggleButton("B");

    private static final JToggleButton fontStyleItalic = new JToggleButton("I");

    private static final JComboBox<Scheme> schemes = new JComboBox<>(SystemConsole.getInstance().getSchemes());

    private static final JComboBox<String> fontFamily = FontComboUtil.getFontCombo(FontComboUtil.MONOSPACED, 14);

    private static final JComboBox<Integer> fontSize = new JComboBox<>(fontSizes);

    public SystemConsoleConfigPanel() {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel p = new JPanel(new FlowLayout());
        p.add(new JLabel(rbc.getString("ConsoleScheme")));

        schemes.setSelectedIndex(this.getPreferencesManager().getScheme());
        schemes.addActionListener((ActionEvent e) -> {
            this.getPreferencesManager().setScheme(schemes.getSelectedIndex());
        });

        schemes.setRenderer((JList<? extends Scheme> list, Scheme scheme, int index, boolean isSelected, boolean hasFocus) -> {
            JPanel p1 = new JPanel();
            p1.setOpaque(index > -1);
            if (isSelected && index > -1) {
                p1.setBackground(list.getSelectionBackground());
                p1.setForeground(list.getSelectionForeground());
            } else {
                p1.setBackground(list.getBackground());
                p1.setForeground(list.getForeground());
            }
            JLabel l = new JLabel(" " + scheme.description + " ");
            l.setOpaque(true);
            l.setFont(new Font("Monospaced", this.getPreferencesManager().getFontStyle(), 12));
            l.setForeground(scheme.foreground);
            l.setBackground(scheme.background);
            p1.add(l);
            // 'Oribble hack as CDE/Motif JComboBox doesn't seem to like
            // displaying JPanel objects in the JComboBox header
            if (UIManager.getLookAndFeel().getName().equals("CDE/Motif") && index == -1) {
                return l;
            }
            return p1;
        });

        p.add(schemes);
        add(p);

        p = new JPanel(new FlowLayout());
        fontFamily.addActionListener((ActionEvent e) -> {
            this.getPreferencesManager().setFontFamily((String) fontFamily.getSelectedItem());
            schemes.repaint();
        });
        fontFamily.setSelectedItem(this.getPreferencesManager().getFontFamily());

        JLabel fontFamilyLabel = new JLabel(rbc.getString("ConsoleFontStyle"));
        fontFamilyLabel.setLabelFor(fontFamily);

        p.add(fontFamilyLabel);
        p.add(fontFamily);

        fontSize.addActionListener((ActionEvent e) -> {
            this.getPreferencesManager().setFontSize((int) fontSize.getSelectedItem());
        });
        fontSize.setToolTipText(rbc.getString("ConsoleFontSize"));
        fontSize.setSelectedItem(this.getPreferencesManager().getFontSize());
        JLabel fontSizeUoM = new JLabel(rbc.getString("ConsoleFontSizeUoM"));

        p.add(fontSize);
        p.add(fontSizeUoM);

        fontStyleBold.setFont(fontStyleBold.getFont().deriveFont(Font.BOLD));
        fontStyleBold.addActionListener((ActionEvent e) -> {
            doFontStyle();
        });
        fontStyleBold.setToolTipText(rbc.getString("ConsoleFontStyleBold"));
        fontStyleBold.setSelected((this.getPreferencesManager().getFontStyle() & Font.BOLD) == Font.BOLD);
        p.add(fontStyleBold);

        fontStyleItalic.setFont(fontStyleItalic.getFont().deriveFont(Font.ITALIC));
        fontStyleItalic.addActionListener((ActionEvent e) -> {
            doFontStyle();
        });
        fontStyleItalic.setSelected((this.getPreferencesManager().getFontStyle() & Font.ITALIC) == Font.ITALIC);
        fontStyleItalic.setToolTipText(rbc.getString("ConsoleFontStyleItalic"));
        p.add(fontStyleItalic);

        add(p);

        p = new JPanel(new FlowLayout());
        final JComboBox<String> wrapStyle = new JComboBox<>(wrapStyleNames);
        wrapStyle.addActionListener((ActionEvent e) -> {
            this.getPreferencesManager().setWrapStyle(wrapStyles[wrapStyle.getSelectedIndex()]);
        });
        wrapStyle.setSelectedIndex(this.getPreferencesManager().getWrapStyle());

        p.add(new JLabel(rbc.getString("ConsoleWrapStyle")));
        p.add(wrapStyle);
        add(p);

        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        add(p);

    }

    private void doFontStyle() {
        this.getPreferencesManager().setFontStyle((fontStyleBold.isSelected() ? Font.BOLD : Font.PLAIN)
                | (fontStyleItalic.isSelected() ? Font.ITALIC : Font.PLAIN));
        schemes.repaint();
    }

    @Override
    public String getPreferencesItem() {
        return "DISPLAY"; // NOI18N
    }

    @Override
    public String getPreferencesItemText() {
        return rbc.getString("MenuDisplay"); // NOI18N
    }

    @Override
    public String getTabbedPreferencesTitle() {
        return rbc.getString("TabbedLayoutConsole"); // NOI18N
    }

    @Override
    public String getLabelKey() {
        return rbc.getString("LabelTabbedLayoutConsole"); // NOI18N
    }

    @Override
    public JComponent getPreferencesComponent() {
        return this;
    }

    @Override
    public boolean isPersistant() {
        return true;
    }

    @Override
    public String getPreferencesTooltip() {
        return null;
    }

    @Override
    public void savePreferences() {
        // do nothing - the persistant manager will take care of this
    }

    @Override
    public boolean isDirty() {
        // console preferences take effect immediately, but are not saved
        // immediately, so we can't tell without rereading the preferences.xml,
        // but it's too expensive to read that file to determine if it matches
        // the in memory preferences for this console, so simply return false
        return false;
    }

    @Override
    public boolean isRestartRequired() {
        // since changes are applied immediately, this is not required
        return false;
    }

    @Override
    public boolean isPreferencesValid() {
        return true; // no validity checking performed
    }

    private SystemConsolePreferencesManager getPreferencesManager() {
        return InstanceManager.getDefault(SystemConsolePreferencesManager.class);
    }
}
