package it.polimi.ingsw.am23.network.socket.messages;

import java.io.Serial;
import java.io.Serializable;

/**
 * Marker base class for every typed message exchanged over the socket
 * transport. Concrete subclasses identify the operation and carry the
 * required parameters / payload.
 */
public abstract class Message implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
