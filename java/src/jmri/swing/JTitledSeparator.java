package jmri.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

/**
 * A separator with a title.
 *
 * JTitledSeparator based on
 * https://github.com/rhwood/DJ-Swing-Suite/blob/master/DJSwingSuite/src/chrriis/dj/swingsuite/JTitledSeparator.java
 * by
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * @author Randall Wood
 */
public class JTitledSeparator extends JPanel {

    private final static class SeparatorPane extends JPanel {

        private SeparatorPane() {
            super(new GridBagLayout());
            setOpaque(false);
            setDoubleBuffered(false);
            add(new JSeparator(), new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        }

        @Override
        public void setBounds(int x, int y, int w, int h) {
            super.setBounds(x, y, w, h);
            doLayout();
        }
    }
    private final JLabel label = new JLabel();

    /**
     * Construct a separator with a title.
     *
     * @param title the title to set.
     */
    public JTitledSeparator(String title) {
        super(new BorderLayout());
        JPanel westPanel = new JPanel(new BorderLayout()) {

            @Override
            public void setBounds(int x, int y, int w, int h) {
                super.setBounds(x, y, w, h);
                doLayout();
            }
        };
        westPanel.setOpaque(false);
        westPanel.setDoubleBuffered(false);
        boolean isLeftToRight = getComponentOrientation().isLeftToRight();
        setOpaque(false);
        westPanel.add(label, BorderLayout.CENTER);
        if (isLeftToRight) {
            add(westPanel, BorderLayout.WEST);
        } else {
            add(westPanel, BorderLayout.EAST);
        }
        SeparatorPane separatorPane = new SeparatorPane();
        if (isLeftToRight) {
            separatorPane.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        } else {
            separatorPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));
        }
        add(separatorPane, BorderLayout.CENTER);
        setTitle(title);
        this.setMaximumSize(new Dimension(Integer.MAX_VALUE, this.getPreferredSize().height));
        adjustLook();
    }

    /**
     * Get the title of this separator.
     *
     * @return the title.
     */
    public String getTitle() {
        return label.getText();
    }

    /**
     * Set the title of the separator.
     *
     * @param title the new title.
     */
    public void setTitle(String title) {
        if (title == null) {
            title = "";
        }
        boolean isVisible = title.length() != 0;
        label.setVisible(isVisible);
        label.setText(title);
    }

    @Override
    public void updateUI() {
        super.updateUI();
        adjustLook();
    }

    private void adjustLook() {
        if (label != null) {
            Color titleColor = UIManager.getColor("TitledBorder.titleColor");
            Font font = UIManager.getFont("TitledBorder.font");
            if (titleColor == null || font == null) {
                TitledBorder titledBorder = new TitledBorder("");
                titleColor = titledBorder.getTitleColor();
                font = titledBorder.getTitleFont();
            }
            label.setForeground(titleColor);
            label.setFont(font);
        }
    }
}
