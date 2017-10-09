# SMPPClient
SMPP Client Utility is a tool for sending Short Message (SMS) to SMSC Server via SMPP protocol.
The tool is an implementation of Twitter Cloudhopper. More information about the cloudhopper: https://github.com/twitter/cloudhopper-smpp

Features
--------
1. Round-robin load balance to multiple SMS Servers (Cluster)
2. Secondary SMSC (backup server)
3. Handle more than 160 characters (SMS limit for single message)
4. Multiple sessions and throughput per second parameters
 
Usage Guide
------------
//Create instance:  
SMSClient smsClient = new SMSClient();

//Send SMS to one SMS Server or one group of SMS Cluster:  
smsClient.sendText("smsc_servers_ip_comma_separated", "smsc_server_port", "smsc_username", "smsc_user_password", "sender_number_or_name", "recipient_numbers_comma_separated", "sms_message", "maximum_sessions", "througput_per_second");

//Send SMS with secondary SMSC:  
smsClient.sendText("smsc1_servers_ip_comma_separated", "smsc1_server_port", "smsc1_username", "smsc1_user_password", "smsc2_servers_ip_comma_separated", "smsc2_server_port", "smsc2_username", "smsc2_user_password", "sender_number_or_name", "recipient_numbers_comma_separated", "sms_message", "maximum_sessions", "througput_per_second");


