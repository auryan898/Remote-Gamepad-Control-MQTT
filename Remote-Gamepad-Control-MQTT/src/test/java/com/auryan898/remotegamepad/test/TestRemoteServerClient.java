package com.auryan898.remotegamepad.test;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.auryan898.ev3gamepad.GameControllerDisplay;
import com.auryan898.ev3gamepad.GameControllerManager;
import com.auryan898.ev3gamepad.keymapping.BaseGamepadKeyMapper;
import com.auryan898.remotegamepad.client.RemoteGamepadClient;
import com.auryan898.remotegamepad.server.RemoteGamepadServer;

public class TestRemoteServerClient {
  public static void main(String[] args) throws MqttException {
    GameControllerManager manager = GameControllerManager.getInstance();
    GameControllerDisplay display = new GameControllerDisplay();
    RemoteGamepadServer server = new RemoteGamepadServer("tcp://127.0.0.1:1883");
    RemoteGamepadClient client = new RemoteGamepadClient("ryan's pc", "player1",
        "tcp://127.0.0.1:1883");

    display.init();
    display.addController(manager.getPhysicalController(1));
    display.addController(manager.getPhysicalController(0));
    display.addController(
        manager.getAssignableController(new BaseGamepadKeyMapper(), "start", "a_button"));
    server.addController("player1",
        manager.getAssignableController(new BaseGamepadKeyMapper(), "start", "a_button"));
    client.start();

    // server.start();
    server.connect();
    while (true) {
      try {
        manager.update();
        display.update();
        server.publishAllControllerData();
        System.out.println(client.toString());
        Thread.sleep(100);
      } catch (InterruptedException e) {
        server.disconnect();
        client.disconnect();
      }
    }

  }
}
