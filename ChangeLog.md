# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [3.0.0] - 2022-11-30

- removed all databind capabilities (#31), which are now provided by [jackson-dataformat-velocypack](https://github.com/arangodb/jackson-dataformat-velocypack).

## [2.5.4] - 2021-08-24

- fixed escaping in JSON string generation (#29)   

## [2.5.3] - 2021-04-23

- improved performances of `VPackParser.toJson()`
- added `VPackSlice.getSchemaDescription()` to return a printable schema description of the VPack slice

## [2.5.2] - 2021-03-23

- fixed start offset of byte array copy in `VPackSlice.toByteArray`

## [2.5.1] - 2020-12-21

- fixed getting attribute from VPackSlice with `null` key ([spring-data#210](https://github.com/arangodb/spring-data/issues/210))

## [2.5.0] - 2020-10-12

- support deserializing ints as BigDecimals
- removed escaping forward slash in `VPackParser`

## [2.4.1] - 2020-08-14

- fixed base64 encoding for Java version >= 9 

## [2.4.0] - 2020-07-29

- added useTypeHints option to VPack.Builder

## [2.3.1] - 2020-05-05

- shaded jackson dependency

## [2.3.0] - 2020-04-28

- bugfix serialization unindexed singleton arrays
- added `add(Byte)`, `is(Byte)`, `getAsByte()` to VPackBuilder and VPackSlice
- use custom VPackBuilder in VPackParser

## [2.2.1] - 2020-03-25

- improved array iteration performances

## [2.2.0] - 2020-03-05

- deserialization support for wildcard types
- deserialization support for java.lang.Iterable
- raised the minimum JVM source and target version to 8
- All-Arguments constructor deserialization
- Static Factory Method deserialization
- Support for Kotlin data classes and Scala case classes
- Builder deserialization

## [2.1.1] - 2020-01-20

- fixed tagging bugs
- fixed custom types byte size
- fixed BCD byte size
- Map and Set VPackInstanceCreators preserve collections entries order (based on `LinkedHashSet` and `LinkedHashMap`) 

## [2.1.0] - 2019-12-20

- added [tags support](https://github.com/arangodb/velocypack/blob/master/VelocyPack.md#tagging)

## [2.0.0] - 2019-12-19

- performance improvements
- raised minimum supported Java version to Java 7

## [1.4.3] - 2019-10-22

- jackson v2.9.10

## [1.4.2] - 2019-07-29

- properly (always) close the builder object

## [1.4.1] - 2018-09-18

### Fixed

- fixed handling of additional fields

## [1.4.0] - 2018-09-18

### Added

- added support for generic types (issue #1, #3)

  Serialize the class name in a field `_class` when necessary. Field name can be configured through `VPack.Builder#typeKey(String)`

## [1.3.0] - 2018-08-02

### Changed

- `VPackDeserializationContext#deserialize(VPackSlice, Class)` to `VPackDeserializationContext#deserialize(VPackSlice, java.lang.reflect.Type)`

## [1.2.0] - 2018-06-08

### Changed

- replaced dependency json-simple with jackson

## [1.1.0] - 2018-04-19

### Added

- added support for deserializing `BigInteger`/`BigDecimal` from String

### Changed

- changed serializing `BigInteger`/`BigDecimal` to String

## [1.0.15] - 2017-04-17

### Fixed

- fixed `DateUtil` does incorrect conversion of UTC time (issue #6)

## [1.0.14] - 2017-11-27

### Fixed

- fixed Json parsing of negative long

## [1.0.13] - 2017-11-03

### Fixed

- fixed deserialization of BigDecimal

## [1.0.12] - 2017-10-23

### Changed

- exclude junit dependency of json-simple

### Fixed

- fixed VPack to JSON parsing of negative `int`
- fixed serialization of negative `int`/`long` value (issue #5)

## [1.0.11] - 2017-07-31

### Fixed

- fixed `DateUtil` (thread-safe)

## [1.0.10] - 2017-07-20

### Fixed

- fixed Json parsing of null within Objects (issue #2)

## [1.0.9] - 2017-06-20

### Fixed

- fixed deserializing of internal field `_id`

## [1.0.8] - 2017-06-13

### Added

- added `VPackSetupContext#registerDeserializer(Type, VPackDeserializer, boolean)`
- added `VPackSetupContext#registerDeserializer(String, Type, VPackDeserializer, boolean)`

## [1.0.7] - 2017-06-09

### Added

- added `VPackSetupContext#registerKeyMapAdapter(type, adapter)`

## [1.0.6] - 2017-06-09

### Added

- added `VPack.Builder#registerKeyMapAdapter(type, adapter)`
- added `VPack#serialize(Object, SerializeOptions)`

## [1.0.5] - 2017-04-13

### Fixed

- fixed VPackSlice float/double bug

## [1.0.4] - 2017-04-11

### Changed

- optimize `VPack.Builder` and `VPackParser.Builder` (thread-safe)

## [1.0.3] - 2017-03-23

### Fixed

- fixed serialization for parameterized types

## [1.0.2] - 2017-03-22

### Added

- added support for deserializing parameterized types

## [1.0.1] - 2017-03-17

### Added

- added support for registering modules on `VPack`,`VPackParser`

[unreleased]: https://github.com/arangodb/java-velocypack/compare/1.4.1...HEAD
[1.4.1]: https://github.com/arangodb/java-velocypack/compare/1.4.0...1.4.1
[1.4.0]: https://github.com/arangodb/java-velocypack/compare/1.3.0...1.4.0
[1.3.0]: https://github.com/arangodb/java-velocypack/compare/2.3.1...1.3.0
[1.2.0]: https://github.com/arangodb/java-velocypack/compare/1.1.0...1.2.0
[1.1.0]: https://github.com/arangodb/java-velocypack/compare/1.0.15...1.1.0
[1.0.15]: https://github.com/arangodb/java-velocypack/compare/1.0.14...1.0.15
[1.0.14]: https://github.com/arangodb/java-velocypack/compare/1.0.13...1.0.14
[1.0.13]: https://github.com/arangodb/java-velocypack/compare/1.0.12...1.0.13
[1.0.12]: https://github.com/arangodb/java-velocypack/compare/1.0.11...1.0.12
[1.0.11]: https://github.com/arangodb/java-velocypack/compare/1.0.10...1.0.11
[1.0.10]: https://github.com/arangodb/java-velocypack/compare/1.0.9...1.0.10
[1.0.9]: https://github.com/arangodb/java-velocypack/compare/1.0.8...1.0.9
[1.0.8]: https://github.com/arangodb/java-velocypack/compare/1.0.7...1.0.8
[1.0.7]: https://github.com/arangodb/java-velocypack/compare/1.0.6...1.0.7
[1.0.6]: https://github.com/arangodb/java-velocypack/compare/1.0.5...1.0.6
[1.0.5]: https://github.com/arangodb/java-velocypack/compare/1.0.4...1.0.5
[1.0.4]: https://github.com/arangodb/java-velocypack/compare/1.0.3...1.0.4
[1.0.3]: https://github.com/arangodb/java-velocypack/compare/1.0.2...1.0.3
[1.0.2]: https://github.com/arangodb/java-velocypack/compare/1.0.1...1.0.2
[1.0.1]: https://github.com/arangodb/java-velocypack/compare/1.0.1
