// SystemConsoleConfigPanel.java

package apps;

import apps.SystemConsole.Scheme;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import jmri.util.swing.FontComboUtil;

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
 * @author Matthew Harris  copyright (c) 2010, 2011
 * @version $Revision$
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

    private static final Integer wrapStyles[] = {
        SystemConsole.WRAP_STYLE_NONE,
        SystemConsole.WRAP_STYLE_LINE,
        SystemConsole.WRAP_STYLE_WORD };

    private static final String wrapStyleNames[] = {
        rbc.getString("ConsoleWrapStyleNone"),
        rbc.getString("ConsoleWrapStyleLine"),
        rbc.getString("ConsoleWrapStyleWord") };

    private static final JToggleButton fontStyleBold = new JToggleButton("B");

    private static final JToggleButton fontStyleItalic = new JToggleButton("I");

    private static final JComboBox scheme = new JComboBox(SystemConsole.getInstance().getSchemes());
    
    private static final JComboBox fontFamily = FontComboUtil.getFontCombo(FontComboUtil.MONOSPACED, 14);

    private static final JComboBox fontSize = new JComboBox(fontSizes);

    public SystemConsoleConfigPanel() {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel p = new JPanel(new FlowLayout());
        p.add(new JLabel(rbc.getString("ConsoleScheme")));

        scheme.setSelectedIndex(SystemConsole.getInstance().getScheme());
        scheme.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SystemConsole.getInstance().setScheme(((JComboBox)e.getSource()).getSelectedIndex());
            }
        });

        scheme.setRenderer(new ListCellRenderer() {
            public Component getListCellRendererComponent(JList list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean hasFocus) {

                Scheme scheme = (Scheme)value;

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
                l.setFont(new Font("Monospaced", SystemConsole.getInstance().getFontStyle(), 12));
                l.setForeground(scheme.foreground);
                l.setBackground(scheme.background);
                p.add(l);

                // 'Oribble hack as CDE/Motif JComboBox doesn't seem to like
                // displaying JPanel objects in the JComboBox header
                if(UIManager.getLookAndFeel().getName().equals("CDE/Motif") && index==-1 ) {
                    return l;
                }
                return p;
            }
        });

        p.add(scheme);
        add(p);

        p = new JPanel(new FlowLayout());
        fontFamily.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SystemConsole.getInstance().setFontFamily((String) ((JComboBox)e.getSource()).getSelectedItem());
                scheme.repaint();
            }
        });
        fontFamily.setSelectedItem(SystemConsole.getInstance().getFontFamily());

        JLabel fontFamilyLabel = new JLabel(rbc.getString("ConsoleFontStyle"));
        fontFamilyLabel.setLabelFor(fontFamily);

        p.add(fontFamilyLabel);
        p.add(fontFamily);

        fontSize.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SystemConsole.getInstance().setFontSize((Integer) ((JComboBox)e.getSource()).getSelectedItem());
            }
        });
        fontSize.setToolTipText(rbc.getString("ConsoleFontSize"));
        fontSize.setSelectedItem(SystemConsole.getInstance().getFontSize());
        JLabel fontSizeUoM = new JLabel(rbc.getString("ConsoleFontSizeUoM"));

        p.add(fontSize);
        p.add(fontSizeUoM);

        fontStyleBold.setFont(fontStyleBold.getFont().deriveFont(Font.BOLD));
        fontStyleBold.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doFontStyle();
            }
        });
        fontStyleBold.setToolTipText(rbc.getString("ConsoleFontStyleBold"));
        fontStyleBold.setSelected((SystemConsole.getInstance().getFontStyle()&Font.BOLD)==Font.BOLD);
        p.add(fontStyleBold);

        fontStyleItalic.setFont(fontStyleItalic.getFont().deriveFont(Font.ITALIC));
        fontStyleItalic.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doFontStyle();
            }
        });
        fontStyleItalic.setSelected((SystemConsole.getInstance().getFontStyle()&Font.ITALIC)==Font.ITALIC);
        fontStyleItalic.setToolTipText(rbc.getString("ConsoleFontStyleItalic"));
        p.add(fontStyleItalic);

        add(p);

        p = new JPanel(new FlowLayout());
        final JComboBox wrapStyle = new JComboBox(wrapStyleNames);
        wrapStyle.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e)  {
               SystemConsole.getInstance().setWrapStyle(wrapStyles[((JComboBox)e.getSource()).getSelectedIndex()]);
           }
        });
        wrapStyle.setSelectedIndex(SystemConsole.getInstance().getWrapStyle());

        p.add(new JLabel(rbc.getString("ConsoleWrapStyle")));
        p.add(wrapStyle);
        add(p);

        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        add(p);

    }

    private static void doFontStyle() {
        SystemConsole.getInstance().setFontStyle(
                (fontStyleBold.isSelected()?Font.BOLD:Font.PLAIN)|
                (fontStyleItalic.isSelected()?Font.ITALIC:Font.PLAIN));
        scheme.repaint();
    }
}
/* @(#)SystemConsoleConfigPanel.java */