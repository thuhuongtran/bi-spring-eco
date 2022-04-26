### Spring Core: AOP
To quickly summarize, AOP stands for aspect orientated programming. Essentially, it is a way for adding behavior to existing code without modifying that code.

- Aspect – a standard code/feature that is scattered across multiple places in the application and is typically different than the actual Business Logic (for example, Transaction management). Each aspect focuses on a specific cross-cutting functionality
- Joinpoint – it's a particular point during execution of programs like method execution, constructor call, or field assignment
- Advice – the action taken by the aspect in a specific joinpoint
- Pointcut – a regular expression that matches a joinpoint. Each time any join point matches a pointcut, a specified advice associated with that pointcut is executed
- Weaving – the process of linking aspects with targeted objects to create an advised object

The annotation we are going to create is one which will be used to log the amount of time it takes a method to execute.
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogExecutionTime {

}
```
his is just the module that will encapsulate our cross-cutting concern, which is our case is method execution time logging. All it is is a class, annotated with @Aspect:

First, we have annotated our method with @Around. This is our advice, and around advice means we are adding extra code both before and after method execution.
```java
@Aspect
@Component
public class ExampleAspect {
    @Around("@annotation(LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }
}
```
We're also logging the method signature, which is provided to use the joinpoint instance. We would also be able to gain access to other bits of information if we wanted to, such as method arguments.
```java
@Component
public class AopService {
    @LogExecutionTime
    public void serve() throws InterruptedException {
        Thread.sleep(2000);
    }
}
```
##### Pointcut
```java
@Pointcut("execution(public String com.baeldung.pointcutadvice.dao.FooDao.findById(Long))")

@Pointcut("execution(* com.baeldung.pointcutadvice.dao.FooDao.*(..))")

@Pointcut("within(com.baeldung.pointcutadvice.dao.FooDao)")

@Pointcut("this(com.baeldung.pointcutadvice.dao.FooDao)")

@Pointcut("execution(* *..find*(Long,..))")

@Pointcut("@target(org.springframework.stereotype.Repository)")

@Pointcut("@args(com.baeldung.pointcutadvice.annotations.Entity)")

@Pointcut("within(@org.springframework.stereotype.Repository *)")

@Pointcut("@annotation(com.baeldung.pointcutadvice.annotations.Loggable)")

@Pointcut("repositoryMethods() && firstLongParamMethods()")
```
```java
@Around("repositoryClassMethods()")
public Object measureMethodExecutionTime(ProceedingJoinPoint pjp) throws Throwable {
    ...
}
```
##### Before and After
@Before is executed before the join point.

@After annotation, is executed after a matched method's execution, whether or not an exception was thrown.

@Around advice can perform custom behavior both before and after the method invocation. It's also responsible for choosing whether to proceed to the join point or to shortcut the advised method execution by providing its own return value or throwing an exception.

```java
@Component
@Aspect
public class LoggingAspect {

    private Logger logger = Logger.getLogger(LoggingAspect.class.getName());

    @Pointcut("@target(org.springframework.stereotype.Repository)")
    public void repositoryMethods() {};

    @Before("repositoryMethods()")
    public void logMethodCall(JoinPoint jp) {
        String methodName = jp.getSignature().getName();
        logger.info("Before " + methodName);
    }

    @AfterReturning(value = "entityCreationMethods()", returning = "entity")
    public void logMethodCall(JoinPoint jp, Object entity) throws Throwable {
        eventPublisher.publishEvent(new FooCreationEvent(entity));
    }

    @Around("repositoryClassMethods()")
    public Object measureMethodExecutionTime(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.nanoTime();
        Object retval = pjp.proceed();
        long end = System.nanoTime();
        String methodName = pjp.getSignature().getName();
        logger.info("Execution of " + methodName + " took " +
                TimeUnit.NANOSECONDS.toMillis(end - start) + " ms");
        return retval;
    }
}
```
### Spring Core: Null-safety
```@Nullable```: Annotation to indicate that a specific parameter, return value, or field can be null.

```@NonNull```: Annotation to indicate that a specific parameter, return value, or field cannot be null (not needed on parameters / return values and fields where @NonNullApi and @NonNullFields apply, respectively).

```@NonNullApi```: Annotation at the package level that declares non-null as the default semantics for parameters and return values.

```@NonNullFields```: Annotation at the package level that declares non-null as the default semantics for fields.

_Source: Baeldung, Spring docs_
