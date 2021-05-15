[![RediSkript Build](https://github.com/Limework/RediSkript/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/Limework/RediSkript/actions/workflows/maven.yml)

RediSkript allows you to communicate between your servers with use of Redis, it's very fast and easy to use.

Skript: https://github.com/SkriptLang/Skript

RediSkript spigot page: https://www.spigotmc.org/resources/rediskript-communicate-between-servers-with-ease.85067/

Jedis: https://github.com/redis/jedis

You can transfer any data in the form of text between your servers, you can program it to execute a set of instructions on the server depending on the redis message, etc. This can be used for making scripts that sync data between all servers and much more!

It is developed and maintained by Govindas & the team of Govindas Limework developers.

Redis Message:
```
on redis message:
  if redis channel is "world":
    broadcast "%redis message% %redis channel% %redis message date%"
 
command /sendredis <text> <text>:
  usage: /sendredis <message> <channel>
  trigger:
    send redis message arg 1 to channel arg 2
    send redis message "hello world!" to channel "world"
```
Managing variables:
```
set variables "test::1", "test::2", "test::3" in channel "global" to 100
#then use this in any server that listens to "global" redis channel and was online when the above line was executed:
send "%{test::*}%" #outputs 100, 100 and 100

delete variables "test::*" in channel "global"

set variable "test::%uuid of player%" in channel "playerdata" to tool of player
#then you can in any server that is listening to "playerdata" channel and was online when the above line was executed:
give {test::%uuid of player%} to player
```
Syntax:
```
variable[s] %strings% in [redis] [channel] %string%
```

There is only one command: /reloadredis it fully reloads the configuration, you can reload IP, password, channels and everything else.

You only need to have matching configuration in every server for communication and a Redis server to connect to. I recommend using VPS for hosting redis server, I personally use VPS from humbleservers.com.

Configuration:
```
Redis:
  #a secure password that cannot be cracked, please change it!
  #it is also recommended to firewall your redis server with iptables so it can only be accessed by specific IP addresses
  Password: "yHy0d2zdBlRmaSPj3CiBwEv5V3XxBTLTrCsGW7ntBnzhfxPxXJS6Q1aTtR6DSfAtCZr2VxWnsungXHTcF94a4bsWEpGAvjL9XMU"
  Host: "127.0.0.1"
  #must be 2 or higher, if you set to lower, the addon will automatically use 2 as a minimum
  MaxConnections: 2
  #the default Redis port
  Port: 6379
  #time out in milliseconds, how long it should take before it decides that it is unable to connect when sending a message
  #90000 = 90 seconds
  TimeOut: 90000
  #also known as SSL, only use this if you're running Redis 6.0.6 or higher, older versions will not work correctly
  #it encrypts your traffic and makes data exchange between distant servers completely secure
  useTLS: false
  #may be useful if you cannot use TLS due to use of older version of Redis
  #however this will not encrypt the initial authentication password, only the messages sent
  #it uses AES-128 SIV encryption which is secure enough for this
  EncryptMessages: true
  EncryptionKey: "16CHARACTERS KEY"
  MacKey: "16CHARACTERS KEY"

#the channels from which this server can receive messages
#you can always send messages to all channels!
#you can add as many channels as you wish!
Channels:
  - "Channel1"
  - "Channel2"
  - "Channel3"
  ```
