# Conversation API
A compact API for conversations on Bukkit and Paper servers, built with [Kyori's Adventure](https://github.com/KyoriPowered/adventure). Requires Java 8 or newer.

For conversations on Fabric servers, see https://github.com/Browsit/ConversationAPI-Fabric

## Depend
```xml
   <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
   </repositories>
```
```xml
    <dependencies>
        <dependency>
            <groupId>com.github.Browsit.ConversationAPI</groupId>
            <artifactId>conversation-bukkit</artifactId>
            <version>LATEST</version>
        </dependency>
    </dependencies>
```

## Setup
```java
    @Override
    public void onEnable() {
        BukkitConversations.init(this);
    }
    
    @Override
    public void onDisable() {
        BukkitConversations.cleanUp();
    }
```

## Using the API
Creating a new Conversation:
```java
Conversations.create(player.getUniqueId())
        .start();
```
Under the hood, conversations are entirely managed - this means that you do not have to worry about registering, ending, or managing audiences. However, `run()` has to be called to make the conversation execute.

### Prompts
You can use Prompts to create interactive conversations and fetch user input. The way you manage this user input is entirely in your own control using Converters, Filters and finally a Fetch.
```java
Conversations.create(player.getUniqueId())
        .prompt("What's 2+2?", Integer.class, prompt -> prompt
        .attempts(3)
        .allAttemptsFailedText("You have ran out of attempts :(")
        .converter(Integer::parseInt)
        .conversionFailText("Only rounded numbers are accepted!")
        .filter(integer -> integer == 4)
        .filterFailText("Your answer was wrong!")
        .fetch((input, sender) -> sender.sendMessage("Correct! The answer was: " + input)))
        .endWhen(TimeClause.create(10000L, "Out of time (10 seconds)!"))
        .start();
```          
In this example, the audience is asked to solve the question in our prompt, for which they are given 3 attempts. First, we Convert the user input to an Integer, after that we use a Filter to check whether the given answer is correct or not, and finally we use a Fetch to actually retrieve the input.

Clauses are used to define when a conversation should end. Conversation API only comes with one Clause at the moment, the TimeClause. You can easily create your own Clauses if necessary.

### Miscellaneous
Conversation API comes with some other options that you can use, below is a list of them and what they do.
| Name | Functionality |
|------|---------------|
| .title(...) | Sets a text which can be later used to identify the conversation. |
| .prefix(...) | Sets a text which gets prepended to each line of the conversation. |
| .echo(boolean) | Sets whether the user input should be echo'd in chat or not |
| .chatVisibility(...) | Used to set which messages the conversation's audience can receive, e.g use to disable chat |
| .finishingText(...) | Sets a text to be displayed after the conversation has ended |
