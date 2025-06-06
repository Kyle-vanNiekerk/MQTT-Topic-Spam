# MQTT-Topic-Spam

This program is designed to rapidly publish messages to specified MQTT topics in order to help identify and troubleshoot feedback loops on an MQTT broker.

## Purpose

The tool repeatedly publishes timestamped messages to one or more MQTT topics at a high rate. This can help you detect and analyze feedback loops or excessive message propagation in your MQTT infrastructure.

## How It Works

- The **publisher** rapidly sends messages to all configured topics for 5 seconds.
- At the same time, a **subscriber** connects to the same topics and monitors incoming messages.
- After the publisher stops, the subscriber continues to listen. If any topic continues to receive messages, the program will report a possible feedback loop for that topic.

## Setup

### 1. Configuration

Create a configuration file at `./config/mqtt.cfg`.  
A sample configuration might look like:

```
Protocol = mqtts
Host/IP = your.mqtt.broker.address
Port = 8883
MQTT Topic = your/topic/one
MQTT Topic = your/topic/two
TLS Encryption = true
Cert Required = false
Username = your_username
Password = your_password
Max Topics = 32
Max Topic Length = 128
```

- **Protocol**: `mqtt` for plain or `mqtts` for TLS.
- **Host/IP**: The MQTT broker address.
- **Port**: Broker port (e.g., 1883 for MQTT, 8883 for MQTT over TLS).
- **MQTT Topic**: Add one line per topic you want to spam.
- **TLS Encryption**: `true` or `false`.
- **Cert Required**: `true` if a CA certificate is required, otherwise `false`.
- **Username/Password**: (Optional) Broker credentials.
- **Max Topics/Max Topic Length**: Limits for topic handling.

### 2. Download Dependencies

Run the provided script to download the Eclipse Paho MQTT Java client library:

```sh
bash get_paho.sh
```

This will download the required `org.eclipse.paho.client.mqttv3-1.2.5.jar` file if it is not already present.

### 3. Compile the Program

Compile all Java files with:

```sh
bash compile.sh
```

### 4. Run the Publisher and Subscriber

Start the program with:

```sh
java -cp .:org.eclipse.paho.client.mqttv3-1.2.5.jar Publisher
```

- The publisher will send messages for 5 seconds, then stop.
- The subscriber will continue running and report any topics that are still being updated, indicating a possible feedback loop.

## Notes

- The program will publish messages as fast as possible to all configured topics.
- Use this tool only in test environments or with permission, as it can generate a high volume of traffic.
- The subscriber will continue running after the publisher stops, so you can monitor for feedback loops.