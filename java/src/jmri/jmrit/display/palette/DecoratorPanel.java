package jmri.jmrit.display.palette;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import javax.annotation.Nonnull;
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
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.PositionableLabel;
import jmri.jmrit.display.PositionablePopupUtil;
import jmri.jmrit.display.SensorIcon;
import jmri.jmrit.display.palette.TextItemPanel.DragDecoratorLabel;
import jmri.util.swing.ImagePanel;
import jmri.util.swing.JmriColorChooser;

/**
 * Panel for positionables with text and/or colored margins and borders.
 * @see ItemPanel palette class diagram
 *
 * @author PeteCressman Copyright (C) 2009, 2015
 */
public class DecoratorPanel extends JPanel implements ChangeListener {

    private FontPanel _fontPanel;

    public static final int STRUT = 6;

    public static final int BORDER = 1;
    public static final int MARGIN = 2;
    public static final int FWIDTH = 3;
    public static final int FHEIGHT = 4;

    static final int FOREGROUND_BUTTON = 1;
    static final int BACKGROUND_BUTTON = 2;
    static final int TRANSPARENT_BUTTON = 3;
    static final int BORDERCOLOR_BUTTON = 4;

    private AJSpinner _borderSpin;
    private AJSpinner _marginSpin;
    private AJSpinner _widthSpin;
    private AJSpinner _heightSpin;

    private JColorChooser _chooser;
    ImagePanel _previewPanel;
    private final JPanel _samplePanel;
    private PositionablePopupUtil _util;
    private HashMap<String, PositionableLabel> _samples = null;    
    private int _selectedButton;
    private String _selectedState;
    private boolean _isPositionableLabel;
    private final ButtonGroup _buttonGroup = new ButtonGroup();
    private AJRadioButton _fontButton;
    private AJRadioButton _borderButton;
    private AJRadioButton _backgroundButton;

    protected BufferedImage[] _backgrounds; // array of Image backgrounds
    protected JComboBox<String> _bgColorBox;

    Editor _editor;
    protected DisplayFrame _paletteFrame;

    public DecoratorPanel(Editor editor, DisplayFrame paletteFrame) {
        _editor = editor;
        _paletteFrame = paletteFrame;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        Color panelBackground = _editor.getTargetPanel().getBackground(); // start using Panel background color
        // create array of backgrounds, _currentBackground already set and used
        _backgrounds = ItemPanel.makeBackgrounds(null,  panelBackground);
        _chooser = new JColorChooser(panelBackground);
        _samples = new HashMap<>();

        _previewPanel = new ImagePanel();
        _previewPanel.setLayout(new BorderLayout());
        _previewPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black, 1),
                Bundle.getMessage("PreviewBorderTitle")));
        _previewPanel.add(Box.createVerticalStrut(STRUT), BorderLayout.PAGE_START);
        _previewPanel.add(Box.createVerticalStrut(STRUT), BorderLayout.PAGE_END);

        _samplePanel = new JPanel();
        _samplePanel.add(Box.createHorizontalStrut(STRUT));
        _samplePanel.setOpaque(false);
    }

    static class AJSpinner extends JSpinner {
        int _which;

        AJSpinner(SpinnerModel model, int which) {
            super(model);
            _which = which;
        }
    }

    static class AJRadioButton extends JRadioButton {
        int _which;
        String _state;

        AJRadioButton(String text, int which, String state) {
            super(text);
            _which = which;
            _state = state;
        }
        
        String getState() {
            return _state;
        }
    }

    public static JPanel makeSpinPanel(String caption, JSpinner spin, ChangeListener listener) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel(Bundle.getMessage(caption)));
        spin.addChangeListener(listener);
        panel.add(spin);
        return panel;
    }

    /* Called by Palette's TextItemPanel i.e. make a new panel item to drag */
    protected void initDecoratorPanel(DragDecoratorLabel sample) {
        sample.setDisplayLevel(Editor.LABELS);
        sample.setBackground(_editor.getTargetPanel().getBackground());
        _util = sample.getPopupUtility();
        _samples.put("Text", sample);
        _selectedState = "Text";
        _isPositionableLabel = true;
        makeFontPanels();
        this.add(makeTextPanel("Text", sample, true));
        _samplePanel.add(sample);
        log.debug("DragDecoratorLabel size {} | panel size {}", sample.getPreferredSize(), _samplePanel.getPreferredSize());
        finishInit(true);
    }

    /* Called by Editor's TextAttrDialog - i.e. update a panel item from menu */
    public void initDecoratorPanel(Positionable pos) {
        Positionable item = pos.deepClone(); // need copy of PositionableJPanel in PopupUtility
        _util = item.getPopupUtility();
        item.remove();      // don't need copy any more. Removes ghost image of PositionableJPanels
        _isPositionableLabel = (pos instanceof PositionableLabel);
        makeFontPanels();

        if (pos instanceof SensorIcon && !((SensorIcon)pos).isIcon()) {
            SensorIcon si = (SensorIcon) pos;
            if (!si.isIcon() && si.isText()) {
                PositionableLabel sample = new PositionableLabel(si.getActiveText(), _editor);
                sample.setForeground(si.getTextActive());
                sample.setBackground(si.getBackgroundActive());
                doPopupUtility("Active", sample, true); // NOI18N

                sample = new PositionableLabel(si.getInactiveText(), _editor);
                sample.setForeground(si.getTextInActive());
                sample.setBackground(si.getBackgroundInActive());
                doPopupUtility("InActive", sample, true); // NOI18N

                sample = new PositionableLabel(si.getUnknownText(), _editor);
                sample.setForeground(si.getTextUnknown());
                sample.setBackground(si.getBackgroundUnknown());
                doPopupUtility("Unknown", sample, true); // NOI18N

                sample = new PositionableLabel(si.getInconsistentText(), _editor);
                sample.setForeground(si.getTextInconsistent());
                sample.setBackground(si.getBackgroundInconsistent());
                doPopupUtility("Inconsistent", sample, true); // NOI18N
            }   // else a non-text SensorIcon cannot be decorated.
        } else { // not a SensorIcon
            PositionableLabel sample = new PositionableLabel("", _editor);
            sample.setForeground(_util.getForeground());
            sample.setBackground(_util.getBackground());
            PositionablePopupUtil util = sample.getPopupUtility();
            util.setHasBackground(_util.hasBackground());
            boolean addtextField;
            if (pos instanceof PositionableLabel) {
                sample.setText(((PositionableLabel)pos).getUnRotatedText());
                if (pos instanceof jmri.jmrit.display.MemoryIcon) {
                    addtextField = false;
                } else {
                    addtextField = true;
                }
            } else {
                // To display PositionableJPanel types as PositionableLabels, set fixed sizes.
                util.setFixedWidth(pos.getWidth() - 2*_util.getBorderSize());
                util.setFixedHeight(pos.getHeight() - 2*_util.getBorderSize());
                if (pos instanceof jmri.jmrit.display.MemoryInputIcon) {
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
                    log.error("Unknown Postionable Type {}", pos.getClass().getName());
                }
            }
            doPopupUtility("Text", sample, addtextField);
            if (log.isDebugEnabled()) {
                log.debug("util width= {} height= {} POS width= {} height= {}",
                        util.getFixedWidth(), util.getFixedHeight(), pos.getWidth(), pos.getHeight());
            }
        }
        finishInit(false);
    }

    protected void fontChange() {
        _selectedButton = FOREGROUND_BUTTON;
        _fontButton.setSelected(true);
        _chooser.setColor(_util.getForeground());
        log.debug("fontChange");
        updateSamples();
     }

    private void finishInit(boolean addBgCombo) {
        _chooser = JmriColorChooser.extendColorChooser(_chooser);
        setSuppressRecentColor(true);
        _chooser.getSelectionModel().addChangeListener(this);
        _chooser.setPreviewPanel(new JPanel());
        add(_chooser);
        _previewPanel.add(_samplePanel, BorderLayout.CENTER);

        // add a SetBackground combo
        if (addBgCombo) {
            add(add(makeBgButtonPanel(_previewPanel, null, _backgrounds))); // no listener on this variant
        }
        add(_previewPanel);
        _previewPanel.setImage(_backgrounds[0]);
        _previewPanel.revalidate();        // force redraw
        // after everything created, set selections
        _fontPanel.setFontSelections();
        updateSamples();
    }

    private void doPopupUtility(String type, PositionableLabel sample, boolean editText) {
        PositionablePopupUtil util = sample.getPopupUtility();
        util.setJustification(_util.getJustification());
        util.setHorizontalAlignment(_util.getJustification());
        int size = _util.getFixedWidth();
        if (_isPositionableLabel) {
            util.setFixedWidth(size);
        }
        size = _util.getFixedHeight();
        if (_isPositionableLabel) {
            util.setFixedHeight(size);
        }
        util.setMargin(_util.getMargin());
        util.setBorderSize(_util.getBorderSize());
        util.setBorderColor(_util.getBorderColor());
        util.setFont(util.getFont().deriveFont(_util.getFontStyle()));
        util.setFontSize(_util.getFontSize());
        util.setFontStyle(_util.getFontStyle());
        util.setOrientation(_util.getOrientation());
        boolean back = (sample.getBackground() != null);
        util.setHasBackground(back);
        sample.setOpaque(back);
        sample.updateSize();

        _samples.put(type, sample);
        _selectedState = type;
        this.add(makeTextPanel(type, sample, editText));
        _samplePanel.add(sample);
        _samplePanel.add(Box.createHorizontalStrut(STRUT));
    }

    private void makeFontPanels() {
        ActionListener fontAction = ((ActionEvent event) -> {
            fontChange(); // callback
        });
        _fontPanel = new FontPanel(_util, fontAction);
        add(_fontPanel);

        JPanel sizePanel = new JPanel();
        SpinnerNumberModel model = new SpinnerNumberModel(_util.getBorderSize(), 0, 100, 1);
        _borderSpin = new AJSpinner(model, BORDER);
        sizePanel.add(makeSpinPanel("borderSize", _borderSpin, this));
        model = new SpinnerNumberModel(_util.getMargin(), 0, 100, 1);
        _marginSpin = new AJSpinner(model, MARGIN);
        sizePanel.add(makeSpinPanel("marginSize", _marginSpin, this));
        if (_isPositionableLabel) {
            model = new SpinnerNumberModel(_util.getFixedWidth(), 0, 1000, 1);
            _widthSpin = new AJSpinner(model, FWIDTH);
            sizePanel.add(makeSpinPanel("fixedWidth", _widthSpin, this));
            model = new SpinnerNumberModel(_util.getFixedHeight(), 0, 1000, 1);
            _heightSpin = new AJSpinner(model, FHEIGHT);
            sizePanel.add(makeSpinPanel("fixedHeight", _heightSpin, this));
        }
        this.add(sizePanel);
    }

    String bundleCaption = null;

    private JPanel makeTextPanel(String state, JLabel sample, boolean addTextField) {
        JPanel panel = new JPanel();
        // use NamedBeanBundle property for basic beans like "Turnout" I18N
        if ("Active".equals(state)) {
            bundleCaption = "SensorStateActive";
        } else if ("InActive".equals(state)) {
            bundleCaption = "SensorStateInactive";
        } else if ("Unknown".equals(state)) {
            bundleCaption = "BeanStateUnknown";
        } else if ("Inconsistent".equals(state)) {
            bundleCaption = "BeanStateInconsistent";
        } else {
            bundleCaption = state;
        }
        panel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage(bundleCaption)));
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

                @Override
                public void keyTyped(KeyEvent evt) {
                }

                @Override
                public void keyPressed(KeyEvent evt) {
                }

                @Override
                public void keyReleased(KeyEvent evt) {
                    JTextField tmp = (JTextField) evt.getSource();
                    sample.setText(tmp.getText());
                    updateSamples();
                }
            }.init(sample));
            p.add(textField);
        }
        panel.add(p);

        p = new JPanel();
        _fontButton = makeColorRadioButton("FontColor", FOREGROUND_BUTTON, state);
        p.add(_fontButton);
        
        _backgroundButton = makeColorRadioButton("FontBackgroundColor", BACKGROUND_BUTTON, state);
        p.add(_backgroundButton);
        
        AJRadioButton button = makeColorRadioButton("transparentBack", TRANSPARENT_BUTTON, state);
        p.add(button);

        _borderButton = makeColorRadioButton("borderColor", BORDERCOLOR_BUTTON, state);
        p.add(_borderButton);

        panel.add(p);
        return panel;
    }

    private AJRadioButton makeColorRadioButton(String caption, int which, String state) {
        AJRadioButton button = new AJRadioButton(Bundle.getMessage(caption), which, state);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                if (button.isSelected()) {
                    _selectedButton =button._which;
                    _selectedState = button._state;
                    PositionableLabel pos =_samples.get(_selectedState);
                    PositionablePopupUtil util = pos.getPopupUtility();
                    switch (button._which) {
                        case FOREGROUND_BUTTON:
                            _chooser.setColor(util.getForeground());
                            break;
                        case BACKGROUND_BUTTON:
                            if (util.hasBackground()) {
                                _chooser.setColor(util.getBackground());
                            }
                            util.setHasBackground(true);
                            pos.setOpaque(true);
                            break;
                        case BORDERCOLOR_BUTTON:
                            _chooser.setColor(util.getBorderColor());
                            break;
                        case TRANSPARENT_BUTTON:
                            util.setHasBackground(false);
                            _util.setHasBackground(false);
                            pos.setOpaque(false);
                            break;
                        default:    // TRANSPARENT_BUTTON
                   }
                    log.debug("Button actionPerformed Colors opaque= {} _state= {} _which= {}",
                            pos.isOpaque(), button._state, button._which);
                    updateSamples();
                }
            }
        });
        _buttonGroup.add(button);            
        return button;
    }

    protected void updateSamples() {
        if (_previewPanel == null) {
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

        Iterator<PositionableLabel> it = _samples.values().iterator();
        while (it.hasNext()) {
            PositionableLabel sam = it.next();
            PositionablePopupUtil util = sam.getPopupUtility();
            sam.setFont(font);
            if (_isPositionableLabel) {
                util.setFixedWidth(_util.getFixedWidth());
                util.setFixedHeight(_util.getFixedHeight());
            } else {
                util.setFixedWidth(util.getFixedWidth() + 2*(mar - util.getMargin()) /*+ bor - util.getBorderSize()*/);
                util.setFixedHeight(util.getFixedHeight() + 2*(mar - util.getMargin()) /*+ bor - util.getBorderSize()*/);
            }
            util.setMargin(mar);
            util.setBorderSize(bor);
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
            sam.setPreferredSize(sam.getSize());
            sam.invalidate();
            if (log.isDebugEnabled()) {
//                log.debug("updateSamples opaque= {} background= {}", sam.isOpaque(), util.getBackground());
                log.debug("util width= {} height= {} SAM width= {} height= {}",
                        util.getFixedWidth(), util.getFixedHeight(), sam.getWidth(), sam.getHeight());
            }
        }
        _samplePanel.repaint();
    }

    /**
     * Create panel element containing [Set background:] drop down list.
     * Special version for Decorator, no access to shared variable previewBgSet.
     * @see ItemPanel
     *
     * @param preview1 ImagePanel containing icon set
     * @param preview2 not used, matches method in ItemPanel
     * @param imgArray array of colored background images
     * @return a JPanel with label and drop down
     */
    private JPanel makeBgButtonPanel(@Nonnull ImagePanel preview1, ImagePanel preview2, BufferedImage[] imgArray) {
        _bgColorBox = new JComboBox<>();
        _bgColorBox.addItem(Bundle.getMessage("PanelBgColor")); // PanelColor key is specific for CPE, too long for combo
        _bgColorBox.addItem(Bundle.getMessage("White"));
        _bgColorBox.addItem(Bundle.getMessage("LightGray"));
        _bgColorBox.addItem(Bundle.getMessage("DarkGray"));
        _bgColorBox.addItem(Bundle.getMessage("Checkers"));
        int index;
        if (_paletteFrame != null) {
            index = _paletteFrame.getPreviewBg();
        } else {
            index = 0;
        }
        _bgColorBox.setSelectedIndex(index);
        _bgColorBox.addActionListener((ActionEvent e) -> {
            if (imgArray != null) {
                // index may repeat
                int previewBgSet = _bgColorBox.getSelectedIndex(); // store user choice
                if (_paletteFrame != null) {
                    _paletteFrame.setPreviewBg(previewBgSet);
                }
                // load background image
                log.debug("Palette Decorator setImage called {}", previewBgSet);
                preview1.setImage(imgArray[previewBgSet]);
                // preview.setOpaque(false); // needed?
                preview1.revalidate();        // force redraw
            } else {
                log.debug("imgArray is empty");
            }
        });
        JPanel backgroundPanel = new JPanel();
        backgroundPanel.setLayout(new BoxLayout(backgroundPanel, BoxLayout.Y_AXIS));
        JPanel pp = new JPanel();
        pp.setLayout(new FlowLayout(FlowLayout.CENTER));
        pp.add(new JLabel(Bundle.getMessage("setBackground")));
        pp.add(_bgColorBox);
        backgroundPanel.add(pp);
        backgroundPanel.setMaximumSize(backgroundPanel.getPreferredSize());
        return backgroundPanel;
    }

    // called when editor changed
    protected BufferedImage[] getBackgrounds() {
        return _backgrounds;
    }
    // called when editor changed
    protected void setBackgrounds(BufferedImage[] imgArray) {
        _backgrounds = imgArray;
        _previewPanel.setImage(imgArray[0]);
        _previewPanel.revalidate();        // force redraw
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        Object obj = e.getSource();
        if (obj instanceof AJSpinner) {
            int num = ((Number) ((AJSpinner) obj).getValue()).intValue();
            switch (((AJSpinner) obj)._which) {
                case BORDER:
                    _util.setBorderSize(num);
                    _borderButton.setSelected(true);
                    _selectedButton = BORDERCOLOR_BUTTON;
                    _chooser.setColor(_util.getBorderColor());
                    break;
                case MARGIN:
                    _util.setMargin(num);
                    _selectedButton = BACKGROUND_BUTTON;
                    Color c = _util.getBackground();
                    if (c != null) {
                        _chooser.setColor(c);
                    }
                    break;
                case FWIDTH:
                    _util.setFixedWidth(num);
                    _selectedButton = BACKGROUND_BUTTON;
                    _backgroundButton.setSelected(true);
                    c = _util.getBackground();
                    if (c != null) {
                        _chooser.setColor(c);
                    }
                    break;
                case FHEIGHT:
                    _util.setFixedHeight(num);
                    _selectedButton = BACKGROUND_BUTTON;
                    _backgroundButton.setSelected(true);
                    c = _util.getBackground();
                    if (c != null) {
                        _chooser.setColor(c);
                    }
                    break;
                default:
                    log.warn("Unexpected _which {}  in stateChanged", ((AJSpinner) obj)._which);
                    break;
            }
            log.debug("stateChanged sizes which= {} _selectedState= {} _selectedButton= {}",
                    ((AJSpinner)obj)._which, _selectedState, _selectedButton);
            updateSamples();
        } else {
            log.debug("stateChanged colors _selectedState= {} _selectedButton= {}",
                    _selectedState, _selectedButton);
            colorChange();
        }
    }
    
    private void colorChange() {
        PositionableLabel pos =_samples.get(_selectedState);
        PositionablePopupUtil util = pos.getPopupUtility();
        switch (_selectedButton) {
            case FOREGROUND_BUTTON:
                if ("Text".equals(_selectedState)) {
                    _util.setForeground(_chooser.getColor());
                }
                util.setForeground(_chooser.getColor());
                break;
            case BACKGROUND_BUTTON:
                if ("Text".equals(_selectedState)) {
                    _util.setBackgroundColor(_chooser.getColor());
                }
                util.setBackgroundColor(_chooser.getColor());
                pos.setOpaque(true);
                break;
            case TRANSPARENT_BUTTON:
                if ("Text".equals(_selectedState)) {
                    _util.setBackgroundColor(null);
                }
                util.setBackgroundColor(null);
                pos.setOpaque(false);
                break;
            case BORDERCOLOR_BUTTON:
                _util.setBorderColor(_chooser.getColor());
                break;
            default:
                log.warn("Unexpected color change for state {}, button# {}", _selectedState, _selectedButton);
                break;
        }
        log.debug("colorChange Colors opaque= {} _selectedState= {} _selectedButton= {}",
                pos.isOpaque(), _selectedState, _selectedButton);
        updateSamples();
    }

    public PositionablePopupUtil getPositionablePopupUtil() {
        return _util;
    }

    public void setAttributes(Positionable pos) {
        if (pos instanceof SensorIcon  && !((SensorIcon)pos).isIcon()) {
            SensorIcon icon = (SensorIcon) pos;
            PositionableLabel sample = _samples.get("Active");
            if (sample.isOpaque()) {
                icon.setBackgroundActive(sample.getBackground());
            } else {
                icon.setBackgroundActive(null);
            }
            icon.setTextActive(sample.getForeground());
            icon.setActiveText(sample.getText());

            sample = _samples.get("InActive");
            icon.setInactiveText(sample.getText());
            if (sample.isOpaque()) {
                icon.setBackgroundInActive(sample.getBackground());
            } else {
                icon.setBackgroundInActive(null);
            }
            icon.setTextInActive(sample.getForeground());

            sample = _samples.get("Unknown");
            icon.setUnknownText(sample.getText());
            if (sample.isOpaque()) {
                icon.setBackgroundUnknown(sample.getBackground());
            } else {
                icon.setBackgroundUnknown(null);
            }
            icon.setTextUnknown(sample.getForeground());

            sample = _samples.get("Inconsistent");
            icon.setInconsistentText(sample.getText());
            if (sample.isOpaque()) {
                icon.setBackgroundInconsistent(sample.getBackground());
            } else {
                icon.setBackgroundInconsistent(null);
            }
            icon.setTextInconsistent(sample.getForeground());
        } else {
            PositionableLabel sample = _samples.get("Text");
            pos.setForeground(sample.getForeground());
            if ( pos instanceof PositionableLabel &&
                !(pos instanceof jmri.jmrit.display.MemoryIcon)) {
                ((PositionableLabel) pos).setText(sample.getText());
            }
            PositionablePopupUtil util = pos.getPopupUtility();
            if (sample.isOpaque()) {
                util.setBackgroundColor(sample.getBackground());
            } else {
                util.setBackgroundColor(null);
            }
            util.setHasBackground(_util.hasBackground());
            util.setFont(_util.getFont());
            util.setFixedWidth(_util.getFixedWidth());
            util.setFixedHeight(_util.getFixedHeight());
            util.setMargin(_util.getMargin());
            util.setBorderSize(_util.getBorderSize());
            if (log.isDebugEnabled()) {
                log.debug("setAttributes(pos) opaque= {} hasBackground= {} background= {}",
                        pos.isOpaque(), util.hasBackground(), util.getBackground());
                PositionablePopupUtil u = pos.getPopupUtility();
                log.debug("setAttributes text sample opaque= {} hasBackground= {} background= {}",
                        sample.isOpaque(), u.hasBackground(), u.getBackground());
            }
        }
        pos.invalidate();
    }
    
    public void setSuppressRecentColor(boolean bool) {
        Iterator<PositionableLabel> iter = _samples.values().iterator();
        while (iter.hasNext()) {
            iter.next().getPopupUtility().setSuppressRecentColor(bool);
        }
        _util.setSuppressRecentColor(bool);        
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DecoratorPanel.class);
}
