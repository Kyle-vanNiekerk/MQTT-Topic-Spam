import org.eclipse.paho.client.mqttv3.*;
import java.util.*;
import java.io.*;

public class Spam {
    private static volatile boolean publishing = true;

    public static void main(String[] args) throws Exception {
        FileHandler.MqttConfig config = FileHandler.readConfig(FileHandler.CONFIG_FILE);

        if (config.topics.isEmpty()) {
            System.out.println("No topics configured. Exiting.");
            return;
        }

        // Start subscriber in a separate thread
        Subscriber subscriber = new Subscriber(config);
        Thread subThread = new Thread(subscriber);
        subThread.start();

        // Start publisher in main thread
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
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < 5000) { // Run for 5 seconds
            for (int i = 0; i < config.topics.size(); ++i) {
                String datetime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
                MqttMessage message = new MqttMessage(datetime.getBytes());
                message.setQos(0);
                client.publish(config.topics.get(i), message);
                msgCounts[i]++;
                System.out.println("Topic: " + config.topics.get(i) + " | Count: " + msgCounts[i]);
            }
            // No sleep for maximum speed
        }
        publishing = false;
        client.disconnect();
        client.close();
        System.out.println("Publisher finished. Subscriber will continue to monitor topics for feedback loops.");
        subThread.join(); // Optionally wait for subscriber (or remove if you want it to run indefinitely)
    }

    // Subscriber as an inner class
    static class Subscriber implements Runnable, MqttCallback {
        private final FileHandler.MqttConfig config;
        private MqttClient client;
        private final Map<String, Long> lastUpdate = new HashMap<>();
        private final Map<String, Integer> msgCount = new HashMap<>();

        public Subscriber(FileHandler.MqttConfig config) {
            this.config = config;
        }

        @Override
        public void run() {
            try {
                String brokerUrl = (config.tlsEncryption ? "ssl" : "tcp") + "://" + config.host + ":" + config.port;
                String clientId = (config.username.isEmpty() ? MqttClient.generateClientId() : config.username + "_sub") + "_sub";
                MqttConnectOptions options = new MqttConnectOptions();
                if (!config.username.isEmpty()) {
                    options.setUserName(config.username);
                    options.setPassword(config.password.toCharArray());
                }
                if (config.tlsEncryption && config.certRequired) {
                    // options.setSocketFactory(SSLSocketFactoryUtil.getSocketFactory("ca.crt"));
                } else if (config.tlsEncryption) {
                    options.setSocketFactory(SSLSocketFactoryUtil.getInsecureSocketFactory());
                }

                client = new MqttClient(brokerUrl, clientId);
                client.setCallback(this);
                client.connect(options);

                for (String topic : config.topics) {
                    client.subscribe(topic, 0);
                    lastUpdate.put(topic, 0L);
                    msgCount.put(topic, 0);
                }

                System.out.println("Subscriber connected and listening for messages...");

                // Monitor for feedback loop after publisher stops
                long feedbackCheckStart = 0;
                boolean feedbackCheckStarted = false;
                while (true) {
                    if (!publishing && !feedbackCheckStarted) {
                        feedbackCheckStart = System.currentTimeMillis();
                        feedbackCheckStarted = true;
                        System.out.println("Publisher stopped. Monitoring for feedback loops...");
                    }
                    if (feedbackCheckStarted && System.currentTimeMillis() - feedbackCheckStart > 5000) {
                        // After 5 seconds of publisher stopping, check for updates
                        for (String topic : config.topics) {
                            long last = lastUpdate.getOrDefault(topic, 0L);
                            if (last > feedbackCheckStart) {
                                System.out.println("FEEDBACK LOOP DETECTED: Topic '" + topic + "' is still being updated!");
                            } else {
                                System.out.println("No feedback detected for topic: " + topic);
                            }
                        }
                        System.out.println("Subscriber will now continue running. Press Ctrl+C to exit.");
                        feedbackCheckStarted = false; // Only report once
                    }
                    Thread.sleep(500);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void connectionLost(Throwable cause) {
            System.out.println("Subscriber connection lost: " + cause.getMessage());
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            lastUpdate.put(topic, System.currentTimeMillis());
            msgCount.put(topic, msgCount.getOrDefault(topic, 0) + 1);
            System.out.println("Subscriber received on " + topic + ": " + new String(message.getPayload()));
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            // Not used for subscriber
        }
    }
}