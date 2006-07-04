# just set initial state for debugging

turnouts.provideTurnout("LT200").setState(UNKNOWN)
turnouts.provideTurnout("LT201").setState(UNKNOWN)
turnouts.provideTurnout("LT202").setState(UNKNOWN)
turnouts.provideTurnout("LT203").setState(UNKNOWN)

sensors.provideSensor("103").setState(ACTIVE)  # run switch

sensors.provideSensor("150").setState(INACTIVE)
sensors.provideSensor("151").setState(INACTIVE)
sensors.provideSensor("152").setState(INACTIVE)
sensors.provideSensor("153").setState(INACTIVE)
sensors.provideSensor("154").setState(INACTIVE)
sensors.provideSensor("155").setState(INACTIVE)
sensors.provideSensor("156").setState(INACTIVE)
sensors.provideSensor("157").setState(INACTIVE)
sensors.provideSensor("158").setState(INACTIVE)
sensors.provideSensor("159").setState(ACTIVE)   # starting point
sensors.provideSensor("160").setState(ACTIVE)   # starting point
sensors.provideSensor("161").setState(INACTIVE)
sensors.provideSensor("162").setState(INACTIVE)
sensors.provideSensor("163").setState(INACTIVE)
sensors.provideSensor("164").setState(INACTIVE)
sensors.provideSensor("165").setState(INACTIVE)

