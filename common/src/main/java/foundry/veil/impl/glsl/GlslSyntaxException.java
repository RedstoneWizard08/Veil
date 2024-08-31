package foundry.veil.impl.glsl;

import org.jetbrains.annotations.ApiStatus;

/**
 * @author Ocelot
 */
@ApiStatus.Internal
public class GlslSyntaxException extends Exception {

    public static final int CONTEXT_AMOUNT = 64;

    private final String message;
    private final String input;
    private final int cursor;

    public GlslSyntaxException(String message, String input, int cursor) {
        super(message);
        this.message = message;
        this.input = input;
        this.cursor = cursor;
    }

    @Override
    public String getMessage() {
        String message = this.message;
        String context = this.getContext();
        if (context != null) {
            message += " at position " + this.cursor + ": " + context;
        }
        return message;
    }

    private String getContext() {
        if (this.input == null || this.cursor < 0) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        int cursor = Math.min(this.input.length(), this.cursor);

        if (cursor > CONTEXT_AMOUNT) {
            builder.append("...");
        }

        builder.append(this.input, Math.max(0, cursor - CONTEXT_AMOUNT), cursor);
        builder.append("<--[HERE]");

        return builder.toString();
    }
}