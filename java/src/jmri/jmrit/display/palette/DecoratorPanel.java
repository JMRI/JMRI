package jmri.jmrit.display.palette;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Hashtable;
import java.util.Iterator;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.PositionableLabel;
import jmri.jmrit.display.PositionablePopupUtil;
import jmri.jmrit.display.SensorIcon;
import jmri.jmrit.display.palette.TextItemPanel.DragDecoratorLabel;

/**
 * Panel for positionables with text and/or colored margins and borders
 * 
 * @author PeteCressman Copyright (C) 2009, 2015
 */
public class DecoratorPanel extends JPanel implements ChangeListener, ItemListener {

    private static final long serialVersionUID = -5434701410549611848L;

    static final String[] JUSTIFICATION = {Bundle.getMessage("left"),
        Bundle.getMessage("center"),
        Bundle.getMessage("right")};

    static final String[] STYLES = {Bundle.getMessage("plain"),
        Bundle.getMessage("bold"),
        Bundle.getMessage("italic"),
        Bundle.getMessage("bold/italic")};

    static final String[] FONTSIZE = {"6", "8", "10", "11", "12", "14", "16",
        "20", "24", "28", "32", "36"};
    public static final int SIZE = 1;
    public static final int STYLE = 2;
    public static final int JUST = 3;

    AJComboBox _fontSizeBox;
    AJComboBox _fontStyleBox;
    AJComboBox _fontJustBox;

    public static final int STRUT = 6;

    public static final int BORDER = 1;
    public static final int MARGIN = 2;
    public static final int FWIDTH = 3;
    public static final int FHEIGHT = 4;

    public static final int TEXT_FONT = 10;
    public static final int ACTIVE_FONT = 11;
    public static final int INACTIVE_FONT = 12;
    public static final int UNKOWN_FONT = 13;
    public static final int INCONSISTENT_FONT = 14;
    public static final int TEXT_BACKGROUND = 20;
    public static final int ACTIVE_BACKGROUND = 21;
    public static final int INACTIVE_BACKGROUND = 22;
    public static final int UNKOWN_BACKGROUND = 23;
    public static final int INCONSISTENT_BACKGROUND = 24;
    public static final int TRANSPARENT_COLOR = 30;
    public static final int ACTIVE_TRANSPARENT_COLOR = 31;
    public static final int INACTIVE_TRANSPARENT_COLOR = 32;
    public static final int UNKNOWN_TRANSPARENT_COLOR = 33;
    public static final int INCONSISTENT_TRANSPARENT_COLOR = 34;
    public static final int BORDER_COLOR = 40;

    AJSpinner _borderSpin;
    AJSpinner _marginSpin;
    AJSpinner _widthSpin;
    AJSpinner _heightSpin;

    JColorChooser _chooser;
    JPanel _previewPanel;
    JPanel _samplePanel;
    private PositionablePopupUtil _util;
    private Hashtable<String, PositionableLabel> _sample = null;
    private int _selectedButton;
    ButtonGroup _buttonGroup = new ButtonGroup();

    Editor _editor;
    java.awt.Window _dialog;

    public DecoratorPanel(Editor editor, javax.swing.JDialog dialog) {
        _editor = editor;
        _dialog = dialog;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        Color bkgrnd = _editor.getTargetPanel().getBackground();
        _chooser = new JColorChooser(bkgrnd);
        _sample = new Hashtable<String, PositionableLabel>();

        _previewPanel = new JPanel();
        _previewPanel.setLayout(new BorderLayout());
        _previewPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black, 1),"Preview"));
        _previewPanel.add(Box.createVerticalStrut(STRUT), BorderLayout.NORTH);
        _previewPanel.add(Box.createVerticalStrut(STRUT), BorderLayout.SOUTH);
        _previewPanel.setBackground(bkgrnd);
         
        _samplePanel = new JPanel();
//      _samplePanel.setLayout(new BoxLayout(_samplePanel, BoxLayout.X_AXIS));
        _samplePanel.add(Box.createHorizontalStrut(STRUT));
        _samplePanel.setBackground(bkgrnd);
    }

    static class AJComboBox extends JComboBox<String> {
        private static final long serialVersionUID = -6157176023804592198L;
        int _which;

        AJComboBox(String[] items, int which) {
            super(items);
            _which = which;
        }
    }

    private JPanel makeBoxPanel(String caption, JComboBox<String> box) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel(Bundle.getMessage(caption)));
        box.addItemListener(this);
        panel.add(box);
        return panel;
    }

    static class AJSpinner extends JSpinner {

        private static final long serialVersionUID = 7526728664296406003L;
        int _which;

        AJSpinner(SpinnerModel model, int which) {
            super(model);
            _which = which;
        }
    }

    static class AJRadioButton extends JRadioButton {
        private static final long serialVersionUID = -8349059653187941804L;
        int which;

        AJRadioButton(String text, int w) {
            super(text);
            which = w;
        }
    }

    private JPanel makeSpinPanel(String caption, JSpinner spin) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel(Bundle.getMessage(caption)));
        spin.addChangeListener(this);
        panel.add(spin);
        return panel;
    }

    protected void initDecoratorPanel(DragDecoratorLabel sample) {
        sample.setDisplayLevel(Editor.LABELS);
        sample.setBackground(_editor.getTargetPanel().getBackground());
        _previewPanel.add(sample);
        _util = sample.getPopupUtility();
        _sample.put("Text", sample);
        this.add(makeTextPanel("Text", sample, TEXT_FONT, true));
        _samplePanel.add(sample);
        
        makeFontPanels();
        _chooser.getSelectionModel().addChangeListener(this);
        _chooser.setPreviewPanel(new JPanel());
        this.add(_chooser);
        _previewPanel.add(_samplePanel, java.awt.BorderLayout.CENTER);
        this.add(_previewPanel);
        updateSamples();
    }
    
    public void initDecoratorPanel(Positionable pos) {
        Positionable item = pos.deepClone();		// copy of PositionableLabel being edited
        String text = Bundle.getMessage("sample");
        _util = item.getPopupUtility();

        if (pos instanceof SensorIcon && !((SensorIcon)pos).isIcon()) {
            SensorIcon si = (SensorIcon) pos;
            if (!si.isIcon() && si.isText()) {
                PositionableLabel sample = new PositionableLabel(si.getActiveText(), _editor);
                sample.setForeground(si.getTextActive());
                Color color = si.getBackgroundActive();
                if (color!=null) {
                    sample.setBackground(color);
                    sample.setOpaque(true);
                }
                doPopupUtility("Active", ACTIVE_FONT, sample, _util, true);

                sample = new PositionableLabel(si.getInactiveText(), _editor);
                sample.setForeground(si.getTextInActive());
                color = si.getBackgroundInActive();
                if (color!=null) {
                    sample.setBackground(color);
                    sample.setOpaque(true);
                }
                doPopupUtility("InActive", INACTIVE_FONT, sample, _util, true);

                sample = new PositionableLabel(si.getUnknownText(), _editor);
                sample.setForeground(si.getTextUnknown());
                color = si.getBackgroundUnknown();
                if (color!=null) {
                    sample.setBackground(color);
                    sample.setOpaque(true);
                }
                doPopupUtility("Unknown", UNKOWN_FONT, sample, _util, true);

                sample = new PositionableLabel(si.getInconsistentText(), _editor);
                sample.setForeground(si.getTextInconsistent());
                color = si.getBackgroundInconsistent();
                if (color!=null) {
                    sample.setBackground(color);
                    sample.setOpaque(true);
                }
                doPopupUtility("Inconsistent", INCONSISTENT_FONT, sample, _util, true);
            }
        } else { // not a SensorIcon
            PositionableLabel sample = new PositionableLabel(text, _editor);
            sample.setForeground(pos.getForeground());
            sample.setBackground(pos.getBackground());
            sample.setOpaque(_util.hasBackground());
            boolean addtextField;
            if (pos instanceof PositionableLabel) {
                sample.setText(((PositionableLabel)pos).getUnRotatedText());
                if (pos instanceof jmri.jmrit.display.MemoryIcon) {
                    addtextField = false;                    
                } else {
                    addtextField = true;
                }
            } else if (pos instanceof jmri.jmrit.display.MemoryInputIcon) {
                JTextField field = (JTextField)((jmri.jmrit.display.MemoryInputIcon)pos).getTextComponent();
                sample.setText(field.getText());
                addtextField = false;
            } else if (pos instanceof jmri.jmrit.display.MemoryComboIcon) {
                JComboBox<String> box = ((jmri.jmrit.display.MemoryComboIcon)pos).getTextComponent();
                sample.setText(box.getSelectedItem().toString());
                addtextField = false;
            } else if (pos instanceof jmri.jmrit.display.MemorySpinnerIcon) {
                JTextField field = (JTextField)((jmri.jmrit.display.MemorySpinnerIcon)pos).getTextComponent();
                sample.setText(field.getText());
                addtextField = false;
            } else {
                addtextField = true;                
            }
            doPopupUtility("Text", TEXT_FONT, sample, _util, addtextField);
        }
        makeFontPanels();
        item.setVisible(false);		// otherwise leaves traces for PositionableJPanels

        _chooser.getSelectionModel().addChangeListener(this);
        _chooser.setPreviewPanel(new JPanel());
        this.add(_chooser);
        _previewPanel.add(_samplePanel, java.awt.BorderLayout.CENTER);
        this.add(_previewPanel);
        updateSamples();
    }
    
    private void doPopupUtility(String type, int which, 
            PositionableLabel sample, PositionablePopupUtil ut, boolean editText) {
        PositionablePopupUtil util = sample.getPopupUtility();
        util.setJustification(ut.getJustification());
        util.setHorizontalAlignment(ut.getJustification());
        util.setFixedWidth(ut.getFixedWidth());
        util.setFixedHeight(ut.getFixedHeight());
        util.setMargin(ut.getMargin());
        util.setBorderSize(ut.getBorderSize());
        util.setBorderColor(ut.getBorderColor());
        util.setFont(util.getFont().deriveFont(ut.getFontStyle()));
        util.setFontSize(ut.getFontSize());
        util.setOrientation(ut.getOrientation());
        sample.updateSize();
       
        _sample.put(type, sample);
        this.add(makeTextPanel(type, sample, which, editText));
        _samplePanel.add(sample);
        _samplePanel.add(Box.createHorizontalStrut(STRUT));
    }

    protected void makeFontPanels() {
        JPanel fontPanel = new JPanel();
        _fontSizeBox = new AJComboBox(FONTSIZE, SIZE);
        fontPanel.add(makeBoxPanel("fontSize", _fontSizeBox));
        int row = 4;
        for (int i = 0; i < FONTSIZE.length; i++) {
            if (_util.getFontSize() == Integer.parseInt(FONTSIZE[i])) {
                row = i;
                break;
            }
        }
        _fontSizeBox.setSelectedIndex(row);

        _fontStyleBox = new AJComboBox(STYLES, STYLE);
        fontPanel.add(makeBoxPanel("fontStyle", _fontStyleBox));
        _fontStyleBox.setSelectedIndex(_util.getFont().getStyle());

        _fontJustBox = new AJComboBox(JUSTIFICATION, JUST);
        fontPanel.add(makeBoxPanel("justification", _fontJustBox));
        switch (_util.getJustification()) {
            case PositionablePopupUtil.LEFT:
                row = 0;
                break;
            case PositionablePopupUtil.RIGHT:
                row = 2;
                break;
            case PositionablePopupUtil.CENTRE:
                row = 1;
                break;
            default:
                row = 2;
        }
        _fontJustBox.setSelectedIndex(row);
        this.add(fontPanel);

        JPanel sizePanel = new JPanel();
        SpinnerNumberModel model = new SpinnerNumberModel(_util.getBorderSize(), 0, 100, 1);
        _borderSpin = new AJSpinner(model, BORDER);
        sizePanel.add(makeSpinPanel("borderSize", _borderSpin));
        model = new SpinnerNumberModel(_util.getMargin(), 0, 100, 1);
        _marginSpin = new AJSpinner(model, MARGIN);
        sizePanel.add(makeSpinPanel("marginSize", _marginSpin));
        model = new SpinnerNumberModel(_util.getFixedWidth(), 0, 1000, 1);
        _widthSpin = new AJSpinner(model, FWIDTH);
        sizePanel.add(makeSpinPanel("fixedWidth", _widthSpin));
        model = new SpinnerNumberModel(_util.getFixedHeight(), 0, 1000, 1);
        _heightSpin = new AJSpinner(model, FHEIGHT);
        sizePanel.add(makeSpinPanel("fixedHeight", _heightSpin));
        this.add(sizePanel);

        JPanel colorPanel = new JPanel();
        colorPanel.add(makeButton(new AJRadioButton(Bundle.getMessage("borderColor"), BORDER_COLOR)));
        this.add(colorPanel);
    }

    private JPanel makeTextPanel(String caption, JLabel sample, int state, boolean addTextField) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage(caption)));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel p = new JPanel();
        if (addTextField) {
            JTextField textField = new JTextField(sample.getText(), 25);
            textField.addKeyListener(new KeyListener() {
                JLabel sample;

                KeyListener init(JLabel s) {
                    sample = s;
                    return this;
                }

                public void keyTyped(KeyEvent E) {
                }

                public void keyPressed(KeyEvent E) {
                }

                public void keyReleased(KeyEvent E) {
                    JTextField tmp = (JTextField) E.getSource();
                    sample.setText(tmp.getText());
                }
            }.init(sample));
            p.add(textField);            
        }
        panel.add(p);

        p = new JPanel();
        p.add(makeButton(new AJRadioButton(Bundle.getMessage("fontColor"), state)));
        p.add(makeButton(new AJRadioButton(Bundle.getMessage("backColor"), state + 10)));
        AJRadioButton button = new AJRadioButton(Bundle.getMessage("transparentBack"), state + 20);
        _buttonGroup.add(button);
        p.add(button);
        button.addActionListener(new ActionListener() {
            AJRadioButton button;

            public void actionPerformed(ActionEvent a) {
                if (button.isSelected()) {
                    switch (button.which) {
                        case TRANSPARENT_COLOR:
                            _sample.get("Text").setOpaque(false);
                            break;
                        case ACTIVE_TRANSPARENT_COLOR:
                            _sample.get("Active").setOpaque(false);
                            break;
                        case INACTIVE_TRANSPARENT_COLOR:
                            _sample.get("InActive").setOpaque(false);
                            break;
                        case UNKNOWN_TRANSPARENT_COLOR:
                            _sample.get("Unknown").setOpaque(false);
                            break;
                        case INCONSISTENT_TRANSPARENT_COLOR:
                            _sample.get("Inconsistent").setOpaque(false);
                            break;
                    }
                    updateSamples();
                }
            }

            ActionListener init(AJRadioButton b) {
                button = b;
                return this;
            }
        }.init(button));
        panel.add(p);

        return panel;
    }

    private AJRadioButton makeButton(AJRadioButton button) {
        button.addActionListener(new ActionListener() {
            AJRadioButton button;

            public void actionPerformed(ActionEvent a) {
                if (button.isSelected()) {
                    int prevButton = _selectedButton;
                    _selectedButton = button.which;
                    if (Math.abs(prevButton-_selectedButton)<5) {
                        changeColor();                        
                    }
                }
            }

            ActionListener init(AJRadioButton b) {
                button = b;
                return this;
            }
        }.init(button));
        _buttonGroup.add(button);            
        return button;
    }

    private void updateSamples() {
        if (_previewPanel==null) {
            return;            
        }
        
        int mar = _util.getMargin();
        int bor = _util.getBorderSize();
        Border outlineBorder;
        if (bor == 0) {
            outlineBorder = BorderFactory.createEmptyBorder(0, 0, 0, 0);
        } else {
            outlineBorder = new LineBorder(_util.getBorderColor(), bor);
        }
        Font font = _util.getFont();
        int just = _util.getJustification();
        
        Iterator<PositionableLabel> it = _sample.values().iterator();
        while (it.hasNext()) {
            PositionableLabel sam = it.next();
            PositionablePopupUtil util = sam.getPopupUtility();
            sam.setFont(font);
            util.setFixedWidth(_util.getFixedWidth());
            util.setFixedHeight(_util.getFixedHeight());
            Border borderMargin;
            if (sam.isOpaque()) {
                borderMargin = new LineBorder(sam.getBackground(), mar);
            } else {
                borderMargin = BorderFactory.createEmptyBorder(mar, mar, mar, mar);
            }
            sam.setBorder(new CompoundBorder(outlineBorder, borderMargin));
            
            switch (just) {
                case PositionablePopupUtil.LEFT:
                    sam.setHorizontalAlignment(JLabel.LEFT);
                    break;
                case PositionablePopupUtil.RIGHT:
                    sam.setHorizontalAlignment(JLabel.RIGHT);
                    break;
                default:
                    sam.setHorizontalAlignment(JLabel.CENTER);
            }
            sam.updateSize();
            sam.setPreferredSize(new Dimension(sam.maxWidth(), sam.maxHeight()));
        }
        if (_dialog!=null) {
            _dialog.pack();            
        }
    }

    public void stateChanged(ChangeEvent e) {
        Object obj = e.getSource();
        if (obj instanceof AJSpinner) {
            int num = ((Number) ((AJSpinner) obj).getValue()).intValue();
            switch (((AJSpinner) obj)._which) {
                case BORDER:
                    _util.setBorderSize(num);
                    break;
                case MARGIN:
                    _util.setMargin(num);
                    break;
                case FWIDTH:
                    _util.setFixedWidth(num);
                    break;
                case FHEIGHT:
                    _util.setFixedHeight(num);
                    break;
            }
        } else {
            changeColor();
        }
        updateSamples();
    }

    public PositionablePopupUtil getPositionablePopupUtil() {
        return _util;
    }

    public void getText(Positionable pos) {
        if (pos instanceof SensorIcon  && !((SensorIcon)pos).isIcon()) {
            SensorIcon icon = (SensorIcon) pos;
            PositionableLabel sample = _sample.get("Active");
            if (sample.isOpaque()) {
                icon.setBackgroundActive(sample.getBackground());                
            } else {
                icon.setBackgroundActive(null);                
            }
            icon.setTextActive(sample.getForeground());
            icon.setActiveText(sample.getText());

            sample = _sample.get("InActive");
            icon.setInactiveText(sample.getText());
            if (sample.isOpaque()) {
                icon.setBackgroundInActive(sample.getBackground());                
            } else {
                icon.setBackgroundInActive(null);                
            }
            icon.setTextInActive(sample.getForeground());

            sample = _sample.get("Unknown");
            icon.setUnknownText(sample.getText());
            if (sample.isOpaque()) {
                icon.setBackgroundUnknown(sample.getBackground());                
            } else {
                icon.setBackgroundUnknown(null);                
            }
            icon.setTextUnknown(sample.getForeground());

            sample = _sample.get("Inconsistent");
            icon.setInconsistentText(sample.getText());
            if (sample.isOpaque()) {
                icon.setBackgroundInconsistent(sample.getBackground());                
            } else {
                icon.setBackgroundInconsistent(null);                
            }
            icon.setTextInconsistent(sample.getForeground());
        } else {
            PositionableLabel sample = _sample.get("Text");
            pos.setForeground(sample.getForeground());
            if ( pos instanceof PositionableLabel &&
                !(pos instanceof jmri.jmrit.display.MemoryIcon)) {
                ((PositionableLabel) pos).setText(sample.getText());
            }
            if (sample.isOpaque()) {
                pos.setBackground(sample.getBackground());                
            } else {
                pos.setBackground(null);                
            }
            _util.setHasBackground(sample.isOpaque());
        }
    }
    
    private void changeColor() {
        switch (_selectedButton) {
            case TEXT_FONT:
                _sample.get("Text").setForeground(_chooser.getColor());
                _util.setForeground(_chooser.getColor());
                break;
            case ACTIVE_FONT:
                _sample.get("Active").setForeground(_chooser.getColor());
                break;
            case INACTIVE_FONT:
                _sample.get("InActive").setForeground(_chooser.getColor());
                break;
            case UNKOWN_FONT:
                _sample.get("Unknown").setForeground(_chooser.getColor());
                break;
            case INCONSISTENT_FONT:
                _sample.get("Inconsistent").setForeground(_chooser.getColor());
                break;
            case TEXT_BACKGROUND:
                _sample.get("Text").setBackground(_chooser.getColor());
                _sample.get("Text").setOpaque(true);
                _util.setBackgroundColor(_chooser.getColor());
                break;
            case ACTIVE_BACKGROUND:
                _sample.get("Active").setBackground(_chooser.getColor());
                _sample.get("Active").setOpaque(true);
                break;
            case INACTIVE_BACKGROUND:
                _sample.get("InActive").setBackground(_chooser.getColor());
                _sample.get("InActive").setOpaque(true);
                break;
            case UNKOWN_BACKGROUND:
                _sample.get("Unknown").setBackground(_chooser.getColor());
                _sample.get("Unknown").setOpaque(true);
                break;
            case INCONSISTENT_BACKGROUND:
                _sample.get("Inconsistent").setBackground(_chooser.getColor());
                _sample.get("Inconsistent").setOpaque(true);
                break;
            case BORDER_COLOR:
                _util.setBorderColor(_chooser.getColor());
                break;
        }
        
    }

    public void itemStateChanged(ItemEvent e) {
        Object obj = e.getSource();
        if (obj instanceof AJComboBox) {
            switch (((AJComboBox) obj)._which) {
                case SIZE:
                    String size = (String) ((AJComboBox) obj).getSelectedItem();
                    _util.setFontSize(Float.valueOf(size));
                    break;
                case STYLE:
                    int style = 0;
                    switch (((AJComboBox) obj).getSelectedIndex()) {
                        case 0:
                            style = Font.PLAIN;
                            break;
                        case 1:
                            style = Font.BOLD;
                            break;
                        case 2:
                            style = Font.ITALIC;
                            break;
                        case 3:
                            style = (Font.BOLD | Font.ITALIC);
                            break;
                    }
                    _util.setFontStyle(style);
                    break;
                case JUST:
                    int just = 0;
                    switch (((AJComboBox) obj).getSelectedIndex()) {
                        case 0:
                            just = PositionablePopupUtil.LEFT;
                            break;
                        case 1:
                            just = PositionablePopupUtil.CENTRE;
                            break;
                        case 2:
                            just = PositionablePopupUtil.RIGHT;
                            break;
                    }
                    _util.setJustification(just);
                    break;
            }
            updateSamples();
        }
    }
}
