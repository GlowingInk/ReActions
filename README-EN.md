# ReActions
[**RuBukkit thread**](http://rubukkit.org/threads/165857/) | [**Documentation** (Russian)](https://github.com/imDaniX/ReActions/wiki) | [**Original**](https://github.com/Redolith/ReActions)

ReActions plugin for Paper is a tool to handle server events. It gives you an ability to check for specific flags, 
and by its results, perform different actions. Syntax is unbelievable simple:
```yaml
EVENT:
  activator:
    condition-check: condition
    flags:
     - FLAG=test
     - ANOTHER_FLAG=test:value
    actions:
     - ACTION=do:something
    reactions:
     - ACTION=do:{something, but if flag wasn't successfull}
     - ANOTHER_ACTION=do something else
```

## Get it

To compile the plugin all you need is to have Maven. So, if you already have it, just execute `mvn clean package`.

You can get the latest stable release from the
**[Releases ![Release](https://img.shields.io/github/release/imDaniX/ReActions.svg)](https://github.com/imDaniX/ReActions/releases/latest/)** tab.

Latest snapshot is available in the
**[Actions ![Snapshot](https://github.com/imDaniX/ReActions/workflows/Java%20CI/badge.svg)](https://github.com/imDaniX/ReActions/actions)** tab.

## License

Project is published under [GPL v3](LICENSE.md).

Also, project contains some of edited code from [Quartz Scheduler](https://github.com/quartz-scheduler/quartz) and
[SpringFramework](https://github.com/spring-projects/spring-framework) projects under 
[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).

## [Developed using IntelliJ IDEA by JetBrains](https://www.jetbrains.com/)
