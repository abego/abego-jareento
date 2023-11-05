# Maintainer Notes

## Releasing a new version

To release a new version:

- Document all changes since the last release in `CHANGELOG.md`.
  - Typically check all "feat:", "fix:", "perf:", "deprecate:" and "!" (Breaking Changes) commits.
  - Make breaking changes bold and add a "[Breaking Change]" suffix.
  - Add a chapter "[x.y.z] - yyyy-mm-dd" immediately under the "[Unreleased]" chapter title.
    - This way all changes collected since the last release are now associated with the upcoming release and we have an empty  "[Unreleased]" chapter.
    - Take care of Semantic Versioning when choosing the version number [x.y.z].
  - Commit the changes as "doc: update CHANGELOG"
  - (More info regarding change logs in https://keepachangelog.com/en/1.0.0/)
- Make sure you have a clean Git working tree (without pending changes,
  all commits pushed) and you are on the `main` branch.
- Run `misc/releasenewversion {version} {nextVersion}`,
  with versions in `X.Y.Z` format, e.g. `0.10.0`.
  _(`{nextVersion}` refers to the version development is targeting
  after this release, DON'T append "-SNAPSHOT".)_
- Perform the remaining manual steps, as printed at the end of the
  automatic release process.


