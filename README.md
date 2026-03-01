# LPT-Utilities
LPT-Utilities is a library that provides various utilities such as encryption and encrypted RCON communication.
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
### Logger
To get an output from the methods you need to initialize the logger `Util.LOGGER`
```java
public static void main(String[] args) throws Exception {
    Util.LOGGER = new Logger() {
        @Override
        public void info(String message) {
            System.out.println(message);
        }
        
        @Override 
        public void error(String message) {
            System.err.println(message);
        }
    };
}
```

### RconClient
```java
public static void main(String[] args) throws Exception {
    try (RconClient client = RconClient.connect("localhost", 25570)) {
        if (client.authenticate("Ch4ng3-M3")) {
            client.sendCommand("say hello");
        } else {
            System.out.println("Failed to authenticate");
        }
    }
}
```

### RconServer
```java
public static void main(String[] args) throws Exception {
    RconServer rconServer = new RconServer(
            25570,
            "Ch4ng3-M3",
            new CommandHandler()
    );
}
```

### MessageHandler
This class is called when the server receives a message
```java
public class CommandHandler implements MessageHandler {
    @Override
    public String[] handleMessage(String message) {
        LOGGER.info("Received message: {}", message);
        
        return new String[] { "hello" };
    }
}
```

## Add LPT-Utilities to your project

### Gradle
Add to `build.gradle`:
```gradle
repositories {
    mavenCentral()
    maven { url = "https://jitpack.io" }
}

dependencies {
    implementation 'com.github.LasseTheBabo:LPT-Utilities:v2.1'
}
```

### Maven
```xml
<dependency>
    <groupId>com.github.LasseTheBabo</groupId>
    <artifactId>LPT-Utilities</artifactId>
    <version>v2.1</version>
</dependency>
```