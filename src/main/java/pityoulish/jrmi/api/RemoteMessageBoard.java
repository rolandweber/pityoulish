/*
 * This work is released into the Public Domain under the
 * terms of the Creative Commons CC0 1.0 Universal license.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */
package pityoulish.jrmi.api;

import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * Remote interface for the Message Board.
 */
public interface RemoteMessageBoard extends Remote
{
  /**
   * Lists messages from this board.
   *
   * @param limit       the maximum number of messages to list
   * @param marker      the continuation {@link MessageList#getMarker marker}
   *                    from a preceding call, or
   *                    <code>null</code> to fetch the oldest messages
   *                    from this board
   *
   * @return    the oldest available messages that are newer than the
   *            <code>marker</code>.
   *            The order of the messages is from older to newer.
   *            The list contains no more than <code>limit</code> messages.
   */
  public MessageList listMessages(int limit, String marker)
    throws RemoteException
    ;


  /**
   * Puts a message on this board.
   * Boards have a limited capacity, so this might drop an old message.
   *
   * @param ticket      the ticket (token) to authorize the operation
   *                    and to identify the originator
   * @param text        the content of the message
   */
  public void putMessage(String ticket, String text)
    throws RemoteException
    ;

}
