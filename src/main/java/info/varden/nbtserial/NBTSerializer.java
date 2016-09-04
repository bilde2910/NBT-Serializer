package info.varden.nbtserial;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;

/**
 * <p>A class for serializing classes to NBT format. Serializable classes must implement {@link INBTSerializable} in
 * order to be serialized to NBT format. This class will serialize all fields in the given object that are annotated
 * with {@link NBTSerialize}.</p>
 * 
 * @author Marius
 */
public class NBTSerializer {
	/*
	 * Minecraft NBT tag IDs.
	 */
	public static final int NBT_TAG_END = 0;
	public static final int NBT_TAG_BYTE = 1;
	public static final int NBT_TAG_SHORT = 2;
	public static final int NBT_TAG_INT = 3;
	public static final int NBT_TAG_LONG = 4;
	public static final int NBT_TAG_FLOAT = 5;
	public static final int NBT_TAG_DOUBLE = 6;
	public static final int NBT_TAG_BYTE_ARRAY = 7;
	public static final int NBT_TAG_STRING = 8;
	public static final int NBT_TAG_LIST = 9;
	public static final int NBT_TAG_COMPOUND = 10;
	public static final int NBT_TAG_INT_ARRAY = 11;
	
	/**
	 * <p>Serializes the given {@link INBTSerializable} instance to an NBT data structure.</p>
	 * <p><b>Note:</b> null values will not be serialized.</p>
	 * 
	 * @param object An {@link INBTSerializable} instance.
	 * @return The given instance represented as a serialized NBT data structure.
	 * @throws IllegalAccessException if a Field object in a serializable class is enforcing
	 * Java language access control and the underlying field is inaccessible.
	 */
	public static final <T extends INBTSerializable> NBTTagCompound serialize(T object) throws IllegalAccessException {
		/*
		 * First thing we need to do is create the NBT compound tag that will represent the
		 * class instance in its serialized form.
		 */
		NBTTagCompound t = new NBTTagCompound();
		/*
		 * Then, we need to get a list of declared fields in the instance's class. We loop
		 * over each of the fields and serialize any that are annotated with @NBTSerialize.
		 */
		Class<?> definition = object.getClass();
		Field[] df = definition.getDeclaredFields();
		for (Field f : df) {
			if (f.isAnnotationPresent(NBTSerialize.class)) {
				/*
				 * Any fields that are annotated with @NBTSerialize will need to be read, so
				 * we set them as accessible in case they are declared as private or
				 * protected.
				 */
				f.setAccessible(true);
				/*
				 * Get the value of the field and check if it is null. If it is, there is no
				 * need to serialize it, so we move on to the next field.
				 */
				Object fv = f.get(object);
				if (fv == null) continue;
				
				/*
				 * Not that we know that the value is not null, we get the name of the tag in
				 * the NBT structure that corresponds to the given field. If blank, use the
				 * name of the field itself.
				 */
				String tn = f.getAnnotation(NBTSerialize.class).name();
				if (tn.equals("")) tn = f.getName();
				/*
				 * Then, we get the declared class of the field.
				 */
				Class<?> fc = f.getType();
				
				/*
				 * Check the assignability of the field against number classes, arrays and
				 * strings. If any of these match, an NBT tag of the type corresponding to the
				 * class of the field will be created and added to the NBT data structure with
				 * the value obtained from the field.
				 */
				if      (fc.isAssignableFrom(byte.class)        || fc.isAssignableFrom(Byte.class))         t.setByte(tn, (Byte) fv);
				else if (fc.isAssignableFrom(boolean.class)     || fc.isAssignableFrom(Boolean.class))      t.setBoolean(tn, (Boolean) fv);
				else if (fc.isAssignableFrom(short.class)       || fc.isAssignableFrom(Short.class))        t.setShort(tn, (Short) fv);
				else if (fc.isAssignableFrom(int.class)         || fc.isAssignableFrom(Integer.class))      t.setInteger(tn, (Integer) fv);
				else if (fc.isAssignableFrom(long.class)        || fc.isAssignableFrom(Long.class))         t.setLong(tn, (Long) fv);
				else if (fc.isAssignableFrom(float.class)       || fc.isAssignableFrom(Float.class))        t.setFloat(tn, (Float) fv);
				else if (fc.isAssignableFrom(double.class)      || fc.isAssignableFrom(Double.class))       t.setDouble(tn, (Double) fv);
				else if (fc.isAssignableFrom(byte[].class))                                                 t.setByteArray(tn, (byte[]) fv);
				else if (fc.isAssignableFrom(Byte[].class))                                                 t.setByteArray(tn, ArrayUtils.toPrimitive((Byte[]) fv));
				else if (fc.isAssignableFrom(String.class))                                                 t.setString(tn, (String) fv);
				else if (fc.isAssignableFrom(int[].class))                                                  t.setIntArray(tn, (int[]) fv);
				else if (fc.isAssignableFrom(Integer[].class))                                              t.setIntArray(tn, ArrayUtils.toPrimitive((Integer[]) fv));

				/*
				 * Lists and other INBTSerializable objects in the class instance must be
				 * serialized themselves before they are added to the NBT data structure.
				 */
				else if (INBTSerializable.class.isAssignableFrom(fc))                                       t.setTag(tn, serialize((INBTSerializable) fv));
				else if (List.class.isAssignableFrom(fc))                                                   t.setTag(tn, serializeList((List) fv));
			}
		}
		/*
		 * When serialization is done, return the completed NBT data structure.
		 */
		return t;
	}
	
	/**
	 * <p>Serializes the given {@link List} instance to an NBT list structure.</p>
	 * 
	 * @param list A {@link List} instance.
	 * @return The given instance represented as a serialized NBT list structure.
	 * @throws IllegalAccessException if a Field object in a serializable class is enforcing
	 * Java language access control and the underlying field is inaccessible.
	 */
	private static <T> NBTTagList serializeList(List<T> list) throws IllegalAccessException {
		/*
		 * First of all, create a blank NBT list tag to store our values. Unlike Java, NBT has
		 * only one one type of list, namely NBTTagList. Java has different implementations of
		 * List (e.g. ArrayList, LinkedList, etc.), which makes deserialization significantly
		 * harder than serialization.
		 */
		NBTTagList c = new NBTTagList();
		/*
		 * We don't know what kind of elements the list contains, and instead of using
		 * reflection sorcery (http://stackoverflow.com/a/14403515/1955334) to figure it out,
		 * it's much easier and more more logical to just check the class of the first element
		 * of the list. This obviously doesn't work if the list is empty. However, if that's
		 * the case, we can just return an empty list tag - the NBT implementation doesn't
		 * need to know what kind of tags it contains if it's empty. (NBTTagList sets its
		 * child tag type to 0 (NBT_TAG_END) if it's empty when writing itself to a stream.)
		 */
		if (list.size() <= 0) return c;
		Class<?> subclass = list.get(0).getClass();
		
		for (int i = 0; i < list.size(); i++) {
			/*
			 * Check the assignability of the class against number classes, arrays and strings. If
			 * any of these match, an NBT tag of the type corresponding to the class of the field
			 * will be created and added to the NBT list structure with the value obtained from
			 * the field.
			 */
			if      (subclass.isAssignableFrom(Byte.class))                 c.appendTag(new NBTTagByte((Byte) list.get(i)));
			else if (subclass.isAssignableFrom(Boolean.class))              c.appendTag(new NBTTagByte(((Boolean) list.get(i)) ? (byte) 1 : (byte) 0));
			else if (subclass.isAssignableFrom(Short.class))                c.appendTag(new NBTTagShort((Short) list.get(i)));
			else if (subclass.isAssignableFrom(Integer.class))              c.appendTag(new NBTTagInt((Integer) list.get(i)));
			else if (subclass.isAssignableFrom(Long.class))                 c.appendTag(new NBTTagLong((Long) list.get(i)));
			else if (subclass.isAssignableFrom(Float.class))                c.appendTag(new NBTTagFloat((Float) list.get(i)));
			else if (subclass.isAssignableFrom(Double.class))               c.appendTag(new NBTTagDouble((Double) list.get(i)));
			else if (subclass.isAssignableFrom(Byte[].class))               c.appendTag(new NBTTagByteArray(ArrayUtils.toPrimitive((Byte[]) list.get(i))));
			else if (subclass.isAssignableFrom(String.class))               c.appendTag(new NBTTagString((String) list.get(i)));
			else if (subclass.isAssignableFrom(Integer[].class))            c.appendTag(new NBTTagIntArray(ArrayUtils.toPrimitive((Integer[]) list.get(i))));

			/*
			 * Lists and other INBTSerializable objects in the class instance must be
			 * serialized themselves before they are added to the NBT list structure.
			 */
			else if (INBTSerializable.class.isAssignableFrom(subclass))     c.appendTag(serialize((INBTSerializable) list.get(i)));
			else if (List.class.isAssignableFrom(subclass))                 c.appendTag(serializeList((List) list.get(i)));
		}
		/*
		 * When serialization is done, return the completed NBT list structure.
		 */
		return c;
	}
	
	/**
	 * <p>Deserializes an NBT data structure into an new {@link INBTSerializable} class
	 * instance. Deserialized objects will contain values from the NBT structure for all
	 * deserialized annotated fields where the corresponding tag is available in the NBT
	 * structure.</p>
	 * <p><b>Note:</b> If an NBT tag is not found for a corresponding field of the given
	 * serializable class, that field will be instantiated as {@code null}.</p>
	 * 
	 * @param definition The {@link INBTSerializable} class structure to use for
	 * deserialization.
	 * @param data The NBT data structure to deserialize.
	 * @return A deserialized instance of the given class definition.
	 * @throws IllegalAccessException if a Field object in a serializable class is enforcing
	 * Java language access control and the underlying field is inaccessible, or if the
	 * constructor for a serializable class or {@link List} is inaccessible.
	 * @throws InstantiationException if a serializable or {@link List} class represents
	 * an abstract class, an interface, an array class, a primitive type, or void; or if the
	 * class has no nullary constructor; or if the instantiation fails for some other reason.
	 */
	public static final <T extends INBTSerializable> T deserialize(Class<T> definition, NBTTagCompound data) throws IllegalAccessException, InstantiationException {
		/*
		 * The first thing we need to do is create an instance of the class that we want to
		 * deserialize to. When using this method, the class MUST have a nullary constructor,
		 * as we otherwise would have no idea what kind of arguments to pass to the
		 * constructor.
		 */
		T instance = definition.newInstance();
		/*
		 * This instance will then be funneled into the main deserialization function.
		 */
		deserialize(instance, data, true);
		/*
		 * Finally, return this instance.
		 */
		return instance;
	}
	
	/**
	 * <p>Deserializes an NBT data structure into an existing {@link INBTSerializable} class
	 * instance. The given instance will be overwritten with values from the NBT structure for
	 * all deserialized annotated fields where the corresponding tag is available in the NBT
	 * structure.</p>
	 * <p><b>Note:</b> If an NBT tag is not found for a corresponding field of the given
	 * serializable class, that field will not be overwritten unless the
	 * interpretMissingFieldValuesAsNull argument is set to {@code true}, in which case the
	 * field is set to {@code null}.</p>
	 * 
	 * @param instance The {@link INBTSerializable} class structure to use for
	 * deserialization.
	 * @param data The NBT data structure to deserialize.
	 * @param interpretMissingFieldValuesAsNull Whether fields in the instance for which there
	 * is no corresponding NBT tag in the given data structure should be set to {@code null}
	 * or left as-is.
	 * @return A deserialized instance of the given class definition.
	 * @throws IllegalAccessException if a Field object in a serializable class is enforcing
	 * Java language access control and the underlying field is inaccessible, or if the
	 * constructor for a serializable class or {@link List} is inaccessible.
	 * @throws InstantiationException if a serializable or {@link List} class represents
	 * an abstract class, an interface, an array class, a primitive type, or void; or if the
	 * class has no nullary constructor; or if the instantiation fails for some other reason.
	 */
	public static final <T extends INBTSerializable> void deserialize(T instance, NBTTagCompound data, boolean interpretMissingFieldValuesAsNull) throws IllegalAccessException, InstantiationException {
		/*
		 * First of all, we need to get a list of declared fields in the instance's class. We
		 * then loop over each of the fields and process any that are annotated with
		 * @NBTSerialize.
		 */
		Field[] df = instance.getClass().getDeclaredFields();
		for (Field f : df) {
			if (f.isAnnotationPresent(NBTSerialize.class)) {
				/*
				 * Once an annotated field is found, get the name of the tag in the NBT
				 * structure that corresponds to the given field. If blank, use the name of
				 * the field itself.
				 */
				String tn = f.getAnnotation(NBTSerialize.class).name();
				if (tn.equals("")) tn = f.getName();
				/*
				 * If the tag is not present in the NBT data structure, decide what to do with
				 * it - either ignore it and move on, or set it to null, depending on what is
				 * specified through the interpretMissingFieldValuesAsNull argument.
				 */
				if (!data.hasKey(tn)) {
					if (interpretMissingFieldValuesAsNull) {
						/*
						 * The developer has specified that they want missing tags to default
						 * to null in the class instance, so we set the field as accessible
						 * (in case it is private or protected) before setting its value to
						 * null.
						 */
						f.setAccessible(true);
						f.set(instance, null);
					}
					/*
					 * Finally, we continue to the next field.
					 */
					continue;
				}
				/*
				 * If we reach this point, a tag that corresponds to the given field was found
				 * in the NBT data structure. No matter what happens now, the value of that
				 * field will end up overwritten, so we set it as accessible in case it is
				 * declared private or protected.
				 */
				f.setAccessible(true);
				/*
				 * Then, we get the declared class of the field.
				 */
				Class<?> fc = f.getType();
				
				/*
				 * Check the assignability of the field against number classes, arrays and
				 * strings. If any of these match, the value of the field will be set to a
				 * corresponding, valid instance of the class that matches from the NBT
				 * data structure.
				 */
				if      (fc.isAssignableFrom(byte.class))       f.setByte       (instance,                          data.getByte(tn));
				else if (fc.isAssignableFrom(Byte.class))       f.set           (instance, Byte.valueOf(            data.getByte(tn)));
				else if (fc.isAssignableFrom(boolean.class))    f.setBoolean    (instance,                          data.getBoolean(tn));
				else if (fc.isAssignableFrom(Boolean.class))    f.set           (instance, Boolean.valueOf(         data.getBoolean(tn)));
				else if (fc.isAssignableFrom(short.class))      f.setShort      (instance,                          data.getShort(tn));
				else if (fc.isAssignableFrom(Short.class))      f.set           (instance, Short.valueOf(           data.getShort(tn)));
				else if (fc.isAssignableFrom(int.class))        f.setInt        (instance,                          data.getInteger(tn));
				else if (fc.isAssignableFrom(Integer.class))    f.set           (instance, Integer.valueOf(         data.getInteger(tn)));
				else if (fc.isAssignableFrom(long.class))       f.setLong       (instance,                          data.getLong(tn));
				else if (fc.isAssignableFrom(Long.class))       f.set           (instance, Long.valueOf(            data.getLong(tn)));
				else if (fc.isAssignableFrom(float.class))      f.setFloat      (instance,                          data.getFloat(tn));
				else if (fc.isAssignableFrom(Float.class))      f.set           (instance, Float.valueOf(           data.getFloat(tn)));
				else if (fc.isAssignableFrom(double.class))     f.setDouble     (instance,                          data.getDouble(tn));
				else if (fc.isAssignableFrom(Double.class))     f.set           (instance, Double.valueOf(          data.getDouble(tn)));
				else if (fc.isAssignableFrom(byte[].class))     f.set           (instance,                          data.getByteArray(tn));
				else if (fc.isAssignableFrom(Byte[].class))     f.set           (instance, ArrayUtils.toObject(     data.getByteArray(tn)));
				else if (fc.isAssignableFrom(String.class))     f.set           (instance,                          data.getString(tn));
				else if (fc.isAssignableFrom(int[].class))      f.set           (instance,                          data.getIntArray(tn));
				else if (fc.isAssignableFrom(Integer[].class))  f.set           (instance, ArrayUtils.toObject(     data.getIntArray(tn)));
				
				/*
				 * Lists and other serializable classes require special treatment on
				 * deserialization. Many classes can subclass java.util.List, including user-
				 * defined ones, so we need to check if a List can be assigned from the field
				 * type instead of checking if the field can be assigned from List (which it
				 * in most cases cannot, e.g. if the field is of type ArrayList, you can't
				 * assign a List instance to it). The same goes for INBTSerializable
				 * instances. An INBTSerializable instance cannot be assigned to an
				 * INBTSerializable subclass field.
				 */
				else if (INBTSerializable.class.isAssignableFrom(fc)) {
					/*
					 * INBTSerializable instances are easy to deserialize, as they can
					 * just recursively be passed back into this very method.
					 */
					NBTTagCompound ntc = data.getCompoundTag(tn);
					Object c = deserialize((Class<? extends INBTSerializable>) fc, ntc);
					f.set(instance, c);
				} else if (List.class.isAssignableFrom(fc)) {
					/*
					 * We need to figure out what type the list contains. This is important,
					 * so that we get the right type object added to the list and so that the
					 * correct type of value is extracted from the NBT list structure.
					 */
					Type listType = ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0];
					/*
					 * We also need to get a Class instance that corresponds to this Type.
					 * This is primarily done to be able to directly and explicitly cast
					 * values obtained from the NBT structure into the type that the List
					 * holds, but also to be able to look up the correct NBT tag type for
					 * the given class.
					 */
					Class<?> lct;
					/*
					 * It may be that the types of the list elements themselves are Lists,
					 * which are also parameterized. This case must be handled differently
					 * from non-parameterized types because they cannot be explicitly casted
					 * to a Class instance. We need to get the raw type from the parameterized
					 * type, which in turn can be casted to a Class.
					 */
					if (listType instanceof ParameterizedType) {
						lct = (Class<?>) ((ParameterizedType) listType).getRawType();
					} else {
						lct = (Class<?>) listType;
					}
					/*
					 * We now need to look up the NBT tag ID that corresponds to the given
					 * class, so that the right type of NBT list is extracted from the NBT
					 * data structure.
					 */
					NBTTagList ntl = data.getTagList(tn, getIDFromClass(lct));
					/*
					 * Finally, the list may be deserialized.
					 */
					List<?> c = deserializeList((Class<? extends List>) fc, lct, listType, ntl);
					f.set(instance, c);
				}
			}
		}
	}
	
	/**
	 * <p>Deserializes an NBT list structure into a {@link List} instance.</p>
	 *  
	 * @param listClass A {@link Class} instance representing the subclass of {@link List}
	 * that the NBT list structure should be deserialized to.
	 * @param subclass A {@link Class} instance representing the class of the elements in
	 * the {@link List} definition.
	 * @param subtype A {@link Type} instance representing the type of the elements in the
	 * {@link List} definition.
	 * @param list The NBT list structure to deserialize.
	 * @return A deserialized {@link List} instance of the given subclass.
	 * @throws IllegalAccessException if a Field object in a serializable class is enforcing
	 * Java language access control and the underlying field is inaccessible, or if the
	 * constructor for a serializable class or {@link List} is inaccessible.
	 * @throws InstantiationException if a serializable or {@link List} class represents
	 * an abstract class, an interface, an array class, a primitive type, or void; or if the
	 * class has no nullary constructor; or if the instantiation fails for some other reason
	 */
	private static <T> List<T> deserializeList(Class<?> listClass, Class<T> subclass, Type subtype, NBTTagList list) throws InstantiationException, IllegalAccessException {
		/*
		 * The reason we need both a Class and a List instance passed to this method is that
		 * we need to be able to cast objects directly into an instance of the subclass
		 * specified. A Class instance may be parameterized, but a Type may not be, so passing
		 * only the Type will not allow us to determine the generic class T. Furthermore, we
		 * cannot cast using Class.cast because that method returns an Object instance, not an
		 * instance of T. T allows us to cast to the subclass explicitly.
		 */
		/*
		 * The first thing we need to do is create an instance of the List class that the
		 * field in the serializable class mandates. This will be passed on and eventually
		 * assigned to the field (or, in the case of nested list, added to a parent list).
		 */
		List<T> c = (List<T>) listClass.newInstance();
		
		for (int i = 0; i < list.tagCount(); i++) {
			/*
			 * Check the assignability of the subclass against number classes, arrays and
			 * strings. If any of these match, a valid instance of that class will be created
			 * from an NBT tag of the corresponding type from the NBT list structure.
			 */
			if      (subclass.isAssignableFrom(Byte.class))         c.add((T) Byte.valueOf(             ((NBTTagByte) list.get(i))      .getByte()));
			else if (subclass.isAssignableFrom(Boolean.class))      c.add((T) Boolean.valueOf(          ((NBTTagByte) list.get(i))      .getByte() != 0));
			else if (subclass.isAssignableFrom(Short.class))        c.add((T) Short.valueOf(            ((NBTTagShort) list.get(i))     .getShort()));
			else if (subclass.isAssignableFrom(Integer.class))      c.add((T) Integer.valueOf(          ((NBTTagInt) list.get(i))       .getInt()));
			else if (subclass.isAssignableFrom(Long.class))         c.add((T) Long.valueOf(             ((NBTTagLong) list.get(i))      .getLong()));
			else if (subclass.isAssignableFrom(Float.class))        c.add((T) Float.valueOf(            ((NBTTagFloat) list.get(i))     .getFloat()));
			else if (subclass.isAssignableFrom(Double.class))       c.add((T) Double.valueOf(           ((NBTTagDouble) list.get(i))    .getDouble()));
			else if (subclass.isAssignableFrom(Byte[].class))       c.add((T) ArrayUtils.toObject(      ((NBTTagByteArray) list.get(i)) .getByteArray()));
			else if (subclass.isAssignableFrom(String.class))       c.add((T)                           ((NBTTagString) list.get(i))    .getString());
			else if (subclass.isAssignableFrom(Integer[].class))    c.add((T) ArrayUtils.toObject(      ((NBTTagIntArray) list.get(i))  .getIntArray()));
			/*
			 * Lists and other serializable classes require special treatment on
			 * deserialization. Many classes can subclass java.util.List, including user-
			 * defined ones, so we need to check if a List can be assigned from the field
			 * type instead of checking if the field can be assigned from List (which it
			 * in most cases cannot, e.g. if the field is of type ArrayList, you can't
			 * assign a List instance to it). The same goes for INBTSerializable
			 * instances. An INBTSerializable instance cannot be assigned to an
			 * INBTSerializable subclass field.
			 */
			else if (INBTSerializable.class.isAssignableFrom(subclass)) {
				/*
				 * INBTSerializable instances are easy to deserialize, as they can
				 * just recursively be passed back into the main deserialization function.
				 */
				NBTTagCompound ntc = (NBTTagCompound) list.get(i);
				Object c2 = deserialize((Class<? extends INBTSerializable>) subclass, ntc);
			} else if (List.class.isAssignableFrom(subclass)) {
				/*
				 * We need to figure out what type the list contains. This is important,
				 * so that we get the right type object added to the list and so that the
				 * correct type of value is extracted from the NBT list structure.
				 */
				Type listType = ((ParameterizedType) subtype).getActualTypeArguments()[0];
				/*
				 * We also need to get a Class instance that corresponds to this Type.
				 * This is primarily done to be able to directly and explicitly cast
				 * values obtained from the NBT structure into the type that the List
				 * holds, but also to be able to look up the correct NBT tag type for
				 * the given class.
				 */
				Class<?> lct;
				/*
				 * It may be that the types of the list elements themselves are Lists,
				 * which are also parameterized. This case must be handled differently
				 * from non-parameterized types because they cannot be explicitly casted
				 * to a Class instance. We need to get the raw type from the parameterized
				 * type, which in turn can be casted to a Class.
				 */
				if (listType instanceof ParameterizedType) {
					lct = (Class<?>) ((ParameterizedType) listType).getRawType();
				} else {
					lct = (Class<?>) listType;
				}
				/*
				 * We now need to look up the NBT tag ID that corresponds to the given
				 * class, so that the right type of NBT list is extracted from the NBT
				 * data structure.
				 */
				NBTTagList ntl = (NBTTagList) list.get(i);
				/*
				 * Finally, the list may be deserialized.
				 */
				List<?> c2 = deserializeList(subclass, lct, listType, ntl);
				c.add((T) c2);
			}
		}
		/*
		 * When deserialization is complete, return the completed list.
		 */
		return c;
	}
	
	/**
	 * <p>Returns the NBT tag ID that corresponds to the given Java {@link Class}.
	 * 
	 * @param clazz The {@link Class} to match against an NBT tag ID.
	 * @return An NBT tag ID.
	 */
	private static int getIDFromClass(Class<?> clazz) {
		/*
		 * Here, we just check assignability of the class against the various supported NBT
		 * tag types to pick the one that is best fit for serializing the given class.
		 */
		if (clazz.isAssignableFrom(byte.class) || clazz.isAssignableFrom(Byte.class) ||
			clazz.isAssignableFrom(boolean.class) || clazz.isAssignableFrom(Boolean.class)) {
			return NBT_TAG_BYTE;
		} else if (clazz.isAssignableFrom(short.class) || clazz.isAssignableFrom(Short.class)) return NBT_TAG_SHORT;
		else if (clazz.isAssignableFrom(int.class) || clazz.isAssignableFrom(Integer.class)) return NBT_TAG_INT;
		else if (clazz.isAssignableFrom(long.class) || clazz.isAssignableFrom(Long.class)) return NBT_TAG_LONG;
		else if (clazz.isAssignableFrom(float.class) || clazz.isAssignableFrom(Float.class)) return NBT_TAG_FLOAT;
		else if (clazz.isAssignableFrom(double.class) || clazz.isAssignableFrom(Double.class)) return NBT_TAG_DOUBLE;
		else if (clazz.isAssignableFrom(byte[].class) || clazz.isAssignableFrom(Byte[].class)) return NBT_TAG_BYTE_ARRAY;
		else if (clazz.isAssignableFrom(String.class)) return NBT_TAG_STRING;
		else if (clazz.isAssignableFrom(int[].class) || clazz.isAssignableFrom(Integer[].class)) return NBT_TAG_INT_ARRAY;
		else if (INBTSerializable.class.isAssignableFrom(clazz)) return NBT_TAG_COMPOUND;
		else if (List.class.isAssignableFrom(clazz)) return NBT_TAG_LIST;
		return NBT_TAG_COMPOUND;
	}
}
