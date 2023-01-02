You can access jmri.Turnout.CLOSED but jmri.Turnout.UNKNOWN is undefined.  You have to access jmri.NamedBean.UNKNOWN instead, which is a real pain.

The Jython property wrapper syntax that makes the next two lines the same is not available:
```
	turnout.state
	turnout.getState()
```

Some super-class methods need to be called via e.g.
```
	self.__super__.waitMsec(1000)
```
But others don’t, and it’s not clear why.

Referencing Python local variables in a Java-based object requires .this. syntax:
```
    m = MyListener()
    #
    # invoked the listener, which sets 'internalVariable'
    #
    m.this.internalVariable
```

We can't yet put JMRI symbols into the Python context, so you have to run
```
	exec(open("jython/jmri_bindings.py3").read())
```
at the start of each script.

Referencing a class type needs a java.type wrapper:
```
    sm = jmri.InstanceManager.getNullableDefault(java.type('jmri.SensorManager'))
```

The debugging support is terrible:  Syntax errors generate opaque error messages that don’t point to the relevant line in the script.

AbstractAutomaton subclasses occasionally cause the whole of JMRI to stop with a thread deadlock.  Not understood why the GraalVM Python interpreter is taking a lock on the Swing/AWT thread.

