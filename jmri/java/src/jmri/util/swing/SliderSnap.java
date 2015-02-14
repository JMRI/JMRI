// SliderSnap.java

/*
 * Copyright (c) 2011 Michael Kneebone. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  o The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package jmri.util.swing;

import java.awt.EventQueue;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSliderUI;

/**
 * This corrects the behaviour of JSliders to correctly snap whilst
 * sliding, rather than to snap after sliding.
 * <p>
 * This was originally published by Michael Kneebone in 
 * <a href="http://www.javaspecialists.eu/archive/Issue148.html">
 * issue 148 of "Java Specialists' Newsletter"</a>
 * to whom credit is given.
 * <p>
 * This version is updated with bug fixes compared to that published above
 * and was provided by Michael Kneebone.
 * <p>
 * Minor changes and bug fixes were made by Matthew Harris when
 * incorporating this into JMRI.
 * <p>
 *
 * @author  Michael Kneebone    Copyright (c) 2007, 2011
 * @author  Matthew Harris      Copyright (c) 2011
 * @version $Revision$
 */
public class SliderSnap extends BasicSliderUI {

    /**
     * The UI class implements the current slider Look and Feel
     */
    private static Class<? extends ComponentUI> sliderClass;
    private static Method xForVal;
    private static Method yForVal;
    private static ReinitListener reinitListener = new ReinitListener();

    public SliderSnap() {
        super(null);
    }

    /**
     * Returns the UI as normal, but intercepts the call, so that a
     * listener can be attached
     * @param c the slider component
     * @return a ComponentUI object with attached listener
     */
    public static ComponentUI createUI(JComponent c) {
        if (c == null || sliderClass == null) {
            return null;
        }
        UIDefaults defaults = UIManager.getLookAndFeelDefaults();
        try {
            Method m = (Method) defaults.get(sliderClass);
            if (m == null) {
                m = sliderClass.getMethod("createUI", //NOI18N
                        new Class<?>[] {JComponent.class});
                defaults.put(sliderClass, m);
            }
            ComponentUI uiObject = (ComponentUI) m.invoke(null, new Object[]{c});
            if (uiObject instanceof BasicSliderUI) {
                c.addHierarchyListener(new MouseAttacher());
            }
            return uiObject;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void init() {
        // Check we're not already initialised
        if (sliderClass != null) {
            return;
        }
        Init init = new Init();
        if (EventQueue.isDispatchThread()) {
            init.run();
        } else {
            // Needs to run on the Event Despatch Thread for data visibility
            try {
                EventQueue.invokeAndWait(init);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Called to attach mouse listeners to the JSlider
     * @param c JSlider to be attached to
     */
    private static void attachTo(JSlider c) {
        MouseMotionListener[] listeners = c.getMouseMotionListeners();
        for (int i = 0; i < listeners.length; i++) {
            MouseMotionListener m = listeners[i];
            if (m instanceof TrackListener) {
                // Remove the original listener
                c.removeMouseMotionListener(m);

                // Create a new snap listener and add it
                SnapListener listen = new SnapListener(m, (BasicSliderUI) c.getUI(), c);
                c.addMouseMotionListener(listen);
                c.addMouseListener(listen);
                c.addPropertyChangeListener("UI", listen); //NOI18N
            }
        }
    }

    private static class SnapListener extends MouseInputAdapter implements PropertyChangeListener {

        /**
         * Parent MouseMotionListener
         */
        private MouseMotionListener delegate;

        /**
         * Original Look and Feel implementation
         */
        private BasicSliderUI ui;

        /**
         * Our slider
         */
        private JSlider slider;

        /**
         * Offset of mouse click from centre of slider thumb
         */
        private int offset;

        public SnapListener(MouseMotionListener delegate, BasicSliderUI ui, JSlider slider) {
            this.delegate = delegate;
            this.ui = ui;
            this.slider = slider;
        }

        /**
         * UI can change at any point, so need to listen for this
         * @param evt property change event
         */
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if ("UI".equals(evt.getPropertyName())) { //NOI18N
                // Remove old listeners and create new ones
                slider.removeMouseMotionListener(this);
                slider.removeMouseListener(this);
                slider.removePropertyChangeListener("UI", this); //NOI18N
                attachTo(slider);
            }
        }

        /**
         * Implements the actual 'snap while dragging' behaviour.
         * If snap to ticks is enabled on this slider, then the location
         * for the nearest tick/label is calculated and the click
         * location is translated before being passed to the delegate.
         * @param evt MouseEvent
         */
        @Override
        public void mouseDragged(MouseEvent evt) {

            // Used to record the amount we translate the event point by
            int xTrans = 0;
            int yTrans = 0;

            // Check if we're set to snap
            if (slider.getSnapToTicks()) {
                int pos = getLocationForValue(getSnappedValue(evt));

                // If above call fails and returns -1, take no further action
                if (pos > -1) {
                    if (slider.getOrientation() == JSlider.HORIZONTAL) {
                        xTrans = pos - evt.getX() + offset;
                        evt.translatePoint(xTrans, 0);
                    } else {
                        yTrans = pos - evt.getY() + offset;
                        evt.translatePoint(0, yTrans);
                    }
                }
            }

            // Pass event onto delegate
            delegate.mouseDragged(evt);

            // Restore original mouse point
            evt.translatePoint(xTrans, yTrans);
        }

        /**
         * When the slider is clicked we need to record the offset
         * from the thumb centre.
         * @param evt MouseEvent
         */
        @Override
        public void mousePressed(MouseEvent evt) {
            int pos = (slider.getOrientation() == JSlider.HORIZONTAL) ?
                evt.getX() : evt.getY();
            int loc = getLocationForValue(getSnappedValue(evt));
            this.offset = (loc < 0) ? 0 : pos - loc;
        }

        /**
         * Pass MouseEvent straight through to delegate
         * @param evt MouseEvent
         */
        @Override
        public void mouseMoved(MouseEvent evt) {
            delegate.mouseMoved(evt);
        }

        /**
         * Pass MouseEvent straight through to delegate
         * @param evt MouseEvent
         */
        @Override
        public void mouseClicked(MouseEvent evt) {
            delegate.mouseMoved(evt);
        }

        /**
         * Pass MouseEvent straight through to delegate
         * @param evt MouseEvent
         */
        @Override
        public void mouseReleased(MouseEvent evt) {
            delegate.mouseMoved(evt);
        }

        /**
         * Pass MouseEvent straight through to delegate
         * @param evt MouseEvent
         */
        @Override
        public void mouseEntered(MouseEvent evt) {
            delegate.mouseMoved(evt);
        }

        /**
         * Pass MouseEvent straight through to delegate
         * @param evt MouseEvent
         */
        @Override
        public void mouseExited(MouseEvent evt) {
            delegate.mouseMoved(evt);
        }

        /**
         * Calculates the nearest snapable value given a MouseEvent.
         * Code adapted from BasicSliderUI
         * @param evt MouseEvent
         * @return nearest snapable value
         */
        public int getSnappedValue(MouseEvent evt) {
            // Determine current value based on orientation
            int value = slider.getOrientation()
                    == JSlider.HORIZONTAL
                    ? ui.valueForXPosition(evt.getX())
                    : ui.valueForYPosition(evt.getY());

            // Now determine if we should adjust the value
            int snappedValue = value;
            int tickSpacing = 0;
            int majorTickSpacing = slider.getMajorTickSpacing();
            int minorTickSpacing = slider.getMinorTickSpacing();
            if (minorTickSpacing > 0) {
                tickSpacing = minorTickSpacing;
            } else if (majorTickSpacing > 0) {
                tickSpacing = majorTickSpacing;
            }

            // If it's not on a tick, change the value
            if (tickSpacing != 0) {
                if ((value - slider.getMinimum()) % tickSpacing != 0) {
                    float temp = (float) (value - slider.getMinimum())
                            / (float) tickSpacing;
                    snappedValue = slider.getMinimum()
                            + (Math.round(temp) * tickSpacing);
                }
            }
            return snappedValue;
        }

        /**
         * Provides the x or y co-ordinate for a slider value
         * depending on orientation
         * @param value the value to get location for
         * @return appropriate x or y co-ordinate
         */
        public int getLocationForValue(int value) {
            try {
                // Reflectively call slider ui code
                Method m = slider.getOrientation()
                        == JSlider.HORIZONTAL
                        ? xForVal : yForVal;
                Integer result = (Integer) m.invoke(ui, new Object[]{Integer.valueOf(value)});
                return result.intValue();
            } catch (InvocationTargetException e) {
                return -1;
            } catch (IllegalAccessException e) {
                return -1;
            }
        }
    }

    /**
     * Listens for when the JSlider becomes visible then
     * attaches the mouse listeners, then removes itself.
     */
    private static class MouseAttacher implements HierarchyListener {

        @Override
        public void hierarchyChanged(HierarchyEvent evt) {
            long flags = evt.getChangeFlags();
            if ((flags & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0
                    && evt.getComponent() instanceof JSlider) {
                JSlider c = (JSlider) evt.getComponent();
                c.removeHierarchyListener(this);
                attachTo(c);
            }
        }
    }

    /**
     * Listens for Look and Feel changes and re-initialises the class.
     */
    private static class ReinitListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if ("lookAndFeel".equals(evt.getPropertyName())) { //NOI18N
                // The Look and Feel was changed so we need to re-insert
                // our hook into the new UIDefaults map
                sliderClass = null;
                xForVal = null;
                yForVal = null;
                UIManager.removePropertyChangeListener(reinitListener);
                init();
            }
        }
    }

    /**
     * Initialises the reflective methods and adjusts the current
     * Look and Feel
     */
    private static class Init implements Runnable {

        @Override
        public void run() {
            try {
                UIDefaults defaults = UIManager.getLookAndFeelDefaults();
                sliderClass = defaults.getUIClass("SliderUI"); //NOI18N

                // Set up two reflective method calls
                xForVal = BasicSliderUI.class.getDeclaredMethod(
                        "xPositionForValue", //NOI18N
                        new Class<?>[]{int.class});
                yForVal = BasicSliderUI.class.getDeclaredMethod(
                        "yPositionForValue", //NOI18N
                        new Class<?>[]{int.class});

                // Allow us access to the methods
                xForVal.setAccessible(true);
                yForVal.setAccessible(true);

                // Replace UI class with ourself
                defaults.put("SliderUI", SliderSnap.class.getName()); //NOI18N
                UIManager.addPropertyChangeListener(reinitListener);
            } catch (NoSuchMethodException e) {
                sliderClass = null;
                xForVal = null;
                yForVal = null;
            }
        }
    }

}

/* @(#)SliderSnap.java */
