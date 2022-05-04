#### Spring Shell
Spring Shell allows one to easily create such a runnable application, where the user will enter textual commands that will get executed until the program terminates.

```java
@ShellComponent
public class MyCommands {
    @ShellMethod("Add two integers together.")
    public int add(int a, int b) {
        return a + b;
    }
}
```
Build the application and run the generated jar, like so;
```
./mvnw clean install -DskipTests
[...]

java -jar target/demo-0.0.1-SNAPSHOT.jar
```
You’ll be greeted by the following screen:
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v1.5.6.RELEASE)

shell:>
```
```
shell:>add 1 2
3
```
_Source: Spring Shell Docs_