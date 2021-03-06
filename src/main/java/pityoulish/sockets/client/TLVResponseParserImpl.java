/*
 * This work is released into the Public Domain under the
 * terms of the Creative Commons CC0 1.0 Universal license.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */
package pityoulish.sockets.client;

import java.nio.ByteBuffer;

import pityoulish.outtake.Missing;
import pityoulish.sockets.tlv.MsgBoardTLV;
import pityoulish.sockets.tlv.MsgBoardType;


/**
 * Implementation of {@link ResponseParser} for the Binary Protocol.
 */
public class TLVResponseParserImpl implements ResponseParser
{

  /** Indicates whether to print the TLVs. */
  protected boolean beVerbose;


  /**
   * Creates a new response parser with the given verbosity.
   *
   * @param verbose   <code>true</code> to print TLVs to System.out,
   *                  <code>false</code> to remain silent
   */
  public TLVResponseParserImpl(boolean verbose)
  {
    beVerbose = verbose;
  }

  /** Creates a silent response parser. */
  public TLVResponseParserImpl()
  {
    this(false);
  }


  // non-javadoc, see interface
  public void parse(ByteBuffer response, Visitor visitor)
    throws Exception
  {
    if (response == null)
       throw new NullPointerException("ByteBuffer");
    if (visitor == null)
       throw new NullPointerException("ResponseParser.Visitor");

    MsgBoardTLV tlv = getResponseTLV(response);

    if (beVerbose)
       System.out.println(tlv.toFullString());

    switch (tlv.getType())
     {
      case INFO_RESPONSE: {
        String text =
          parseNestedStringValue(tlv, MsgBoardType.TEXT, null);
        visitor.visitInfo(text);
      } break;

      case ERROR_RESPONSE: {
        String text =
          parseNestedStringValue(tlv, MsgBoardType.TEXT, null);
        visitor.visitError(text);
      } break;

      case MESSAGE_BATCH: {
        parseMessageBatch(tlv, visitor);
      } break;

      case TICKET_GRANT: {
        String ticket = null;

        // PYL:keep
        Missing.here("parse the Ticket Grant response");
        // Refer to the specification of the Binary Protocol for details.
        // See the other cases on how to parse a single value from the TLV.
        // Watch out for the correct encoding of the ticket.
        // PYL:cut
        ticket = parseNestedStringValue(tlv, MsgBoardType.TICKET, "US-ASCII");
        // PYL:end

        visitor.visitTicketGrant(ticket);
      } break;

      default:
        throw new UnsupportedOperationException
          (Catalog.INVALID_TOP_TLV_TYPE_1.format(tlv.getType()));
     }
  } // parse


  /**
   * Creates the top-level TLV and performs some validations.
   *
   * @param response  buffer holding the response to parse.
   *                  The buffer must be backed by an array.
   *
   * @return    the constructed TLV representing the response,
   *            never <code>null</code>
   *
   * @throws Exception  in case of invalid response data
   */
  protected MsgBoardTLV getResponseTLV(ByteBuffer response)
    throws Exception
  {
    // need at least 4 bytes for a valid MsgBoardTLV
    if (response.limit() < 4)
       throw new Exception("response.limit="+response.limit());

    MsgBoardTLV result = null;

    try {
      result = new MsgBoardTLV
        (response.array(), response.position()+response.arrayOffset());

    } catch (RuntimeException rtx) {
      throw new Exception
        (Catalog.INVALID_TOP_TLV_HEADER_0.format());
    }

    if (result.getEnd() > response.limit() + response.arrayOffset())
       throw new Exception
         (Catalog.INVALID_TOP_TLV_LENGTH_0.format());

    // We could check for a valid type here. But the caller is going to
    // switch on the type anyway, so it's simpler to check it there.

    return result;
  }


  /**
   * Parses a TLV with a single string value.
   *
   * @param parent  the TLV of which to parse the content
   * @param expect  the expected type of the contained TLV
   * @param enc     the expected encoding, for example "US-ASCII",
   *                or <code>null</code> for "UTF-8"
   *
   * @return    the string value of the contained TLV
   *
   * @throws Exception in case of a problem
   */
  protected String parseNestedStringValue(MsgBoardTLV  parent,
                                          MsgBoardType expect,
                                          String enc)
    throws Exception
  {
    MsgBoardTLV nested = parent.getNestedTLV();
    if (nested == null)
       throw new Exception
         (Catalog.MISSING_NESTED_TLV_3.format(parent.getType(),
                                              parent.getStart(),
                                              expect));
    if (nested.getType() != expect)
       throw new Exception
         (Catalog.UNEXPECTED_TLV_3.format(nested.getType(),
                                          nested.getStart(),
                                          expect));
    if (nested.getEnd() > parent.getEnd())
       throw new Exception
         (Catalog.OVERLONG_TLV_2.format(nested.getType(),
                                        nested.getStart()));

    return parseStringValue(nested, enc);
  }


  /**
   * Parses the string value of a TLV.
   *
   * @param tlv   the TLV with expected string value
   * @param enc   the expected encoding, for example "US-ASCII",
   *              or <code>null</code> for "UTF-8"
   *
   * @return the string value, or an empty string if the value is empty
   *
   * @throws Exception in case of a problem
   */
  protected String parseStringValue(MsgBoardTLV tlv, String enc)
    throws Exception
  {
    if (tlv.getLength() < 1)
       return "";

    if (enc == null)
       enc = "UTF-8";

    return new String(tlv.getData(), tlv.getValueStart(),
                      tlv.getLength(), enc);
  }


  /**
   * Parses a message batch and calls the visitor.
   *
   * @param mbtlv       the TLV with the message batch
   * @param visitor     the visitor to invoke on the message batch elements
   *
   * @throws Exception in case of a problem
   */
  protected void parseMessageBatch(MsgBoardTLV mbtlv, Visitor visitor)
    throws Exception
  {
    // The binary protocol specifies an order for the nested TLVs.
    // The Marker and an optional Missed indicator appear before the messages.

    MsgBoardTLV nested = mbtlv.getNestedTLV();
    if (nested == null)
       throw new Exception
         (Catalog.MISSING_NESTED_TLV_3.format(mbtlv.getType(),
                                              mbtlv.getStart(),
                                              MsgBoardType.MARKER));
    if (nested.getType() != MsgBoardType.MARKER)
       throw new Exception
         (Catalog.UNEXPECTED_TLV_3.format(nested.getType(),
                                          nested.getStart(),
                                          MsgBoardType.MARKER));
    if (nested.getEnd() > mbtlv.getEnd())
       throw new Exception
         (Catalog.OVERLONG_TLV_2.format(nested.getType(),
                                        nested.getStart()));
    String marker = parseStringValue(nested, "US-ASCII");

    boolean missed = false;
    nested = nested.getNextTLV(mbtlv.getEnd());
    if ((nested != null) && (nested.getType() == MsgBoardType.MISSED))
     {
       missed = true;
       nested = nested.getNextTLV(mbtlv.getEnd());
     }

    visitor.enterMessageBatch(marker, missed);

    while (nested != null)
     {
       if (nested.getType() != MsgBoardType.MESSAGE)
          throw new Exception
            (Catalog.UNEXPECTED_TLV_3.format(nested.getType(),
                                             nested.getStart(),
                                             MsgBoardType.MESSAGE));
       if (nested.getEnd() > mbtlv.getEnd())
          throw new Exception
            (Catalog.OVERLONG_TLV_2.format(nested.getType(),
                                           nested.getStart()));

       parseMessage(nested, visitor);
       nested = nested.getNextTLV(mbtlv.getEnd());
     }

    visitor.leaveMessageBatch();
  }


  /**
   * Parses a message and calls the visitor.
   *
   * @param msgtlv      the TLV with the message
   * @param visitor     the visitor to invoke on the message
   *
   * @throws Exception in case of a problem
   */
  protected void parseMessage(MsgBoardTLV msgtlv, Visitor visitor)
    throws Exception
  {
    // The order of elements within the message is not specified.
    // Parse them as they come, then check if something is missing.
    String originator = null;
    String timestamp = null;
    String text = null;

    MsgBoardTLV nested = msgtlv.getNestedTLV();
    while (nested != null)
     {
       if (nested.getEnd() > msgtlv.getEnd())
          throw new Exception
            (Catalog.OVERLONG_TLV_2.format(nested.getType(),
                                           nested.getStart()));

       switch (nested.getType())
        {
         case ORIGINATOR:
           if (originator != null)
              throw new Exception
                (Catalog.DUPLICATE_TLV_2.format(nested.getType(),
                                                nested.getStart()));
           originator = parseStringValue(nested, "US-ASCII");
           break;

         case TIMESTAMP:
           if (timestamp != null)
              throw new Exception
                (Catalog.DUPLICATE_TLV_2.format(nested.getType(),
                                                nested.getStart()));
           timestamp = parseStringValue(nested, "US-ASCII");
           break;

         case TEXT:
           if (text != null)
              throw new Exception
                (Catalog.DUPLICATE_TLV_2.format(nested.getType(),
                                                nested.getStart()));
           text = parseStringValue(nested, "UTF-8");
           break;

         default:
           throw new Exception
             (Catalog.UNEXPECTED_TLV_2.format(nested.getType(),
                                              nested.getStart()));
        }
       nested = nested.getNextTLV(msgtlv.getEnd());
     }

    if (originator == null)
       throw new Exception
         (Catalog.MISSING_NESTED_TLV_3.format(msgtlv.getType(),
                                              msgtlv.getStart(),
                                              MsgBoardType.ORIGINATOR));
    if (timestamp == null)
       throw new Exception
         (Catalog.MISSING_NESTED_TLV_3.format(msgtlv.getType(),
                                              msgtlv.getStart(),
                                              MsgBoardType.TIMESTAMP));
    if (text == null)
       throw new Exception
         (Catalog.MISSING_NESTED_TLV_3.format(msgtlv.getType(),
                                              msgtlv.getStart(),
                                              MsgBoardType.TEXT));

    visitor.visitMessage(originator, timestamp, text);
  }

}
