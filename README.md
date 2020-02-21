![ArangoDB-Logo](https://docs.arangodb.com/assets/arangodb_logo_2016_inverted.png)

# ArangoDB VelocyPack Java

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.arangodb/velocypack/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.arangodb/velocypack)

Java implementation for [VelocyPack](https://github.com/arangodb/velocypack).

## Maven

To add the dependency to your project with maven, add the following code to your pom.xml:

```XML
<dependencies>
  <dependency>
    <groupId>com.arangodb</groupId>
    <artifactId>velocypack</artifactId>
    <version>1.0.13</version>
  </dependency>
</dependencies>
```

If you want to test with a snapshot version (e.g. 1.0.0-SNAPSHOT), add the staging repository of oss.sonatype.org to your pom.xml:

```XML
<repositories>
  <repository>
    <id>arangodb-snapshots</id>
    <url>https://oss.sonatype.org/content/groups/staging</url>
  </repository>
</repositories>
```

## Compile

```
mvn clean install -DskipTests=true -Dgpg.skip=true -Dmaven.javadoc.skip=true -B
```

# Usage

## build VelocyPack - Object

```Java
  VPackBuilder builder = new VPackBuilder();
  builder.add(ValueType.OBJECT); // object start
  builder.add("foo", "bar"); // add field "foo" with value "bar"
  builder.close(); // object end

  VPackSlice slice = builder.slice(); // create slice
```

## working with VPackSlice - Object

```Java
  VPackSlice slice = ...
  int size = slice.size(); // number of fields
  VPackSlice foo = slice.get("foo"); // get field "foo"
  String value = foo.getAsString(); // get value from "foo"

  // iterate over the fields
  for (final Iterator<Entry<String, VPackSlice>> iterator = slice.objectIterator(); iterator.hasNext();) {
    Entry<String, VPackSlice> field = iterator.next();
    ...
  }
```

## build VelocyPack - Array

```Java
  VPackBuilder builder = new VPackBuilder();
  builder.add(ValueType.ARRAY); // array start
  builder.add(1); // add value 1
  builder.add(2); // add value 2
  builder.add(3); // add value 3
  builder.close(); // array end

  VPackSlice slice = builder.slice(); // create slice
```

## working with VPackSlice - Array

```Java
  VPackSlice slice = ...
  int size = slice.size(); // number of values

  // iterate over values
  for (int i = 0; i < slice.size(); i++) {
    VPackSlice value = slice.get(i);
    ...
  }

  // iterate over values with Iterator
  for (final Iterator<VPackSlice> iterator = slice.arrayIterator(); iterator.hasNext();) {
    VPackSlice value = iterator.next();
    ...
  }
```

## build VelocyPack - nested Objects

```Java
  VPackBuilder builder = new VPackBuilder();
  builder.add(ValueType.OBJECT); // object start
  builder.add("foo", ValueType.OBJECT); // add object in field "foo"
  builder.add("bar", 1); // add field "bar" with value 1 to object "foo"
  builder.close(); // object "foo" end
  builder.close(); // object end

  VPackSlice slice = builder.slice(); // create slice
```

## serialize POJO

```Java
  MyBean entity = new MyBean();
  VPack vpack = new VPack.Builder().build();
  VPackSlice slice = vpack.serialize(entity);
```

## deserialize VelocyPack

```Java
  VPackSlice slice = ...
  VPack vpack = new VPack.Builder().build();
  MyBean entity = vpack.deserialize(slice, MyBean.class);
```

## parse Json to VelocPack

```Java
  String json = ...
  VPackParser parser = new VPackParser.Builder().build();
  VPackSlice slice = parser.fromJson(json);
```

## parse VelocyPack to Json

```Java
  VPackSlice slice = ...
  VPackParser parser = new VPackParser.Builder().build();
  String json = parser.toJson(slice);
```

# Registering modules

Both `VPack` and `VPackParser` allow registering of modules which can offer custom serializers/deserializers for additional types.

## VPackModule

```Java
  VPackModule module = ...
  VPack vpack = new VPack.Builder().registerModule(module).build();
```

## VPackParserModule

```Java
  VPackParserModule module = ...
  VPackParser parser = VPackParser.Builder().registerModule(module).build();
```

# Configure serialization / deserialization

## POJOs

The class `VPack` can serialize/deserialize POJOs. They need at least a constructor without parameter. Also [Builder deserialization](#builder-deserialization),
[All-Arguments-Constructor deserialization](#all-arguments-constructor-deserialization) and [Static Factory Method deserialization](#static-factory-method-deserialization) are supported. 


```Java
  public class MyObject {

    private String name;
    private Gender gender;
    private int age;

    public MyObject() {
      super();
    }

  }
```

## serialized fieldnames

To use a different serialized name for a field, use the annotation `SerializedName`.

```Java
  public class MyObject {

    @SerializedName("title")
    private String name;

    private Gender gender;
    private int age;

    public MyObject() {
      super();
    }

  }
```

## ignore fields

To ignore fields at serialization/deserialization, use the annotation `Expose`

```Java
  public class MyObject {

    @Expose
    private String name;
    @Expose(serialize = true, deserialize = false)
    private Gender gender;
    private int age;

    public MyObject() {
      super();
    }

  }
```

## custom de-/serializer

```Java
  VPack vpack = new VPack.Builder()
    .registerDeserializer(MyObject.class, new VPackDeserializer<MyObject>() {
      @Override
      public MyObject deserialize(
        final VPackSlice parent,
        final VPackSlice vpack,
        final VPackDeserializationContext context) throws VPackException {

          final MyObject obj = new MyObject();
          obj.setName(vpack.get("name").getAsString());
          return obj;
      }
    }).registerSerializer(MyObject.class, new VPackSerializer<MyObject>() {
      @Override
      public void serialize(
        final VPackBuilder builder,
        final String attribute,
        final MyObject value,
        final VPackSerializationContext context) throws VPackException {

          builder.add(attribute, ValueType.OBJECT);
          builder.add("name", value.getName());
          builder.close();
      }
    }).build();
```


# Builder deserialization

Deserialization using builders is supported using the following annotations:

## @VPackPOJOBuilder

It allows specifying the builder setters and build method. It has the following fields:

- `buildMethodName: String`: the build method to call on the builder object after having set all the attributes
- `withSetterPrefix: String`: the prefix of the builder setters

This annotation can target:
- the builder class having a public no-arg constructor, eg:
```java
@VPackPOJOBuilder(buildMethodName = "build", withSetterPrefix = "set")
public class Builder {
    public Builder() {
        // ...
    }

    public MyClass build() {
        // ...
    }

    // ...
}
```
- a public static factory method which returns the builder, eg:
```java
public class MyClass {
    @VPackPOJOBuilder(buildMethodName = "build", withSetterPrefix = "with")
    public static Builder<MyClass> builder() {
        //...
     }
    // ...
}
```

## @VPackDeserialize

It allows to specify the builder class that will be used during the deserialization. It has the following fields:
- `builder: Class<?>`: builder class to use
- `builderConfig: VPackPOJOBuilder`: it allows specifying the builder setters and build method, useful in case the 
builder code is auto generated and you cannot add `@VPackPOJOBuilder` to it.

This annotation can target:
- `setter`: allows specifying the builder for the setter argument
- `field`: allows specifying the builder for the field
- `class`: allows specifying the builder for the class
- `parameter`: allows specifying the builder for a constructor (or factory method) parameter

Example:
```java
@VPackDeserialize(builder = MyClassBuilder.class,
				  builderConfig = @VPackPOJOBuilder(buildMethodName = "build",
													withSetterPrefix = "with"))
public class MyClass {
    // ...
} 
```


# All-Arguments-Constructor deserialization

Deserialization using All-Arguments-Constructor is supported annotating the constructor with `@VPackCreator` and 
annotating each of its parameters with `@SerializedName("name")`, eg:

```java
public class Person {
	private final String name;
	private final int age;

	@VPackCreator
	public Person(
			@SerializedName("name") String name,
			@SerializedName("age") int age
    ) {
        this.name = name;
        this.age = age;
    }
    // ...
}
```


# Static Factory Method deserialization

Deserialization using Static Factory Method is supported annotating the method with `@VPackCreator` and 
annotating each of its parameters with `@SerializedName("name")`, eg:

```java
public class Person {
	private final String name;
	private final int age;

	private Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

	@VPackCreator
	public static Person of(
			@SerializedName("name") String name,
			@SerializedName("age") int age
    ) {
        return new Person(name, age);
    }

    // ...
}
```


# Kotlin data classes

Deserialization of Kotlin data classes is supported annotating the method with `@VPackCreator` and annotating each of 
its parameters with `@SerializedName("name")`, eg:

```kotlin
data class Person @VPackCreator constructor(
        @SerializedName("name") val name: String,
        @SerializedName("age") val age: Int
)
```


# Learn more

- [ArangoDB](https://www.arangodb.com/)
- [ChangeLog](ChangeLog.md)
- [JavaDoc](http://arangodb.github.io/java-velocypack/javadoc-1_4)
