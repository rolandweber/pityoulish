/*
 * This work is released into the Public Domain under the
 * terms of the Creative Commons CC0 1.0 Universal license.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */
package pityoulish.sockets.follow;

import java.util.logging.Logger;
import java.util.logging.Level;

import pityoulish.logutil.Log;
import pityoulish.logutil.LogConfig;
import pityoulish.cmdline.ArgsInterpreter;
import pityoulish.cmdline.BackendHandler;
import pityoulish.mbclient.MsgBoardClientHandler;
import pityoulish.sockets.client.MsgBoardClientHandlerImpl;
import pityoulish.sockets.client.RequestBuilder;
import pityoulish.sockets.client.ResponseParser;
import pityoulish.sockets.client.TLVRequestBuilderImpl;
import pityoulish.sockets.client.TLVResponseParserImpl;
import pityoulish.sockets.client.FormattingVisitorImpl;
import pityoulish.sockets.client.SocketBackendHandler;
import pityoulish.sockets.client.SocketBackendHandlerImpl;


/**
 * Main entry point to the Follow-the-Board Client with sockets.
 */
public class Main
{
  protected final static Logger LOGGER = Log.getPackageLogger(Main.class);

  /**
   * Main entry point.
   *
   * @param args        the command-line arguments
   *
   * @throws Exception  in case of a problem
   */
  public final static void main(String[] args)
    throws Exception
  {
    LogConfig.configure(Main.class);
    LOGGER.log(Level.INFO, "starting Message Board follower");

    // rb and rp define the binary format, in this case TLV
    // They could be replaced by a JSON implementation, for example.
    RequestBuilder rb = new TLVRequestBuilderImpl();
    ResponseParser rp = new TLVResponseParserImpl();

    // The visitor encapsulates application logic for processing the response.
    // It doesn't know about the binary format, but about response elements.
    TrackingVisitorImpl tvi = new TrackingVisitorImpl();

    // sbh connects to the backend, mbch sends requests and receives messages
    SocketBackendHandler sbh = new SocketBackendHandlerImpl();
    MsgBoardClientHandler mbch =
      new MsgBoardClientHandlerImpl(sbh, rb, rp, tvi);

    // ftbh interprets command-line arguments
    FollowTheBoardHandler ftbh = new FollowTheBoardHandler(mbch, tvi);
    ArgsInterpreter ai = new ArgsInterpreter(sbh, ftbh.getCommandName(), ftbh);

    int status = ai.handle(args);
    System.exit(status);
  }

}

