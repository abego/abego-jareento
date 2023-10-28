package org.abego.jareento.base;

/**
 * The instance can be identified by a String value, as returned by its 
 * {@code id} property.
 */
public interface WithId {

    /**
     * Returns the String ID that identifies this object.
     * <p>
     * More specific implementations may refine the {@code @ID} value.
     */
    @ID("")
    String getId();
}
