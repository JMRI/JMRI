package jmri.jmrit.logixng.util.parser;

import java.util.*;

/**
 * Manager for LogixNG formula functions.
 * 
 * @author Daniel Bergqvist   Copyright (C) 2020
 */
public class FunctionManager implements jmri.InstanceManagerAutoDefault {
    
    private final Map<String, Function> _functions = new HashMap<>();
    
    public FunctionManager() {
        for (FunctionFactory actionFactory : ServiceLoader.load(FunctionFactory.class)) {
            actionFactory.getFunctions().forEach((function) -> {
                if (_functions.containsKey(function.getName())) {
                    throw new RuntimeException("Function " + function.getName() + " is already registered. Class: " + function.getClass().getName());
                }
//                System.err.format("Add function %s, %s%n", function.getName(), function.getClass().getName());
                _functions.put(function.getName(), function);
            });
        }
    }
    
    public Map<String, Function> getFunctions() {
        return Collections.unmodifiableMap(_functions);
    }
    
    public Function get(String name) {
        return _functions.get(name);
    }
    
    public Function put(String name, Function function) {
        return _functions.put(name, function);
    }
    
}
