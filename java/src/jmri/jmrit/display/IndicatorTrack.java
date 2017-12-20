package jmri.jmrit.display;

import java.util.ArrayList;
import jmri.NamedBeanHandle;
import jmri.Sensor;
import jmri.jmrit.logix.OBlock;

/**
 * Defines track objects that display status color.
 *
 * @author Pete Cressman Copyright (c) 2010
 */
public interface IndicatorTrack extends Positionable {

    public void setOccSensor(String pName);

    public void setOccSensorHandle(NamedBeanHandle<Sensor> senHandle);

    public Sensor getOccSensor();

    public NamedBeanHandle<Sensor> getNamedOccSensor();

    public void setOccBlock(String pName);

    public void setOccBlockHandle(NamedBeanHandle<OBlock> blockHandle);

    public OBlock getOccBlock();

    public NamedBeanHandle<OBlock> getNamedOccBlock();

    public void setShowTrain(boolean set);

    public boolean showTrain();

    public ArrayList<String> getPaths();

    public void addPath(String path);

    public void removePath(String path);

    public void setStatus(int state);
}
