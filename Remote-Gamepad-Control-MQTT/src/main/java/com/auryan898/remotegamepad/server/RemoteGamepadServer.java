package com.auryan898.remotegamepad.server;

import java.util.HashMap;
import java.util.Map.Entry;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import com.auryan898.ev3gamepad.GameController;
import com.auryan898.ev3gamepad.GameControllerManager;
import java.util.List;

public class RemoteGamepadServer {
  protected static long UPDATE_INTERVAL = 0;

  protected HashMap<String, GameController> controllers = new HashMap<>();
  public MqttClient client;

  public RemoteGamepadServer(String hostUrl) {
    this(hostUrl, "PC");
  }

  public RemoteGamepadServer(String hostUrl, String deviceName) {
    try {
      this.client = new MqttClient(hostUrl, deviceName);
    } catch (MqttException e) {
      e.printStackTrace();
    }
  }

  public boolean connect() throws MqttSecurityException, MqttException {
    if (client.isConnected())
      return false;
    MqttConnectOptions options = new MqttConnectOptions();
    options.setCleanSession(true);
    client.connect(options);
    return true;
  }

  public boolean disconnect() throws MqttException {
    if (!client.isConnected())
      return false;
    this.client.disconnect();
    return true;
  }

  public void start() throws MqttException {
    GameControllerManager manager = GameControllerManager.getInstance();
    try {
      this.connect();
      while (true) {
        try {
          manager.update();
          this.publishAllControllerData();
          Thread.sleep(UPDATE_INTERVAL);
        } catch (InterruptedException e) {

        }
      }

    } catch (MqttException e) {
      e.printStackTrace();
    } finally {
      if (client != null && client.isConnected())
        client.disconnect();
    }
  }

  public void addController(String name, GameController controller) {
    controllers.put(name, controller);
  }

  public void publishAllControllerData() throws MqttPersistenceException, MqttException {
    for (Entry<String, GameController> cont : controllers.entrySet()) {
      this.publishControllerData(cont.getKey(), cont.getValue());
    }
  }

  protected void publishControllerData(String key, GameController controller)
      throws MqttPersistenceException, MqttException {
    if (this.client != null && this.client.isConnected()) {
      MqttMessage message = new MqttMessage();
      message.setPayload(this.convertToString(controller).getBytes());
      client.publish(key, message);
    }
  }

  protected String convertToString(GameController controller) {
    String res = "";
    List<String> signature = controller.getMapper().getAllNamedKeys();
    int i = 0;
    for (String key : signature) {
      res += key + "=" + controller.getValue(key);
      if (i < signature.size() - 1) {
        res += ",";
      }
      i++;
    }
    return res;
  }
}
