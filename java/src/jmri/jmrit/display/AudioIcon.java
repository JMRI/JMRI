package jmri.jmrit.display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import jmri.*;
import jmri.jmrit.audio.AudioSource;
import jmri.jmrit.catalog.NamedIcon;
import jmri.util.swing.JmriMouseEvent;

/**
 * An icon that plays an audio on a web panel.
 *
 * @author Daniel Bergqvist (C) 2023
 */
public class AudioIcon extends PositionableLabel {

    public static final String PROPERTY_COMMAND = "Command";
    public static final String PROPERTY_COMMAND_PLAY = "Play";
    public static final String PROPERTY_COMMAND_STOP = "Stop";

    public static final IdentityManager IDENTITY_MANAGER = new IdentityManager();

    private final int _identity;
    private NamedIcon _originalIcon = new NamedIcon("resources/icons/audio_icon.gif", "resources/icons/audio_icon.gif");
    private String _originalText = Bundle.getMessage("AudioIcon_Text");
    private OnClickOperation _onClickOperation = OnClickOperation.DoNothing;
    private boolean _playSoundWhenJmriPlays = true;
    private boolean _stopSoundWhenJmriStops = false;

    // the associated Audio object
    private NamedBeanHandle<Audio> _namedAudio;

    public AudioIcon(String s, @Nonnull Editor editor) {
        super(s, editor);
        _identity = IDENTITY_MANAGER.getIdentity(this);
        _originalText = s;
    }

    public AudioIcon(int identity, String s, @Nonnull Editor editor) {
        super(s, editor);
        _identity = IDENTITY_MANAGER.getIdentity(identity, this);
        _originalText = s;
    }

    public AudioIcon(@CheckForNull NamedIcon s, @Nonnull Editor editor) {
        super(s, editor);
        _identity = IDENTITY_MANAGER.getIdentity(this);
        _originalIcon = _namedIcon;

        // Please retain the line below. It's used to create the resources/icons/audio_icon.gif icon
        // createAudioIconImage();
    }

    public AudioIcon(int identity, @CheckForNull NamedIcon s, @Nonnull Editor editor) {
        super(s, editor);
        _identity = IDENTITY_MANAGER.getIdentity(identity, this);
        _originalIcon = _namedIcon;

        // Please retain the line below. It's used to create the resources/icons/audio_icon.gif icon
        // createAudioIconImage();
    }

    @Override
    public Positionable deepClone() {
        AudioIcon pos = new AudioIcon(getText(), _editor);
        pos._originalIcon = new NamedIcon(_originalIcon);
        pos._originalText = _originalText;
        pos.setAudio(getNamedAudio().getName());
        pos._onClickOperation = _onClickOperation;
        pos._playSoundWhenJmriPlays = _playSoundWhenJmriPlays;
        pos._stopSoundWhenJmriStops = _stopSoundWhenJmriStops;

        return super.finishClone(pos);
    }

    @Override
    @Nonnull
    public String getTypeString() {
        return Bundle.getMessage("PositionableType_AudioIcon");
    }

    @Override
    @Nonnull
    public String getNameString() {
        String name;
        if (_namedAudio == null) {
            name = Bundle.getMessage("NotConnected");
        } else {
            name = _namedAudio.getBean().getDisplayName(
                    NamedBean.DisplayOptions.USERNAME_SYSTEMNAME);
        }
        return name;
    }

    public int getIdentity() {
        return _identity;
    }

    /**
     * Attached a named audio to this display item
     *
     * @param pName System/user name to lookup the audio object
     */
    public void setAudio(String pName) {
        if (InstanceManager.getNullableDefault(jmri.AudioManager.class) != null) {
            try {
                Audio audio = InstanceManager.getDefault(AudioManager.class).provideAudio(pName);
                setAudio(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, audio));
            } catch (AudioException | IllegalArgumentException ex) {
                log.error("Audio '{}' not available, icon won't see changes", pName);
            }
        } else {
            log.error("No AudioManager for this protocol, icon won't see changes");
        }
    }

    /**
     * Attached a named audio to this display item
     *
     * @param s the Audio
     */
    public void setAudio(NamedBeanHandle<Audio> s) {
        _namedAudio = s;
        if (_namedAudio != null) {
            setName(_namedAudio.getName());  // Swing name for e.g. tests
        }
    }

    public Audio getAudio() {
        if (_namedAudio == null) {
            return null;
        }
        return _namedAudio.getBean();
    }

    @Override
    public jmri.NamedBean getNamedBean() {
        return getAudio();
    }

    public NamedBeanHandle<Audio> getNamedAudio() {
        return _namedAudio;
    }

    public void setOnClickOperation(OnClickOperation operation) {
        _onClickOperation = operation;
    }

    public OnClickOperation getOnClickOperation() {
        return _onClickOperation;
    }

    public void setPlaySoundWhenJmriPlays(boolean value) {
        _playSoundWhenJmriPlays = value;
    }

    public boolean getPlaySoundWhenJmriPlays() {
        return _playSoundWhenJmriPlays;
    }

    public void setStopSoundWhenJmriStops(boolean value) {
        _stopSoundWhenJmriStops = value;
    }

    public boolean getStopSoundWhenJmriStops() {
        return _stopSoundWhenJmriStops;
    }

    public void play() {
        log.debug("AudioIcon.play()");
        firePropertyChange(PROPERTY_COMMAND, null, PROPERTY_COMMAND_PLAY);
    }

    public void stop() {
        log.debug("AudioIcon.stop()");
        firePropertyChange(PROPERTY_COMMAND, null, PROPERTY_COMMAND_STOP);
    }

    @Override
    protected void edit() {
        makeIconEditorFrame(this, "Audio", true, null);
        _iconEditor.setPickList(jmri.jmrit.picker.PickListModel.audioPickModelInstance());
        _iconEditor.setIcon(0, "plainIcon", _namedIcon);
        _iconEditor.makeIconPanel(false);

        // set default icons, then override with this turnout's icons
        ActionListener addIconAction = (ActionEvent a) -> updateAudio();
        _iconEditor.complete(addIconAction, true, true, true);
        _iconEditor.setSelection(getAudio());
    }

    void updateAudio() {
        setAudio(_iconEditor.getTableSelection().getDisplayName());
        var iconMap = _iconEditor.getIconMap();
        NamedIcon newIcon = iconMap.get("plainIcon");
        setIcon(newIcon);
        _iconEditorFrame.dispose();
        _iconEditorFrame = null;
        _iconEditor = null;
        invalidate();
    }

    @Override
    protected void editIcon() {
        super.editIcon();
        // If the icon is changed, we must remember that in case the user
        // switches between icon -> text -> icon
        _originalIcon = _namedIcon;
    }

    @Override
    public void doMousePressed(JmriMouseEvent e) {
        log.debug("doMousePressed");
        if (!e.isMetaDown() && !e.isAltDown()) {
            if (_onClickOperation != OnClickOperation.DoNothing && _namedAudio != null) {
                Audio audio = _namedAudio.getBean();
                if (audio.getSubType() == Audio.SOURCE && (audio instanceof AudioSource)) {
                    AudioSource source = (AudioSource)audio;
                    if (source.getState() == Audio.STATE_PLAYING) {
                        source.stop();
                    } else {
                        source.play();
                    }
                }
            }
        }
        super.doMousePressed(e);
    }

    private void changeAudioIconType() {
        _unRotatedText = null;
        if (isIcon()) {
            _icon = false;
            _text = true;
            setText(_originalText);
            setIcon(null);
            setOpaque(true);
        } else if (isText()) {
            _icon = true;
            if (getText() != null) {
                _originalText = getText();
            }
            _text = false;
            setText(null);
            setUnRotatedText(null);
            setOpaque(false);
            setIcon(_originalIcon);
        }
        int deg = getDegrees();
        rotate(deg);
    }

    /**
     * Pop-up just displays the audio name.
     *
     * @param popup the menu to display
     * @return always true
     */
    @Override
    public boolean showPopUp(JPopupMenu popup) {
        if (isEditable()) {
            if (isIcon()) {
                popup.add(new AbstractAction(Bundle.getMessage("ChangeToText")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        changeAudioIconType();
                    }
                });
            } else {
                popup.add(new AbstractAction(Bundle.getMessage("ChangeToIcon")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        changeAudioIconType();
                    }
                });
            }

            JMenu menu = new JMenu(Bundle.getMessage("AudioIcon_WebPanelMenu"));
            ButtonGroup buttonGroup = new ButtonGroup();

            JRadioButtonMenuItem rbMenuItem = new JRadioButtonMenuItem(Bundle.getMessage("AudioIcon_WebPanelMenu_OnClickPlaySoundGlobally"));
            rbMenuItem.addActionListener((ActionEvent event) -> {
                _onClickOperation = OnClickOperation.PlaySoundGlobally;
            });
            rbMenuItem.setSelected(_onClickOperation == OnClickOperation.PlaySoundGlobally);
            menu.add(rbMenuItem);
            buttonGroup.add(rbMenuItem);

            rbMenuItem = new JRadioButtonMenuItem(Bundle.getMessage("AudioIcon_WebPanelMenu_OnClickPlaySoundLocally"));
            rbMenuItem.addActionListener((ActionEvent event) -> {
                _onClickOperation = OnClickOperation.PlaySoundLocally;
            });
            rbMenuItem.setSelected(_onClickOperation == OnClickOperation.PlaySoundLocally);
            menu.add(rbMenuItem);
            buttonGroup.add(rbMenuItem);

            rbMenuItem = new JRadioButtonMenuItem(Bundle.getMessage("AudioIcon_WebPanelMenu_OnClickDoNothing"));
            rbMenuItem.addActionListener((ActionEvent event) -> {
                _onClickOperation = OnClickOperation.DoNothing;
            });
            rbMenuItem.setSelected(_onClickOperation == OnClickOperation.DoNothing);
            menu.add(rbMenuItem);
            buttonGroup.add(rbMenuItem);

            JCheckBoxMenuItem cbMenuItem2 = new JCheckBoxMenuItem(Bundle.getMessage("AudioIcon_WebPanelMenu_PlaySoundWhenJmriPlays"));
            cbMenuItem2.addActionListener((ActionEvent event) -> {
                _playSoundWhenJmriPlays = cbMenuItem2.isSelected();
            });
            cbMenuItem2.setSelected(_playSoundWhenJmriPlays);
            menu.add(cbMenuItem2);

            JCheckBoxMenuItem cbMenuItem3 = new JCheckBoxMenuItem(Bundle.getMessage("AudioIcon_WebPanelMenu_StopSoundWhenJmriStops"));
            cbMenuItem3.addActionListener((ActionEvent event) -> {
                _stopSoundWhenJmriStops = cbMenuItem3.isSelected();
            });
            cbMenuItem3.setSelected(_stopSoundWhenJmriStops);
            menu.add(cbMenuItem3);

            popup.add(menu);
        }
        return true;
    }


/*
    // Please retain this commented method. It's used to create the resources/icons/logixng/logixng_icon.gif icon

    private void createAudioIconImage() {

        try {
            int width = 69, height = 39;

            // TYPE_INT_ARGB specifies the image format: 8-bit RGBA packed into integer pixels
            java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);

            java.awt.Graphics2D ig2 = bi.createGraphics();

            ig2.setColor(java.awt.Color.WHITE);
            ig2.fillRect(0, 0, width-1, height-1);
            ig2.setColor(java.awt.Color.BLACK);
            ig2.drawRect(0, 0, width-1, height-1);

            java.awt.Font font = new java.awt.Font("Verdana", java.awt.Font.BOLD, 15);
            ig2.setFont(font);
            ig2.setPaint(java.awt.Color.black);

            // Draw string twice to get more bold
            ig2.drawString("Audio", 11, 24);
            ig2.drawString("Audio", 12, 24);

            javax.imageio.ImageIO.write(bi, "gif", new java.io.File(jmri.util.FileUtil.getExternalFilename("resources/icons/audio_icon.gif")));
        } catch (java.io.IOException ie) {
            throw new RuntimeException(ie);
        }
    }
*/


    public enum OnClickOperation {
        PlaySoundGlobally(Bundle.getMessage("AudioIcon_WebPanelMenu_OnClickPlaySoundGlobally")),
        PlaySoundLocally(Bundle.getMessage("AudioIcon_WebPanelMenu_OnClickPlaySoundLocally")),
        DoNothing(Bundle.getMessage("AudioIcon_WebPanelMenu_OnClickDoNothing"));

        private final String _text;

        private OnClickOperation(String text) {
            this._text = text;
        }

        @Override
        public String toString() {
            return _text;
        }

    }


    public static class IdentityManager {

        Map<Integer, AudioIcon> _identities = new HashMap<>();
        int _lastIdentity = -1;

        private IdentityManager() {
            // Private constructor to keep it as a singleton
        }

        public int getIdentity(AudioIcon audioIcon) {
            _lastIdentity++;
            _identities.put(_lastIdentity, audioIcon);
            return _lastIdentity;
        }

        public int getIdentity(int identity, AudioIcon audioIcon) {
            if (_identities.containsKey(identity)) {
                log.error("Identity {} already exists", identity);
                return getIdentity(audioIcon);
            }
            _identities.put(identity, audioIcon);
            if (identity > _lastIdentity) {
                _lastIdentity = identity;
            }
            return identity;
        }

        public AudioIcon getAudioIcon(int identity) {
            return _identities.get(identity);
        }

    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AudioIcon.class);
}
