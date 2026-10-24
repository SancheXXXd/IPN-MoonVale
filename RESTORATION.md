# Source provenance and verification limits

This is a reconstructed MoonVale source project, not the original MoonVale Git history.
Its base is IPN v2.3.8 commit `bb3acc6f6ccbbf367fa5818bf8101e1247c68dae`,
with the Fabric 26.3 platform layer. MoonVale changes were reconstructed from
bytecode and decompiled classes in the supplied binary. Icons and translations
were taken from that binary. Later build and privacy fixes are maintained here.

The previous input JAR had SHA-256
`0c97195df7da0c097d5407b1671c6d9e1dca74b34bd46a4d0ce83d8b357de2a4`.
It is NOT the output of this corrected source tree. Do not publish that old JAR
with these sources or claim byte-for-byte correspondence with it.

## Current verification

`./gradlew build` compiles Kotlin/Java, generates parsers and ModInfo, and checks
archive contents, metadata, licenses, expected source/dependency class names,
selected disabled entry points and removed update/telemetry references.
`python3 scripts/verify_reproducible.py` also compares two clean builds in different
absolute directories, on the same machine and JDK. Reports are generated only
after successful checks. Gradle dependency locks and checksum verification pin
resolved dependencies; checksums are trust-on-first-use, not a security audit.

The project-side inherited update checks and keepalive implementation were removed.
This does not audit independent installed dependencies or their network behavior.

## Not verified

Minecraft has not been launched by these scripts. They do not establish gameplay
correctness, complete absence of reachable disabled code, server/bot recognition,
Modrinth approval, cross-platform reproducibility or a security audit. Source and
binary compatibility must be tested with the target client and server before release.
The original implementation of many disabled features remains in the source tree.
