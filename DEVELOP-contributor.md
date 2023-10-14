# Contributor Notes

This document contains valuable information for developers that wish to
contribute to this module.

## Conventions

The conventions defined in the "Client Developer Notes" (`DEVELOP-client.md`)
also apply to contributors.

## Documentation

Some documentation interesting for contributors may not be written in JavaDoc
but contained in "normal" comments or separate, non-Java files.

## Coding Guidelines

### Factory Methods (`new...`)

The name of a factory method starts with `new...`.

`new...` is used in favor of `create...` to makes it more clear that the method
returns a "new" (plain) Java Object, similar to the use of the keyword `new`.
In particular, `new...` avoids the ambiguity of the prefix `create...` which
sometimes also implies that a "persistent" object is created, as in
`File.createTempFile` or in the context of "CRUD" operations
("Create-Read-Update-Delete").
