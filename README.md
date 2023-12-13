# Conversation API
A compact API for conversations on Bukkit and Fabric, built with [Kyori's Adventure](https://github.com/KyoriPowered/adventure).

## Setup
Conversations for Bukkit:
```java
    @Override
    public void onEnable() {
        Conversations.init(BukkitAudiences.create(this));
        new BukkitConversationsForwarder().register(this);
    }
    
    @Override
    public void onDisable() {
        Conversations.cleanUp();
    }
```

Conversations for Fabric:
```java
    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            Conversations.init(FabricServerAudiences.of(server));
            new FabricConversationsFowarder().register(server);
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            Conversations.cleanUp();
        });
    }
```

## Using the API
Creating a new Conversation:
```java
        new Conversation(Conversations.provider().player(player.getUniqueId()))
                .run();
```
Under the hood, Conversations are entirely managed - This means that you do not have to worry about registering, ending, or things like managing audiences. However, `run()` has to be called to make the conversation execute.

##### Prompts
You can use Prompts to create interactive conversations and fetch user input. The way you manage this user input is entirely in your own control using Converters, Filters and finally a Fetch.

Example;
```java
        new Conversation(Conversations.provider().player(player.getUniqueId()))
                .prompt(new Prompt<Integer>(Component.text("What's 2+2?"))
                        .attempts(3)
                        .allAttemptsFailedText(Component.text("You have ran out of attempts :("))
                        .converter(Integer::parseInt)
                        .conversionFailText(Component.text("Only rounded numbers are accepted!"))
                        .filter(integer -> integer == 4)
                        .filterFailText(Component.text("Your answer was wrong!"))
                        .fetch((input, sender) -> sender.sendMessage(Component.text("Correct! The answer was: " + input))))
                .run();
```          
In this example, the audience is asked to solve the question in our prompt, for which they are given 3 attempts. First, we Convert the user input to an Integer, after that we use a Filter to check whether the given answer is correct or not, and finally we use a Fetch to actually retrieve the input.

#### Clauses
Clauses are used to define when a conversation should end. AdventureConversations only comes with one Clause by default at the moment, the TimeClause. You can easily create your own Clauses if necessary.

Previous example but with a TimeClause, to end the conversation after 10 seconds;
```java
        new Conversation(Conversations.provider().player(player.getUniqueId()))
                .prompt(new Prompt<Integer>(Component.text("What's 2+2?"))
                        .attempts(3)
                        .allAttemptsFailedText(Component.text("You have ran out of attempts :("))
                        .converter(Integer::parseInt)
                        .conversionFailText(Component.text("Only rounded numbers are accepted!"))
                        .filter(integer -> integer == 4)
                        .filterFailText(Component.text("Your answer was wrong!"))
                        .fetch((input, sender) -> sender.sendMessage(Component.text("Correct! The answer was: " + input))))
                .endWhen(new TimeClause(10000L, Component.text("Out of time!")))
                .run();

```

#### Other Options
AdventureConversations comes with some other options that you can use, below is a list of them and what they do.
| Name | Functionality |
|------|---------------|
| .by(...) | Sets a name that gets prepended to each line of the conversation. |
| .echo(boolean) | Sets whether the user input should be echo'd in chat or not |
| .chatVisibility(...) | Used to set which messages the conversation's audience can receive, e.g use to disable chat |
| .finishingText(...) | Sets a text to be displayed after the conversation has ended |

## License
MIT