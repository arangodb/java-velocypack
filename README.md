
![ArangoDB-Logo](https://docs.arangodb.com/assets/arangodb_logo_2016_inverted.png)

# ArangoDB VelocyPack Java

Java implementation for [VelocyPack](https://github.com/arangodb/velocypack).

## Maven

To add the dependency to your project with maven, add the following code to your pom.xml:

```XML
<dependencies>
  <dependency>
    <groupId>com.arangodb</groupId>
    <artifactId>velocypack</artifactId>
    <version>1.0.0</version>
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


# Basic usage

## build VelocyPack - Object

``` Java
  VPackBuilder builder = new VPackBuilder();
  builder.add(ValueType.OBJECT); // object start
  builder.add("foo", "bar"); // add field "foo" with value "bar"
  builder.close(); // object end
  
  VPackSlice slice = builder.slice(); // create slice
```

## working with VPackSlice - Object

``` Java
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

``` Java
  VPackBuilder builder = new VPackBuilder();
  builder.add(ValueType.ARRAY); // array start
  builder.add(1); // add value 1
  builder.add(2); // add value 2
  builder.add(3); // add value 3
  builder.close(); // array end

  VPackSlice slice = builder.slice(); // create slice
```

## working with VPackSlice - Array

``` Java
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

``` Java
  VPackBuilder builder = new VPackBuilder();
  builder.add(ValueType.OBJECT); // object start
  builder.add("foo", ValueType.OBJECT); // add object in field "foo"
  builder.add("bar", 1); // add field "bar" with value 1 to object "foo"
  builder.close(); // object "foo" end
  builder.close(); // object end

  VPackSlice slice = builder.slice(); // create slice
```

## serialize JavaBean

```Java
  MyBean entity = new MyBean();
  VPack vpack = new VPack.Builder().build();
  VPackSlice slice = vpack.serialize(entity);
```

## deserialize VelocyPack

``` Java
  VPackSlice slice = ...
  VPack vpack = new VPack.Builder().build();
  MyBean entity = vpack.deserialize(slice, MyBean.class);
```

## parse Json to VelocPack

``` Java
  String json = ...
  VPackParser parser = new VPackParser();
  VPackSlice slice = parser.fromJson(json);
```

## parse VelocyPack to Json

``` Java
  VPackSlice slice = ...
  VPackParser parser = new VPackParser();
  String json = parser.toJson(slice);
```

# Configure serialization / deserialization

## JavaBeans
The class `VPack` can serialize/deserialize JavaBeans. They need at least a constructor without parameter.

``` Java
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

``` Java
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

``` Java
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

``` Java
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