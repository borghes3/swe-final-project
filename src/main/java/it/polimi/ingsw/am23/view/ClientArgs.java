package it.polimi.ingsw.am23.view;

import java.util.List;

/**
 * Small helper to parse command-line arguments shared by the CLI and the GUI
 * client launchers.
 */
public final class ClientArgs {

    private static final String RMI_CALLBACK_FLAG = "--rmiCallback=";

    private ClientArgs() {
    }

    /**
     * Parses the {@code --rmiCallback=<port>} flag from the given arguments.
     *
     * @param args raw program arguments; may be {@code null}
     * @return the requested callback port, or {@code 0} if the flag is absent
     * (which makes RMI pick a free random port)
     * @throws IllegalArgumentException if the flag is present but its value is
     *                                  not an integer in the {@code 1..65535} range
     */
    public static int parseRmiCallbackPort(String[] args) {
        if (args == null) {
            return 0;
        }
        for (String arg : args) {
            if (arg != null && arg.startsWith(RMI_CALLBACK_FLAG)) {
                String value = arg.substring(RMI_CALLBACK_FLAG.length());
                int port;
                try {
                    port = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "Invalid value for --rmiCallback: '" + value + "' is not an integer.");
                }
                if (port < 1 || port > 65535) {
                    throw new IllegalArgumentException(
                            "Invalid value for --rmiCallback: " + port + " (must be between 1 and 65535).");
                }
                return port;
            }
        }
        return 0;
    }

    /**
     * Convenience overload for callers that hold their args as a {@link List}
     * (e.g. JavaFX {@code getParameters().getRaw()}).
     */
    public static int parseRmiCallbackPort(List<String> args) {
        return parseRmiCallbackPort(args == null ? null : args.toArray(new String[0]));
    }
}
