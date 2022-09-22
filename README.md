# ReActions
[**Тема на RuBukkit**](http://rubukkit.org/threads/165857/) | [**Документация**](https://github.com/imDaniX/ReActions/wiki) | [**Оригинал**](https://github.com/Redolith/ReActions)

The project is currently (still) in its early state and not available on large platforms like SpigotMC. Because of that,
this README file's language is Russian, as most of project's users are Russian-speaking. Although, 
[README-EN.md](README-EN.md) is still available.

Плагин ReActions для Paper представляет собой инструмент обработки событий. Он позволяет проверять выполнение условий и,
в зависимости от результата проверки, выполнять разные действия. Синтаксис плагина устроен донельзя просто:
```yaml
СОБЫТИЕ:
  активатор:
    условие-события: условие
    flags:
     - ПРОВЕРКА=тест
     - ДРУГАЯ_ПРОВЕРКА=тест:значение
    actions:
     - ДЕЙСТВИЕ=делать:дело
    reactions:
     - ДЕЙСТВИЕ=делать:{дело, если флаг не сработал}
     - ДРУГОЕ_ДЕЙСТВИЕ=делать другое дело
```

## Взять к себе

Для компиляции плагина требуется Maven. Имея его на руках, достаточно войти в корневую папку проекта и ввести `mvn clean package`.

Если вы просто хотите скачать плагин - возьмите его из вкладки [Actions ![Snapshot](https://github.com/imDaniX/ReActions/workflows/Java%20CI/badge.svg)](https://github.com/imDaniX/ReActions/actions)

## Лицензия

Проект опубликован под лицензией [GPL v3](LICENSE.md).

Кроме того, в проекте используется редактированная часть исходного кода 
[Quartz Scheduler](https://github.com/quartz-scheduler/quartz) под лицензией 
[Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0). Изменения включают в себя: рекомендации Intellij IDEA; 
рефакторинг коллекций с использованием библиотеки [FastUtil](https://fastutil.di.unimi.it/).

## [Разработано с IntelliJ IDEA от JetBrains](https://www.jetbrains.com/)
