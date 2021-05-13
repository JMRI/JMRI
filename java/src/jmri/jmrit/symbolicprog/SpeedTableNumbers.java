package jmri.jmrit.symbolicprog;

/**
 * Enum for which numbers should be shown on the speed table.
 * 
 * @author Daniel Bergqvist (C) 2021
 */
public enum SpeedTableNumbers {
    
    None(Bundle.getMessage("SpeedTable_None"), (index) -> {return false;}),
    All(Bundle.getMessage("SpeedTable_All"), (index) -> {return true;}),
    Only1of3(Bundle.getMessage("SpeedTable_1of3"), (index) -> {return (index % 3) == 0;}),
    Only1of7(Bundle.getMessage("SpeedTable_1of7"), (index) -> {return (index % 7) == 0;});
    
    private final String label;
    private final Filter filter;
    
    private SpeedTableNumbers(String label, Filter filter) {
        this.label = label;
        this.filter = filter;
    }
    
    @Override
    public String toString() {
        return label;
    }
    
    public boolean filter(int index) {
        return filter.filter(index);
    }
    
    
    private static interface Filter {
        boolean filter(int index);
    }
    
}
