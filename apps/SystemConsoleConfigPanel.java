// SystemConsoleConfigPanel.java

package apps;

import apps.SystemConsole.Schemes;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

/**
 * Allow certain elements of the System Console to be configured.
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
public class SystemConsoleConfigPanel extends JPanel {

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
        24 };

    private static final Integer fontStyles[] = {
        Font.PLAIN,
        Font.BOLD,
        Font.ITALIC,
        (Font.BOLD|Font.ITALIC) };

    private static final String fontStyleNames[] = {
        rbc.getString("ConsoleFontStyleNormal"),
        rbc.getString("ConsoleFontStyleBold"),
        rbc.getString("ConsoleFontStyleItalic"),
        rbc.getString("ConsoleFontStyleBoldItalic") };

    private static final JCheckBox savePosition = new JCheckBox(rbc.getString("ConsoleWindowSavePosition"));

    private static final JCheckBox saveSize = new JCheckBox(rbc.getString("ConsoleWindowSaveSize"));

    public SystemConsoleConfigPanel() {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel p = new JPanel(new FlowLayout());
        p.add(new JLabel(rbc.getString("ConsoleScheme")));

        final JComboBox scheme = new JComboBox(SystemConsole.schemes.toArray());
        scheme.setSelectedIndex(SystemConsole.getScheme());
        scheme.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SystemConsole.setScheme(((JComboBox)e.getSource()).getSelectedIndex());
            }
        });

        scheme.setRenderer(new ListCellRenderer() {
            public Component getListCellRendererComponent(JList list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean hasFocus) {

                Schemes scheme = (Schemes)value;

                JPanel p = new JPanel();
                p.setOpaque(index > -1);

                if (isSelected && index > -1) {
                    p.setBackground(list.getSelectionBackground());
                    p.setForeground(list.getSelectionForeground());
                } else {
                    p.setBackground(list.getBackground());
                    p.setForeground(list.getForeground());
                }

                JLabel l = new JLabel(" " + scheme.description + " ");
                l.setOpaque(true);
                l.setFont(new Font("Monospaced", SystemConsole.getFontStyle(), 12));
                l.setForeground(scheme.foreground);
                l.setBackground(scheme.background);
                p.add(l);

                return p;
            }
        });

        p.add(scheme);
        add(p);

        p = new JPanel(new FlowLayout());
        JComboBox fontSize = new JComboBox(fontSizes);
        fontSize.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SystemConsole.setFontSize((Integer) ((JComboBox)e.getSource()).getSelectedItem());
            }
        });
        fontSize.setSelectedItem(SystemConsole.getFontSize());
        JLabel fontSizeLabel = new JLabel(rbc.getString("ConsoleFontSize"));
        fontSizeLabel.setLabelFor(fontSize);
        JLabel fontSizeUoM = new JLabel(rbc.getString("ConsoleFontSizeUoM"));

        p.add(fontSizeLabel);
        p.add(fontSize);
        p.add(fontSizeUoM);
        add(p);

        p = new JPanel(new FlowLayout());
        final JComboBox fontStyle = new JComboBox(fontStyleNames);
        fontStyle.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SystemConsole.setFontStyle(fontStyles[((JComboBox)e.getSource()).getSelectedIndex()]);
                scheme.repaint();
            }
        });

        p.add(new JLabel(rbc.getString("ConsoleFontStyle")));
        p.add(fontStyle);
        add(p);

        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(savePosition);
        p.add(saveSize);
        add(p);

    }

    public static void setPositionSaved(boolean position) {
        savePosition.setSelected(position);
    }

    public static boolean isPositionSaved() {
        return savePosition.isSelected();
    }

    public static void setSizeSaved(boolean size) {
        saveSize.setSelected(size);
    }

    public static boolean isSizeSaved() {
        return saveSize.isSelected();
    }
}
/* @(#)SystemConsoleConfigPanel.java */