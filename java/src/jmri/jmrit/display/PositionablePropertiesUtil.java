package jmri.jmrit.display;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates the UI to set the properties of a range of Positionable Icons on (Control) Panels.
 */
public class PositionablePropertiesUtil {

    Frame mFrame = null;
    protected Positionable _parent;
    JPanel detailpanel = new JPanel();
    JTabbedPane propertiesPanel;

    PositionablePropertiesUtil(Positionable p) {
        _parent = p;
    }

    public void display() {
        propertiesPanel = new JTabbedPane();
        getCurrentValues();
        JPanel exampleHolder = new JPanel();
        //example = new JLabel(text);

        for (int i = 0; i < txtList.size(); i++) {
            JPanel p = new JPanel();
            p.setBorder(BorderFactory.createTitledBorder(txtList.get(i).getDescription()));
            p.add(txtList.get(i).getLabel()); // add a visual example for each
            exampleHolder.add(p);
        }
        //exampleHolder.add(example);
        JPanel tmp = new JPanel();

        JButton cancel = new JButton(Bundle.getMessage("ButtonCancel"));
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                undoChanges();
                mFrame.dispose();
            }
        });

        tmp.setLayout(new BoxLayout(tmp, BoxLayout.Y_AXIS));
        tmp.add(propertiesPanel);
        tmp.add(detailpanel);
        tmp.add(exampleHolder);
        textPanel();
        editText();
        borderPanel();
        sizePosition();
        JPanel _buttonArea = new JPanel();
        _buttonArea.add(cancel);

        JButton applyButton = new JButton(Bundle.getMessage("ButtonApply"));
        _buttonArea.add(applyButton);
        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fontApply();
            }
        });
        JButton okButton = new JButton(Bundle.getMessage("ButtonOK"));
        _buttonArea.add(okButton);
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fontApply();
                mFrame.dispose();
            }
        });
        tmp.add(_buttonArea);
        exampleHolder.setBackground(_parent.getParent().getBackground());
        mFrame = new JFrame(_parent.getNameString());
        mFrame.add(tmp);
        mFrame.pack();
        mFrame.setVisible(true);
        preview();
    }

    JComponent _textPanel;
    //JLabel example;

    ImageIcon[] images;
    String[] _fontcolors = {"Black", "Dark Gray", "Gray", "Light Gray", "White", "Red", "Orange", "Yellow", "Green", "Blue", "Magenta"}; // NOI18N
    // Strings in _fontcolors are used to:
    // a. look up a system color (triplet of RGB int values) by uppercase key like "DARK_GRAY"
    // b. to look up the translation of a color name (after stripping out spaces) by key like "DarkGrey"
    String[] _backgroundcolors;
    JComboBox<Integer> fontColor;
    JComboBox<Integer> backgroundColor;
    JTextField fontSizeField;

    String[] _justification = {Bundle.getMessage("left"), Bundle.getMessage("right"), Bundle.getMessage("center")};
    JComboBox<String> _justificationCombo;

    /**
     * Create and fill in the Font (Decoration) tab of the UI.
     */
    void textPanel() {
        _textPanel = new JPanel();
        _textPanel.setLayout(new BoxLayout(_textPanel, BoxLayout.Y_AXIS));
        JPanel fontColorPanel = new JPanel();
        fontColorPanel.add(new JLabel(Bundle.getMessage("FontColor") + ": "));

        JPanel backgroundColorPanel = new JPanel();
        backgroundColorPanel.add(new JLabel(Bundle.getMessage("FontBackgroundColor") + ": "));
        Color defaultLabelBackground = backgroundColorPanel.getBackground();
        _backgroundcolors = new String[_fontcolors.length + 1];
        for (int i = 0; i < _fontcolors.length; i++) {
            _backgroundcolors[i] = _fontcolors[i]; // copy _fontcolors[] to _backgroundcolors[]
        }
        _backgroundcolors[_backgroundcolors.length - 1] = "ColorClear"; // NOI18N
        // add extra line for transparent bg color; colors stored as RGB int values in xml

        Integer[] intArray = new Integer[_backgroundcolors.length];
        images = new ImageIcon[_backgroundcolors.length];
        Color desiredColor = Color.black;
        int backCurrentColor = _backgroundcolors.length - 1;
        for (int i = 0; i < _backgroundcolors.length; i++) {
            intArray[i] = Integer.valueOf(i);
            try {
                // try to get a color by name using reflection
                Field f = Color.class.getField((_backgroundcolors[i].toUpperCase()).replaceAll(" ", "_")); // like "DARK_GRAY"
                desiredColor = (Color) f.get(null);
            } catch (NoSuchFieldException ce) {
                //Can be considered normal if background is set None/Clear
                desiredColor = null;
            } catch (SecurityException ce) {
                //Can be considered normal if background is set None/Clear
                desiredColor = null;
            } catch (IllegalAccessException ce) {
                //Can be considered normal if background is set None/Clear
                desiredColor = null;
            }
            if (desiredColor != null) {
                images[i] = getColourIcon(desiredColor);
                if (desiredColor.equals(defaultBackground)) {
                    backCurrentColor = i;
                }
            } else {
                images[i] = getColourIcon(defaultLabelBackground);
                images[i].setDescription(_backgroundcolors[i]); // NOI18N
                // look up translation of color name in ColorComboBoxRenderer (ca. line 882)
            }
            if (images[i] != null) {
                images[i].setDescription(_backgroundcolors[i]);
                // look up translation of color name in ColorComboBoxRenderer (ca. line 882)
            }
        }
        backgroundColor = new JComboBox<Integer>(intArray);
        backgroundColor.setRenderer(new ColorComboBoxRenderer<Integer>());
        backgroundColor.setMaximumRowCount(5);
        backgroundColor.setSelectedIndex(backCurrentColor);
        backgroundColor.addActionListener(PreviewActionListener);
        backgroundColorPanel.add(backgroundColor);

        int fontCurrentColor = 0;
        for (int i = 0; i < _fontcolors.length; i++) {
            intArray[i] = Integer.valueOf(i);
            try {
                Field f = Color.class.getField((_fontcolors[i].toUpperCase()).replaceAll(" ", "_"));
                desiredColor = (Color) f.get(null);
            } catch (Exception ce) {
                log.error("Unable to get font color from field " + ce);
            }
            if (desiredColor != null && desiredColor.equals(defaultForeground)) {
                fontCurrentColor = i;
            }
        }

        fontColor = new JComboBox<Integer>(intArray);
        fontColor.setRenderer(new ColorComboBoxRenderer<Integer>());
        fontColor.setMaximumRowCount(5);
        fontColor.setSelectedIndex(fontCurrentColor);
        fontColor.addActionListener(PreviewActionListener);
        fontColorPanel.add(fontColor);

        JPanel fontSizePanel = new JPanel();
        fontSizePanel.setLayout(new BoxLayout(fontSizePanel, BoxLayout.Y_AXIS));
        fontSizeChoice = new JList<String>(fontSizes);

        fontSizeChoice.setSelectedValue("" + fontSize, true);
        fontSizeChoice.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listScroller = new JScrollPane(fontSizeChoice);
        listScroller.setPreferredSize(new Dimension(60, 80));

        JPanel FontPanel = new JPanel();
        fontSizeField = new JTextField("" + fontSize, fontSizeChoice.getWidth());
        fontSizeField.addKeyListener(PreviewKeyActionListener);
        fontSizePanel.add(fontSizeField);
        fontSizePanel.add(listScroller);
        FontPanel.add(fontSizePanel);

        JPanel Style = new JPanel();
        Style.setLayout(new BoxLayout(Style, BoxLayout.Y_AXIS));
        Style.add(bold);
        Style.add(italic);
        FontPanel.add(Style);
        _textPanel.add(FontPanel);

        JPanel ColorPanel = new JPanel();
        ColorPanel.setLayout(new BoxLayout(ColorPanel, BoxLayout.Y_AXIS));
        //ColorPanel.add(fontColorPanel);
        //ColorPanel.add(backgroundColorPanel);
        _textPanel.add(ColorPanel);

        JPanel justificationPanel = new JPanel();
        _justificationCombo = new JComboBox<String>(_justification);
        if (justification == 0x00) {
            _justificationCombo.setSelectedIndex(0);
        } else if (justification == 0x02) {
            _justificationCombo.setSelectedIndex(1);
        } else {
            _justificationCombo.setSelectedIndex(2);
        }
        justificationPanel.add(new JLabel(Bundle.getMessage("Justification") + ": "));
        justificationPanel.add(_justificationCombo);
        _textPanel.add(justificationPanel);

        _justificationCombo.addActionListener(PreviewActionListener);
        bold.addActionListener(PreviewActionListener);
        italic.addActionListener(PreviewActionListener);
        //fontSizeChoice.addActionListener(PreviewActionListener);
        fontSizeChoice.getSelectionModel().addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            @Override
            public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                fontSizeField.setText(fontSizeChoice.getSelectedValue());
                preview();
            }
        });

        for (int i = 0; i < txtList.size(); i++) { // repeat 4 times for sensor icons, or just once
            final int x = i;

            int fontcolor = 0;
            int backcolor = _backgroundcolors.length - 1;
            for (int j = 0; j < _backgroundcolors.length; j++) {
                try {
                    // try to get a color by name using reflection
                    Field f = Color.class.getField((_backgroundcolors[j].toUpperCase()).replaceAll(" ", "_"));
                    desiredColor = (Color) f.get(null);
                } catch (NoSuchFieldException ce) {
                    desiredColor = null;
                } catch (IllegalAccessException ce) {
                    desiredColor = null;
                }
                if (desiredColor != null) {
                    if (desiredColor.equals(txtList.get(i).getBackground())) {
                        backcolor = j;
                    }
                    if (desiredColor.equals(txtList.get(i).getForeground())) {
                        fontcolor = j;
                    }
                }
            }

            final JComboBox<Integer> txtColor = new JComboBox<Integer>(intArray);
            JPanel txtPanel = new JPanel();
            //txtPanel.setLayout(new BoxLayout(txtPanel, BoxLayout.Y_AXIS));
            JPanel p = new JPanel();
            txtColor.setRenderer(new ColorComboBoxRenderer<Integer>());
            txtColor.setMaximumRowCount(5);

            txtColor.setSelectedIndex(fontcolor);
            txtColor.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    txtList.get(x).setForeground(colorFromComboBox(txtColor, Color.black));
                }
            });
            p.add(new JLabel(Bundle.getMessage("FontColor") + ": "));
            p.add(txtColor);
            txtPanel.add(p);
            final JComboBox<Integer> txtBackColor = new JComboBox<Integer>(intArray);
            txtBackColor.setRenderer(new ColorComboBoxRenderer<Integer>());
            txtBackColor.setMaximumRowCount(5);
            txtBackColor.setSelectedIndex(backcolor);
            txtBackColor.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    txtList.get(x).setBackground(colorFromComboBox(txtBackColor, null));
                }
            });
            p = new JPanel();
            p.add(new JLabel(Bundle.getMessage("FontBackgroundColor") + ": "));
            p.add(txtBackColor);

            String _borderTitle = txtList.get(i).getDescription();
            if (_borderTitle.equals(Bundle.getMessage("TextExampleLabel"))) {
                _borderTitle = Bundle.getMessage("TextDecoLabel"); // replace default label by an appropriate one for text decoration box on Font tab
            }
            txtPanel.setBorder(BorderFactory.createTitledBorder(_borderTitle));
            txtPanel.add(p);

            _textPanel.add(txtPanel);

        }
        propertiesPanel.addTab(Bundle.getMessage("FontTabTitle"), null, _textPanel, Bundle.getMessage("FontTabTooltip"));
    }

    ActionListener PreviewActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            preview();
        }
    };

    ChangeListener SpinnerChangeListener = new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent actionEvent) {
            preview();
        }
    };

    FocusListener textFieldFocus = new FocusListener() {
        @Override
        public void focusGained(FocusEvent e) {
        }

        @Override
        public void focusLost(FocusEvent e) {
            JTextField tmp = (JTextField) e.getSource();
            if (tmp.getText().equals("")) {
                tmp.setText("0");
                preview();
            }
        }
    };

    KeyListener PreviewKeyActionListener = new KeyListener() {
        @Override
        public void keyTyped(KeyEvent E) {
        }

        @Override
        public void keyPressed(KeyEvent E) {
        }

        @Override
        public void keyReleased(KeyEvent E) {
            JTextField tmp = (JTextField) E.getSource();
            if (!tmp.getText().equals("")) {
                preview();
            }
        }
    };

    JComboBox<Integer> borderColorCombo;
    javax.swing.JSpinner borderSizeTextSpin;
    javax.swing.JSpinner marginSizeTextSpin;

    /**
     * Create and fill in the Border tab of the UI.
     */
    void borderPanel() {
        Color desiredColor = null;
        JPanel borderPanel = new JPanel();

        Integer[] intArray = new Integer[_backgroundcolors.length];
        int borderCurrentColor = _backgroundcolors.length - 1;
        for (int i = 0; i < (_backgroundcolors.length - 1); i++) {
            intArray[i] = Integer.valueOf(i);
            try {
                Field f = Color.class.getField((_fontcolors[i].toUpperCase()).replaceAll(" ", "_"));
                desiredColor = (Color) f.get(null);
            } catch (Exception ce) {
                log.error("Unable to convert the selected font color to a color " + ce);
            }
            if (desiredColor != null && desiredColor.equals(defaultBorderColor)) {
                borderCurrentColor = i;
            }
        }
        //Last colour on the background is None.
        intArray[_backgroundcolors.length - 1] = Integer.valueOf(_backgroundcolors.length - 1);
        borderColorCombo = new JComboBox<Integer>(intArray);
        borderColorCombo.setRenderer(new ColorComboBoxRenderer<Integer>());
        borderColorCombo.setMaximumRowCount(5);
        borderColorCombo.setSelectedIndex(borderCurrentColor);
        borderColorCombo.addActionListener(PreviewActionListener);

        JPanel borderColorPanel = new JPanel();
        borderColorPanel.add(new JLabel(Bundle.getMessage("borderColor") + ": "));
        borderColorPanel.add(borderColorCombo);

        JPanel borderSizePanel = new JPanel();
        borderSizeTextSpin = getSpinner(borderSize, Bundle.getMessage("borderSize"));
        borderSizeTextSpin.addChangeListener(SpinnerChangeListener);
        borderSizePanel.add(new JLabel(Bundle.getMessage("borderSize") + ": "));
        borderSizePanel.add(borderSizeTextSpin);

        JPanel marginSizePanel = new JPanel();
        marginSizeTextSpin = getSpinner(marginSize, Bundle.getMessage("marginSize"));
        marginSizeTextSpin.addChangeListener(SpinnerChangeListener);

        marginSizePanel.add(new JLabel(Bundle.getMessage("marginSize") + ": "));
        marginSizePanel.add(marginSizeTextSpin);

        borderPanel.setLayout(new BoxLayout(borderPanel, BoxLayout.Y_AXIS));
        borderPanel.add(borderColorPanel);
        borderPanel.add(borderSizePanel);
        borderPanel.add(marginSizePanel);

        propertiesPanel.addTab(Bundle.getMessage("Border"), null, borderPanel, Bundle.getMessage("BorderTabTooltip"));

    }

    javax.swing.JSpinner xPositionTextSpin;
    javax.swing.JSpinner yPositionTextSpin;
    javax.swing.JSpinner widthSizeTextSpin;
    javax.swing.JSpinner heightSizeTextSpin;
    JCheckBox autoWidth;

    /**
     * Create and fill in the Contents tab of the UI (Text Label objects).
     */
    void editText() {
        JPanel editText = new JPanel();
        editText.setLayout(new BoxLayout(editText, BoxLayout.Y_AXIS));
        for (int i = 0; i < txtList.size(); i++) {
            final int x = i;
            JPanel p = new JPanel();

            String _borderTitle = txtList.get(i).getDescription();
            if (_borderTitle.equals(Bundle.getMessage("TextExampleLabel"))) {
                _borderTitle = Bundle.getMessage("TextBorderLabel"); // replace label provided by Ctor by an appropriate one for text string box on Contents tab
            }
            p.setBorder(BorderFactory.createTitledBorder(_borderTitle));

            JLabel txt = new JLabel(Bundle.getMessage("TextValueLabel") + ": ");
            JTextField textField = new JTextField(txtList.get(i).getText(), 20);
            textField.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent E) {
                }

                @Override
                public void keyPressed(KeyEvent E) {
                }

                @Override
                public void keyReleased(KeyEvent E) {
                    JTextField tmp = (JTextField) E.getSource();
                    txtList.get(x).setText(tmp.getText());
                    preview();
                }
            });
            p.add(txt);
            p.add(textField);
            editText.add(p);
        }
        propertiesPanel.addTab(Bundle.getMessage("EditTextLabel"), null, editText, Bundle.getMessage("EditTabTooltip"));
    }

    /**
     * Create and fill in the Size &amp; Position tab of the UI.
     */
    void sizePosition() {

        JPanel posPanel = new JPanel();

        JPanel xyPanel = new JPanel();
        xyPanel.setLayout(new BoxLayout(xyPanel, BoxLayout.Y_AXIS));
        JPanel xPanel = new JPanel();
        JLabel txt = new JLabel(" X: ");
        xPositionTextSpin = getSpinner(xPos, "x position");
        xPositionTextSpin.addChangeListener(SpinnerChangeListener);
        xPanel.add(txt);
        xPanel.add(xPositionTextSpin);

        JPanel yPanel = new JPanel();
        txt = new JLabel(" Y: ");
        yPositionTextSpin = getSpinner(yPos, "y position");
        yPositionTextSpin.addChangeListener(SpinnerChangeListener);
        yPanel.add(txt);
        yPanel.add(yPositionTextSpin);

        xyPanel.add(xPanel);
        xyPanel.add(yPanel);

        JPanel sizePanel = new JPanel();
        sizePanel.setLayout(new BoxLayout(sizePanel, BoxLayout.Y_AXIS));
        JPanel widthPanel = new JPanel();
        widthSizeTextSpin = getSpinner(fixedWidth, Bundle.getMessage("width"));
        widthSizeTextSpin.addChangeListener(SpinnerChangeListener);
        /*widthSizeText = new JTextField(""+fixedWidth, 10);
         widthSizeText.addKeyListener(PreviewKeyActionListener);*/
        txt = new JLabel(Bundle.getMessage("width") + ": ");
        widthPanel.add(txt);
        widthPanel.add(widthSizeTextSpin);

        JPanel heightPanel = new JPanel();
        /*heightSizeText = new JTextField(""+fixedHeight, 10);
         heightSizeText.addKeyListener(PreviewKeyActionListener);*/
        heightSizeTextSpin = getSpinner(fixedHeight, Bundle.getMessage("height"));
        heightSizeTextSpin.addChangeListener(SpinnerChangeListener);
        txt = new JLabel(Bundle.getMessage("height") + ": ");
        heightPanel.add(txt);
        heightPanel.add(heightSizeTextSpin);

        sizePanel.add(widthPanel);
        sizePanel.add(heightPanel);

        posPanel.add(xyPanel);
        posPanel.add(sizePanel);
        posPanel.setLayout(new BoxLayout(posPanel, BoxLayout.Y_AXIS));

        propertiesPanel.addTab(Bundle.getMessage("SizeTabTitle"), null, posPanel, Bundle.getMessage("SizeTabTooltip"));
    }

    void fontApply() {
        pop.setFontSize(Integer.parseInt(fontSizeField.getText()));
        if (bold.isSelected()) {
            pop.setFontStyle(Font.BOLD, 0);
        } else {
            pop.setFontStyle(0, Font.BOLD);
        }
        if (italic.isSelected()) {
            pop.setFontStyle(Font.ITALIC, 0);
        } else {
            pop.setFontStyle(0, Font.ITALIC);
        }
        Color desiredColor = Color.black;
        try {
            String selectedColor = _fontcolors[fontColor.getSelectedIndex()];
            Field f = Color.class.getField(((selectedColor).toUpperCase()).replaceAll(" ", "_"));
            desiredColor = (Color) f.get(null);
        } catch (NoSuchFieldException ce) {
            desiredColor = Color.black;
        } catch (SecurityException ce) {
            desiredColor = Color.black;
        } catch (IllegalAccessException ce) {
            desiredColor = Color.black;
        }

        if (_parent instanceof SensorIcon) {
            SensorIcon si = (SensorIcon) _parent;
            if (si.isIcon()) {
                PositionableLabel pp = (PositionableLabel) _parent;
                pp.setText(txtList.get(0).getText());
                pop.setForeground(txtList.get(0).getForeground());
                pop.setBackgroundColor(txtList.get(0).getBackground());
            } else {
                si.setActiveText(txtList.get(0).getText());
                si.setTextActive(txtList.get(0).getForeground());
                si.setBackgroundActive(txtList.get(0).getBackground());

                si.setInactiveText(txtList.get(1).getText());
                si.setTextInActive(txtList.get(1).getForeground());
                si.setBackgroundInActive(txtList.get(1).getBackground());

                si.setUnknownText(txtList.get(2).getText());
                si.setTextUnknown(txtList.get(2).getForeground());
                si.setBackgroundUnknown(txtList.get(2).getBackground());

                si.setInconsistentText(txtList.get(3).getText());
                si.setTextInconsistent(txtList.get(3).getForeground());
                si.setBackgroundInconsistent(txtList.get(3).getBackground());
            }
        } else {
            PositionableLabel pp = (PositionableLabel) _parent;
            pp.setText(txtList.get(0).getText());
            pop.setForeground(txtList.get(0).getForeground());
            pop.setBackgroundColor(txtList.get(0).getBackground());
        }

        int deg = _parent.getDegrees();
        if (deg!=0) {
            _parent.rotate(0);
        }
        desiredColor = colorFromComboBox(borderColorCombo, null);
        pop.setBorderColor(desiredColor);

        pop.setBorderSize(((Number) borderSizeTextSpin.getValue()).intValue());

        pop.setMargin(((Number) marginSizeTextSpin.getValue()).intValue());
        _parent.setLocation(((Number) xPositionTextSpin.getValue()).intValue(), ((Number) yPositionTextSpin.getValue()).intValue());
        pop.setFixedWidth(((Number) widthSizeTextSpin.getValue()).intValue());
        pop.setFixedHeight(((Number) heightSizeTextSpin.getValue()).intValue());
        switch (_justificationCombo.getSelectedIndex()) {
            case 0:
                pop.setJustification(0x00);
                break;
            case 1:
                pop.setJustification(0x02);
                break;
            case 2:
                pop.setJustification(0x04);
                break;
            default:
                log.warn("Unhandled combo index: {}", _justificationCombo.getSelectedIndex());
                break;
        }
        _parent.rotate(deg);
    }

    void cancelButton() {
        mFrame.dispose();
    }

    void preview() {
        int attrs = Font.PLAIN;
        if (bold.isSelected()) {
            attrs = Font.BOLD;
        }
        if (italic.isSelected()) {
            attrs |= Font.ITALIC;
        }

        Font newFont = new Font(_parent.getFont().getName(), attrs, Integer.parseInt(fontSizeField.getText()));

        Color desiredColor;

        desiredColor = colorFromComboBox(borderColorCombo, null);
        Border borderMargin = BorderFactory.createEmptyBorder(0, 0, 0, 0);
        int margin = ((Number) marginSizeTextSpin.getValue()).intValue();
        Border outlineBorder = BorderFactory.createEmptyBorder(0, 0, 0, 0);
        if (desiredColor != null) {
            outlineBorder = new LineBorder(desiredColor, ((Number) borderSizeTextSpin.getValue()).intValue());
        } else {
            outlineBorder = BorderFactory.createEmptyBorder(0, 0, 0, 0);
        }
        int hoz = 0;
        switch (_justificationCombo.getSelectedIndex()) {
            case 0:
                hoz = (0x02);
                break;
            case 1:
                hoz = (0x04);
                break;
            case 2:
                hoz = (0x00);
                break;
            default:
                log.warn("Unhandled combo index: {}", _justificationCombo.getSelectedIndex());
                break;
        }

        for (int i = 0; i < txtList.size(); i++) {
            JLabel tmp = txtList.get(i).getLabel();
            if (tmp.isOpaque()) {
                borderMargin = new LineBorder(tmp.getBackground(), margin);
            } else {
                borderMargin = BorderFactory.createEmptyBorder(margin, margin, margin, margin);
            }
            tmp.setFont(newFont);
            tmp.setHorizontalAlignment(hoz);
            tmp.setBorder(new CompoundBorder(outlineBorder, borderMargin));
            tmp.setSize(new Dimension(maxWidth(tmp), maxHeight(tmp)));
            tmp.setPreferredSize(new Dimension(maxWidth(tmp), maxHeight(tmp)));

        }
        mFrame.pack();
    }

    int maxWidth(JLabel tmp) {
        int max = 0;
        if (((Number) widthSizeTextSpin.getValue()).intValue() != 0) {
            max = ((Number) widthSizeTextSpin.getValue()).intValue();
            max += ((Number) borderSizeTextSpin.getValue()).intValue() * 2;
        } else {
            if (tmp.getText().trim().length() > 0) {
                max = tmp.getFontMetrics(tmp.getFont()).stringWidth(tmp.getText());
            }
            if (pop != null) {
                max += ((Number) marginSizeTextSpin.getValue()).intValue() * 2;
                max += ((Number) borderSizeTextSpin.getValue()).intValue() * 2;
            }
        }
        return max;
    }

    public int maxHeight(JLabel tmp) {
        int max = 0;
        if (((Number) heightSizeTextSpin.getValue()).intValue() != 0) {
            max = ((Number) heightSizeTextSpin.getValue()).intValue();
            max += ((Number) borderSizeTextSpin.getValue()).intValue() * 2;
        } else {
            if (tmp.getText().trim().length() > 0) {
                max = tmp.getFontMetrics(tmp.getFont()).getHeight();
            }
            if (pop != null) {
                max += ((Number) marginSizeTextSpin.getValue()).intValue() * 2;
                max += ((Number) borderSizeTextSpin.getValue()).intValue() * 2;
            }
        }

        return max;
    }
    PositionablePopupUtil pop;

    private void undoChanges() {
        if (_parent instanceof SensorIcon) {
            SensorIcon si = (SensorIcon) _parent;
            if (si.isIcon()) {
                PositionableLabel pp = (PositionableLabel) _parent;
                pp.setText(txtList.get(0).getOrigText());
                pop.setForeground(txtList.get(0).getOrigForeground());
                pop.setBackgroundColor(txtList.get(0).getOrigBackground());
            } else {
                si.setActiveText(txtList.get(0).getOrigText());
                si.setTextActive(txtList.get(0).getOrigForeground());
                si.setBackgroundActive(txtList.get(0).getOrigBackground());

                si.setInactiveText(txtList.get(1).getOrigText());
                si.setTextInActive(txtList.get(1).getOrigForeground());
                si.setBackgroundInActive(txtList.get(1).getOrigBackground());

                si.setUnknownText(txtList.get(2).getOrigText());
                si.setTextUnknown(txtList.get(2).getOrigForeground());
                si.setBackgroundUnknown(txtList.get(2).getOrigBackground());

                si.setInconsistentText(txtList.get(3).getOrigText());
                si.setTextInconsistent(txtList.get(3).getOrigForeground());
                si.setBackgroundInconsistent(txtList.get(3).getOrigBackground());
            }
        } else {
            PositionableLabel pp = (PositionableLabel) _parent;
            pp.setText(txtList.get(0).getOrigText());
            pop.setForeground(txtList.get(0).getOrigForeground());
            pop.setBackgroundColor(txtList.get(0).getOrigBackground());
        }
        int deg = _parent.getDegrees();
        if (deg!=0) {
            _parent.rotate(0);
        }
        pop.setJustification(justification);
        pop.setFixedWidth(fixedWidth);
        pop.setFixedHeight(fixedHeight);
        pop.setMargin(marginSize);
        pop.setBorderSize(borderSize);
        pop.setFontStyle(0, fontStyle);
        pop.setFontSize(fontSize);
        pop.setBorderColor(defaultBorderColor);
        _parent.setLocation(xPos, yPos);
        _parent.rotate(deg);
    }

    private void getCurrentValues() {
        pop = _parent.getPopupUtility();
        txtList = new ArrayList<TextDetails>();

        if (_parent instanceof SensorIcon) {
            SensorIcon si = (SensorIcon) _parent;
            if (si.isIcon()) {
                // just 1 label Example
                txtList.add(new TextDetails(Bundle.getMessage("TextExampleLabel"), pop.getText(), pop.getForeground(), _parent.getBackground()));
            } else {
                // 4 different labels (and bordered boxes to set decoration of) labels
                txtList.add(new TextDetails(Bundle.getMessage("SensorStateActive"), si.getActiveText(), si.getTextActive(), si.getBackgroundActive()));
                txtList.add(new TextDetails(Bundle.getMessage("SensorStateInactive"), si.getInactiveText(), si.getTextInActive(), si.getBackgroundInActive()));
                txtList.add(new TextDetails(Bundle.getMessage("BeanStateUnknown"), si.getUnknownText(), si.getTextUnknown(), si.getBackgroundUnknown()));
                txtList.add(new TextDetails(Bundle.getMessage("BeanStateInconsistent"), si.getInconsistentText(), si.getTextInconsistent(), si.getBackgroundInconsistent()));
            }
        } else {
            // just 1 label Example
            txtList.add(new TextDetails(Bundle.getMessage("TextExampleLabel"), pop.getText(), pop.getForeground(), _parent.getBackground()));
        }

        fixedWidth = pop.getFixedWidth();
        fixedHeight = pop.getFixedHeight();
        marginSize = pop.getMargin();
        borderSize = pop.getBorderSize();
        justification = pop.getJustification();
        fontStyle = pop.getFontStyle();
        fontSize = pop.getFontSize();
        if ((Font.BOLD & fontStyle) == Font.BOLD) {
            bold.setSelected(true);
        }
        if ((Font.ITALIC & fontStyle) == Font.ITALIC) {
            italic.setSelected(true);
        }
        if (_parent.isOpaque()) {
            defaultBackground = _parent.getBackground();
        }
        defaultForeground = pop.getForeground();
        defaultBorderColor = pop.getBorderColor();
        if (_parent instanceof MemoryIcon) {
            MemoryIcon pm = (MemoryIcon) _parent;
            xPos = pm.getOriginalX();
            yPos = pm.getOriginalY();
        } else {
            xPos = _parent.getX();
            yPos = _parent.getY();
        }
    }
    private int fontStyle;
    private Color defaultForeground;
    private Color defaultBackground;
    private Color defaultBorderColor;
    private int fixedWidth = 0;
    private int fixedHeight = 0;
    private int marginSize = 0;
    private int borderSize = 0;
    private int justification;
    private int fontSize;
    private int xPos;
    private int yPos;

    private ArrayList<TextDetails> txtList = null;

    private JCheckBox italic = new JCheckBox(Bundle.getMessage("Italic"), false);
    private JCheckBox bold = new JCheckBox(Bundle.getMessage("Bold"), false);

    protected JList<String> fontSizeChoice;

    protected String fontSizes[] = {"6", "8", "10", "11", "12", "14", "16",
        "20", "24", "28", "32", "36"};

    Color colorFromComboBox(JComboBox<Integer> select, Color defaultColor) {
        Color desiredColor = defaultColor;
        try {
            // try to get a color by name using reflection
            String selectedColor = _backgroundcolors[select.getSelectedIndex()];
            Field f = Color.class.getField((selectedColor.toUpperCase()).replaceAll(" ", "_"));
            desiredColor = (Color) f.get(null);
        } catch (NoSuchFieldException ce) {
            desiredColor = defaultColor;
        } catch (SecurityException ce) {
            desiredColor = defaultColor;
        } catch (IllegalAccessException ce) {
            desiredColor = defaultColor;
        }
        return desiredColor;
    }

    ImageIcon getColourIcon(Color color) {

        int ICON_DIMENSION = 10;
        BufferedImage image = new BufferedImage(ICON_DIMENSION, ICON_DIMENSION,
                BufferedImage.TYPE_INT_RGB);

        Graphics g = image.getGraphics();
        // set completely transparent
        g.setColor(color);
        g.fillRect(0, 0, ICON_DIMENSION, ICON_DIMENSION);

        ImageIcon icon = new ImageIcon(image);
        return icon;

    }

    javax.swing.JSpinner getSpinner(int value, String tooltip) {
        SpinnerNumberModel model = new SpinnerNumberModel(0, 0, 1000, 1);
        javax.swing.JSpinner spinX = new javax.swing.JSpinner(model);
        spinX.setValue(value);
        spinX.setToolTipText(tooltip);
        spinX.setMaximumSize(new Dimension(
                spinX.getMaximumSize().width, spinX.getPreferredSize().height));
        return spinX;
    }

    class ColorComboBoxRenderer<E> extends JLabel
            implements ListCellRenderer<E> {

        public ColorComboBoxRenderer() {
            setOpaque(true);
            setHorizontalAlignment(LEFT);
            setVerticalAlignment(CENTER);
        }

        // FIXME: This still needs the JList typed, but I'm unsure how to type it properly
        @Override
        public Component getListCellRendererComponent(@SuppressWarnings("rawtypes") JList list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            if (value == null) {
                return this;
            }
            int selectedIndex = ((Integer) value).intValue();
            ImageIcon icon = images[selectedIndex];
            // String colorString = _backgroundcolors[selectedIndex];
            // called every time the user opens color drop down and while hovering over/selecting a color from list
            String colorString = Bundle.getMessage(_backgroundcolors[selectedIndex].replaceAll(" ", ""));
            // I18N looks up translated name of color in Bundle
            setIcon(icon);
            setText(colorString);
            return this;
        }
    }

    static class TextDetails {

        TextDetails(String desc, String txt, Color fore, Color back) {
            if (txt == null) {
                text = "";
                // contents of icon state labels <active> are entered in SensorIcon.java
            } else {
                text = txt;
            }
            description = desc;
            example = new JLabel(text);
            setForeground(fore);
            setBackground(back);
            origForeground = fore;
            origBackground = back;
            origText = txt;
        }

        Color foreground;
        Color background;
        Color origForeground;
        Color origBackground;
        String origText;
        String text;
        JLabel example;
        String description;

        Color getForeground() {
            return foreground;
        }

        Color getBackground() {
            return background;
        }

        String getText() {
            return text;
        }

        Color getOrigForeground() {
            return origForeground;
        }

        Color getOrigBackground() {
            return origBackground;
        }

        String getOrigText() {
            return origText;
        }

        String getDescription() {
            return description;
        }

        void setForeground(Color fore) {
            foreground = fore;
            example.setForeground(fore);
        }

        void setBackground(Color back) {
            background = back;
            if (back != null) {
                example.setOpaque(true);
                example.setBackground(back);
            } else {
                example.setOpaque(false);
            }
        }

        void setText(String txt) {
            text = txt;
            example.setText(txt);
        }

        JLabel getLabel() {
            return example;
        }

    }
    private final static Logger log = LoggerFactory.getLogger(PositionablePropertiesUtil.class.getName());
}
