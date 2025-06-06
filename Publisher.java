import org.eclipse.paho.client.mqttv3.*;
import java.util.*;
import java.io.*;

public class Publisher {
    public static void main(String[] args) throws Exception {
        FileHandler.MqttConfig config = FileHandler.readConfig(FileHandler.CONFIG_FILE);

        if (config.topics.isEmpty()) {
            System.out.println("No topics configured. Exiting.");
            return;
        }

        String brokerUrl = (config.tlsEncryption ? "ssl" : "tcp") + "://" + config.host + ":" + config.port;
        String clientId = config.username.isEmpty() ? MqttClient.generateClientId() : config.username + "_pub";
        MqttConnectOptions options = new MqttConnectOptions();
        if (!config.username.isEmpty()) {
            options.setUserName(config.username);
            options.setPassword(config.password.toCharArray());
        }
        if (config.tlsEncryption && config.certRequired) {
            // Set CA certificate if needed (requires additional setup)
            // options.setSocketFactory(SSLSocketFactoryUtil.getSocketFactory("ca.crt"));
        } else if (config.tlsEncryption) {
            // Accept all certs (not secure, for testing only)
            options.setSocketFactory(SSLSocketFactoryUtil.getInsecureSocketFactory());
        }

        MqttClient client = new MqttClient(brokerUrl, clientId);
        client.connect(options);

        System.out.println("Connected to MQTT broker at " + brokerUrl);

        long[] msgCounts = new long[config.topics.size()];
        while (true) {
            for (int i = 0; i < config.topics.size(); ++i) {
                String datetime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
                MqttMessage message = new MqttMessage(datetime.getBytes());
                message.setQos(0);
                client.publish(config.topics.get(i), message);
                msgCounts[i]++;
                System.out.println("Topic: " + config.topics.get(i) + " | Count: " + msgCounts[i]);
            }
            // Remove or reduce the sleep for higher publish rate
            // Thread.sleep(1); // Optional: 1 ms delay for less CPU usage
        }
    }
}