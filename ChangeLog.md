# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]

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
