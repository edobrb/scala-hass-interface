# scala-hass-interface
[![](https://jitpack.io/v/edobrb/scala-hass-interface.svg)](https://jitpack.io/#edobrb/scala-hass-interface)

### Purpose
This artifact aims to create a bridge between Home Assistant web API and any JVM software.
The purpose is to provide a complete, easy and type safe library for interacting with Hass from the JVM.
[Hass Websocket documentation.](https://developers.home-assistant.io/docs/api/websocket/)
### Usage

The usage is unfolded by examples:

##### Creation of Hass interface
```
implicit val hass: Hass = Hass(
  "ip:port", 
  "auth token", 
  retryOnError = true) 
//this will establish a connection to ws://ip:port/api/websocket
```

#### The hard way
Event listening
```
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
A specific event listening
```
hass.onConnection { () => println("connected!") }
hass.onClose{ () => println("connectino closed!") }
```
Service call
```
val turnOnAllMyLight = LightTurnService(Seq("my_lamp", "my_other_lamp"), On).brightness(255)
hass call turnOnAllMyLight
```

#### An easier approach
```
  val my_light = Light()            //will bound to light.my_light entity
  val my_other_light = Light()      //will bound to light.my_other_light entity
  val my_switch = Switch("sonoff1") // will bound to switch.sonoff1 entity
  val home_power = Sensor()         // will bound to sensor.home_power entity
  val light_group = LightGroup(my_light, my_other_light)

  home_power.onStateValueChange {
    case value: String => println(value)
  }

  my_switch.onStateValueChange {
    case On => 
      println("This switch should stay off!")
      my_switch.turn(Off)
  }

  my_light.onStateChange {
    case LightState(_, On, _, time, _) => println("my_lamp turn on at " + time)
  }

  my_light.turnOn(_.rgb(255, 0, 255).brightness(255).transition(1))
  light_group.toggle()
```