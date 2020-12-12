package com.auryan898.remotegamepad.client;

import java.util.HashMap;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

public class RemoteGamepadClient implements IMqttMessageListener {
  public MqttClient client;

  private String deviceName;
  private String controllerName;
  private HashMap<String, Float> gamepadValues = new HashMap<>();

  public RemoteGamepadClient(String deviceName, String controllerName, String hostUrl) {
    try {
      this.client = new MqttClient(hostUrl, deviceName);
      this.deviceName = deviceName;
      this.controllerName = controllerName;
    } catch (MqttException e) {
      e.printStackTrace();
    }
  }

  public String getDeviceName() {
    return deviceName;
  }

  public String getControllerName() {
    return controllerName;
  }

  public void setControllerName(String controllerName) {
    this.controllerName = controllerName;
  }

  public void start() {
    try {
      if (this.client != null) {
        this.connect();
        this.client.subscribe(controllerName, this);
      }
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

  @Override
  public void messageArrived(String topic, MqttMessage message) throws Exception {
    String[] res = message.toString().split(",");
    String[] keyValue;
    for (int i = 0; i < res.length; i++) {
      keyValue = res[i].split("=");
      if (keyValue.length >= 2) {
        try {
          Float f = Float.valueOf(keyValue[1]);
          if (keyValue[0] != null && !keyValue[0].equals("null") && f != null)
            gamepadValues.put(keyValue[0], f);
        } catch (NumberFormatException e) {
          gamepadValues.put(keyValue[0], 0f);
        }
      }
    }
  }
  
  public void sendMessage(String topic, String data) throws MqttPersistenceException, MqttException {
    if (this.client != null && this.client.isConnected()) {
      MqttMessage message = new MqttMessage();
      message.setPayload(data.getBytes());
      client.publish(topic, message);
    }
  }

  public HashMap<String, Float> getGamepadValues() {
    return gamepadValues;
  }

  public String toString() {
    return this.controllerName + " : " + gamepadValues.toString();
  }
}
