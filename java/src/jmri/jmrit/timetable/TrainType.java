package jmri.jmrit.timetable;

import java.awt.Color;
import jmri.util.swing.JmriColorChooser;
/**
 * Define the content of a Train Type record.
 *
 * @author Dave Sand Copyright (C) 2018
 */
public class TrainType {

    public TrainType(int typeId, int layoutId, String typeName, String typeColor) {
        _typeId = typeId;
        _layoutId = layoutId;
        _typeName = typeName;
        setTypeColor(typeColor);
    }

    private int _typeId = 0;
    private int _layoutId = 0;
    private String _typeName = "";
    private String _typeColor = "";

    public int getTypeId() {
        return _typeId;
    }

    public int getLayoutId() {
        return _layoutId;
    }

    public String getTypeName() {
        return _typeName;
    }

    public void setTypeName(String newName) {
        _typeName = newName;
    }

    public String getTypeColor() {
        return _typeColor;
    }

    public void setTypeColor(String newColor) {
        _typeColor = newColor;
        JmriColorChooser.addRecentColor(Color.decode(newColor));
    }

    public String toString() {
        return _typeName;
    }

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrainType.class);
}
