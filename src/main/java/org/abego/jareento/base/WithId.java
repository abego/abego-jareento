package org.abego.jareento.base;

/**
 * Used on types those instances can be identified by a String value (the ID).
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
