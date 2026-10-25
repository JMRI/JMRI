package jmri.jmrix.dccpp.swing.messages;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Predicate;

import javax.annotation.CheckForNull;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;

import jmri.InvokeOnGuiThread;
import jmri.jmrit.Sound;
import jmri.jmrit.display.Positionable;
import jmri.jmrix.dccpp.*;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;

/** Panel showing EXRAIL MESSAGE() broadcasts from a DCC-EX command station, styled like the Virtual LCD. */
public class MessagesPanel extends JPanel implements DCCppListener, MessagesConfiguration {

    public static final String DEFAULT_SOUND_FILE = "program:resources/sounds/bell_stroke.wav";

    private final boolean _isMemoEditable;
    private final JmriJFrame _frame;
    private final Positionable _positionable;
    private final MessageQueue _queue;
    private final javax.swing.Timer _timer;
    private final List<JLabel> _lines = new ArrayList<>();
    private final Map<String, Boolean> _checkedSoundFiles = new HashMap<>();
    private Predicate<URL> _soundCheck = MessagesPanel::isPlayable;
    private Consumer<URL> _soundPlayer = url -> new Sound(url).play(true);
    private final DateFormat _timeFormat = new SimpleDateFormat("HH:mm:ss");
    private DCCppTrafficController _tc;
    private DCCppSystemConnectionMemo _memo;
    private Font _font;
    private boolean _autoOpen = true;
    private boolean _playSound = false;
    private String _soundFile = DEFAULT_SOUND_FILE;
    private Color _foreground = Color.WHITE;
    private Color _background = Color.BLACK;
    private Dimension _messagesSize;

    public MessagesPanel(@CheckForNull JmriJFrame frame, boolean isMemoEditable) {
        this(frame, null, isMemoEditable);
    }

    public MessagesPanel(@CheckForNull JmriJFrame frame, @CheckForNull Positionable pos, boolean isMemoEditable) {
        this(frame, pos, isMemoEditable, System::currentTimeMillis);
    }

    MessagesPanel(@CheckForNull JmriJFrame frame, @CheckForNull Positionable pos, boolean isMemoEditable,
            LongSupplier clock) {
        _isMemoEditable = isMemoEditable;
        _frame = frame;
        _positionable = pos;
        _queue = new MessageQueue(clock);
        _timer = new javax.swing.Timer(1000, e -> tick());
    }

    public void initComponents() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // same 5x8 font as the Virtual LCD
        try {
            InputStream stream = new FileInputStream(new File("resources/fonts/5x8_lcd_hd44780u_a02.ttf"));
            _font = Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(16f).deriveFont(Font.BOLD);
        } catch (IOException e1) { log.error("failed to find or open font file");
        } catch (FontFormatException e2) { log.error("font file not valid");
        }

        setOpaque(true);
        rebuild();
    }

    public void dispose() {
        _timer.stop();
        if (_tc != null) {
            _tc.removeDCCppListener(DCCppInterface.CS_INFO, this);
        }
    }

    @InvokeOnGuiThread
    public void addMessage(String text) {
        _queue.add(text);
        rebuild();
        if (!_timer.isRunning() && _queue.getExpirySeconds() > 0) {
            _timer.start();
        }
        playSound();
    }

    @InvokeOnGuiThread
    public void clearMessages() {
        _queue.clear();
        _timer.stop();
        rebuild();
    }

    @InvokeOnGuiThread
    void tick() {
        if (_queue.expire()) {
            rebuild();
        }
        if (_queue.getEntries().isEmpty()) {
            _timer.stop();
        }
    }

    boolean isExpiryTimerRunning() {
        return _timer.isRunning();
    }

    public int getLineCount() {
        return _lines.size();
    }

    public String getLineText(int index) {
        return _lines.get(index).getText();
    }

    public String getNameString() {
        return Bundle.getMessage("MessagesPositionable", _memo != null ? _memo.getSystemPrefix() : "");
    }

    private void playSound() {
        if (!_playSound) {
            return;
        }
        URL url = FileUtil.findURL(_soundFile);
        Boolean playable = _checkedSoundFiles.get(_soundFile);
        if (playable == null) {
            playable = url != null && _soundCheck.test(url);
            _checkedSoundFiles.put(_soundFile, playable);
            if (!playable) {
                log.warn("Unable to play DCC-EX message sound {}", _soundFile);
            }
        }
        if (playable) {
            _soundPlayer.accept(url);
        }
    }

    // tests swap these to count checks and plays without real audio
    void setSoundHooks(Predicate<URL> check, Consumer<URL> player) {
        _soundCheck = check;
        _soundPlayer = player;
    }

    // Sound logs its own errors on every play, so check the file and audio line once up front
    private static boolean isPlayable(URL url) {
        try (AudioInputStream stream = AudioSystem.getAudioInputStream(url); Clip clip = AudioSystem.getClip(null)) {
            clip.open(stream);
            return true;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException | IllegalArgumentException e) {
            return false;
        }
    }

    private void rebuild() {
        removeAll();
        _lines.clear();

        List<String> texts = new ArrayList<>();
        List<MessageQueue.Entry> entries = _queue.getEntries();
        if (entries.isEmpty()) {
            texts.add(Bundle.getMessage("MessagesNoMessages"));
        } else {
            for (MessageQueue.Entry e : entries) {
                texts.add(_timeFormat.format(new Date(e.getTimeMillis())) + "  " + e.getText());
            }
        }
        if (_messagesSize != null) {
            while (texts.size() > _messagesSize.height) {
                texts.remove(texts.size() - 1);
            }
            while (texts.size() < _messagesSize.height) {
                texts.add("");
            }
        }

        for (String text : texts) {
            JLabel label = new JLabel(cutIfNeeded(text));
            if (_font != null) label.setFont(_font);
            label.setOpaque(true);
            label.setBackground(_background);
            label.setForeground(_foreground);
            _lines.add(label);
            add(label);
        }
        setBackground(_background);
        packOrResize();
        repaint();
    }

    private String cutIfNeeded(String s) {
        if (_messagesSize != null) {
            StringBuilder sb = new StringBuilder(s);
            while (sb.length() < _messagesSize.width) {
                sb.append(' ');
            }
            if (sb.length() > _messagesSize.width) {
                sb.setLength(_messagesSize.width);
            }
            return sb.toString();
        }
        return s;
    }

    private void packOrResize() {
        if (_positionable != null) {
            var d = this.getPreferredSize();
            this.setSize(d);
            _positionable.setSize(d);
            // rebuild adds fresh labels and nothing else on a panel lays them out
            doLayout();
        } else if (_frame != null) {
            _frame.pack();
        } else {
            revalidate();
        }
    }

    @Override
    public void setMemo(DCCppSystemConnectionMemo memo) {
        if (_tc != null) {
            _tc.removeDCCppListener(DCCppInterface.CS_INFO, this);
        }
        _memo = memo;
        _tc = memo.getDCCppTrafficController();
        _tc.addDCCppListener(DCCppInterface.CS_INFO, this);
    }

    @Override
    public DCCppSystemConnectionMemo getMemo() {
        return _memo;
    }

    @Override
    public boolean isMemoEditable() {
        return _isMemoEditable;
    }

    @Override
    public void setExpirySeconds(int seconds) {
        _queue.setExpirySeconds(seconds);
        if (_queue.getExpirySeconds() == 0) {
            _timer.stop();
        } else if (!_queue.getEntries().isEmpty()) {
            _timer.start();
        }
    }

    @Override
    public int getExpirySeconds() {
        return _queue.getExpirySeconds();
    }

    @Override
    public void setMaxLines(int lines) {
        _queue.setMaxLines(lines);
        rebuild();
    }

    @Override
    public int getMaxLines() {
        return _queue.getMaxLines();
    }

    @Override
    public void setAutoOpen(boolean autoOpen) {
        _autoOpen = autoOpen;
    }

    @Override
    public boolean isAutoOpen() {
        return _autoOpen;
    }

    @Override
    public void setPlaySound(boolean playSound) {
        _playSound = playSound;
    }

    @Override
    public boolean isPlaySound() {
        return _playSound;
    }

    @Override
    public void setSoundFile(String path) {
        _soundFile = path;
    }

    @Override
    public String getSoundFile() {
        return _soundFile;
    }

    @Override
    public void setMessageForeground(Color color) {
        _foreground = color;
        rebuild();
    }

    @Override
    public Color getMessageForeground() {
        return _foreground;
    }

    @Override
    public void setMessageBackground(Color color) {
        _background = color;
        rebuild();
    }

    @Override
    public Color getMessageBackground() {
        return _background;
    }

    @Override
    public void setMessagesSize(@CheckForNull Dimension size) {
        if (size != null && size.width > 0 && size.height > 0) {
            _messagesSize = size;
        } else {
            _messagesSize = null;
        }
        rebuild();
    }

    @CheckForNull
    @Override
    public Dimension getMessagesSize() {
        return _messagesSize;
    }

    /** {@inheritDoc} */
    @Override
    public void message(DCCppReply msg) {
        if (msg.isMessageReply()) {
            addMessage(msg.getMessageString());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void message(DCCppMessage msg) {
    }

    /** {@inheritDoc} */
    @Override
    public void notifyTimeout(DCCppMessage msg) {
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MessagesPanel.class);

}
