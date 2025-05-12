package util;

import java.io.Serializable;

/**
 * A generic message class for communication between client and server.
 * Encapsulates a message type and optional arguments.
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private String type;
    private Object args;

    /**
     * Creates a new message with a type and arguments.
     *
     * @param type The message type used for routing and handling
     * @param args The message arguments/payload (can be null)
     */
    public Message(String type, Object args) {
        this.type = type;
        this.args = args;
    }

    /**
     * Gets the message type.
     *
     * @return The message type
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the message arguments.
     *
     * @return The message arguments
     */
    public Object getArgs() {
        return args;
    }

    @Override
    public String toString() {
        return "Message{" +
                "type='" + type + '\'' +
                ", args=" + (args != null ? args.getClass().getSimpleName() : "null") +
                '}';
    }
}