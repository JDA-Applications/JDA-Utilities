# Command Package

A easy to use, powerful, and highly flexible command framework for bots using JDA.

This package includes all the tools necessary to start a fully functioning JDA bot
with commands, making use of the **Command Client**.

```java
public class MyBot 
{
    public static void main(String[] args)
    {
        CommandClientBuilder builder = new CommandClientBuilder();
        
        // Set your bot's prefix
        builder.setPrefix("!");
        builder.setAlternativePrefix("+");
        
        // Add commands
        builder.addCommand(new CoolCommand());
        
        // Customize per-guild unique settings
        builder.setGuildSettingsManager(new MyBotsGuildSettingsManager());
        
        CommandClient client = builder.build();
        
        new JDABuilder(AccountType.BOT)
            // ...
            .addEventListeners(client) // Add the new CommandClient as a listener
            // ...
            .buildAsync();
    }
}
```
