package org.abego.jareento.base;

/**
 * The instance can be identified by a String value, its {@link #id()}.
 */
public interface WithId {

    /**
     * Returns the String ID that identifies this object.
     * <p>
     * More specific implementations may refine the {@code @ID} value.
     */
    @ID("")
    String id();
}
