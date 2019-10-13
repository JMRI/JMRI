# The result of the last method in the script is returned by the Java
# ScriptEngine.eval(...) methods, so this Jython script is calling a single
# non-void method of an object provided in the bindings for the ScriptEngine.
#
# Note that the bindings are not necessarily the default bindings a JMRI
# application provides to ScriptEngines.

profiles.getAutoStartActiveProfileTimeout()