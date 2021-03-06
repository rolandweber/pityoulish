/*
 * This work is released into the Public Domain under the
 * terms of the Creative Commons CC0 1.0 Universal license.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */
package pityoulish.jrmi.client;

import pityoulish.mbclient.HostPortBackendHandler;
import pityoulish.jrmi.api.RemoteMessageBoard;
import pityoulish.jrmi.api.RemoteTicketIssuer;



/**
 * Backend handler for contacting the RMI Registry.
 */
public interface RegistryBackendHandler extends HostPortBackendHandler
{
  /**
   * Look up the {@link RemoteMessageBoard} in the registry.
   *
   * @return    a stub for the Message Board
   *
   * @throws Exception    in case of a problem
   */
  public RemoteMessageBoard getRemoteMessageBoard()
    throws Exception
    ;


  /**
   * Look up the {@link RemoteTicketIssuer} in the registry.
   *
   * @return    a stub for the Ticket Issuer
   *
   * @throws Exception    in case of a problem
   */
  public RemoteTicketIssuer getRemoteTicketIssuer()
    throws Exception
    ;

}

