# Inventory Profiles Next (MoonVale)

Allowed version of the **Inventory Profiles Next** mod for the MoonVale server.

Minecraft **26.3** · Fabric · Version **2.3.8+26.3**

Авторы адаптации: **SancheXXXd_**, **Omeleox**.

## Сборка

Требуется **JDK 25** и подключение к интернету для первой загрузки Gradle и Maven-зависимостей.
Gradle Wrapper включён; отдельная установка Gradle не требуется.

Windows (PowerShell):

```powershell
.\gradlew.bat build
```

Linux / macOS:

```sh
chmod +x gradlew
./gradlew build
```

Готовый мод: `build/libs/IPNMoonVale-fabric-2.3.8-mc26.3.jar`.
Архивы `-sources.jar` и `-unshaded.jar` не предназначены для установки вместо готового мода.

Для запуска нужны Fabric Loader, Fabric API, libIPN 6.9.x и совместимый Fabric Language Kotlin.
Mod Menu необязателен. Controlify и Carpet используются только как compileOnly-зависимости
для совместимости с соответствующими сторонними модами; они не встраиваются в готовый JAR.

## Структура

- `src/main/kotlin` — Kotlin-код мода.
- `src/main/java` — Java-код и mixin-классы.
- `src/main/antlr` — исходные грамматики парсеров; Java-парсеры генерируются при сборке.
- `src/main/resources` — актуальные метаданные, переводы, иконки и конфигурации из присланного JAR.
- `libs` — зафиксированные моды-зависимости для компиляции.
- `gradle/wrapper`, `gradlew`, `gradlew.bat` — Gradle Wrapper.
- `.github/workflows/build.yml` — проверка сборки на GitHub.
- `CHANGES.md` — сравнение с оригинальным IPN 2.3.8.
- `RESTORATION.md` — происхождение исходников и границы проверки.

## Версия

Текущее значение версии: `2.3.8+26.3`.
При изменении версии синхронизируйте `gradle.properties`,
`src/main/resources/fabric.mod.json` и `src/main/kotlin/org/anti_ad/mc/ipnext/gen/ModInfo.kt`.
Название готового JAR задаётся в блоке `shadowJar` в `build.gradle`.

## Исходный проект и лицензия

Основа: [Inventory Profiles Next](https://codeberg.org/Inventory-Profiles-Next-Mod/IPN),
версия [v2.3.8](https://codeberg.org/Inventory-Profiles-Next-Mod/IPN/src/tag/v2.3.8).

Исходный IPN: Mirinimi / Plamen K. Kosseff, jsnimda и другие участники проекта.
Авторские и лицензионные уведомления сохранены.
Лицензия: **AGPL-3.0-or-later**, см. `LICENSE`.
Сторонние зависимости имеют собственные лицензии.
