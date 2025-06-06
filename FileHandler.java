import java.io.*;
import java.util.*;

public class FileHandler {
    public static final String CONFIG_FILE = "./config/mqtt.cfg";
    public static final int DEFAULT_MAX_TOPICS = 32;
    public static final int DEFAULT_MAX_TOPIC_LENGTH = 128;
    public static final int DEFAULT_PORT = 1883;

    public static class MqttConfig {
        public String protocol = "mqtt";
        public String host = "127.0.0.1";
        public int port = DEFAULT_PORT;
        public List<String> topics = new ArrayList<>();
        public boolean tlsEncryption = false;
        public boolean certRequired = false;
        public String username = "";
        public String password = "";
        public int maxTopics = DEFAULT_MAX_TOPICS;
        public int maxTopicLength = DEFAULT_MAX_TOPIC_LENGTH;
    }

    public static MqttConfig readConfig(String filepath) throws IOException {
        File file = new File(filepath);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            MqttConfig config = defaultConfig();
            writeConfig(filepath, config);
            return config;
        }

        MqttConfig config = defaultConfig();
        config.topics.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("//")) continue;
                String[] parts = line.split("=", 2);
                if (parts.length != 2) continue;
                String key = parts[0].trim();
                String value = parts[1].trim();

                switch (key) {
                    case "Protocol": config.protocol = value; break;
                    case "Host/IP": config.host = value; break;
                    case "Port": config.port = Integer.parseInt(value); break;
                    case "MQTT Topic":
                        if (config.topics.size() < config.maxTopics)
                            config.topics.add(value);
                        break;
                    case "TLS Encryption": config.tlsEncryption = value.equalsIgnoreCase("true"); break;
                    case "Cert Required": config.certRequired = value.equalsIgnoreCase("true"); break;
                    case "Username": config.username = value; break;
                    case "Password": config.password = value; break;
                    case "Max Topics": config.maxTopics = Integer.parseInt(value); break;
                    case "Max Topic Length": config.maxTopicLength = Integer.parseInt(value); break;
                }
            }
        }
        return config;
    }

    public static void writeConfig(String filepath, MqttConfig config) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filepath))) {
            pw.println("Protocol = " + config.protocol);
            pw.println("Host/IP = " + config.host);
            pw.println("Port = " + config.port);
            for (String topic : config.topics) {
                pw.println("MQTT Topic = " + topic);
            }
            pw.println("TLS Encryption = " + config.tlsEncryption);
            pw.println("Cert Required = " + config.certRequired);
            pw.println("Username = " + config.username);
            pw.println("Password = " + config.password);
            pw.println("Max Topics = " + config.maxTopics);
            pw.println("Max Topic Length = " + config.maxTopicLength);
        }
    }

    public static MqttConfig defaultConfig() {
        MqttConfig config = new MqttConfig();
        config.topics.add("test/topic");
        return config;
    }
}