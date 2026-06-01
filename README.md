# Каленятор

Семейное Android-приложение: калькулятор, календарь с праздниками, погода и семейные даты (дни рождения, годовщины).

Прототип веб-версии: `calculator.html`  
Нативное приложение: папка `android/`

## Возможности

- **Калькулятор** — базовые операции и научные функции (корень, степень, sin/cos/tan, π)
- **Календарь** — государственные праздники Узбекистана и/или России (выбор региона)
- **Семейные даты** — добавление, редактирование, удаление; отображение в календаре
- **Погода** — Open-Meteo (Ташкент / Москва по региону), без API-ключа
- **Язык** — русский / oʻzbekcha
- **Синхронизация** — экспорт/импорт JSON через Telegram или файловый менеджер
- **Напоминания** — push за 1 / 3 / 7 / 14 дней до семейной даты
- **Виджет** — ближайшая дата на главном экране Android

## Сборка APK (для семьи)

> **Важно:** в Android Studio открывайте **`G:\Projects\Calculator`** (корень) или **`android/`** — не папку `android/app`.  
> Подробнее: [КАК_ОТКРЫТЬ_В_ANDROID_STUDIO.md](КАК_ОТКРЫТЬ_В_ANDROID_STUDIO.md)

1. Установите [Android Studio](https://developer.android.com/studio) (JDK 17+).
2. **File → Open** → папка **`Calculator`** (корень проекта).
3. **Build → Build Bundle(s) / APK(s) → Build APK(s)**  
   или в терминале из `android/`:
   ```bash
   .\gradlew.bat assembleDebug
   ```
4. APK: `android/app/build/outputs/apk/debug/app-debug.apk`

### Подписанный release (рекомендуется)

```bash
.\gradlew.bat assembleRelease
```

Создайте keystore в Android Studio: **Build → Generate Signed Bundle / APK**.

## Установка на телефоны племянников

1. Скопируйте APK (USB, Telegram, Google Drive) или скачайте с **GitHub Releases**.
2. На Android включите **Установка из неизвестных источников** для выбранного приложения.
3. Откройте APK и установите.

### GitHub Releases (обновления для семьи)

Пошаговая настройка: **[GITHUB_RELEASES.md](GITHUB_RELEASES.md)**

Кратко:

1. Репозиторий на GitHub → `git push`
2. `copy github.properties.example github.properties` → укажите `github.owner` и `github.repo`
3. Тег `git tag v1.3.0` → `git push origin v1.3.0` — Actions соберёт и выложит `kalenyator.apk`
4. В приложении: **Настройки → Проверить обновление APK** (ссылка на последний APK)

## Синхронизация семейных дат

1. На вашем телефоне: **Настройки → Экспорт дат (JSON)** → отправьте файл в семейный чат.
2. На другом телефоне: **Настройки → Импорт дат** → выберите файл.

## iOS (позже)

Код на Kotlin/Compose. Для iOS позже можно вынести общую логику в **Kotlin Multiplatform** или сделать отдельный SwiftUI-клиент с тем же JSON-форматом синхронизации.

## Стек

- Kotlin 2 · Jetpack Compose · Material 3
- Room · DataStore · Retrofit (Open-Meteo)
- minSdk 26 · targetSdk 35
