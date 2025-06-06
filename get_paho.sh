#!/bin/bash
PAHO_VERSION=1.2.5
JAR=org.eclipse.paho.client.mqttv3-$PAHO_VERSION.jar
URL=https://repo.eclipse.org/content/repositories/paho-releases/org/eclipse/paho/org.eclipse.paho.client.mqttv3/$PAHO_VERSION/$JAR

if [ ! -f "$JAR" ]; then
    echo "Downloading $JAR..."
    wget "$URL"
else
    echo "$JAR already exists."
fi