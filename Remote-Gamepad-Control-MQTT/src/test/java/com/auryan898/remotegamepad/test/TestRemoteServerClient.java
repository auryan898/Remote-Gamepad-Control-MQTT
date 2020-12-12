package com.auryan898.remotegamepad.test;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.auryan898.ev3gamepad.AssignableGameController;
import com.auryan898.ev3gamepad.GameControllerDisplay;
import com.auryan898.ev3gamepad.GameControllerManager;
import com.auryan898.ev3gamepad.PhysicalGameController;
import com.auryan898.ev3gamepad.keymapping.BaseGamepadKeyMapper;
import com.auryan898.remotegamepad.client.RemoteGamepadClient;
import com.auryan898.remotegamepad.server.RemoteGamepadServer;

public class TestRemoteServerClient implements Runnable {
  public static void main(String[] args) throws MqttException {
    // ---START SERVER SIDE CODE---

    // Get the manager and some controllers
    manager = GameControllerManager.getInstance();
    PhysicalGameController p1 = manager.getPhysicalController(0);
    PhysicalGameController p2 = manager.getPhysicalController(1);
    AssignableGameController player1 = manager.getAssignableController(new BaseGamepadKeyMapper(),
        "start", "a_button");

    // Make a display and show those controllers
    display = new GameControllerDisplay();
    display.init();
    display.addController(p1);
    display.addController(p2);
    display.addController(player1);

    // Create the server and add a controller, with a name
    server = new RemoteGamepadServer("tcp://127.0.0.1:1883");
    server.addController("player1", player1);
    server.connect(); // connect to the mqtt server
    
    // Start a thread to update and publish gamepad data
    Thread t = new Thread(new TestRemoteServerClient());
    t.start();
    // ---END OF SERVER SIDE CODE---

    // ---START CLIENT SIDE CODE---
    // All the code required to setup the client.
    RemoteGamepadClient client = new RemoteGamepadClient("ryan's pc", "player1",
        "tcp://127.0.0.1:1883");
    client.start();

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
    // ---END OF CLIENT SIDE CODE---
  }

  private static GameControllerManager manager;
  private static GameControllerDisplay display;
  private static RemoteGamepadServer server;

  @Override
  public void run() {
    while (true) {
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
  }
}
