# Installer
Starloaders semi-cross-plattform installer.

While the installer itself requires Java 8+, Starloader and StarloaderAPI will require Java 11+.
You can download Java 11+ at [Adoptium](https://adoptium.net/releases.html?variant=openjdk16&jvmVariant=hotspot),
[AdoptOpenJDK](https://adoptopenjdk.net/) or [Azul Zulu](https://www.azul.com/downloads/?package=jdk#download-openjdk).

Linux users beware: Your vendor OpenJDK may in most cases not work. As such the above advice stands.


## Behind the magic
The installer downloads a hardcoded starloader version and places it into the galimulator folder.
If you are on windows it then proceeds to replace the galimulator-windows-64-bit.exe file with
SL³ (which is also downloaded from a hardcoded place) and on linux it replaces the
galimulator-linux-64-bit file with
```sh
#!/bin/sh
java -jar starloader.jar
```

## Adding runtime arguments

## Windows
This can be done by using Steam's builtin `Launch Options` facitility.
To set the maximum heap size to 4Gb you will for example have `"java -jar starloader.jar -xmx"`
the quotation marks HAVE to be present at all times, otherwise it wont start.
To also boot up a console `--console` can be appended to the launch options, AFTER
the quotation marks. All other arguments have to be provided BEFORE the second quotation mark

## Linux
This can be done by modifing the beforementioned galimulator-linux-64-bit, steam will treat
the file like your ordinary shell script.

## A not about proton
If you run Galimulator via Proton or the Steam Linux runtime then the install will be ruined.
Instead, uncheck the `For use of a Specific Steam Play compatibillity Tool` in the `Compatibillity`
menu.
