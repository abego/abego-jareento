package org.abego.jareento.base;

import org.eclipse.jdt.annotation.Nullable;

/**
 * This module's Exception class.
 * <p>
 * Even though the module defines its own Exception class you must not
 * assume it will only throw these exceptions. Be prepared that "any"
 * Exception/Throwable may be thrown when calling a method of the API.
 */
public class JareentoException extends RuntimeException {
    /**
     * Constructs a new {@link JareentoException} with the specified detail message and
     * cause.
     * <p>
     * Note that the detail message associated with {@code cause} is
     * <i>not</i> automatically incorporated in this exception's detail message.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method).
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link #getCause()} method).  (A {@code null} value is
     *                permitted, and indicates that the cause is nonexistent or
     *                unknown.)
     */
    public JareentoException(String message, @Nullable Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new {@link JareentoException} with the specified detail message.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public JareentoException(String message) {
        this(message, null);
    }
}
