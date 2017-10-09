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
smsClient.sendText(String smsc_servers_ip_comma_separated, int smsc_server_port, String smsc_username, String smsc_user_password, String sender_number_or_name, String[] recipient_numbers, String sms_message, int maximum_sessions, int througput_per_second);

//Send SMS with secondary SMSC:  
smsClient.sendText(String smsc1_servers_ip_comma_separated, int smsc1_server_port, String smsc1_username, String smsc1_user_password, String smsc2_servers_ip_comma_separated, int smsc2_server_port, String smsc2_username, String smsc2_user_password, String sender_number_or_name, String[] recipient_numbers, String sms_message, int maximum_sessions, int througput_per_second);
