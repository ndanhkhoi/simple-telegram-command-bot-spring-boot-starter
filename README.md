# Simple Telegram Command Bot Spring Boot Starter

[![Jitpack](https://jitpack.io/v/ndanhkhoi/simple-telegram-command-bot-spring-boot-starter.svg)](https://jitpack.io/#ndanhkhoi/simple-telegram-command-bot-spring-boot-starter)
[![MIT License](http://img.shields.io/badge/license-MIT-blue.svg?style=flat)](https://github.com/ndanhkhoi/simple-telegram-command-bot-spring-boot-starter/blob/master/LICENSE)

A simple-to-use library to create Telegram Long Polling Bots in Java and Spring Boot

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
    <version>0.1</version>
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
    implementation 'com.github.ndanhkhoi:simple-telegram-command-bot-spring-boot-starter:0.1'
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
    botRoutePackages: {YOUR_BOT_ROUTES_PACKAGE}
```
- Create your bot route

```java
@BotRoute
public class HelloWorldBotResource {

    @CommandDescription("Say hello world")
    @CommandMapping(value = "/Hi", allowAllUserAccess = true)
    public String hi(Update update) {
        return "Hello world";
    }

}

```
## Dependencies
This library uses following dependencies:
1. [Spring Boot Starter](https://github.com/spring-projects/spring-boot/tree/main/spring-boot-project/spring-boot-starters)
2. [Telegram Bot Java Library](https://github.com/rubenlagus/TelegramBots)
3. [Lombok](https://github.com/projectlombok/lombok)
4. [Reactor Core](https://github.com/reactor/reactor-core)
5. [Reactor Extra Utilities](https://github.com/reactor/reactor-addons)
6. [Reflections](https://github.com/ronmamo/reflections)
7. [Google Guava](https://github.com/google/guava)
8. [Apache Commons Lang](https://github.com/apache/commons-lang)
9. [Apache Commons IO](https://github.com/apache/commons-io)
10. [Apache Commons BeanUtils](https://github.com/apache/commons-beanutils)

## Telegram Bot API
This library use [Telegram bot API](https://core.telegram.org/bots), you can find more information following the link.

## Jitpack

More infomation from [Jitpack](https://jitpack.io/#ndanhkhoi/simple-telegram-command-bot-spring-boot-starter)


## License
MIT License

Copyright (c) 2021 Nguyễn Đức Anh Khôi

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
