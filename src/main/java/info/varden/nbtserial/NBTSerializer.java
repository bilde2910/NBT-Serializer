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

public class NBTSerializer {
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
	
	public static final <T extends INBTSerializable> NBTTagCompound serialize(T object) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
		NBTTagCompound t = new NBTTagCompound();
		Class<?> definition = object.getClass();
		Field[] df = definition.getDeclaredFields();
		for (Field f : df) {
			f.setAccessible(true);
			if (f.isAnnotationPresent(NBTSerialize.class)) {
				f.setAccessible(true);
				Object fv = f.get(object);
				if (fv == null) continue;
				
				String tn = f.getAnnotation(NBTSerialize.class).name();
				if (tn.equals("")) tn = f.getName();
				Class<?> fc = f.getType();
				
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

				else if (List.class.isAssignableFrom(fc))                                                   t.setTag(tn, serializeList((List) fv));
				else if (INBTSerializable.class.isAssignableFrom(fc))                                       t.setTag(tn, serialize((INBTSerializable) fv));
			}
		}
		return t;
	}
	
	private static <T> NBTTagList serializeList(List<T> list) throws InstantiationException, IllegalAccessException {
		NBTTagList c = new NBTTagList();
		if (list.size() <= 0) return c;
		Class<?> subclass = list.get(0).getClass();
		
		for (int i = 0; i < list.size(); i++) {
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

			else if (List.class.isAssignableFrom(subclass))                 c.appendTag(serializeList((List) list.get(i)));
			else if (INBTSerializable.class.isAssignableFrom(subclass))     c.appendTag(serialize((INBTSerializable) list.get(i)));
		}
		return c;
	}
	
	public static final <T extends INBTSerializable> T deserialize(Class<T> definition, NBTTagCompound data) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
		T t = definition.newInstance();
		Field[] df = definition.getDeclaredFields();
		for (Field f : df) {
			f.setAccessible(true);
			if (f.isAnnotationPresent(NBTSerialize.class)) {
				f.setAccessible(true);
				String tn = f.getAnnotation(NBTSerialize.class).name();
				if (tn.equals("")) tn = f.getName();
				Class<?> fc = f.getType();
				
				if      (fc.isAssignableFrom(byte.class))       f.setByte       (t,                         data.getByte(tn));
				else if (fc.isAssignableFrom(Byte.class))       f.set           (t, Byte.valueOf(           data.getByte(tn)));
				else if (fc.isAssignableFrom(boolean.class))    f.setBoolean    (t,                         data.getBoolean(tn));
				else if (fc.isAssignableFrom(Boolean.class))    f.set           (t, Boolean.valueOf(        data.getBoolean(tn)));
				else if (fc.isAssignableFrom(short.class))      f.setShort      (t,                         data.getShort(tn));
				else if (fc.isAssignableFrom(Short.class))      f.set           (t, Short.valueOf(          data.getShort(tn)));
				else if (fc.isAssignableFrom(int.class))        f.setInt        (t,                         data.getInteger(tn));
				else if (fc.isAssignableFrom(Integer.class))    f.set           (t, Integer.valueOf(        data.getInteger(tn)));
				else if (fc.isAssignableFrom(long.class))       f.setLong       (t,                         data.getLong(tn));
				else if (fc.isAssignableFrom(Long.class))       f.set           (t, Long.valueOf(           data.getLong(tn)));
				else if (fc.isAssignableFrom(float.class))      f.setFloat      (t,                         data.getFloat(tn));
				else if (fc.isAssignableFrom(Float.class))      f.set           (t, Float.valueOf(          data.getFloat(tn)));
				else if (fc.isAssignableFrom(double.class))     f.setDouble     (t,                         data.getDouble(tn));
				else if (fc.isAssignableFrom(Double.class))     f.set           (t, Double.valueOf(         data.getDouble(tn)));
				else if (fc.isAssignableFrom(byte[].class))     f.set           (t,                         data.getByteArray(tn));
				else if (fc.isAssignableFrom(Byte[].class))     f.set           (t, ArrayUtils.toObject(    data.getByteArray(tn)));
				else if (fc.isAssignableFrom(String.class))     f.set           (t,                         data.getString(tn));
				else if (fc.isAssignableFrom(int[].class))      f.set           (t,                         data.getIntArray(tn));
				else if (fc.isAssignableFrom(Integer[].class))  f.set           (t, ArrayUtils.toObject(    data.getIntArray(tn)));
				
				else if (List.class.isAssignableFrom(fc)) {
					Type listType = ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0];
					Class<?> lct;
					if (listType instanceof ParameterizedType) {
						lct = (Class<?>) ((ParameterizedType) listType).getRawType();
					} else {
						lct = (Class<?>) listType;
					}
					NBTTagList ntl = data.getTagList(tn, getIDFromClass(lct));
					List<?> c = deserializeList((Class<? extends List>) fc, lct, listType, ntl);
					f.set(t, c);
				} else if (INBTSerializable.class.isAssignableFrom(fc)) {
					NBTTagCompound ntc = data.getCompoundTag(tn);
					Object c = deserialize((Class<? extends INBTSerializable>) fc, ntc);
					f.set(t, c);
				}
			}
		}
		return t;
	}
	
	private static <T> List<T> deserializeList(Class<?> listClass, Class<T> subclass, Type subtype, NBTTagList list) throws InstantiationException, IllegalAccessException {
		List<T> c = (List<T>) listClass.newInstance();
		for (int i = 0; i < list.tagCount(); i++) {
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
			
			else if (List.class.isAssignableFrom(subclass)) {
				Type listType = ((ParameterizedType) subtype).getActualTypeArguments()[0];
				Class<?> lct;
				if (listType instanceof ParameterizedType) {
					lct = (Class<?>) ((ParameterizedType) listType).getRawType();
				} else {
					lct = (Class<?>) listType;
				}
				NBTTagList ntl = (NBTTagList) list.get(i);
				List<?> c2 = deserializeList(subclass, lct, listType, ntl);
				c.add((T) c2);
			} else if (INBTSerializable.class.isAssignableFrom(subclass)) {
				NBTTagCompound ntc = (NBTTagCompound) list.get(i);
				Object c2 = deserialize((Class<? extends INBTSerializable>) subclass, ntc);
			}
		}
		return c;
	}
	
	private static int getIDFromClass(Class<?> clazz) {
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
		else if (List.class.isAssignableFrom(clazz)) return NBT_TAG_LIST;
		return NBT_TAG_COMPOUND;
	}
}
