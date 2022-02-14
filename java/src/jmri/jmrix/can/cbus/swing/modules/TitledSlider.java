package jmri.jmrix.can.cbus.swing.modules;

import java.awt.*;
import java.util.TimerTask;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jmri.util.TimerUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSlider with a titled border
 *
 * @author Andrew Crosland Copyright (C) 2022
 */
public class TitledSlider extends JPanel implements ChangeListener {
        
    protected JSlider tSlide;
    protected int _index;
    protected String _title;
    protected UpdateNV _update;

    /**
     * Construct a new titledSlider
     * 
     * @param title to be displayed
     * @param index of the associated NV 
     * @param update callback funtion to apply new value
     */
    public TitledSlider(String title, int index, UpdateNV update) {
        super();
        _title = title;
        _index = index;
        _update = update;
        tSlide = new JSlider();
    }

    /**
     * Initialise the slider
     * 
     * @param init  Initial value
     * @param min   Minimum value
     * @param max   Maximum value
     */
    public void init(int min, int max, int init) {
        GridLayout grid = new GridLayout(1, 1);
        setLayout(grid);

        Border border = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        TitledBorder titled = BorderFactory.createTitledBorder(border, _title);
        setBorder(titled);

        tSlide.setMinimum(min);
        tSlide.setMaximum(max);
        tSlide.setValue(init);
        tSlide.addChangeListener(this);

        add(tSlide);
    }

    /**
     * Set slider value
     * 
     * Only do this if value has actually changed, otherwise we can trigger
     * an endless round of updating to the table and the gui.
     * 
     * @param v  value
     */
    public void setValue(int v) {
        if (v != tSlide.getValue()) {
            tSlide.setValue(v);
        }
    }

    /**
     * Get slider value
     * 
     * @return slider value
     */
    public int getValue() {
        return tSlide.getValue();
    }

    /**
     * Set the tool tip
     * 
     * @param tt tooltip text
     */
    public void setToolTip(String tt) {
        tSlide.setToolTipText(tt);
    }

    /**
     * Call back with updated value
     * 
     * We restrict the frequency of updates, with a timer, whilst the slider 
     * is adjusting to, prevent CBUS replies from earlier updates looking
     * like a new value change and triggering another update as this can
     * lead to an endless round of updates.
     * 
     * @param e the slider change event
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        if (!source.getValueIsAdjusting()) {
            log.debug("TitledSlider.stateChanged() finished adjusting - final update");
            _update.setNewVal(_index);
            if (timerTask != null) {
                timerTask.cancel();
                timerTask = null;
            }
        } else if (timerTask == null) {
            log.debug("TitledSlider.stateChanged() update");
            _update.setNewVal(_index);
            startTimer();
        }
    }

    /**
     ** The preferred width on the panel must consider the width of the text
     ** used on the TitledBorder
     * 
     * from <a href=https://stackoverflow.com/questions/43425939/how-to-get-the-titledborders-title-to-display-properly-in-the-gui></a>
     */
    @Override
    public Dimension getPreferredSize() {

        Dimension preferredSize = super.getPreferredSize();

        Border border = getBorder();
        int borderWidth = 0;

        if (border instanceof TitledBorder) {
            Insets insets = getInsets();
            TitledBorder titledBorder = (TitledBorder)border;
            borderWidth = titledBorder.getMinimumSize(this).width + insets.left + insets.right;
        }

        int preferredWidth = Math.max(preferredSize.width, borderWidth);

        return new Dimension(preferredWidth, preferredSize.height);
    }

    private TimerTask timerTask;

    /**
     * Start timer for update throttling
     */
    private void startTimer() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                timerTask = null;
            }
        };
        TimerUtil.schedule(timerTask, 100);
    }
    
    private final static Logger log = LoggerFactory.getLogger(TitledSlider.class);

}
