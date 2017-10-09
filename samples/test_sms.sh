#!/bin/sh
export JAVA_HOME=/opt/ibm/ibm-java-x86_64-60
export CLASS_PATH=$(echo lib/*.jar | tr ' ' ':')
SMPP_HOST="smpp_server1,smpp_server1_alternate"
SMPP_PORT="smpp_server1_service_port"
SMPP_USER="smpp_server1_user_name"
SMPP_PWD="smpp_server1_user_password"
SMPP_HOST2="smpp_server2,smpp_server2_alternate"
SMPP_PORT2="smpp_server2_port"
SMPP_USER2="smpp_server2_user_name"
SMPP_PWD2="smpp_server2_user_password"
SMPP_SENDER="smpp_server_number_or_name"
SMPP_RECIPIENTS=$1
SMPP_TEXT=$2
SMPP_MAXSESSIONS=8
SMPP_THROUGHPUT=5
$JAVA_HOME/bin/java -cp $CLASS_PATH com.ibm.smpp.client.utils.SMSClient $SMPP_HOST $SMPP_PORT $SMPP_USER $SMPP_PWD $SMPP_HOST2 $SMPP_PORT2 $SMPP_USER2 $SMPP_PWD2 $SMPP_SENDER "$SMPP_RECIPIENTS" "$SMPP_TEXT" "$SMPP_MAXSESSIONS" "$THROUGHPUT"
