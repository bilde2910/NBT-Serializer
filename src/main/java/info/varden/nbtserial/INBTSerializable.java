package info.varden.nbtserial;

/**
 * <p>An interface that identifies a {@link Class} as being NBT serializable.</p>
 * 
 * @author Marius
 */
public interface INBTSerializable {
	/*
	 * This interface exists more or less just to ensure that developers cannot attempt to
	 * serialize classes that they cannot modify. There would be no point in doing that, as
	 * the @NBTSerialize annotation is required on all fields that should be serialized. And
	 * if they cannot modify the source of a class to implement this interface, then they
	 * certainly can't add the @NBTSerialize annotations either. It would simply result in an
	 * empty NBTTagCompound, so it's best to just ignore those to avoid clutter in the NBT
	 * data structure.
	 */
}
