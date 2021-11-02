package jmri.jmrit.logixng.util.parser.functions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jmri.*;
import jmri.jmrit.logixng.SymbolTable;
import jmri.jmrit.logixng.util.parser.CalculateException;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.jmrit.logixng.util.parser.Function;
import jmri.jmrit.logixng.util.parser.FunctionFactory;
import jmri.jmrit.logixng.util.parser.WrongNumberOfParametersException;
import jmri.util.TypeConversionUtil;

import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of layout functions.
 * 
 * @author Daniel Bergqvist 2021
 */
@ServiceProvider(service = FunctionFactory.class)
public class LayoutFunctions implements FunctionFactory {

    @Override
    public String getModule() {
        return "Layout";
    }
    
    @Override
    public Set<Function> getFunctions() {
        Set<Function> functionClasses = new HashSet<>();
        functionClasses.add(new GetTurnoutStateFunction());
        functionClasses.add(new SetTurnoutStateFunction());
        functionClasses.add(new GetSensorStateFunction());
        functionClasses.add(new SetSensorStateFunction());
        functionClasses.add(new GetSignalHeadAppearanceFunction());
        functionClasses.add(new SetSignalHeadAppearanceFunction());
        functionClasses.add(new GetSignalMastAspectFunction());
        functionClasses.add(new SetSignalMastAspectFunction());
        return functionClasses;
    }
    
    
    
    public static class GetTurnoutStateFunction implements Function {
        
        @Override
        public String getModule() {
            return new LayoutFunctions().getModule();
        }
        
        @Override
        public String getName() {
            return "getTurnoutState";
        }
        
        @Override
        public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                throws CalculateException, JmriException {
            if (parameterList.isEmpty()) {
                throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters2", getName(), 1));
            }
            
            String name = TypeConversionUtil.convertToString(
                    parameterList.get(0).calculate(symbolTable), false);
            
            Turnout t = InstanceManager.getDefault(TurnoutManager.class).getNamedBean(name);
            if (t == null) throw new CalculateException(Bundle.getMessage("LayoutFunctions_GetTurnoutState_TurnoutNotFound", name));
            return t.getKnownState();
        }
        
        @Override
        public String getDescription() {
            return Bundle.getMessage("LayoutFunctions.getTurnoutState_Descr");
        }
        
    }
    
    
    public static class SetTurnoutStateFunction implements Function {
        
        @Override
        public String getModule() {
            return new LayoutFunctions().getModule();
        }
        
        @Override
        public String getName() {
            return "setTurnoutState";
        }
        
        @Override
        public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                throws CalculateException, JmriException {
            if (parameterList.isEmpty()) {
                throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters2", getName(), 2));
            }
            
            String name = TypeConversionUtil.convertToString(
                    parameterList.get(0).calculate(symbolTable), false);
            int value = (int) TypeConversionUtil.convertToLong(
                    parameterList.get(1).calculate(symbolTable));
            
            Turnout t = InstanceManager.getDefault(TurnoutManager.class).getNamedBean(name);
            if (t == null) throw new CalculateException(Bundle.getMessage("LayoutFunctions_SetTurnoutState_TurnoutNotFound", name));
            t.setState(value);
            return t.getKnownState();
        }
        
        @Override
        public String getDescription() {
            return Bundle.getMessage("LayoutFunctions.setTurnoutState_Descr");
        }
        
    }
    
    
    
    public static class GetSensorStateFunction implements Function {
        
        @Override
        public String getModule() {
            return new LayoutFunctions().getModule();
        }
        
        @Override
        public String getName() {
            return "getSensorState";
        }
        
        @Override
        public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                throws CalculateException, JmriException {
            if (parameterList.isEmpty()) {
                throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters2", getName(), 1));
            }
            
            String name = TypeConversionUtil.convertToString(
                    parameterList.get(0).calculate(symbolTable), false);
            
            Sensor s = InstanceManager.getDefault(SensorManager.class).getNamedBean(name);
            if (s == null) throw new CalculateException(Bundle.getMessage("LayoutFunctions_GetSensorState_SensorNotFound", name));
            return s.getKnownState();
        }
        
        @Override
        public String getDescription() {
            return Bundle.getMessage("LayoutFunctions.getSensorState_Descr");
        }
        
    }
    
    
    public static class SetSensorStateFunction implements Function {
        
        @Override
        public String getModule() {
            return new LayoutFunctions().getModule();
        }
        
        @Override
        public String getName() {
            return "setSensorState";
        }
        
        @Override
        public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                throws CalculateException, JmriException {
            if (parameterList.isEmpty()) {
                throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters2", getName(), 2));
            }
            
            String name = TypeConversionUtil.convertToString(
                    parameterList.get(0).calculate(symbolTable), false);
            int value = (int) TypeConversionUtil.convertToLong(
                    parameterList.get(1).calculate(symbolTable));
            
            Sensor s = InstanceManager.getDefault(SensorManager.class).getNamedBean(name);
            if (s == null) throw new CalculateException(Bundle.getMessage("LayoutFunctions_SetSensorState_SensorNotFound", name));
            s.setState(value);
            return s.getKnownState();
        }
        
        @Override
        public String getDescription() {
            return Bundle.getMessage("LayoutFunctions.setSensorState_Descr");
        }
        
    }
    
    
    
    public static class GetSignalHeadAppearanceFunction implements Function {
        
        @Override
        public String getModule() {
            return new LayoutFunctions().getModule();
        }
        
        @Override
        public String getName() {
            return "getSignalHeadAppearance";
        }
        
        @Override
        public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                throws CalculateException, JmriException {
            if (parameterList.isEmpty()) {
                throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters2", getName(), 1));
            }
            
            String name = TypeConversionUtil.convertToString(
                    parameterList.get(0).calculate(symbolTable), false);
            
            SignalHead sh = InstanceManager.getDefault(SignalHeadManager.class).getNamedBean(name);
            if (sh == null) throw new CalculateException(Bundle.getMessage("LayoutFunctions_GetSignalHeadAppearance_SignalHeadNotFound", name));
            return sh.getAppearance();
        }
        
        @Override
        public String getDescription() {
            return Bundle.getMessage("LayoutFunctions.getSignalHeadAppearance_Descr");
        }
        
    }
    
    
    public static class SetSignalHeadAppearanceFunction implements Function {
        
        @Override
        public String getModule() {
            return new LayoutFunctions().getModule();
        }
        
        @Override
        public String getName() {
            return "setSignalHeadAppearance";
        }
        
        @Override
        public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                throws CalculateException, JmriException {
            if (parameterList.isEmpty()) {
                throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters2", getName(), 2));
            }
            
            String name = TypeConversionUtil.convertToString(
                    parameterList.get(0).calculate(symbolTable), false);
            int aspect = (int) TypeConversionUtil.convertToLong(
                    parameterList.get(1).calculate(symbolTable));
            
            SignalHead sh = InstanceManager.getDefault(SignalHeadManager.class).getNamedBean(name);
            if (sh == null) throw new CalculateException(Bundle.getMessage("LayoutFunctions_SetSignalHeadAppearance_SignalHeadNotFound", name));
            sh.setAppearance(aspect);
            return sh.getAppearance();
        }
        
        @Override
        public String getDescription() {
            return Bundle.getMessage("LayoutFunctions.setSignalHeadAppearance_Descr");
        }
        
    }
    
    
    
    public static class GetSignalMastAspectFunction implements Function {
        
        @Override
        public String getModule() {
            return new LayoutFunctions().getModule();
        }
        
        @Override
        public String getName() {
            return "getSignalMastAspect";
        }
        
        @Override
        public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                throws CalculateException, JmriException {
            if (parameterList.isEmpty()) {
                throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters2", getName(), 1));
            }
            
            String name = TypeConversionUtil.convertToString(
                    parameterList.get(0).calculate(symbolTable), false);
            
            SignalMast sm = InstanceManager.getDefault(SignalMastManager.class).getNamedBean(name);
            if (sm == null) throw new CalculateException(Bundle.getMessage("LayoutFunctions_GetSignalMastAspect_SignalMastNotFound", name));
            return sm.getAspect();
        }
        
        @Override
        public String getDescription() {
            return Bundle.getMessage("LayoutFunctions.getSignalMastAspect_Descr");
        }
        
    }
    
    
    public static class SetSignalMastAspectFunction implements Function {
        
        @Override
        public String getModule() {
            return new LayoutFunctions().getModule();
        }
        
        @Override
        public String getName() {
            return "setSignalMastAspect";
        }
        
        @Override
        public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                throws CalculateException, JmriException {
            if (parameterList.isEmpty()) {
                throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters2", getName(), 2));
            }
            
            String name = TypeConversionUtil.convertToString(
                    parameterList.get(0).calculate(symbolTable), false);
            String aspect = TypeConversionUtil.convertToString(
                    parameterList.get(1).calculate(symbolTable), false);
            
            SignalMast sm = InstanceManager.getDefault(SignalMastManager.class).getNamedBean(name);
            if (sm == null) throw new CalculateException(Bundle.getMessage("LayoutFunctions_SetSignalMastAspect_SignalMastNotFound", name));
            sm.setAspect(aspect);
            return sm.getAspect();
        }
        
        @Override
        public String getDescription() {
            return Bundle.getMessage("LayoutFunctions.setSignalMastAspect_Descr");
        }
        
    }
    
}
