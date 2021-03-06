/*
 * This work is released into the Public Domain under the
 * terms of the Creative Commons CC0 1.0 Universal license.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */
package pityoulish.jrmi.follow;

import java.util.logging.Logger;
import java.util.logging.Level;

import pityoulish.logutil.Log;
import pityoulish.logutil.LogConfig;
import pityoulish.cmdline.ArgsInterpreter;
import pityoulish.cmdline.BackendHandler;

import pityoulish.jrmi.client.RegistryBackendHandler;
import pityoulish.jrmi.client.RegistryBackendHandlerImpl;


/**
 * Main entry point to the Follow-the-Board Client with Java RMI.
 * This is a read-only client which periodically polls the message board
 * and prints newly appeared messages.
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

    DataFormatter df = new DataFormatterImpl(System.out);

    // rbh deals with the registry, ftbh calls the server remotely
    RegistryBackendHandler rbh = new RegistryBackendHandlerImpl();
    FollowTheBoardHandler ftbh = new FollowTheBoardHandler(rbh, df);

    ArgsInterpreter ai = new ArgsInterpreter(rbh, ftbh.getCommandName(), ftbh);

    int status = ai.handle(args);
    System.exit(status);
  }

}

