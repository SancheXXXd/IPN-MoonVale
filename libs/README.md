# Dependencies

Third-party mod JARs are not checked in or bundled. `build.gradle` resolves pinned
Modrinth version IDs via its Maven endpoint. Gradle dependency locks and checksum
verification record the resolved artifacts. ANTLR runtime and JWildcard are the
only shaded libraries; see THIRD-PARTY-NOTICES.md.
