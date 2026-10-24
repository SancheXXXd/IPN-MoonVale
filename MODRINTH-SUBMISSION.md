# Publication checklist

Do not resubmit an old patched/obfuscated JAR merely because new sources exist.
Acceptance is determined by Modrinth moderation, not this build process.

1. Commit this complete source tree, including Gradle locks, checksum verification,
   wrapper, licenses and CI. Run the workflow on that commit.
2. Test the resulting release JAR in Minecraft 26.3 with Java 25, Fabric Loader
   0.19.5 or newer, Fabric API, Fabric Language Kotlin 1.14.1+kotlin.2.4.20 or
   newer, and compatible libIPN 6.9.0. Check the exact version ranges in
   `fabric.mod.json`. Confirm startup, config screens, sorting, row/column sorting,
   slot locks, persistence and no crashes. Test optional integrations separately.
3. Check that automatic refilling/trading/crafting and other disabled inputs do
   not run, and verify the actual MoonVale server requirements with its operators.
4. Download the release JAR from that workflow. Verify its SHA256SUMS. Publish
   that exact JAR, not a file from an old download or manual binary patch.
5. Remove all previously uploaded versions containing obfuscated code, as requested
   by moderation. Do not remove or rewrite license/copyright notices.
6. Set all runtime dependencies on each Modrinth version: Fabric API, Fabric Language
   Kotlin and libIPN. Do not mark compile-only integrations as required. Use the
   matching Minecraft version and Fabric loader.
7. Link this public source repository and original IPN. Set AGPL-3.0-or-later.
   Use accurate fork/disabled-feature wording; do not claim complete deletion or
   official server approval without evidence.
8. Set the required derivative-content disclosure in the project settings. This
   project also received substantial AI-assisted source/build/documentation work;
   set the applicable AI-generated-content disclosure honestly under current rules.
   Removing comments does not eliminate a disclosure requirement. Check all other
   applicable disclosures, including inherited ones; do not make blanket claims
   about independently installed dependencies.
9. Explain to moderation that the replacement is built without name obfuscation,
   identify the source commit, link the workflow, and attach matching checksums.
   Only claim client/server tests that were actually performed.

Policy references:
- https://modrinth.com/legal/rules
- https://support.modrinth.com/en/articles/16567675-content-disclosures

## Suggested moderation reply (fill in and verify before sending)

I replaced the old binary with a new build from the linked public source commit:
[COMMIT URL]. The build does not use name obfuscation or minification. ANTLR and
JWildcard are package-relocated with their licenses retained. The workflow
[WORKFLOW URL] compares two clean builds in separate directories and provides
checksums and source archives. I removed the previous obfuscated versions,
updated upstream attribution, license, dependencies and applicable disclosures.
The uploaded JAR SHA-256 is [SHA256]. Client/server tests performed: [ACTUAL TESTS].
