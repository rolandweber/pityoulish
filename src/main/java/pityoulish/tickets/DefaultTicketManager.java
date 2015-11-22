/*
 * This work is released into the Public Domain under the
 * terms of the Creative Commons CC0 1.0 Universal license.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */
package pityoulish.tickets;

import java.util.Map;
import java.util.HashMap;
import java.net.InetAddress;


//@@@ Move texts to properties files. Prepare for localization.
//@@@ Maintain an enum for mapping names to message codes?

/**
 * Default implementation of a {@link TicketManager}.
 * Ticket managers must be thread safe.
 */
public class DefaultTicketManager implements TicketManager
{
  public final static long TIME_TO_LIVE_MS = 128000; // milliseconds

  /** Characters to be used in the random part of a token. */
  protected final static String RANDOM_TOKEN_CHARS =
    "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789._";


  protected Map<String,TicketImpl>      ticketsByUsername;
  protected Map<InetAddress,TicketImpl> ticketsByAddress;
  protected Map<String,TicketImpl>      ticketsByToken;

  //@@@ no housekeeping implemented, expired tickets should get purged


  public DefaultTicketManager()
  {
    ticketsByUsername = new HashMap<String,TicketImpl>();
    ticketsByAddress  = new HashMap<InetAddress,TicketImpl>();
    ticketsByToken    = new HashMap<String,TicketImpl>();
  }


  public synchronized Ticket obtainTicket(String username,
                                          InetAddress address)
    throws TicketException
  {
    if (username == null)
       throw new NullPointerException("username");

    if (username.length() < 1)
       throw new TicketException("The username is empty.");

    final long now = System.currentTimeMillis();

    TicketImpl tick = ticketsByUsername.get(username);
    if ((tick != null) && !tick.isExpired(now))
       throw new TicketException("This user already has a ticket.");

    if (address != null)
     {
       // Checking for a network address in this fashion is NOT a reasonable
       // thing to do. Different clients may share the same network address,
       // by means of SOCKS proxies or firewalls with NAT.
       // This is just a placeholder for sanity checks that could be performed
       // by a real application. It also isn't secure in any way, because a
       // machine can have more than one network address, or use SOCKS proxies
       // and firewalls with NAT.

       tick = ticketsByAddress.get(address);
       if ((tick != null) && !tick.isExpired(now))
          throw new TicketException("This address already has a ticket.");
     }
    //@@@ trigger housekeeping if an expired ticket was found?

    final String token = computeToken(username);
    final int  actions = 3;                     //@@@ randomize? 2..5
    final long  expiry = now + TIME_TO_LIVE_MS; //@@@ randomize?
    tick = new TicketImpl(this, username, address, token, expiry, actions);

    ticketsByUsername.put(username, tick);
    if (address != null)
       ticketsByAddress.put(address, tick);
    ticketsByToken.put(token, tick);

    return tick;

  } // obtainTicket


  public synchronized Ticket lookupTicket(String token,
                                          InetAddress address)
    throws TicketException
  {
    if (token == null)
       throw new NullPointerException("token");

    if (token.length() < 1)
       throw new TicketException("The ticket token is empty.");

    TicketImpl tick = ticketsByToken.get(token);
    if (tick == null)
       throw new TicketException("Ticket '"+token+"' not found.");

    tick.validate(this, null, address, token);

    return tick;

  } // lookupTicket


  public synchronized void returnTicket(Ticket tick)
    throws TicketException
  {
    if (tick == null)
       throw new NullPointerException("Ticket");
    if (!(tick instanceof TicketImpl))
       throw new IllegalArgumentException
         ("wrong class of ticket: "+tick.getClass().getName());

    TicketImpl timp = (TicketImpl) tick;

    // make sure that it is a ticket from here
    timp.validate(this, null, null, timp.getToken());

    ticketsByUsername.remove(timp.getUsername());
    if (timp.getAddress() != null)
       ticketsByAddress.remove(timp.getAddress());
    ticketsByToken.remove(timp.getToken());
  }


  /**
   * Computes a random token for a username.
   *
   * @param username    the username, will be included in the token
   *
   * @return    a token for a new ticket for the argument user
   */
  protected String computeToken(String username)
  {
    // concatenate the username with some random characters
    StringBuilder sb = new StringBuilder(username.length()+6);
    sb.append(username).append('@');

    double random = Math.random();
    // 5*6 = 30 bits of randomness should be available...
    for (int i=0; i<5; i++)
     {
       double which = random * RANDOM_TOKEN_CHARS.length();
       sb.append(RANDOM_TOKEN_CHARS.charAt((int) which));
       random = which - (double)(int)which;
     }

    return sb.toString();
  }

}