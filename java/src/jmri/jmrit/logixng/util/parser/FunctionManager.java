package jmri.jmrit.logixng.util.parser;

import java.util.*;

/**
 * Manager for LogixNG formula functions.
 *
 * @author Daniel Bergqvist   Copyright (C) 2020
 */
public class FunctionManager implements jmri.InstanceManagerAutoDefault {

    private final Map<String, Constant> _constants = new HashMap<>();
    private final Map<String, Function> _functions = new HashMap<>();


    public FunctionManager() {
        for (FunctionFactory actionFactory : ServiceLoader.load(FunctionFactory.class)) {
            actionFactory.getConstants().forEach((constant) -> {
                if (!constant.getModule().equals(actionFactory.getModule())) {
                    log.error("Constant \"{}\" doesn't belong to the module \"{}\" of its declaring class {}",
                            constant.getName(), actionFactory.getModule(), actionFactory.getClass().getName());
                }
                if (_constants.containsKey(constant.getName())) {
                    throw new RuntimeException("Constant " + constant.getName() + " is already registered. Class: " + constant.getClass().getName());
                }
//                System.err.format("Add constant %s, %s%n", constant.getName(), constant.getClass().getName());
                _constants.put(constant.getName(), constant);
            });
            actionFactory.getFunctions().forEach((function) -> {
                if (!function.getModule().equals(actionFactory.getModule())) {
                    log.error("Function \"{}\" doesn't belong to the module \"{}\" of its declaring class {}",
                            function.getName(), actionFactory.getModule(), actionFactory.getClass().getName());
                }
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

    public Constant getConstant(String name) {
        return _constants.get(name);
    }

    public void put(String name, Constant constant) {
        _constants.put(name, constant);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FunctionManager.class);
}
