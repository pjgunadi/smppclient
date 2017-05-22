package com.ibm.smpp.client.utils;

import com.cloudhopper.smpp.PduAsyncResponse;
import com.cloudhopper.smpp.impl.DefaultSmppSessionHandler;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.type.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientSmppSessionHandler extends DefaultSmppSessionHandler
{
  private static final Logger logger = LoggerFactory.getLogger(ClientSmppSessionHandler.class);

  public ClientSmppSessionHandler() {
    super(logger);
  }

  public void firePduRequestExpired(PduRequest pduRequest)
  {
    logger.info("PDU request expired: {}", pduRequest);
  }

  public PduResponse firePduRequestReceived(PduRequest pduRequest)
  {
    PduResponse response = pduRequest.createResponse();
    logger.info("PduRequest Received: " + response.getResultMessage());
    //logger.info("SMS Received: {}", pduRequest);
    if (pduRequest.getCommandId() == SmppConstants.CMD_ID_DELIVER_SM) {
        DeliverSm mo = (DeliverSm) pduRequest;
        int length = mo.getShortMessageLength();
        Address source_address = mo.getSourceAddress();
        Address dest_address = mo.getDestAddress();
        byte[] shortMessage = mo.getShortMessage();
        String SMS= new String(shortMessage);
        logger.info(source_address + ", " + dest_address + ", " + SMS);
    }
    return response;
  }

  public void fireExpectedPduResponseReceived(PduAsyncResponse pduAsyncResponse)
  {
    logger.info("PduRequest Received: " + pduAsyncResponse.getResponse().getResultMessage());
    super.fireExpectedPduResponseReceived(pduAsyncResponse);
  }

  public String lookupResultMessage(int commandStatus)
  {
    logger.info("lookupResultMessage: " + commandStatus);
    return super.lookupResultMessage(commandStatus);
  }
}


