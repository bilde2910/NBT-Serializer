package info.varden.nbtserial;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>An annotation used by {@link NBTSerializer} to declare a field in an
 * {@link INBTSerializable} as serializable.
 * 
 * @author Marius
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD})
public @interface NBTSerialize {
	/**
	 * <p>The name of the NBT tag that the field should be serialized to. Uses the field name
	 * if not specified or set to an empty string.</p>
	 * 
	 * @return
	 */
	String name() default "";
	
	/**
	 * <p>Forces {@link NBTSerializer} to instantiate a class of this type instead of the
	 * field's declared type when this field is deserialized. This should only be used on
	 * fields whose classes have no nullary constructors or which are abstract classes or
	 * interfaces. Plain {@link Map} fields require this, for instance - Map is an interface
	 * and therefore cannot be directly instantiated, so if NBTSerializer is not told to
	 * instantiate a specific implementor of Map, it will try to instantiate Map directly,
	 * causing an {@link InstantiationException}. Setting this value to e.g. {@link HashMap}
	 * will make NBTSerializer instantiate a HashMap to put here instead, avoiding the
	 * exception.</p>
	 * <p><b>Note:</b> This value affects only the class of the field itself. You cannot
	 * change the class type of generics of this class, if any. This means you can turn a
	 * {@code List<Object>} into an {@code ArrayList<Object>}, but you cannot substitute the
	 * {@code Object} with another class.</p>
	 * <p><b>Note:</b> If this is set to a class that is not assignable to the class of the
	 * field, {@code NBTSerializer.deserialize()} will throw an
	 * {@link IllegalArgumentException} or {@link ClassCastException}.</p>
	 * 
	 * @return
	 */
	Class<?> typeOverride() default Object.class;
}
