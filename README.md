# MQTT-Topic-Spam

This program is designed to rapidly publish messages to specified MQTT topics in order to help identify and troubleshoot feedback loops on an MQTT broker.

## Purpose

The tool repeatedly publishes timestamped messages to one or more MQTT topics at a high rate. This can help you detect and analyze feedback loops or excessive message propagation in your MQTT infrastructure.

## Setup

### 1. Configuration

Create a configuration file at `./config/mqtt.cfg`.  
A sample configuration might look like:

```
Protocol = mqtts
Host/IP = your.mqtt.broker.address
Port = 1883
MQTT Topic = your/topic/one
MQTT Topic = your/topic/two
TLS Encryption = false
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

### 4. Run the Publisher

Start the publisher with:

```sh
java -cp .:org.eclipse.paho.client.mqttv3-1.2.5.jar Publisher
```

## Notes

- The program will publish messages as fast as possible to all configured topics.
- Use this tool only in test environments or with permission, as it can generate a high volume of traffic.
