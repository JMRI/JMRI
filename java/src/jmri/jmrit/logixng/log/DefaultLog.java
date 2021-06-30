package jmri.jmrit.logixng.log;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * The default implementation of a LogixNG log
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class DefaultLog implements Log {

    private final List<String> _items = new ArrayList<>();
    private final Map<String, Integer> _itemMap = new HashMap<>();
    
    /** {@inheritDoc} */
    @Override
    public int addItem(String systemName) {
        
        // Don't add the item if it's already there
        Integer index = _itemMap.get(systemName);
        if (index != null) {
            return index;
        }
        
        int newIndex = _items.size();
        _items.add(systemName);
        _itemMap.put(systemName, newIndex);
        
        return newIndex;
    }
    
    /** {@inheritDoc} */
    @Override
    public int getItemIndex(String systemName) {
        Integer index = _itemMap.get(systemName);
        if (index != null) {
            return index;
        } else {
            throw new IllegalArgumentException(String.format("item '%s' is not in the log", systemName));
        }
    }

    /** {@inheritDoc} */
    @Override
    public void clearItemList() {
        _items.clear();
    }
    
    /** {@inheritDoc} */
    @Override
    public List<String> getItemList() {
        return _items;
    }
    
    /** {@inheritDoc} */
    @Override
    public int getNumItems() {
        return _items.size();
    }
    
}
