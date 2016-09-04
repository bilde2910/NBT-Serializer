package info.varden.nbtserial;

/**
 * <p>An exception that is thrown whenever {@link NBTSerializer} attempts to serialize or
 * deserialize a class that it cannot handle.</p>
 * 
 * @author Marius
 */
public class UnserializableClassException extends Exception {
	/**
	 * The class on which serialization was attempted.
	 */
	private final Class<?> clazz;
	
	public UnserializableClassException(Class<?> clazz) {
		this.clazz = clazz;
	}
	
	/**
	 * Gets the class that {@link NBTSerializer} failed to serialize or deserialize.
	 * @return The {@link Class} instance that caused the error
	 */
	public Class<?> getOffendingClass() {
		return this.clazz;
	}
}
