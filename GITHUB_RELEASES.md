# GitHub Releases для Каленятора

Автоматическая сборка APK и раздача семье через [GitHub Releases](https://docs.github.com/en/repositories/releasing-projects-on-github).

## Что уже настроено в проекте

| Файл | Назначение |
|------|------------|
| `.github/workflows/release.yml` | Сборка APK и публикация при теге `v*` или вручную |
| `github.properties.example` | Шаблон: ваш логин GitHub и имя репозитория |
| `android/app/build.gradle.kts` | Подставляет URL обновления в APK |

Имя файла на Releases всегда **`kalenyator.apk`** — тогда ссылка «последняя версия» не меняется:

```text
https://github.com/ВАШ_ЛОГИН/ИМЯ_РЕПО/releases/latest/download/kalenyator.apk
```

Кнопка **Настройки → Проверить обновление APK** откроет эту ссылку в браузере.

---

## Шаг 1. Репозиторий на GitHub

1. На [github.com](https://github.com) нажмите **New repository**.
2. Имя, например: `Calculator` (или `Kalenyator`).
3. **Public** или **Private** (для семьи часто хватает private + доступ по invite).
4. **Не** добавляйте README с GitHub, если уже есть локальный проект.

В папке проекта (PowerShell):

```powershell
cd G:\Projects\Calculator
git init
git add .
git commit -m "Initial commit: Kalenyator Android app"
git branch -M main
git remote add origin https://github.com/ВАШ_ЛОГИН/ИМЯ_РЕПО.git
git push -u origin main
```

Замените `ВАШ_ЛОГИН` и `ИМЯ_РЕПО`.

---

## Шаг 2. `github.properties`

```powershell
copy github.properties.example github.properties
```

Откройте `github.properties`:

```properties
github.owner=ravshan-example
github.repo=Calculator
```

Пересоберите APK локально — в приложении подставится правильный URL:

```powershell
.\gradlew.bat :app:assembleDebug
```

---

## Шаг 3. Первый релиз

### Вариант A — через Git (рекомендуется)

Перед тегом обновите версию в `android/app/build.gradle.kts`:

- `versionName` — например `1.3.0`
- `versionCode` — увеличьте на 1

```powershell
git add .
git commit -m "Release 1.3.0"
git tag v1.3.0
git push origin main
git push origin v1.3.0
```

После push тега откройте **Actions** на GitHub — workflow **Release APK** соберёт APK и создаст Release.

### Вариант B — вручную в GitHub

1. **Actions** → **Release APK** → **Run workflow**.
2. Укажите тег, например `v1.3.0`.
3. После успеха: **Releases** → скачайте `kalenyator.apk`.

### Вариант C — загрузить APK вручную

1. **Releases** → **Draft a new release**.
2. Tag: `v1.3.0`, прикрепите APK с именем **`kalenyator.apk`** (имя важно).
3. **Publish release**.

---

## Шаг 4. Раздать семье

- Ссылка на страницу релизов:  
  `https://github.com/ВАШ_ЛОГИН/ИМЯ_РЕПО/releases`
- Прямая ссылка на последний APK:  
  `https://github.com/ВАШ_ЛОГИН/ИМЯ_РЕПО/releases/latest/download/kalenyator.apk`

На телефоне: скачать → разрешить установку из браузера → открыть APK.

---

## Подписанный release (по желанию)

Сейчас CI собирает **debug** APK (без keystore) — для семьи этого обычно достаточно.

Для подписанного **release**:

1. **Build → Generate Signed Bundle / APK** в Android Studio.
2. Локально: `.\gradlew.bat assembleRelease`
3. Загрузите `app-release.apk` на Releases как `kalenyator.apk`.

Подпись в CI через секреты GitHub (`KEYSTORE_BASE64`, пароли) можно добавить позже — напишите, если нужен отдельный workflow.

---

## Обновление приложения у родственников

1. Выпустите новый тег `v1.3.1`, workflow загрузит новый `kalenyator.apk`.
2. URL `.../releases/latest/download/kalenyator.apk` **не меняется** — пересборка старого APK не нужна, если `github.properties` уже был верным.
3. В приложении: **Настройки → Проверить обновление APK** (или отправьте ссылку в Telegram).

---

## Частые проблемы

| Проблема | Решение |
|----------|---------|
| Toast «Укажите github.owner…» | Создайте `github.properties`, пересоберите APK |
| 404 при открытии ссылки | Ещё нет Release с файлом `kalenyator.apk` |
| Actions не запускается | Включите **Actions** в Settings репозитория |
| Private repo, ссылка не открывается | Семья должна войти в GitHub или раздавайте APK через Telegram |

---

## Проверка

```powershell
# Локальная сборка
.\gradlew.bat :app:assembleDebug

# После первого Release в браузере:
# https://github.com/ВАШ_ЛОГИН/ИМЯ_РЕПО/releases/latest/download/kalenyator.apk
```
