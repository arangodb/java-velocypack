![ArangoDB-Logo](https://user-images.githubusercontent.com/3998723/207981337-79d49127-48fc-4c7c-9411-8a688edca1dd.png)

# ArangoDB VelocyPack Java

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.arangodb/velocypack/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.arangodb/velocypack)
[![CircleCI](https://dl.circleci.com/status-badge/img/gh/arangodb/java-velocypack/tree/main.svg?style=svg)](https://dl.circleci.com/status-badge/redirect/gh/arangodb/java-velocypack/tree/main)

Java implementation for [VelocyPack](https://github.com/arangodb/velocypack).

## Maven

To add the dependency to your project with maven, add the following code to your pom.xml:

```XML
<dependencies>
  <dependency>
    <groupId>com.arangodb</groupId>
    <artifactId>velocypack</artifactId>
    <version>x.y.z</version>
  </dependency>
</dependencies>
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


# Learn more

- [ArangoDB](https://www.arangodb.com/)
- [ChangeLog](ChangeLog.md)
- [JavaDoc](http://arangodb.github.io/java-velocypack/)
