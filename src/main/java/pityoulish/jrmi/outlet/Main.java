/*
 * This work is released into the Public Domain under the
 * terms of the Creative Commons CC0 1.0 Universal license.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */
package pityoulish.jrmi.outlet;

import pityoulish.cmdline.ArgsInterpreter;
import pityoulish.cmdline.BackendHandler;


/**
 * Main entry point to the Message Board Client with sockets.
 */
public class Main
{
  /**
   * Main entry point.
   *
   * @param args        the command-line arguments
   */
  public final static void main(String[] args)
    throws Exception
  {
    // rbh deals with the registry, mbch calls the server remotely
    RegistryBackendHandler rbh = new RegistryBackendHandlerImpl();
    MsgOutletHandler       moh = new MsgOutletHandlerImpl(rbh);

    // mbcd interprets command-line arguments
    MsgOutletCommandDispatcher mocd = new MsgOutletCommandDispatcher(moh);
    ArgsInterpreter ai = new ArgsInterpreter(rbh, mocd);

    int status = ai.handle(args);

    // For the learning experience, I don't want the JVM to terminate while
    // there are still RMI threads running. Call System.exit only for errors.
    if (status != 0)
       System.exit(status);
  }

}

