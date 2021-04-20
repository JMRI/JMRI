package jmri;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.JComboBox;

/**
 * Implementation of NamedBeanPropertyDescriptor for multiple choice properties.
 * @author Steve Young Copyright (C) 2020
 * @since 4.21.3
 */
public abstract class SelectionPropertyDescriptor extends NamedBeanPropertyDescriptor<String> {
    
    private final String[] values;
    private final String[] valueToolTips;
    
    /**
     * Create a new SelectionPropertyDescriber.
     * @param key Property Key - used to identify the property in Bean.getProperty(String).
     * @param options Options for the property in String array.
     * @param optionTips Tool-tips for options of the property in String array.
     * @param defVal Default property value.
     */
    public SelectionPropertyDescriptor(
            @Nonnull String key,
            @Nonnull String[] options,
            @Nonnull String[] optionTips,
            @Nonnull String defVal ) {
        super(key, defVal );
        values = options;
        valueToolTips = optionTips;
    }
    
    /** 
     * Get the Class of the property.
     * <p>
     * SelectionPropertyDescriber uses JComboBox.class
     * @return JComboBox.class.
     */
    @Override
    public Class<?> getValueClass() {
        return JComboBox.class;
    }
    
    /**
     * Get the property options.
     * Should be same length as getOptionToolTips()
     * @return copy of the property options.
     */
    public String[] getOptions(){
        return Arrays.copyOf(values,values.length);
    }
    
    /**
     * Get Tool-tips for the options.
     * Should be same length as getOptions()
     * @return list of tool-tips.
     */
    public List<String> getOptionToolTips(){
        return Arrays.asList(valueToolTips);
    }
    
}
