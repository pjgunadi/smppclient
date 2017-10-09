package com.ibm.smpp.client.utils;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.commons.gsm.GsmUtil;
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
//import com.cloudhopper.smpp.tlv.Tlv;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.ArrayList;
//import java.util.ListIterator;
import java.util.Random;
import java.util.Calendar;
import java.lang.Thread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SMSClient
{
  private static final Logger logger = LoggerFactory.getLogger(SMSClient.class);
  private DefaultSmppClient clientBootstrap;
  private String sessionName;

  public SMSClient() {
    sessionName = new String("SMS.Session");
  }

  public SMSClient(String sname) {
    sessionName = new String(sname);
  }

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

  private SmppSession createSession(String sessionname, String host, int port, String username, String password) {
    SmppSession session = null;
    try {
      host = getRoundRobin(host);
      //DefaultSmppClient clientBootstrap = new DefaultSmppClient();
      DefaultSmppSessionHandler sessionHandler = new ClientSmppSessionHandler();
      SmppSessionConfiguration config = new SmppSessionConfiguration();
      config.setWindowSize(1);
      config.setName(sessionname);
      config.setType(SmppBindType.TRANSCEIVER);
      config.setHost(host);
      config.setPort(port);
      config.setConnectTimeout(10000L);
      config.setSystemId(username);
      config.setPassword(password);
      config.getLoggingOptions().setLogBytes(true);
      config.setRequestExpiryTimeout(30000L);
      config.setWindowMonitorInterval(15000L);
      config.setCountersEnabled(true);
      session = clientBootstrap.bind(config, sessionHandler);

      logger.debug("create session a session by having the bootstrap connect");
      logger.debug("synchronous enquireLink call - send it and wait for a response");
      EnquireLinkResp enquireLinkResp1 = session.enquireLink(new EnquireLink(), 10000L);
      logger.debug("enquire_link_resp #1: commandStatus [" + enquireLinkResp1.getCommandStatus() + "=" + enquireLinkResp1.getResultMessage() + "]");

    } catch (Exception e) {
      System.out.println(e.getMessage());
      e.printStackTrace(System.out);
      logger.error("", e);
    }
    return session;
  }

  private SmppSession createSessionHA(String sessionname, String host0, int port0, String username0, String password0, String host1, int port1, String username1, String password1) {
    SmppSession session = null;
    try {
      session = createSession(sessionname, host0, port0, username0, password0);
      if (session == null || !session.isBound()) {
        System.out.println("Unable to Bind to SMSC Host: " + host0 + " Port: " + port0);
        System.out.println("Trying SMSC Host: " + host1 + " Port: " + port1);
        session = createSession(sessionname, host1, port1, username1, password1);
      }
    } catch (Exception e) {
      System.out.println(e.getMessage());
      e.printStackTrace(System.out);
      logger.error("", e);
    }
    return session;
  }

  private void closeSession(ArrayList<SmppSession> sessions) {
    try {
      for (int i=0; i < sessions.size(); i++) {
        sessions.get(i).unbind(5000L);
        if (sessions.get(i) != null) {
          sessions.get(i).destroy();
        }
      }
    } catch (Exception e) {
      System.out.println(e.getMessage());
      e.printStackTrace(System.out);
      logger.error("", e);
    }
  }

  private String[] submitMessage(ArrayList<SmppSession> sessions, String sender, String[] receipts, String text, int tps) {
    String[] results = new String[receipts.length];
    int sidx=-1;
    int snum = sessions.size();
    int tpmsg = (1000/tps)+100;
    long dt = 0;
    ArrayList<Calendar>[] ts = new ArrayList[snum];
    for (int i=0; i < snum; i++) {
      ts[i] = new ArrayList<Calendar>();
    }

    try {
      byte[] textBytes = CharsetUtil.encode(text, CharsetUtil.CHARSET_ISO_8859_15);
      byte[] referenceNumber = new byte[1];

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

        System.out.println("Sending text :" + text + " from " + sender + " to " + receipts[i]);
        int receipt_ton = 0;
        int receipt_npi = 1;
        String receiptnr = receipts[i].toString();
        if (receipts[i].startsWith("+")) {
          receipt_ton = 1;
          receiptnr = receipts[i].substring(1);
        }

        new Random().nextBytes(referenceNumber);
        byte[][] msgs = GsmUtil.createConcatenatedBinaryShortMessages(textBytes, referenceNumber[0]);
        if (msgs == null) {
          SubmitSm submit = new SubmitSm();
          submit.setSourceAddress(new Address(new Integer(sender_ton).byteValue(), new Integer(sender_npi).byteValue(), sender));
          submit.setDestAddress(new Address(new Integer(receipt_ton).byteValue(), new Integer(receipt_npi).byteValue(), receiptnr));
          submit.setShortMessage(textBytes);
          sidx = (sidx+1) % snum;
          Calendar tstart = Calendar.getInstance();
          SubmitSmResp submitResp = sessions.get(sidx).submit(submit, 10000L);
          ts[sidx].add(Calendar.getInstance());
          dt = ts[sidx].get(ts[sidx].size()-1).getTimeInMillis() - tstart.getTimeInMillis();
          if (dt < tpmsg) {
            System.out.format("Sleeping for %d ms\n", tpmsg-dt);
            Thread.sleep(tpmsg-dt);
          }
          results[i] = submitResp.getResultMessage();
          System.out.println("Result :" + results[i]);
          if (ts[sidx].size() >= tps) {
            dt = ts[sidx].get(ts[sidx].size()-1).getTimeInMillis() - ts[sidx].get(0).getTimeInMillis();
            if (dt < 1100) {
              System.out.format("Sleeping for %d ms\n", 1100-dt);
              Thread.sleep(1100-dt);
            }
            ts[sidx].remove(0);
          }
        } else {
          for (int j = 0; j < msgs.length; j++) {
            SubmitSm submit = new SubmitSm();
            submit.setSourceAddress(new Address(new Integer(sender_ton).byteValue(), new Integer(sender_npi).byteValue(), sender));
            submit.setDestAddress(new Address(new Integer(receipt_ton).byteValue(), new Integer(receipt_npi).byteValue(), receiptnr));
            submit.setEsmClass(SmppConstants.ESM_CLASS_UDHI_MASK);
            submit.setShortMessage(msgs[j]);
            sidx = (sidx+1) % snum;
            Calendar tstart = Calendar.getInstance();
            SubmitSmResp submitResp = sessions.get(sidx).submit(submit, 10000L);
            ts[sidx].add(Calendar.getInstance());
            dt = ts[sidx].get(ts[sidx].size()-1).getTimeInMillis() - tstart.getTimeInMillis();
            if (dt < tpmsg) {
              System.out.format("Sleeping for %d ms\n", tpmsg-dt);
              Thread.sleep(tpmsg-dt);
            }
            results[i] = submitResp.getResultMessage();
            System.out.println("Result :" + results[i]);
            if (ts[sidx].size() >= tps) {
              dt = ts[sidx].get(ts[sidx].size()-1).getTimeInMillis() - ts[sidx].get(0).getTimeInMillis();
              if (dt < 1100) {
                System.out.format("Sleeping for %d ms\n", 1100-dt);
                Thread.sleep(1100-dt);
              }
              ts[sidx].remove(0);
            }
          }
        }
      }
    } catch (Exception e) {
      System.out.println(e.getMessage());
      e.printStackTrace(System.out);
      logger.error("", e);
    }
    return results;
  }

  public String[] sendText(String host, int port, String username, String password, String sender, String[] receipts, String text, int smax, int tps) {
    System.out.println("SMPPClient>sendText>start");
    String[] results = new String[receipts.length];

    try {
      clientBootstrap = new DefaultSmppClient();
      int countparts = (int) Math.ceil(receipts.length * text.length()/160.0);
      int numprocessors = Runtime.getRuntime().availableProcessors();
      int snum = countparts > smax ? smax : countparts;
      snum = snum > numprocessors ? numprocessors : snum;
      System.out.format("# Message Parts: %d\n# Processors: %d\n# Sessions: %d\n", countparts, numprocessors, snum);
      ArrayList sessions = new ArrayList<SmppSession>();
      for (int i=0; i < snum; i++) {
        String sname = sessionName + Integer.toString(i);
        SmppSession session = createSession(sname, host, port, username, password);
        if (session != null && session.isBound()) {
          sessions.add(session);
        }
      }
      results = submitMessage(sessions, sender, receipts, text, tps);
      closeSession(sessions);
      logger.info("Shutting down client bootstrap...");
      clientBootstrap.destroy();

    } catch (Exception e) {
      System.out.println(e.getMessage());
      e.printStackTrace(System.out);
      logger.error("", e);
    }
    System.out.println("SMPPClient>sendText>end");
    return results;
  }

  public String[] sendText(String host0, int port0, String username0, String password0, String host1, int port1, String username1, String password1, String sender, String[] receipts, String text, int smax, int tps) {
    System.out.println("SMPPClient>sendText>start");
    String[] results = new String[receipts.length];

    try {
      clientBootstrap = new DefaultSmppClient();
      int countparts = (int) Math.ceil(receipts.length * text.length()/160.0);
      int numprocessors = Runtime.getRuntime().availableProcessors();
      int snum = countparts > smax ? smax : countparts;
      snum = snum > numprocessors ? numprocessors : snum;
      System.out.format("# Message Parts: %d\n# Processors: %d\n# Sessions: %d\n", countparts, numprocessors, snum);
      ArrayList sessions = new ArrayList<SmppSession>();
      for (int i=0; i < snum; i++) {
        String sname = sessionName + Integer.toString(i);
        SmppSession session = createSessionHA(sname, host0, port0, username0, password0, host1, port1, username1, password1);
        if (session != null && session.isBound()) {
          sessions.add(session);
        }
      }
      results = submitMessage(sessions, sender, receipts, text, tps);
      closeSession(sessions);
      logger.info("Shutting down client bootstrap...");
      clientBootstrap.destroy();

    } catch (Exception e) {
      System.out.println(e.getMessage());
      e.printStackTrace(System.out);
      logger.error("", e);
    }
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
    SMSClient smppClient = new SMSClient();

    String host0 = "";
    int port0 = 0;
    String username0 = "";
    String password0 = "";
    String host1 = "";
    int port1 = 0;
    String username1 = "";
    String password1 = "";
    String sender = "";
    int maxsessions = 1;
    int tps = 1;
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
        maxsessions = Integer.parseInt(args[9]);
        tps = Integer.parseInt(args[10]);
        receipts = args[11].split(",");
        text = args[12];

        logger.info("host0 :" + host0);
        logger.info("port0 :" + port0);
        logger.info("username0 :" + username0);
        logger.info("password0 :" + password0);
        logger.info("host1 :" + host1);
        logger.info("port1 :" + port1);
        logger.info("username1 :" + username1);
        logger.info("password1 :" + password1);
        logger.info("sender :" + sender);
        logger.info("maxsessions:" + maxsessions);
        logger.info("Thruput per second:" + tps);
        logger.info("receipts :" + Arrays.toString(receipts));
        logger.info("text :" + text);

        smppClient.sendText(host0, port0, username0, password0, host1, port1, username1, password1, sender, receipts, text, maxsessions, tps);

      } else {
        host0 = args[0];
        port0 = Integer.parseInt(args[1]);
        username0 = args[2];
        password0 = args[3];
        sender = args[4];
        maxsessions = Integer.parseInt(args[5]);
        tps = Integer.parseInt(args[6]);
        receipts = args[7].split(",");
        text = args[8];

        logger.info("host :" + host0);
        logger.info("port :" + port0);
        logger.info("username :" + username0);
        logger.info("password :" + password0);
        logger.info("sender :" + sender);
        logger.info("maxsessions:" + maxsessions);
        logger.info("Thruput per second:" + tps);
        logger.info("receipts :" + Arrays.toString(receipts));
        logger.info("text :" + text);

        smppClient.sendText(host0, port0, username0, password0, sender, receipts, text, maxsessions, tps);
      }
    }

  }
}
