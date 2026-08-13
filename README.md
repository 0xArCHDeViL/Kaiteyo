<div align="center">

  <img src="preview_assets/kaiteyo_logo.svg" height=120 style="border-radius: 20px;">

  # Kaiteyo (書いてよ)
  ![Version Badge](https://img.shields.io/badge/version-v1.1.0-blue?style=for-the-badge&labelColor=1A1A1A&color=C2FC8B)
  ![License](https://img.shields.io/badge/license-GPLv3-green?style=for-the-badge&labelColor=1A1A1A&color=FEAB57)

  **An offline-first Japanese learning application for Android phones and tablets**

  [![Android](https://img.shields.io/badge/Android%2012%2B-1A1A1A?style=for-the-badge&logo=android&logoColor=white)](https://github.com/0xArCHDeViL/Kaiteyo/releases)

</div>

---

## About Kaiteyo

Kaiteyo (書いてよ) — "write it!" in Japanese — is an Android-only application for learning Japanese. Phones use a portrait-only experience; tablets and pads support portrait and landscape with the same mobile screen flow. Originally based on Kanji Dojo by syt0r, Kaiteyo is now independently developed with its own learning architecture, data pipeline, and feature set.

### Features

- **Study kanji and kana** — Follow JLPT levels or school grades
- **Spaced repetition** — Scientifically sound SRS review system
- **Custom decks** — Create your own study decks from 6000+ characters
- **Built-in dictionary** — Search letters and words with definitions
- **Flashcards** — Study words with interactive flashcards
- **Writing practice** — Stroke order diagrams and drawing canvas
- **Offline-first** — Works completely offline
- **Android tablet experience** — Portrait and landscape with the same touch-first mobile flow
- **Android phone experience** — Portrait-only interaction model optimized for touch
- **Native delivery target** — ARM64-v8a devices running Android 12 or newer

## Screenshots

<p float="left">
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" height="400"/>
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" height="400"/>
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/3.png" height="400"/>
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/4.png" height="400"/>
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/5.png" height="400"/>
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/6.png" height="400"/>
</p>

## Downloads

### Android

[![Play Store](https://img.shields.io/badge/Google_Play-414141?style=for-the-badge&logo=google-play&logoColor=white)](https://play.google.com/store/apps/details?id=ua.syt0r.kanji)
[![F-Droid](https://img.shields.io/badge/F--Droid-1976D2?style=for-the-badge&logo=f-droid&logoColor=white)](https://f-droid.org/en/packages/ua.syt0r.kanji.fdroid/)
[![GitHub Releases](https://img.shields.io/badge/GitHub_Releases-1A1A1A?style=for-the-badge&logo=github&logoColor=white)](https://github.com/0xArCHDeViL/Kaiteyo/releases/latest)


## Quick Start

```bash
# Clone the repository
git clone https://github.com/0xArCHDeViL/Kaiteyo.git
cd kaiteyo

# Build the Android debug APK
./gradlew :app:assembleDebug

# Build the ARM64 Android release APK
./gradlew :app:assembleRelease
```

## Documentation

The project includes comprehensive documentation in the `/docs` directory:

| Category | Location |
|----------|----------|
| Architecture | `docs/06_ARCHITECTURE.md` |
| Development | `docs/07_DEVELOPMENT_GUIDE.md` |
| Contribution | `docs/08_CONTRIBUTING.md` |
| Coding standards | `docs/12_CODING_STANDARDS.md` |
| Release process | `docs/14_RELEASE_PROCESS.md` |
| Active backlog | `docs/planning/` |
| Android troubleshooting | `docs/troubleshooting/` |

## Technical Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose for Android
- **DI**: Koin
- **Database**: SQLDelight
- **Networking**: Ktor
- **Preferences**: DataStore
- **Build**: Gradle with version catalog

## Contributing

Contributions are welcome! Please read:

1. `docs/08_CONTRIBUTING.md` — Contribution guidelines and validation requirements
2. `docs/07_DEVELOPMENT_GUIDE.md` — Android setup and build commands
3. `docs/12_CODING_STANDARDS.md` — Kotlin, Compose, data, and repository hygiene standards

## License

> (c) 2022-2023 Yaroslav Shuliak (original Kanji Dojo)
> 
> Kaiteyo is a fork of Kanji Dojo. It is independently developed with its own design language, roadmap, branding, and feature set.
>
> This is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
> 
> This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
> 
> You should have received a copy of the GNU General Public License along with this app. If not, see https://www.gnu.org/licenses/.

## Credits

Originally based on [Kanji Dojo](https://github.com/syt0r/Kanji-Dojo) by syt0r. Kaiteyo is now independently developed.

### Data Sources

- **KanjiVG** — Writing strokes, radicals information (CC BY-SA 3.0)
- **Kanji Dic** — Character info, meanings, readings (CC BY-SA 3.0)
- **Tanos by Jonathan Waller** — JLPT classification (CC BY)
- **JMDict** — Japanese-Multilingual dictionary (CC BY-SA 4.0)
- **JmdictFurigana** — Furigana resource (CC BY-SA 4.0)
- **Leeds University Frequency List** — Word frequency ranking (CC BY)
- **yomichan-jlpt-vocab** — JLPT vocabulary tags (CC BY-SA 4.0)
