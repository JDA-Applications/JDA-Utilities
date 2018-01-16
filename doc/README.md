# Doc Package

Flexible runtime documentation for any bot's commands. With the CommandDoc system,
any bot can have organized, formatted, and detailed documentation of it's functions.

```java
@CommandInfo
(
    name = {"mycommand", "coolcommand"},
    description = "Use this command if you are cool! B)",
    requirements = {"You must be cool."}
)
@Error("You are not cool enough to use this command :(")
public class MyCommand
{
    // ...
}
```
