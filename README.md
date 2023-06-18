# ReActions
[**Тема на RuBukkit**](http://rubukkit.org/threads/165857/) | [**Документация**](https://github.com/GlowingInk/ReActions/wiki) | [**Оригинал**](https://github.com/Redolith/ReActions)

The project is currently (still) in its early state and is not available on large platforms like SpigotMC. Because of 
that, and as the most of project's users are Russian-speaking, this README file's language is Russian too. 
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

Последний стабильный релиз доступен во вкладке
**[Releases ![Release](https://img.shields.io/github/release/GlowingInk/ReActions.svg)](https://github.com/GlowingInk/ReActions/releases/latest/)**

Готовый снапшот можно скачать из вкладки 
**[Actions ![Snapshot](https://github.com/GlowingInk/ReActions/workflows/Java%20CI/badge.svg)](https://github.com/GlowingInk/ReActions/actions)** 

## Лицензия

Проект опубликован под лицензией [GPL v3](LICENSE.md).

Кроме того, в проекте используется редактированная часть исходного кода 
[Quartz Scheduler](https://github.com/quartz-scheduler/quartz) (класс `CronExpression`).

## [Разработано с IntelliJ IDEA от JetBrains](https://www.jetbrains.com/)
