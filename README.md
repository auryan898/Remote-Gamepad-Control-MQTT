# Remote Gamepad Control (Using MQTT)

In this repository, there are three files to pay attention to:
[`RemoteGamepadServer.java`](https://github.com/auryan898/Remote-Gamepad-Control-MQTT/tree/master/Remote-Gamepad-Control-MQTT/src/main/java/com/auryan898/remotegamepad/client),
[`RemoteGamepadClient.java`](https://github.com/auryan898/Remote-Gamepad-Control-MQTT/tree/master/Remote-Gamepad-Control-MQTT/src/main/java/com/auryan898/remotegamepad/server),
and [`TestRemoteServerClient.java`](https://github.com/auryan898/Remote-Gamepad-Control-MQTT/tree/master/Remote-Gamepad-Control-MQTT/src/test/java/com/auryan898/remotegamepad/test)

- `RemoteGamepadServer.java` runs on a PC (tested on Windows) and depends on [EV3-Gamepad-Control](https://github.com/auryan898/EV3-Gamepad-Control)
and Eclipse Paho ([website](https://www.eclipse.org/paho/) , [jar](https://github.com/auryan898/Remote-Gamepad-Control-MQTT/blob/master/Remote-Gamepad-Control-MQTT/lib/org.eclipse.paho.client.mqttv3-1.2.5.jar)).  
It provides a utility to send the input data to MQTT from `GameController` instances of `EV3-Gamepad-Control`.

- `RemoteGamepadClient.java` runs on a client, and depends only on Eclipse Paho ([website](https://www.eclipse.org/paho/),
[jar](https://github.com/auryan898/Remote-Gamepad-Control-MQTT/blob/master/Remote-Gamepad-Control-MQTT/lib/org.eclipse.paho.client.mqttv3-1.2.5.jar))  
It provides a utility to read the gamepad input data sent by `RemoteGamepadServer.java` and store it into a HashMap for later usage.

- `TestRemoteServerClient.java` is an example code using both client and server at the same time, assuming that your MQTT server is running on your current device at tcp://127.0.0.1:1883

## Installation

You'll need to create an MQTT server, such this one, [mosquitto](https://mosquitto.org/)

Download for usage: `RemoteGamepadServer.java` and `RemoteGamepadClient.java` onto the server and client respectively.  
All source java files can be downloaded and extracted from the [Latest Release](https://github.com/auryan898/Remote-Gamepad-Control-MQTT/releases).  
**On server-side, the jar itself can be imported as a dependency. It can be imported client-side, but this is untested.**

Both server and client need to include Eclipse Paho ([website](https://www.eclipse.org/paho/) , [jar](https://github.com/auryan898/Remote-Gamepad-Control-MQTT/blob/master/Remote-Gamepad-Control-MQTT/lib/org.eclipse.paho.client.mqttv3-1.2.5.jar))  
Only the server requires [EV3-Gamepad-Control](https://github.com/auryan898/EV3-Gamepad-Control) as a dependency.

## Client-side Usage

This example code is from `TestRemoteServerClient.java`. First, we setup a `RemoteGamepadClient` named `ryan's pc` listening for   
the controller data at `player1`, using the MQTT server at tcp://127.0.0.1:1883. Then the stored data can be accessed any way you like,  
for example, printing out the data in a while loop:

    RemoteGamepadClient client = new RemoteGamepadClient("ryan's pc", "player1", "tcp://127.0.0.1:1883");
    client.start(); // start listening for data sent

    while (true) {
        // Access data by calling this HashMap ->
        System.out.println(client.getGamepadValues());
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          client.disconnect();
          e.printStackTrace();
          break;
        }
    }
    
## Server-side Usage

This example code is from `TestRemoteServerClient.java`. 

First get the game controller manager and some gamepad controllers to go with it:

    manager = GameControllerManager.getInstance();
    PhysicalGameController p1 = manager.getPhysicalController(0);
    PhysicalGameController p2 = manager.getPhysicalController(1);
    AssignableGameController player1 = manager.getAssignableController(new BaseGamepadKeyMapper(),
        "start", "a_button");

Then make a display and show those controllers on the pc screen:

    display = new GameControllerDisplay();
    display.init();
    display.addController(p1);
    display.addController(p2);
    display.addController(player1);

Now we create the gamepad server and add a controller, with a name (used by gamepad client to read data):

    server = new RemoteGamepadServer("tcp://127.0.0.1:1883");
    server.addController("player1", player1);
    server.connect(); // connect to the mqtt server
    
Finally, we start an update loop, that udpates the controllers and publishes gamepad data:
    
    while (true) { // this can run in another thread
      try {
        manager.update();
        display.update();
        server.publishAllControllerData();
        Thread.sleep(100);
      } catch (InterruptedException | MqttException e) {
        try {
          server.disconnect();
        } catch (MqttException e1) {
          e1.printStackTrace();
        }
      } finally {
        break;
      }
    }
