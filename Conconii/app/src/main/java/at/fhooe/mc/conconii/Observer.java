package at.fhooe.mc.conconii;

/**
 * Customized observer pattern interface
 *
 *  @author Robsen & Gix
 */
public interface Observer {
    /**
     * Gets called for non-message-specific notifications.
     */
    void update();

    /**
     * Gets called at notification with a specific message.
     *
     * @param msg The message to identify the changes.
     */
    void update(String msg);
}
