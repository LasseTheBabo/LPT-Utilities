# LPT-Utilities
LPT-Utilities is a Minecraft library that provides various utilities such as encryption and encrypted RCON communication.
The code used in `org.lpt.util.rcon.packet` is a slightly
modified version of the code from https://github.com/jobfeikens/rcon

## Features
- **Encryption**
  - AES-256
  - RSA

- **Encrypted RCON**
    - AES-256 key is send with RSA
    - Messages are encrypted with AES-256
    - Build-in `RconClient` and `RconServer` classes

## Example Usage
### RconClient
```java
public static void main(String[] args) throws Exception {
    RconClient client = RconClient.connect("localhost", 25570);
    if (client == null)
        return;

    client.authenticate("password123");
    client.sendCommand("say hello");
    client.close();
}
```

### RconServer
```java
public static void main(String[] args) throws Exception {
    RconServer server = new RconServer(25570, "password123", new CommandHandler());
    server.open();
}
```

## Add LPT-Utilities to your project

### Gradle
Add to `build.gradle`:
```gradle
repositories {
    mavenCentral()
}

dependencies {
    implementation 'com.github.LasseTheBabo:LPT-Utilities:v1.4'
}
```

### Maven
```xml
<dependency>
    <groupId>com.github.LasseTheBabo</groupId>
    <artifactId>LPT-Utilities</artifactId>
    <version>v1.4</version>
</dependency>
```