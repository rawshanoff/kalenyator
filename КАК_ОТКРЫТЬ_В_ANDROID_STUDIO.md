# Как открыть проект в Android Studio

## Ошибка «Directory does not contain a Gradle build»

Вы открыли **не ту папку**. Gradle-проект лежит здесь:

```
G:\Projects\Calculator
```

или (то же самое):

```
G:\Projects\Calculator\android
```

**Нельзя** открывать только `android\app` — там нет `settings.gradle.kts`.

---

## Правильные шаги

1. **File → Close Project** (закройте текущий проект).
2. **File → Open**.
3. Выберите папку **`G:\Projects\Calculator`** (корень, где лежат `calculator.html` и папка `android`).
4. Нажмите **OK**.
5. Дождитесь **Gradle Sync** (первый раз 5–15 минут — скачаются зависимости).
6. Если спросит JDK: **File → Settings → Build → Build Tools → Gradle → Gradle JDK** → выберите **JDK 17** или **Embedded JDK**.

## Собрать APK

**Build → Build Bundle(s) / APK(s) → Build APK(s)**

Готовый файл:

```
android\app\build\outputs\apk\debug\app-debug.apk
```

## Если Sync снова падает

- **File → Invalidate Caches → Invalidate and Restart**
- Проверьте интернет (Gradle качает библиотеки).
- Убедитесь, что установлен **Android SDK** (Tools → SDK Manager → Android 14/15).

## Раздача APK через GitHub

См. **[GITHUB_RELEASES.md](GITHUB_RELEASES.md)** — репозиторий, `github.properties`, автоматический Release по тегу `v*`.
