# NFS TMXTOOL

This tool allows to encode TMX audio files for NFS ProStreet/Undercover/World games. Learn more here: [NFSMods](https://nfsmods.xyz/mod/4999)

## Compiling from source

In order to compile tmxtool, you'll need JDK 11 and Maven. After that navigate to the folder with the source code, where _pom.xml_ is located and run:
```
mvn clean package
```
After that, compiled jar will be located in _target_ folder. Enjoy!

## Changelog

- 1.1: Initial public release
- 1.1.1: Fixed an issue where Half Throttle and Full Throttle GIN's were in reverse positions
- 1.2: Added translations, changed "Launch GINTool" logic

## Contributing
If you want to contribute to the project (add a translation, etc.), don't hesitate to make pull requests. Example messages file can be seen here: [messages_en.properties](../main/src/main/resources/i18n/messages_en.properties)
