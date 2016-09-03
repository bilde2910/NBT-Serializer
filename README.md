# NBT Serializer
NBT Serializer is a small Java library for serializing objects to NBT in Java. It requires Minecraft (package net.minecraft.nbt) and Apache Commons Lang (package org.apache.commons.lang3.ArrayUtils) to function. It is particularly useful when you need to de- and reconstruct objects to/from binary data so that they may be sent over networks or saved to the file system.

## Why it exists
I created this serializer in order to save work manually serializing and deserializing certain objects in another Minecraft-related project of mine. Hopefully, the creation of this serializer means I will never have to write any more code to serialize to NBT ever again. I thought this would be a quick fix, but as we all know, it doesn't work that way.

![XKCD - The General Problem (xkcd.com/974)](http://imgs.xkcd.com/comics/the_general_problem.png)

(Source: XKCD, http://xkcd.com/974/)

Hopefully, this will also be of use to other developers. That's why I'm posting it on GitHub. Feel free to use this yourself.

## Setup
Simply add the classes from this repository into your own project, and you can access them. If you want to do this, though, I kindly request that you change the package name so that there are no conflicts with other mods that may happen to have an implementation of this project as well.

## Usage
To mark a class as serializable, implement the `INBTSerializable` interface on the class. This must be done for every class you want to serialize. Then, on every field in that class that you want to be included in the serialized object, attach the `@NBTSerialize` annotation. You may optionally supply a tag name; default or blank will use the field name. Serializable classes are recommended to have a nullary constructor, but may choose not to add one. In that case, you need to pass an already constructed instance of the class when you deserialize it, as opposed to just passing a reference to its class.

Classes that may be serialized include `Byte`, `Byte[]`, `Boolean`, `Short`, `Integer`, `Integer[]`, `Long`, `Float`, and `Double`, as well as the primitive equivalents of all these, plus `String`, any subclass of `List`, and other `INBTSerializable` classes.

Example serialized class:

```java
import java.util.ArrayList;
import java.util.Arrays;
import info.varden.nbtserial.*;

public class Person implements INBTSerializable {
    
    /* 
     * A default constructor is required for serialization.
     * Using @Deprecated might be a good idea so that you don't
     * accidentally use this constructor in your own code.
     */
    @Deprecated
    public Person() {}
    
    // Preferred constructor
    public Person(String name, int age, ArrayList<String> hobbies, String secret) {
        this.name = name;
        this.age = age;
        this.hobbies = hobbies;
        this.secret = secret;
    }
    
    @NBTSerialize
    public String name;
    
    @NBTSerialize
    public int age;
    
    @NBTSerialize
    public ArrayList<String> hobbies;
    
    @NBTSerialize
    private String secret;
    
}
```

Then, all you have to do is call `NBTSerializer.serialize()` on an instance of this class, and the instance will be serialized!

```java
Person alice = new Person("Alice", 31,
    new ArrayList<String>(Arrays.asList(new String[] {"Skiing", "Knitting", "Card games"})),
    "Has a crush on Bob");

// Serialize Alice
NBTTagCompound serialized = NBTSerializer.serialize(alice);

// Deserialize Alice
Person newAlice = NBTSerializer.deserialize(alice, Person.class);

System.out.println(newAlice.name);      // Alice
System.out.println(newAlice.age);       // 31
System.out.println(newAlice.hobbies);   // [Skiing, Knitting, Card games]
System.out.println(newAlice.secret);    // Has a crush on Bob
```

Here's how the above NBT serialized class would look like in an NBT viewer:

![Alice.nbt viewed in NBTExplorer](https://raw.githubusercontent.com/bilde2910/NBT-Serializer/master/docs/result.png)
