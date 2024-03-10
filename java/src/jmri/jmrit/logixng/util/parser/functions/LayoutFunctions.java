package jmri.jmrit.logixng.util.parser.functions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jmri.*;
import jmri.jmrit.logixng.SymbolTable;
import jmri.jmrit.logixng.util.parser.*;
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

        addTurnoutExistsFunction(functionClasses);
        addGetTurnoutStateFunction(functionClasses);
        addSetTurnoutStateFunction(functionClasses);
        addSensorExistsFunction(functionClasses);
        addGetSensorStateFunction(functionClasses);
        addSetSensorStateFunction(functionClasses);
        addLightExistsFunction(functionClasses);
        addGetLightStateFunction(functionClasses);
        addSetLightStateFunction(functionClasses);
        addSignalHeadExistsFunction(functionClasses);
        addGetSignalHeadAppearanceFunction(functionClasses);
        addSetSignalHeadAppearanceFunction(functionClasses);
        addSignalMastExistsFunction(functionClasses);
        addGetSignalMastAspectFunction(functionClasses);
        addSetSignalMastAspectFunction(functionClasses);

        return functionClasses;
    }

    @Override
    public Set<Constant> getConstants() {
        Set<Constant> constantClasses = new HashSet<>();
        constantClasses.add(new Constant(getModule(), "Unknown", NamedBean.UNKNOWN));
        constantClasses.add(new Constant(getModule(), "Inconsistent", NamedBean.INCONSISTENT));

        constantClasses.add(new Constant(getModule(), "Off", Light.OFF));
        constantClasses.add(new Constant(getModule(), "On", Light.ON));

        constantClasses.add(new Constant(getModule(), "Inactive", Sensor.INACTIVE));
        constantClasses.add(new Constant(getModule(), "Active", Sensor.ACTIVE));

        constantClasses.add(new Constant(getModule(), "Closed", Turnout.CLOSED));
        constantClasses.add(new Constant(getModule(), "Thrown", Turnout.THROWN));
        constantClasses.add(new Constant(getModule(), "CabLockout", Turnout.CABLOCKOUT));
        constantClasses.add(new Constant(getModule(), "PushButtonLockout", Turnout.PUSHBUTTONLOCKOUT));
        constantClasses.add(new Constant(getModule(), "Unlocked", Turnout.UNLOCKED));
        constantClasses.add(new Constant(getModule(), "Locked", Turnout.LOCKED));

        constantClasses.add(new Constant(getModule(), "Dark", SignalHead.DARK));
        constantClasses.add(new Constant(getModule(), "Red", SignalHead.RED));
        constantClasses.add(new Constant(getModule(), "FlashRed", SignalHead.FLASHRED));
        constantClasses.add(new Constant(getModule(), "Yellow", SignalHead.YELLOW));
        constantClasses.add(new Constant(getModule(), "FlashYellow", SignalHead.FLASHYELLOW));
        constantClasses.add(new Constant(getModule(), "Green", SignalHead.GREEN));
        constantClasses.add(new Constant(getModule(), "FlashGreen", SignalHead.FLASHGREEN));
        constantClasses.add(new Constant(getModule(), "Lunar", SignalHead.LUNAR));
        constantClasses.add(new Constant(getModule(), "FlashLunar", SignalHead.FLASHLUNAR));
        constantClasses.add(new Constant(getModule(), "Held", SignalHead.HELD));

        constantClasses.add(new Constant(getModule(), "sensors", InstanceManager.getNullableDefault(SensorManager.class)));
        constantClasses.add(new Constant(getModule(), "turnouts", InstanceManager.getNullableDefault(TurnoutManager.class)));
        constantClasses.add(new Constant(getModule(), "lights", InstanceManager.getNullableDefault(LightManager.class)));
        constantClasses.add(new Constant(getModule(), "signals", InstanceManager.getNullableDefault(SignalHeadManager.class)));
        constantClasses.add(new Constant(getModule(), "masts", InstanceManager.getNullableDefault(SignalMastManager.class)));
        constantClasses.add(new Constant(getModule(), "routes", InstanceManager.getNullableDefault(RouteManager.class)));
        constantClasses.add(new Constant(getModule(), "blocks", InstanceManager.getNullableDefault(BlockManager.class)));
        constantClasses.add(new Constant(getModule(), "reporters", InstanceManager.getNullableDefault(ReporterManager.class)));
        constantClasses.add(new Constant(getModule(), "memories", InstanceManager.getNullableDefault(MemoryManager.class)));
        constantClasses.add(new Constant(getModule(), "powermanager", InstanceManager.getNullableDefault(PowerManager.class)));
        constantClasses.add(new Constant(getModule(), "addressedProgrammers", InstanceManager.getNullableDefault(AddressedProgrammerManager.class)));
        constantClasses.add(new Constant(getModule(), "globalProgrammers", InstanceManager.getNullableDefault(GlobalProgrammerManager.class)));
        constantClasses.add(new Constant(getModule(), "dcc", InstanceManager.getNullableDefault(CommandStation.class)));
        constantClasses.add(new Constant(getModule(), "audio", InstanceManager.getNullableDefault(AudioManager.class)));
        constantClasses.add(new Constant(getModule(), "shutdown", InstanceManager.getNullableDefault(ShutDownManager.class)));
        constantClasses.add(new Constant(getModule(), "layoutblocks", InstanceManager.getNullableDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class)));
        constantClasses.add(new Constant(getModule(), "warrants", InstanceManager.getNullableDefault(jmri.jmrit.logix.WarrantManager.class)));
        constantClasses.add(new Constant(getModule(), "sections", InstanceManager.getNullableDefault(SectionManager.class)));
        constantClasses.add(new Constant(getModule(), "transits", InstanceManager.getNullableDefault(TransitManager.class)));

        constantClasses.add(new Constant(getModule(), "InstanceManager", InstanceManager.getDefault()));

//        constantClasses.add(new Constant(getModule(), "FileUtil", FileUtilSupport.getDefault());

        return constantClasses;
    }

    @Override
    public String getConstantDescription() {
        return Bundle.getMessage("Layout.ConstantDescriptions");
    }

    private void addTurnoutExistsFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "turnoutExists", Bundle.getMessage("LayoutFunctions.turnoutExists_Descr")) {
            @Override
            public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                    throws CalculateException, JmriException {
                if (parameterList.isEmpty()) {
                    throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters2", getName(), 1));
                }

                String name = TypeConversionUtil.convertToString(
                        parameterList.get(0).calculate(symbolTable), false);

                Turnout t = InstanceManager.getDefault(TurnoutManager.class).getNamedBean(name);
                return t != null;
            }
        });
    }

    private void addGetTurnoutStateFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "getTurnoutState", Bundle.getMessage("LayoutFunctions.getTurnoutState_Descr")) {
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
        });
    }

    private void addSetTurnoutStateFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "setTurnoutState", Bundle.getMessage("LayoutFunctions.setTurnoutState_Descr")) {
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
        });
    }

    private void addSensorExistsFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "sensorExists", Bundle.getMessage("LayoutFunctions.sensorExists_Descr")) {
            @Override
            public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                    throws CalculateException, JmriException {
                if (parameterList.isEmpty()) {
                    throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters2", getName(), 1));
                }

                String name = TypeConversionUtil.convertToString(
                        parameterList.get(0).calculate(symbolTable), false);

                Sensor s = InstanceManager.getDefault(SensorManager.class).getNamedBean(name);
                return s != null;
            }
        });
    }

    private void addGetSensorStateFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "getSensorState", Bundle.getMessage("LayoutFunctions.getSensorState_Descr")) {
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
        });
    }

    private void addSetSensorStateFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "setSensorState", Bundle.getMessage("LayoutFunctions.setSensorState_Descr")) {
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
        });
    }

    private void addLightExistsFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "lightExists", Bundle.getMessage("LayoutFunctions.lightExists_Descr")) {
            @Override
            public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                    throws CalculateException, JmriException {
                if (parameterList.isEmpty()) {
                    throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters2", getName(), 1));
                }

                String name = TypeConversionUtil.convertToString(
                        parameterList.get(0).calculate(symbolTable), false);

                Light l = InstanceManager.getDefault(LightManager.class).getNamedBean(name);
                return l != null;
            }
        });
    }

    private void addGetLightStateFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "getLightState", Bundle.getMessage("LayoutFunctions.getLightState_Descr")) {
            @Override
            public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                    throws CalculateException, JmriException {
                if (parameterList.isEmpty()) {
                    throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters2", getName(), 1));
                }

                String name = TypeConversionUtil.convertToString(
                        parameterList.get(0).calculate(symbolTable), false);

                Light l = InstanceManager.getDefault(LightManager.class).getNamedBean(name);
                if (l == null) throw new CalculateException(Bundle.getMessage("LayoutFunctions_GetLightState_LightNotFound", name));
                return l.getKnownState();
            }
        });
    }

    private void addSetLightStateFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "setLightState", Bundle.getMessage("LayoutFunctions.setLightState_Descr")) {
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

                Light l = InstanceManager.getDefault(LightManager.class).getNamedBean(name);
                if (l == null) throw new CalculateException(Bundle.getMessage("LayoutFunctions_SetLightState_LightNotFound", name));
                l.setState(value);
                return l.getKnownState();
            }
        });
    }

    private void addSignalHeadExistsFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "signalHeadExists", Bundle.getMessage("LayoutFunctions.signalHeadExists_Descr")) {
            @Override
            public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                    throws CalculateException, JmriException {
                if (parameterList.isEmpty()) {
                    throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters2", getName(), 1));
                }

                String name = TypeConversionUtil.convertToString(
                        parameterList.get(0).calculate(symbolTable), false);

                SignalHead sh = InstanceManager.getDefault(SignalHeadManager.class).getNamedBean(name);
                return sh != null;
            }
        });
    }

    private void addGetSignalHeadAppearanceFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "getSignalHeadAppearance", Bundle.getMessage("LayoutFunctions.getSignalHeadAppearance_Descr")) {
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
        });
    }

    private void addSetSignalHeadAppearanceFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "setSignalHeadAppearance", Bundle.getMessage("LayoutFunctions.setSignalHeadAppearance_Descr")) {
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
        });
    }

    private void addSignalMastExistsFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "signalMastExists", Bundle.getMessage("LayoutFunctions.signalMastExists_Descr")) {
            @Override
            public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                    throws CalculateException, JmriException {
                if (parameterList.isEmpty()) {
                    throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters2", getName(), 1));
                }

                String name = TypeConversionUtil.convertToString(
                        parameterList.get(0).calculate(symbolTable), false);

                SignalMast sh = InstanceManager.getDefault(SignalMastManager.class).getNamedBean(name);
                return sh != null;
            }
        });
    }

    private void addGetSignalMastAspectFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "getSignalMastAspect", Bundle.getMessage("LayoutFunctions.getSignalMastAspect_Descr")) {
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
        });
    }

    private void addSetSignalMastAspectFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "setSignalMastAspect", Bundle.getMessage("LayoutFunctions.setSignalMastAspect_Descr")) {
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
        });
    }

}
