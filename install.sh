#!/bin/bash

ARTIFACT_FILE="com.gamenews.gnn_chatgpt-all.jar"
ARTIFACT_FOLDER="build/libs"

INSTALLATION_FOLDER="$HOME/.local/gnn/"

PROPERTIES_FILE="application.conf"
SYSTEMD_FILE="gnn.service"
SYSTEMD_FOLDER="$HOME/.config/systemd/user"

echo "Building Server Executable"
sleep 2
./gradlew buildFatJar

echo "Creating $INSTALLATION_FOLDER"
mkdir -p $INSTALLATION_FOLDER

touch $INSTALLATION_FOLDER/$PROPERTIES_FILE

echo "Stopping server..."
systemctl --user stop $SYSTEMD_FILE

echo "Copying file $ARTIFACT_FOLDER/$ARTIFACT_FILE to $INSTALLATION_FOLDER/$ARTIFACT_FILE"
cp $ARTIFACT_FOLDER/$ARTIFACT_FILE $INSTALLATION_FOLDER/$ARTIFACT_FILE

echo "Installing systemd unit $SYSTEMD_FILE in $SYSTEMD_FOLDER/$SYSTEMD_FILE"
mkdir -p $SYSTEMD_FOLDER
cp -f $SYSTEMD_FILE $SYSTEMD_FOLDER/$SYSTEMD_FILE

echo "Starting server"
systemctl --user daemon-reload
systemctl --user restart $SYSTEMD_FILE
systemctl --user enable $SYSTEMD_FILE
