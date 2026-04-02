# Staircast
A [Project Zomboid](https://store.steampowered.com/app/108600/Project_Zomboid/) mod that lets the player see upstairs while standing on a staircase.

**Supported game version:** 42.13 and above

## Installation
**From Steam:** [Subscribe on Workshop](https://steamcommunity.com/sharedfiles/filedetails/?id=3684713089) and install [ZombieBuddy](https://steamcommunity.com/workshop/filedetails/?id=3619862853).

**From source:** Clone this repo into `~/Zomboid/Workshop/`, install [ZombieBuddy](https://steamcommunity.com/workshop/filedetails/?id=3619862853), and build.

## Building
**Prerequisites:** JDK 17, Maven

Install local artifacts with the actual paths and versions from your setup:
```sh
mvn install:install-file -DgroupId=projectzomboid -DartifactId=projectzomboid -Dpackaging=jar -Dfile=/path/to/your/projectzomboid.jar -Dversion=42.13.2
mvn install:install-file -DgroupId=me.zed_0xff -DartifactId=zombie_buddy -Dpackaging=jar -Dfile=/path/to/your/ZombieBuddy.jar -Dversion=1.6.4
```
Then from the source root:
```sh
mvn package
```
### Build 41
Before installing artifacts, generate the jar from your game directory:
```sh
jar cf projectzomboid.jar -C . astar -C . com -C . de -C . fmod -C . javax -C . N3D -C . org -C . se -C . zombie
```
Then install it as usual and build with the `b41` profile:
```sh
mvn package -P b41
```

## License
[MIT](LICENSE)
