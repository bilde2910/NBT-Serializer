package info.varden.nbtserial;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
	 * <p>The name of the NBT tag that the field should be serialized to.
	 * Uses the field name not specified or set to an empty string.</p>
	 * @return
	 */
	String name() default "";
}
