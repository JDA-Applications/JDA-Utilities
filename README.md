[version]: https://api.bintray.com/packages/jagrosh/maven/JDA-Utilities/images/download.svg
[download]: https://bintray.com/jagrosh/maven/JDA-Utilities/_latestVersion
[license]: https://img.shields.io/badge/License-Apache%202.0-lightgrey.svg
[issues]: https://img.shields.io/github/issues/JDA-Applications/JDA-Utilities.svg 
[issueslink]: https://github.com/JDA-Applications/JDA-Utilities/issues

[ ![version][] ][download]
[ ![license][] ](https://github.com/JDA-Applications/JDA-Utilities/tree/master/LICENSE)
[ ![issues][] ][issueslink]

## JDA-Utilities
JDA-Utilities is a series of tools and utilities for use with [JDA](https://github.com/DV8FromTheWorld/JDA) to assist in bot creation.

## Version 2.0 Notice
Version 2.0 will be a come with a lot of big (some breaking) changes to the library.

If you use this library for anything, it is **STRONGLY** recommended you read [this gist](https://gist.github.com/TheMonitorLizard/4f09ac2a3c9d8019dc3cde02cc456eee)
which documents all of the changes that will be made, as to better prepare yourself for moving to 2.0.

Note that the gist is in no way final, and will be modified possibly until the day that we release 2.0,
so it's a good idea to keep up-to-date with it in the coming weeks.

If you have questions or concerns about any of these changes, please contact **Shengaero**#9090.

## Getting Started
You will need to add this project as a dependency (either from the latest .jar from the releases page, or via maven or gradle), as well as [JDA](https://github.com/DV8FromTheWorld/JDA). With maven, you can use the snippets below:
```xml
  <dependency>
    <groupId>com.jagrosh</groupId>
    <artifactId>JDA-Utilities</artifactId>
    <version>1.9</version>
    <scope>compile</scope>
  </dependency>
  <dependency>
    <groupId>net.dv8tion</groupId>
    <artifactId>JDA</artifactId>
    <version>LATEST</version>
  </dependency>
```
```xml
  <repository>
    <id>central</id>
    <name>bintray</name>
    <url>http://jcenter.bintray.com</url>
  </repository>
```
With gradle:
```groovy
dependencies {
    compile 'com.jagrosh:JDA-Utilities:1.9'
    compile 'net.dv8tion:JDA:LATEST'
}

repositories {
    jcenter()
}
```

## Examples
Check out the [ExampleBot](https://github.com/jagrosh/ExampleBot) for a simple bot example.

Other guides and information can be found on the [wiki](https://github.com/JDA-Applications/JDA-Utilities/wiki).

## Projects
[**Vortex**](https://github.com/jagrosh/Vortex) - Vortex is an easy-to-use moderation bot that utilizes the JDA-Utilities library for the Command Client and some of the menus<br>
[**JMusicBot**](https://github.com/jagrosh/MusicBot) - This music bot uses the Command Client for its base, and several menus, including the OrderedMenu for search results and the Paginator for the current queue<br>
[**GiveawayBot**](https://github.com/jagrosh/GiveawayBot) - GiveawayBot is a basic bot for hosting quick giveaways!<br>
