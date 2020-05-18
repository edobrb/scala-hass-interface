# scala-hass-interface
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/970cd0da136b45b3b221c92c925635c1)](https://app.codacy.com/manual/edobrb/scala-hass-interface?utm_source=github.com&utm_medium=referral&utm_content=edobrb/scala-hass-interface&utm_campaign=Badge_Grade_Dashboard)
[![](https://jitpack.io/v/edobrb/scala-hass-interface.svg)](https://jitpack.io/#edobrb/scala-hass-interface)
[![Build Status](https://travis-ci.com/edobrb/scala-hass-interface.svg?branch=master)](https://travis-ci.com/edobrb/scala-hass-interface)
[![codecov](https://codecov.io/gh/edobrb/scala-hass-interface/branch/master/graph/badge.svg)](https://codecov.io/gh/edobrb/scala-hass-interface)
## Purpose
This artifact aims to create a bridge between Home Assistant web API and any JVM software.
The purpose is to provide a complete, easy and type safe library for interacting with Hass from the JVM.
[Hass Websocket documentation.](https://developers.home-assistant.io/docs/api/websocket/)

## Features
-   [x] Connection via websocket
-   [x] Token authentication
-   [x] Auto-reconnection
 
Supported entities and relative services:
-   [x] Light
-   [x] Switch
-   [x] Sensor
-   [ ] Template
-   [x] BinarySensor
-   [x] InputBoolean
-   [x] InputDateTime
-   [ ] Sun
-   [ ] Weather
-   [ ] Person
-   [ ] Automation
-   [ ] Script

## Usage

The usage is unfolded by examples:

### Creation of Hass interface
```scala
implicit val hass: Hass = Hass(
  "ip:port", 
  "auth token", 
  retryOnError = true) 
//this will establish a connection to ws://ip:port/api/websocket
```

### Usage of hass instance
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

### Interact with hass by creating entities
This approach is easier and cleaner:
```scala
  val my_light = Light() //will bound to light.my_light entity
  my_light.onState {
    case (On, time, state) if state.brightness.exists(_ == 255) => 
      println(s"my_lamp turn on with maximum brightness at $time (${state.rgb})")
  }
  my_light.turnOn(_.rgb(255, 0, 255).brightness(255).transition(1))


  val my_switch = Switch("sonoff1") //will bound to switch.sonoff1 entity
  my_switch.onState {
    case (Off, _, _) => 
      println("This switch should stay on!")
      my_switch.turn(On)
  }


  val home_power = Sensor() //will bound to sensor.home_power entity
  home_power.onState {
    case (value, time, _) => println(time + ": " + value)
  }


  val my_other_light = Light() //will bound to light.my_other_light entity
  val light_group = LightGroup(my_light, my_other_light)
  light_group.toggle()
```