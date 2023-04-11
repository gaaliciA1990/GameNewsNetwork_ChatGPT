#!/bin/bash

ARTIFACT_FILE="com.gamenews.gnn_chatgpt-all.jar"
ARTIFACT_FOLDER="build/libs"

INSTALLATION_FOLDER="/opt/gnn/"

PROPERTIES_FILE="application.conf"
SYSTEMD_FILE="gnn.service"

echo "Building Server Executable"
sleep 2
./gradlew buildFatJar

echo "Creating $INSTALLATION_FOLDER"
sudo mkdir -p $INSTALLATION_FOLDER

sudo touch $INSTALLATION_FOLDER/$PROPERTIES_FILE

sudo systemctl stop $SYSTEMD_FILE

sudo cp $ARTIFACT_FOLDER/$ARTIFACT_FILE $INSTALLATION_FOLDER/$ARTIFACT_FILE

sudo cp $SYSTEMD_FILE /etc/systemd/system/$SYSTEMD_FILE

sudo systemctl daemon-reload

sudo systemctl restart $SYSTEMD_FILE
