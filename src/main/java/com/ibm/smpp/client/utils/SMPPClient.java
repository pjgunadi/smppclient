package com.ibm.smpp.client.utils;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.smpp.SmppBindType;
import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.impl.DefaultSmppClient;
import com.cloudhopper.smpp.impl.DefaultSmppSessionHandler;
import com.cloudhopper.smpp.pdu.EnquireLink;
import com.cloudhopper.smpp.pdu.EnquireLinkResp;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.pdu.SubmitSmResp;
import com.cloudhopper.smpp.type.Address;
import com.cloudhopper.smpp.type.LoggingOptions;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.tlv.Tlv;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SMPPClient
{
  private static final Logger logger = LoggerFactory.getLogger(SMPPClient.class);

  private String getRoundRobin(String instr) {
    String[] tmplist = {""};
    String outstr = "";
    int i = 0;
    try {
      tmplist = instr.split(",");
      i = new Random().nextInt(tmplist.length);
      outstr = tmplist[i].toString();
    } catch (Exception e) {
      System.out.println(e.getMessage());
      e.printStackTrace(System.out);
      logger.error("", e);
    } 
    return outstr;
  }

  public String[] sendText(String host, int port, String username, String password, String sender, String[] receipts, String text) {
    System.out.println("SMPPClient>sendText>start");
    String[] results = new String[receipts.length];

    SmppSession session0 = null;
    DefaultSmppClient clientBootstrap = null;
    try
    {
      host = getRoundRobin(host);
      clientBootstrap = new DefaultSmppClient();
      DefaultSmppSessionHandler sessionHandler = new ClientSmppSessionHandler();
      SmppSessionConfiguration config0 = new SmppSessionConfiguration();
      config0.setWindowSize(1);
      config0.setName("SMS.Session.0");
      config0.setType(SmppBindType.TRANSCEIVER);
      config0.setHost(host);
      config0.setPort(port);

      config0.setConnectTimeout(10000L);
      config0.setSystemId(username);

      config0.setPassword(password);

      config0.getLoggingOptions().setLogBytes(true);
      config0.setRequestExpiryTimeout(30000L);
      config0.setWindowMonitorInterval(15000L);
      config0.setCountersEnabled(true);

      logger.debug("create session a session by having the bootstrap connect");

      System.out.println("host :" + host);
      System.out.println("port :" + port);
      System.out.println("username :" + username);
      System.out.println("password :" + password);
      System.out.println("sender :" + sender);
      System.out.println("receipts :" + Arrays.toString(receipts));
      System.out.println("text :" + text);

      session0 = clientBootstrap.bind(config0, sessionHandler);

      logger.debug("synchronous enquireLink call - send it and wait for a response");
      EnquireLinkResp enquireLinkResp1 = session0.enquireLink(new EnquireLink(), 10000L);
      logger.debug("enquire_link_resp #1: commandStatus [" + enquireLinkResp1.getCommandStatus() + "=" + enquireLinkResp1.getResultMessage() + "]");

      byte[] textBytes = CharsetUtil.encode(text, CharsetUtil.CHARSET_GSM);

      System.out.println(Arrays.toString(receipts));

      int sender_ton = 0;
      int sender_npi = 1;
      boolean balphanum;
      if (sender.startsWith("+")) {
        sender_ton = 1;
        sender = sender.substring(1);
      }
      if (isInteger(sender)) {
        if (sender.length() <= 8) {
          sender_ton = 3;
          sender_npi = 0;
        }
      } else {
        sender_ton = 5;
        sender_npi = 0;
      }
      
      for (int i = 0; i < receipts.length; i++) {
        SubmitSm submit0 = new SubmitSm();

        System.out.println("Sending text :" + text + " from " + sender + " to " + receipts[i]);
        int receipt_ton = 0;
        int receipt_npi = 1;
        String receiptnr = receipts[i].toString();
        if (receipts[i].startsWith("+")) {
          receipt_ton = 1;
          receiptnr = receipts[i].substring(1);
        }
	submit0.setSourceAddress(new Address(new Integer(sender_ton).byteValue(), new Integer(sender_npi).byteValue(), sender));
        submit0.setDestAddress(new Address(new Integer(receipt_ton).byteValue(), new Integer(receipt_npi).byteValue(), receiptnr));
        if (textBytes.length > 255) {
          submit0.setShortMessage(new byte[0]);
          Tlv tlv = new Tlv(SmppConstants.TAG_MESSAGE_PAYLOAD, textBytes, "message_payload");
          submit0.addOptionalParameter(tlv);
        } else {
          submit0.setShortMessage(textBytes);
        }
        SubmitSmResp submitResp = session0.submit(submit0, 10000L);
        System.out.println("Result :" + submitResp.getResultMessage());
        results[i] = submitResp.getResultMessage();
      }

      session0.unbind(5000L);
    } catch (Exception e) {
      System.out.println(e.getMessage());
      e.printStackTrace(System.out);
      logger.error("", e);
    }

    if (session0 != null)
    {
      session0.destroy();
    }

    if (clientBootstrap != null) clientBootstrap.destroy();

    System.out.println("SMPPClient>sendText>end");
    return results;
  }

  public String[] sendText(String host0, int port0, String username0, String password0, String host1, int port1, String username1, String password1, String sender, String[] receipts, String text) {
    System.out.println("SMPPClient>sendText>start");
    String[] results = new String[receipts.length];

    SmppSession session0 = null;
    DefaultSmppClient clientBootstrap = null;
    try
    {
      host0 = getRoundRobin(host0);
      host1 = getRoundRobin(host1);
      clientBootstrap = new DefaultSmppClient();
      DefaultSmppSessionHandler sessionHandler = new ClientSmppSessionHandler();
      SmppSessionConfiguration config0 = new SmppSessionConfiguration();
      SmppSessionConfiguration config1 = new SmppSessionConfiguration();
      config0.setWindowSize(1);
      config0.setName("SMS.Session.0");
      config0.setType(SmppBindType.TRANSCEIVER);
      config0.setHost(host0);
      config0.setPort(port0);
      config0.setConnectTimeout(10000L);
      config0.setSystemId(username0);
      config0.setPassword(password0);
      config0.getLoggingOptions().setLogBytes(true);
      config0.setRequestExpiryTimeout(30000L);
      config0.setWindowMonitorInterval(15000L);
      config0.setCountersEnabled(true);

      config1.setWindowSize(1);
      config1.setName("SMS.Session.1");
      config1.setType(SmppBindType.TRANSCEIVER);
      config1.setHost(host1);
      config1.setPort(port1);
      config1.setConnectTimeout(10000L);
      config1.setSystemId(username1);
      config1.setPassword(password1);
      config1.getLoggingOptions().setLogBytes(true);
      config1.setRequestExpiryTimeout(30000L);
      config1.setWindowMonitorInterval(15000L);
      config1.setCountersEnabled(true);

      logger.debug("create session a session by having the bootstrap connect");

      System.out.println("host0 :" + host0);
      System.out.println("port0 :" + port0);
      System.out.println("username0 :" + username0);
      System.out.println("password0 :" + password0);
      System.out.println("host1 :" + host1);
      System.out.println("port1 :" + port1);
      System.out.println("username1 :" + username1);
      System.out.println("password1 :" + password1);
      System.out.println("sender :" + sender);
      System.out.println("receipts :" + Arrays.toString(receipts));
      System.out.println("text :" + text);

      session0 = clientBootstrap.bind(config0, sessionHandler);
      if (session0 == null || !session0.isBound()) {
        System.out.println("Unable to Bind to SMSC Host: " + host0 + " Port: " + port0);
        System.out.println("Trying SMSC Host: " + host1 + " Port: " + port1);
        clientBootstrap = new DefaultSmppClient();
        sessionHandler = new ClientSmppSessionHandler();
        session0 = clientBootstrap.bind(config1, sessionHandler);
      }

      logger.debug("synchronous enquireLink call - send it and wait for a response");
      EnquireLinkResp enquireLinkResp1 = session0.enquireLink(new EnquireLink(), 10000L);
      logger.debug("enquire_link_resp #1: commandStatus [" + enquireLinkResp1.getCommandStatus() + "=" + enquireLinkResp1.getResultMessage() + "]");

      byte[] textBytes = CharsetUtil.encode(text, CharsetUtil.CHARSET_GSM);

      System.out.println(Arrays.toString(receipts));

      int sender_ton = 0;
      int sender_npi = 1;
      boolean balphanum;
      if (sender.startsWith("+")) {
        sender_ton = 1;
        sender = sender.substring(1);
      }
      if (isInteger(sender)) {
        if (sender.length() <= 8) {
          sender_ton = 3;
          sender_npi = 0;
        }
      } else {
        sender_ton = 5;
        sender_npi = 0;
      }
      
      for (int i = 0; i < receipts.length; i++) {
        SubmitSm submit0 = new SubmitSm();

        System.out.println("Sending text :" + text + " from " + sender + " to " + receipts[i]);
        int receipt_ton = 0;
        int receipt_npi = 1;
        String receiptnr = receipts[i].toString();
        if (receipts[i].startsWith("+")) {
          receipt_ton = 1;
          receiptnr = receipts[i].substring(1);
        }
	submit0.setSourceAddress(new Address(new Integer(sender_ton).byteValue(), new Integer(sender_npi).byteValue(), sender));
        submit0.setDestAddress(new Address(new Integer(receipt_ton).byteValue(), new Integer(receipt_npi).byteValue(), receiptnr));
        if (textBytes.length > 255) {
          submit0.setShortMessage(new byte[0]);
          Tlv tlv = new Tlv(SmppConstants.TAG_MESSAGE_PAYLOAD, textBytes, "message_payload");
          submit0.addOptionalParameter(tlv);
        } else {
          submit0.setShortMessage(textBytes);
        }
        SubmitSmResp submitResp = session0.submit(submit0, 10000L);
        System.out.println("Result :" + submitResp.getResultMessage());
        results[i] = submitResp.getResultMessage();
      }

      session0.unbind(5000L);
    } catch (Exception e) {
      System.out.println(e.getMessage());
      e.printStackTrace(System.out);
      logger.error("", e);
    }

    if (session0 != null)
    {
      session0.destroy();
    }

    if (clientBootstrap != null) clientBootstrap.destroy();

    System.out.println("SMPPClient>sendText>end");
    return results;
  }

  public static boolean isInteger(String s) {
    try { 
        Integer.parseInt(s); 
    } catch(NumberFormatException e) { 
        return false; 
    } catch(NullPointerException e) {
        return false;
    }
    return true;
  }

  public static void main(String[] args) throws Exception
  {
    SMPPClient smppClient = new SMPPClient();

    String host0 = "";
    int port0 = 0;
    String username0 = "";
    String password0 = "";
    String host1 = "";
    int port1 = 0;
    String username1 = "";
    String password1 = "";
    String sender = "";
    String[] receipts = {""};
    String text = "";

    for (int i=0; i < args.length; i++) {
      logger.info("Argument " + i + ": " + args[i]);
    }

    if (args.length > 0) {
      if (args.length > 7) {
        host0 = args[0];
        port0 = Integer.parseInt(args[1]);
        username0 = args[2];
        password0 = args[3];
        host1 = args[4];
        port1 = Integer.parseInt(args[5]);
        username1 = args[6];
        password1 = args[7];
        sender = args[8];
        receipts = args[9].split(",");
        text = args[10];        

        logger.info("host0 :" + host0);
        logger.info("port0 :" + port0);
        logger.info("username0 :" + username0);
        logger.info("password0 :" + password0);
        logger.info("host1 :" + host1);
        logger.info("port1 :" + port1);
        logger.info("username1 :" + username1);
        logger.info("password1 :" + password1);
        logger.info("sender :" + sender);
        logger.info("receipts :" + Arrays.toString(receipts));
        logger.info("text :" + text);

        smppClient.sendText(host0, port0, username0, password0, host1, port1, username1, password1, sender, receipts, text);

      } else {
        host0 = args[0];
        port0 = Integer.parseInt(args[1]);
        username0 = args[2];
        password0 = args[3];
        sender = args[4];
        receipts = args[5].split(",");
        text = args[6];

        logger.info("host :" + host0);
        logger.info("port :" + port0);
        logger.info("username :" + username0);
        logger.info("password :" + password0);
        logger.info("sender :" + sender);
        logger.info("receipts :" + Arrays.toString(receipts));
        logger.info("text :" + text);

        smppClient.sendText(host0, port0, username0, password0, sender, receipts, text);
      }
    }

  }
}
