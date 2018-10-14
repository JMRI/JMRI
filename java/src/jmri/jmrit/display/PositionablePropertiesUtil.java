package jmri.jmrit.display;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import jmri.util.swing.SplitButtonColorChooserPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates the UI to set the properties of a range of Positionable Icons on
 * (Control) Panels.
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

        tmp.setLayout(new BoxLayout(tmp, BoxLayout.Y_AXIS));
        tmp.add(propertiesPanel);
        tmp.add(detailpanel);
        tmp.add(exampleHolder);
        textPanel();
        editText();
        borderPanel();
        sizePosition();

        JPanel _buttonArea = new JPanel();

        JButton cancel = new JButton(Bundle.getMessage("ButtonCancel"));
        _buttonArea.add(cancel);
        cancel.addActionListener((ActionEvent e) -> {
            undoChanges();
            mFrame.dispose();
        });

        JButton applyButton = new JButton(Bundle.getMessage("ButtonApply"));
        _buttonArea.add(applyButton);
        applyButton.addActionListener((ActionEvent e) -> {
            fontApply();
        });

        JButton okButton = new JButton(Bundle.getMessage("ButtonOK"));
        _buttonArea.add(okButton);
        okButton.addActionListener((ActionEvent e) -> {
            fontApply();
            mFrame.dispose();
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

        JPanel fontSizePanel = new JPanel();
        fontSizePanel.setLayout(new BoxLayout(fontSizePanel, BoxLayout.Y_AXIS));
        fontSizeChoice = new JList<>(fontSizes);

        fontSizeChoice.setSelectedValue("" + fontSize, true);
        fontSizeChoice.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listScroller = new JScrollPane(fontSizeChoice);
        listScroller.setPreferredSize(new Dimension(60, 80));

        JPanel FontPanel = new JPanel();
        fontSizeField = new JTextField("" + fontSize, fontSizeChoice.getWidth());
        fontSizeField.addKeyListener(previewKeyActionListener);
        fontSizePanel.add(fontSizeField);
        fontSizePanel.add(listScroller);
        FontPanel.add(fontSizePanel);

        JPanel Style = new JPanel();
        Style.setLayout(new BoxLayout(Style, BoxLayout.Y_AXIS));
        Style.add(bold);
        Style.add(italic);
        FontPanel.add(Style);
        _textPanel.add(FontPanel);

        JPanel justificationPanel = new JPanel();
        _justificationCombo = new JComboBox<>(_justification);
        switch (justification) {
            case 0x00:
                _justificationCombo.setSelectedIndex(0);
                break;
            case 0x02:
                _justificationCombo.setSelectedIndex(1);
                break;
            default:
                _justificationCombo.setSelectedIndex(2);
                break;
        }
        justificationPanel.add(new JLabel(Bundle.getMessage("Justification") + ": "));
        justificationPanel.add(_justificationCombo);
        _textPanel.add(justificationPanel);

        _justificationCombo.addActionListener(previewActionListener);
        bold.addActionListener(previewActionListener);
        italic.addActionListener(previewActionListener);
        //fontSizeChoice.addActionListener(previewActionListener);
        fontSizeChoice.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            fontSizeField.setText(fontSizeChoice.getSelectedValue());
            preview();
        });

        for (int i = 0; i < txtList.size(); i++) { // repeat 4 times for sensor icons, or just once
            final int x = i;

            JPanel txtPanel = new JPanel();

            JColorChooser txtColorChooser = new JColorChooser(defaultForeground);
            txtColorChooser.setPreviewPanel(new JPanel()); // remove the preview panel
            AbstractColorChooserPanel txtColorPanels[] = { new SplitButtonColorChooserPanel()};
            txtColorChooser.setChooserPanels(txtColorPanels);
            txtColorChooser.getSelectionModel().addChangeListener(previewChangeListener);
            txtPanel.add(txtColorChooser);
            txtColorChooser.getSelectionModel().addChangeListener((ChangeEvent ce) -> {
                txtList.get(x).setForeground(txtColorChooser.getColor());
            });

            JPanel p = new JPanel();
            p.add(new JLabel(Bundle.getMessage("FontColor") + ": "));
            p.add(txtColorChooser);

            txtPanel.add(p);

            defaultBackground = _parent.getBackground();
            JColorChooser txtBackColorChooser = new JColorChooser(defaultBackground);
            txtBackColorChooser.setPreviewPanel(new JPanel()); // remove the preview panel
            AbstractColorChooserPanel txtBackColorPanels[] = { new SplitButtonColorChooserPanel()};
            txtBackColorChooser.setChooserPanels(txtBackColorPanels);
            txtBackColorChooser.getSelectionModel().addChangeListener(previewChangeListener);
            txtPanel.add(txtBackColorChooser);
            txtBackColorChooser.getSelectionModel().addChangeListener((ChangeEvent ce) -> {
                txtList.get(x).setBackground(txtBackColorChooser.getColor());
            });
            p = new JPanel();
            p.add(new JLabel(Bundle.getMessage("FontBackgroundColor") + ": "));
            p.add(txtBackColorChooser);

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

    ActionListener previewActionListener = (ActionEvent actionEvent) -> {
        preview();
    };

    ChangeListener spinnerChangeListener = (ChangeEvent actionEvent) -> {
        preview();
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

    KeyListener previewKeyActionListener = new KeyListener() {
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

    ChangeListener previewChangeListener = (ChangeEvent ce) -> {
        preview();
    };

    private JColorChooser borderColorChooser = null;
    javax.swing.JSpinner borderSizeTextSpin;
    javax.swing.JSpinner marginSizeTextSpin;

    /**
     * Create and fill in the Border tab of the UI.
     */
    void borderPanel() {
        JPanel borderPanel = new JPanel();

        borderColorChooser = new JColorChooser(defaultBorderColor);
        AbstractColorChooserPanel borderColorPanels[] = { new SplitButtonColorChooserPanel()};
        borderColorChooser.setChooserPanels(borderColorPanels);
        borderColorChooser.setPreviewPanel(new JPanel()); // remove the preview panel

        borderColorChooser.getSelectionModel().addChangeListener(previewChangeListener);

        JPanel borderColorPanel = new JPanel();
        borderColorPanel.add(new JLabel(Bundle.getMessage("borderColor") + ": "));
        borderColorPanel.add(borderColorChooser);

        JPanel borderSizePanel = new JPanel();
        borderSizeTextSpin = getSpinner(borderSize, Bundle.getMessage("borderSize"));
        borderSizeTextSpin.addChangeListener(spinnerChangeListener);
        borderSizePanel.add(new JLabel(Bundle.getMessage("borderSize") + ": "));
        borderSizePanel.add(borderSizeTextSpin);

        JPanel marginSizePanel = new JPanel();
        marginSizeTextSpin = getSpinner(marginSize, Bundle.getMessage("marginSize"));
        marginSizeTextSpin.addChangeListener(spinnerChangeListener);

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
        xPositionTextSpin.addChangeListener(spinnerChangeListener);
        xPanel.add(txt);
        xPanel.add(xPositionTextSpin);

        JPanel yPanel = new JPanel();
        txt = new JLabel(" Y: ");
        yPositionTextSpin = getSpinner(yPos, "y position");
        yPositionTextSpin.addChangeListener(spinnerChangeListener);
        yPanel.add(txt);
        yPanel.add(yPositionTextSpin);

        xyPanel.add(xPanel);
        xyPanel.add(yPanel);

        JPanel sizePanel = new JPanel();
        sizePanel.setLayout(new BoxLayout(sizePanel, BoxLayout.Y_AXIS));
        JPanel widthPanel = new JPanel();
        widthSizeTextSpin = getSpinner(fixedWidth, Bundle.getMessage("width"));
        widthSizeTextSpin.addChangeListener(spinnerChangeListener);
        /*widthSizeText = new JTextField(""+fixedWidth, 10);
         widthSizeText.addKeyListener(previewKeyActionListener);*/
        txt = new JLabel(Bundle.getMessage("width") + ": ");
        widthPanel.add(txt);
        widthPanel.add(widthSizeTextSpin);

        JPanel heightPanel = new JPanel();
        /*heightSizeText = new JTextField(""+fixedHeight, 10);
         heightSizeText.addKeyListener(previewKeyActionListener);*/
        heightSizeTextSpin = getSpinner(fixedHeight, Bundle.getMessage("height"));
        heightSizeTextSpin.addChangeListener(spinnerChangeListener);
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

        Color desiredColor;
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
        if (deg != 0) {
            _parent.rotate(0);
        }
        desiredColor = borderColorChooser.getColor();
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

        desiredColor = borderColorChooser.getColor();
        Border borderMargin;
        int margin = ((Number) marginSizeTextSpin.getValue()).intValue();
        Border outlineBorder;
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
        if (deg != 0) {
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
        txtList = new ArrayList<>();

        if (_parent instanceof SensorIcon) {
            SensorIcon si = (SensorIcon) _parent;
            if (si.isIcon()) {
                // just 1 label Example
                txtList.add(new TextDetails(Bundle.getMessage("TextExampleLabel"), pop.getText(), pop.getForeground(), pop.getBackground()));
            } else {
                // 4 different labels (and bordered boxes to set decoration of) labels
                txtList.add(new TextDetails(Bundle.getMessage("SensorStateActive"), si.getActiveText(), si.getTextActive(), si.getBackgroundActive()));
                txtList.add(new TextDetails(Bundle.getMessage("SensorStateInactive"), si.getInactiveText(), si.getTextInActive(), si.getBackgroundInActive()));
                txtList.add(new TextDetails(Bundle.getMessage("BeanStateUnknown"), si.getUnknownText(), si.getTextUnknown(), si.getBackgroundUnknown()));
                txtList.add(new TextDetails(Bundle.getMessage("BeanStateInconsistent"), si.getInconsistentText(), si.getTextInconsistent(), si.getBackgroundInconsistent()));
            }
        } else {
            // just 1 label Example
            txtList.add(new TextDetails(Bundle.getMessage("TextExampleLabel"), pop.getText(), pop.getForeground(), pop.getBackground()));
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
    private Color defaultForeground = Color.black;
    private Color defaultBackground;
    private Color defaultBorderColor = Color.black;
    private int fixedWidth = 0;
    private int fixedHeight = 0;
    private int marginSize = 0;
    private int borderSize = 0;
    private int justification;
    private int fontSize;
    private int xPos;
    private int yPos;

    private ArrayList<TextDetails> txtList = null;

    private final JCheckBox italic = new JCheckBox(Bundle.getMessage("Italic"), false);
    private final JCheckBox bold = new JCheckBox(Bundle.getMessage("Bold"), false);

    protected JList<String> fontSizeChoice;

    protected String fontSizes[] = {"6", "8", "10", "11", "12", "14", "16",
        "20", "24", "28", "32", "36"};

    javax.swing.JSpinner getSpinner(int value, String tooltip) {
        SpinnerNumberModel model = new SpinnerNumberModel(0, 0, 1000, 1);
        javax.swing.JSpinner spinX = new javax.swing.JSpinner(model);
        spinX.setValue(value);
        spinX.setToolTipText(tooltip);
        spinX.setMaximumSize(new Dimension(
                spinX.getMaximumSize().width, spinX.getPreferredSize().height));
        return spinX;
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

    private final static Logger log = LoggerFactory.getLogger(PositionablePropertiesUtil.class);
}
