# scala-hass-interface
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/970cd0da136b45b3b221c92c925635c1)](https://app.codacy.com/manual/edobrb/scala-hass-interface?utm_source=github.com&utm_medium=referral&utm_content=edobrb/scala-hass-interface&utm_campaign=Badge_Grade_Dashboard)
[![](https://jitpack.io/v/edobrb/scala-hass-interface.svg)](https://jitpack.io/#edobrb/scala-hass-interface)
[![Build Status](https://travis-ci.com/edobrb/scala-hass-interface.svg?branch=master)](https://travis-ci.com/edobrb/scala-hass-interface)
[![codecov](https://codecov.io/gh/edobrb/scala-hass-interface/branch/master/graph/badge.svg)](https://codecov.io/gh/edobrb/scala-hass-interface)
## Purpose
This artifact aims to create a bridge between Home Assistant web API and any JVM software.
The purpose is to provide a complete, easy and type safe library for interacting with Home Assistant from the JVM.
[Hass Websocket documentation.](https://developers.home-assistant.io/docs/api/websocket/)

## Requirements
The library needs a Bearer Token in order to authenticate to your Home Assistant instance. It can be generated in your Home Assistant profile section.

## Features

Supported entities and relative services:
-   Light
-   Switch
-   Sensor
-   BinarySensor
-   InputBoolean
-   InputDateTime
-   InputText
-   Sun
-   Person
-   Script

Future entities to supports:
-   Automation
-   Template
-   Weather
-   DeviceTracker
-   Zone

## Usage

The usage is unfolded by examples:

### Creation of Hass interface
This will establish a connection to `ws://ip:port/api/websocket`
```scala
implicit val hass: Hass = Hass(
  "ip:port", 
  "bearer token", 
  retryOnError = true) 
```

### Interact with hass by creating entities
This approach is the easier and cleaner. You can bound any of the supported entities with an object that can handles all the required operations and listens for relevant events:
```scala
val my_light = Light() //will bound to light.my_light entity
my_light.onState {
  case (time, state) if state.value == On && state.brightness.contains(255) =>
    println(s"my_lamp turn on with maximum brightness at $time (${state.rgb})")
}
my_light.turnOn(_.rgb(255, 0, 255).brightness(255).transition(1))


val my_switch = Switch("sonoff1") //will bound to switch.sonoff1 entity
my_switch.onValue {
  case (_, Off) =>
    println(s"${my_switch.entityId} should stay on!")
    my_switch.turn(On)
}

val home_power = Sensor() //will bound to sensor.home_power entity
home_power.onValueChange {
  case (time, oldValue, newValue) => println(time + ": " + (oldValue + " -> " + newValue))
}
home_power.onState {
  case (time, state) if state.numericValue.exists(_ > 3000) => println("Power exceed limit!")
}
```

### Usage of lower level API
Generics events listening:
```scala
hass.onEvent {
  case SwitchStateChangedEvent(entityName, oldState, newState, timeFired, origin) => ???
  case LightStateChangedEvent (entityName, oldState, newState, timeFired, origin) => ???
  case SensorStateChangedEvent(entityName, oldState, newState, timeFired, origin) => ???
 
  case LightTurnServiceCallEvent(service, fired, from) => ???
  case SwitchTurnServiceCallEvent(service, fired, from) => ???
 
  case genericStateChangeEvent: StateChangedEvent[_] => ???
  case genericServiceCallEvent: ServiceCallEvent => ???
  case unknownEvent: UnknownEvent => ???
}
```
Connection events listener:
```scala
hass.onConnection { () => println("connected!") }
hass.onClose{ () => println("connectino closed!") }
```
Service call:
```scala
val turnOnAllMyLight = LightTurnService(Seq("my_lamp", "my_other_lamp"), On).brightness(255)
hass call turnOnAllMyLight
```

### Handle complex automation tasks with Signals
A channel can be used to trigger a delayed event in order to handle timed automations.
```scala
val channel = hass.channel("my automation channel")
my_switch.onState {
    case (On, _, _) => channel.signal("Do something", 5.seconds)
    case (Off, _, _) => channel.reset() //cancel all pending signals
}
channel.onSignal(_ => {
    case "Do something" => 
      //do something (5 seconds after the 'my_switch' has turned on, and still on)
      channel.signal((3, "Continue"), 10.seconds)
    case (number:Int, "Continue") => 
      //do something (after another 10 seconds)
})
```