/*
 * This work is released into the Public Domain under the
 * terms of the Creative Commons CC0 1.0 Universal license.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */
package pityoulish.tutorial;

import pityoulish.cmdline.ArgsInterpreter;
import pityoulish.cmdline.BackendHandler;


/**
 * Main entry point to Tutorial program.
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
    ArgsInterpreter ai = new ArgsInterpreter(new BackendHandler.None(),
                                             new TutorialCommandHandler());
    int status = ai.handle(args);
    System.exit(status);
  }

}
