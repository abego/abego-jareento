# Client Developer Notes

This document contains valuable information for developers using this module
in their own applications' code ("clients of this module").

## Conventions

### Semantic Versioning

This module uses "Semantic Versioning" (https://semver.org).

### API and SPI

The client code interacts with the module only through the module's API.
The API is defined through public interfaces and enums and possibly a few
public classes.

Interfaces marked as "SPI" (Service Provider Interface) are expected to be
implemented in client code. For interfaces not marked as "SPI" you must not
create your own implementation. Also, you must not extend types of the API,
except those marked as "SPI".

### Hidden Implementation

The implementation of the module consists of all code that is non-`public` or
located inside an `internal` or `shared` package or its sub-packages. This also
includes classes implementing the interfaces defined by the module's API.

You must not access implementation code directly. This also includes
`public` types inside an `internal` or `shared` package or its sub-packages,
even though this code is visible to client code according to Java visibility
rules.

You must not make any assumptions based on the current implementation.

The implementation code may change at any time, without prior notice. Relying
on certain implementation details may break your application when using a future
version of the module.

In summary this means: the implementation must be considered "hidden" to the
clients of the module.

(The module system introduced with Java 9 provides a better way to hide the
implementation details. However, this module is designed to work with Java 8,
so using these Java 9 features was not an option.)

### Exceptions

Even though the module may define its own Exception class(es) you must not
assume it will only throw these exceptions. Be prepared that "any"
Exception/Throwable may be thrown when calling a method of the API.

### Nullity

By default, functions don't return `null` values, parameter values must not be
`null`, etc. . When `null` is supported or returned the annotation
`@Nullable` is used. Some methods that returning `null` have a name ending with
`...OrNull` to make it more obvious that a `null` may be returned.
