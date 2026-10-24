# Inventory Profiles Next (MoonVale)

A fork of [Inventory Profiles Next](https://codeberg.org/Inventory-Profiles-Next-Mod/IPN) for the MoonVale server.

## What was disabled?
- Automatic item and tool refilling
- Inventory profile switching and the profile selection widget
- Automatic villager trading and the trading bookmarks interface
- Continuous automatic crafting
- Stonecutter automation
- Anvil automation, including bulk renaming
- Item scrolling hotkeys
- Swipe Moving
- Automatic locked-slot management (LockedSlotKeeper)
- Mass-transfer and mass-throw hotkeys

## What was kept?
- Inventory sorting, including sorting by rows and columns
- Manual slot locking
- Slot highlighting

[![MoonVale](https://i.postimg.cc/T1B62HmD/a6b1b9b2da77f5b08e4b2cd03a23f74dc67d164e-(3).png)](https://moonvale.ru) [![Discord](https://i.postimg.cc/y8gTSpR4/a6b1b9b2da77f5b08e4b2cd03a23f74dc67d164e-(6).png)](https://discord.gg/v5eVH6RMg7) [![Modrith](https://cdn.modrinth.com/data/cached_images/a6b1b9b2da77f5b08e4b2cd03a23f74dc67d164e.png)](https://modrinth.com/project/inventory-profiles-next-moonvale) 

## Build

Requires JDK 25 and Python 3. Run:

```sh
python3 scripts/verify_reproducible.py
```

This builds twice in separate directories and rejects differing release or source
archives. Artifacts and SHA256SUMS are written to `build/libs/`; static verification
and reproducibility reports are written to `build/reports/`. GitHub Actions runs
the same command. No obfuscation or minification is applied to project classes.
ANTLR and JWildcard are package-relocated, not name-obfuscated.

The disabled features above were disconnected from the normal input, tick and
screen entry points. Much of their implementation remains in the source; they
were **not completely deleted**. This is not a guarantee of server approval.

## Credits and license

Based on IPN v2.3.8 by Mirinimi (Plamen K. Kosseff), jsnimda and upstream contributors.
Original copyright notices are retained. MoonVale modifications: SancheXXXd_,
Omeleox. Licensed under **AGPL-3.0-or-later**; see [LICENSE](LICENSE) and
[THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md).

See [RESTORATION.md](RESTORATION.md) for source provenance and limitations, and
[MODRINTH-SUBMISSION.md](MODRINTH-SUBMISSION.md) before publishing.
