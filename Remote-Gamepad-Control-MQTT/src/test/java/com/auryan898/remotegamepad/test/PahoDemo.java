package com.auryan898.remotegamepad.test;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class PahoDemo implements MqttCallback {

  MqttClient client;

  public PahoDemo() {
  }

  public static void main(String[] args) {
    new PahoDemo().doDemo();
  }

  public void doDemo() {
    try {
      client = new MqttClient("tcp://127.0.0.1:1883", "Sending");
      client.connect();
      client.setCallback(this);
      client.subscribe("player1");
      MqttMessage message = new MqttMessage();
      message.setPayload("A single message from my computer fff"
          .getBytes());
      client.publish("player1", message);
      Thread.sleep(1000);
      client.disconnect();
    } catch (MqttException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void connectionLost(Throwable cause) {
    // TODO Auto-generated method stub

  }

  @Override
  public void messageArrived(String topic, MqttMessage message)
      throws Exception {
    System.out.println(message);
  }

  @Override
  public void deliveryComplete(IMqttDeliveryToken token) {
    // TODO Auto-generated method stub

  }

}