/*
 * This work is released into the Public Domain under the
 * terms of the Creative Commons CC0 1.0 Universal license.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */
package pityoulish.msgboard;


/**
 * A board for system messages.
 * System messages are volatile, meaning that they can be updated or removed.
 * However, missing an obsolete system message does not make a batch
 * {@link MessageBatch#isDiscontinuous discontinuous}.
 * As their name indicates, system messages are generated by the system.
 * Unlike user messages, they don't require an external originator.
 */
public interface SystemMessageBoard extends MessageBoard
{
  /**
   * Puts a system message on this board.
   * Boards have a limited capacity, so this might drop an old message.
   *
   * @param slot        the name of the slot to update, or <code>null</code>.
   *                    If the board already contains a system message for
   *                    this slot, the old one is removed before the new one
   *                    appears on the board.
   *                    If no slot is specified, the new system message is
   *                    not an update, and cannot be updated or removed either.
   * @param text        the content of the message
   *
   * @return    the new message on the board, including a timestamp
   */
  public Message putSystemMessage(String slot, String text)
    ;


  /**
   * Removes a system message from this board.
   *
   * @param slot        the name of the slot from which to remove the message
   *
   * @return    <code>true</code> if a message was removed, or
   *            <code>false</code> if there was no message in that slot
   *            on the board
   */
  public boolean removeSystemMessage(String slot)
    ;

}
