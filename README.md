# Simple Telegram Command Bot Spring Boot Starter

[![Jitpack](https://jitpack.io/v/ndanhkhoi/simple-telegram-command-bot-spring-boot-starter.svg)](https://jitpack.io/#ndanhkhoi/simple-telegram-command-bot-spring-boot-starter)
[![MIT License](http://img.shields.io/badge/license-MIT-blue.svg?style=flat)](https://github.com/ndanhkhoi/simple-telegram-command-bot-spring-boot-starter/blob/master/LICENSE)
[![gradle-publish](https://github.com/ndanhkhoi/simple-telegram-command-bot-spring-boot-starter/actions/workflows/gradle-publish.yml/badge.svg)](https://github.com/ndanhkhoi/simple-telegram-command-bot-spring-boot-starter/actions/workflows/gradle-publish.yml)
[![Code Grade](https://api.codiga.io/project/32027/status/svg)](https://app.codiga.io/project/32027/dashboardd)
[![Code Quality Score](https://api.codiga.io/project/32027/score/svg)](https://app.codiga.io/project/32027/dashboard)

A simple-to-use library to create Telegram Long Polling Bots in Java and Spring Boot with syntax like Spring MVC

- [Simple Telegram Command Bot Spring Boot Starter](#simple-telegram-command-bot-spring-boot-starter)
  * [Usage](#usage)
  * [How to use](#how-to-use)
  * [BotRoute](#botroute)
  * [CommandMapping](#commandmapping)
  * [CommandDescription](#commanddescription)
  * [Authorization](#authorization)
  * [Supported arguments](#supported-arguments)
    + [Arguments by type](#arguments-by-type)
    + [Arguments by annotation](#arguments-by-annotation)
  * [Supported return values](#supported-return-values)
    + [Single value](#single-value)
    + [Collection value](#collection-value)
    + [Reactive support](#reactive-support)
  * [Default Commands](#default-commands)
  * [Logging Channel](#logging-channel)
  * [Handle Exception](#handle-exception)
    + [BotRouteAdvice](#botrouteadvice)
    + [BotExceptionHandler](#botexceptionhandler)
    + [Supported return values of BotExceptionHandler](#supported-return-values-of-botexceptionhandler)
  * [CallbackQuerySubscriber](#callbackquerysubscriber)
  * [Others Subscriber Bean](#others-subscriber-bean)
  * [Configurations](#configurations)
    + [Properties](#properties)
  * [Dependencies](#dependencies)
  * [Telegram Bot API](#telegram-bot-api)
  * [Jitpack](#jitpack)
  * [License](#license)

<small><i><a href='http://ecotrust-canada.github.io/markdown-toc/'>Table of contents generated with markdown-toc</a></i></small>


## Usage

Just import add the library to your project with one of these options:

1. Using Maven Central Repository:
- Step 1. Add the JitPack repository to your build file
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
- Step 2. Add the dependency
```xml
<dependency>
    <groupId>com.github.ndanhkhoi</groupId>
    <artifactId>simple-telegram-command-bot-spring-boot-starter</artifactId>
    <version>2022.09.17</version>
</dependency>
```
2. Using Gradle:
- Step 1. Add the JitPack repository to your build file
```gradle
repositories {
    maven { url 'https://jitpack.io' }
}
```
- Step 2. Add the dependency
```gradle
dependencies {
    implementation 'com.github.ndanhkhoi:simple-telegram-command-bot-spring-boot-starter:2022.09.17'
}
```

## How to use

- Create spring boot application from [Spring Initializr](https://start.spring.io)

- Add [Telegram Bot Java Library](https://github.com/rubenlagus/TelegramBots) as dependency

- Add [Simple Telegram Command Bot Spring Boot Starter](https://github.com/ndanhkhoi/simple-telegram-command-bot-spring-boot-starter)

- Add bot's properties to your **application.yml** 

```yaml
khoinda:
  bot:
    username: {YOUR_BOT_USERNAME}
    token: {YOUR_BOT_TOKEN}
    bot-route-packages:
        - {YOUR_BOT_ROUTES_PACKAGE}
```
- Create your bot route

```java
@BotRoute
public class HelloWorldBotRoute {

    @CommandDescription("Say hello world")
    @CommandMapping(value = "/hi", allowAllUserAccess = true)
    public String hi(Update update) {
        return "Hello world";
    }

}

```
## BotRoute
An annotation indicates that a particular class serves the role of a router.

## CommandMapping
An annotation is used to map bot requests to route methods. 

## CommandDescription
An annotation is used to describe command. You can see it which default `/help` command

## Authorization
You can authorize command with these properties in `@CommandMapping` annotaion:
- `allowAllUserAccess` - boolean, if true all users/group can be call this command
- `allowAllGroupAccess` - a flag to mark a command can be called by any groups
- `accessUserIds` - an array contains user id can call this command
- `accessGroupIds` - an array contains group id can call this command
- `accessMemberIds` - an array contains user id can call this command in the group
- `onlyAdmin` - boolean, if true only admin of group can be call this command
- `onlyForGroup` - boolean, a flag to mark a command can be called in groups
- `onlyForOwner` - boolean, if true only bot's owner can be call this command

## Supported arguments
### Arguments by type
- `Update` - An Update object of telegram bot API
- `Message` - A message object of telegram bot API
- `List<PhotoSize>` - If message contains photo, it will be hold them, or else it will be return `null`
- `Document` - If message contains file, it will be hold them, or else it will be return `null`
### Arguments by annotation
- `@CommandName` - An annotation to mark a param in command method as a command name
- `@CommandBody` - An annotation to mark a param in command method as a command body
- `@ChatId` - An annotation to mark a param in command method as a chat id, can be use on `Long` type
- `@SendUserId` - An annotation to mark a param in command method as a user id, can be use on `Long` type
- `@SendUsername` - An annotation to mark a param in command method as a username, can be use on `String` type

## Supported return values

### Single value
- `String` - the text will be reply to user make a request
- `InputFile/File/byte[]/ByteArrayResource` - the file will be reply to user make a request
- `BotApiMethod` - it will be excuted automatically
- `Void` - do nothing

### Collection value
- `Collection<T>` - with T is one of single value types, it will be do a same job with single value types for each element in this collection

### Reactive support
- `Mono<T>` - same as single value but for Reactive
- `Flux<T>` - same as collection value but for Reactive

## Default Commands
- `/help` - List of available command(s) for this chat
- `/get_log_file` - Get an application log file. This command must be called by owner of a bot in `khoinda.bot.bot-owner-chat-id` in application.properties or application.yml

## Logging Channel
If you want to send log when new update received, you can config your channel id to `khoinda.bot.logging-chat-id` in application.properties or application.yml

## Handle Exception

Here is an example for handler of `NoSuchElementException`. When bot command request throw `NoSuchElementException`, it will reply a text: `"404 Not Found !"`
```java
@BotRouteAdvice
public class RouteAdvice {

    @BotExceptionHandler(NoSuchElementException.class)
    public String handleNoSuchElement(Update update, NoSuchElementException ex) {
        return "404 Not Found !";
    }

}
```

### BotRouteAdvice
An annotation indicates that a particular class serves the role of a router for exception.

### BotExceptionHandler
An annotation is used to mark method is a exception handler.

### Supported return values of BotExceptionHandler
- `String` - the text will be replied to user make a request
- `BotApiMethod` - it will be excuted automatically

## CallbackQuerySubscriber

You can create a bean that inplements `CallbackQuerySubscriber` to trigger callback query:

```java
@Component
public class CustomCallbackQuerySubscriber implements CallbackQuerySubscriber {

    private final SimpleTelegramLongPollingCommandBot simpleTelegramLongPollingCommandBot;
    
    public CustomCallbackQuerySubscriber(SimpleTelegramLongPollingCommandBot simpleTelegramLongPollingCommandBot) {
        this.simpleTelegramLongPollingCommandBot = simpleTelegramLongPollingCommandBot;
    }

    @Override
    public void accept(Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Callback query data: " + update.getCallbackQuery().getData());
        sendMessage.setChatId(update.getCallbackQuery().getMessage().getChatId() + "");
        simpleTelegramLongPollingCommandBot.executeSneakyThrows(sendMessage);
    }

}

```
## Others Subscriber Bean

There are some beans that you can create an inplements to do your stuff
- `CallbackQuerySubscriber` - handle callback query (like button pressed, etc, ...)
- `CommandNotFoundUpdateSubscriber` - handle unknown commands
- `NonCommandUpdateSubscriber` - handle an update does not contain command
- `PreSubscriber` - do your job before process command
- `PosSubscriber` - do your job after process command

## Configurations

### Properties
By default, you can configure only these properties:

| Property                                | Description                                                      | Default value       |
|-----------------------------------------|------------------------------------------------------------------|---------------------|
| khoinda.bot.username                    | Bot's username                                                   |                     |
| khoinda.bot.token                       | Bot's token                                                      |                     |
| khoinda.bot.logging-chat-id             | Chat id can received logging when new `Update` recieved          |                     |
| khoinda.bot.bot-owner-chat-id           | Chat id of bot's owner                                           | `new ArrayList<>()` |
| khoinda.bot.bot-route-packages          | Package(s) name that includes BotRoute class                     | `new ArrayList<>()` |
| khoinda.bot.enable-update-trace         | Enable /update_trace for owner                                   | `false`             |
| khoinda.bot.disable-default-commands    | Disable /help, /start by default                                 | `false`             |
| khoinda.bot.executor.core-pool-size     | Bot executor core pool size                                      | `8`                 |
| khoinda.bot.executor.max-pool-size      | Bot executor max pool size                                       | `Integer.MAX_VALUE` |
| khoinda.bot.executor.queue-capacity     | Bot executor queue capacity                                      | `Integer.MAX_VALUE` |
| khoinda.bot.executor.thread-name-prefix | Bot executor thread name prefix                                  | `bot-task-`         |
| khoinda.bot.register-delay              | Number of second(s) delay to register bot when application ready | `0`                 |

## Dependencies
This library uses following dependencies:
1. [Spring Boot Starter](https://github.com/spring-projects/spring-boot/tree/main/spring-boot-project/spring-boot-starters)
2. [Telegram Bot Java Library](https://github.com/rubenlagus/TelegramBots)
3. [Lombok](https://github.com/projectlombok/lombok)
4. [Reactor Core](https://github.com/reactor/reactor-core)
5. [Reactor Extra Utilities](https://github.com/reactor/reactor-addons)
6. [Google Guava](https://github.com/google/guava)
7. [Apache Commons Lang](https://github.com/apache/commons-lang)
8. [Apache Commons IO](https://github.com/apache/commons-io)

## Telegram Bot API
This library use [Telegram bot API](https://core.telegram.org/bots), you can find more information following the link.

## Jitpack

More infomation from [Jitpack](https://jitpack.io/#ndanhkhoi/simple-telegram-command-bot-spring-boot-starter)


## License
MIT License

Copyright (c) 2021 Nguyen Duc Anh Khoi

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
