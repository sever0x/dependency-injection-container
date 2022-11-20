# dependency-injection-container
## How to install
Run these commands in terminal:

    git clone https://github.com/shdwraze/dependency-injection-container.git
    cd dependency-injection-container
    mvn clean install

Add dependency in pom.xml:

        <dependency>
            <groupId>com.shdwraze</groupId>
            <artifactId>di-container</artifactId>
            <version>1.0</version>
        </dependency>

------------


## How to use
This example describes a simple project, which you can check out in this repository

Let's create some models
**Account model:**
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Account {
    private int id;

    private String login;

    private User user;
}
```

**User model:**
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String name;

    private String surname;
}
```

Let's create service interfaces
**AccountService interface:**
```java
public interface AccountService {
    List<Account> getAllAccounts();

    void createAccount(Account account);

    void removeAccount(Account account);

    Account getAccountById(int id);
}
```

**UserService interface:**
```java
public interface UserService {
    void getFullName(User user);
}
```

Let's add simple UserService interface implementations
UserServiceImpl class:
```java
@Component
public class UserServiceImpl implements UserService {

    @Override
    public void getFullName(User user) {
        System.out.println("My name is " + user.getName() + " " + user.getSurname());
    }
}
```
TestUserServiceImpl class:
```java
@Component
public class TestUserServiceImpl implements UserService {
    
    @Override
    public void getFullName(User user) {
        System.out.println("Full name " + user.getName() + " " + user.getSurname());
    }
}
```

Next, let's create an AccountService implementation that will use the @Autowired constructor to inject the UserService bean.
```java
public class AccountServiceImpl implements AccountService {

    private UserService userService;

    @Autowired
    public AccountServiceImpl(UserService userService) {
        this.userService = userService;
    }
```

Because we have multiple implementations of the UserService interface, we must specify which class to use for injection. We can use @Qualifier to do this:
```java
    @Autowired
    public AccountServiceImpl(@Qualifier("UserServiceImpl") UserService userService) {
        this.userService = userService;
    }
```
As a result, we will implement the UserServiceImpl class specifically, and not the first one found.

To execute any method with business logic, we can use @PostConstructor. Let's create such a method in our AccountServiceImpl class:
```java
    @PostConstructor
    public void demo() {
        User user = new User("Giovanni", "Giorgio");
        Account account = new Account(1, "giorgio", user);

        createAccount(account);
        List<Account> accounts = getAllAccounts();

        for (Account acc : accounts) {
            System.out.println(acc.toString());
        }

        Account accountById = getAccountById(1);
        System.out.println("Login: " + accountById.getLogin());
        userService.getFullName(user);

        removeAccount(accountById);
        System.out.println("All accounts have been deleted? " + getAllAccounts().isEmpty());
    }
```
> Note: You can annotate multiple methods with the @PostConstructor annotation, but they must be void.

Full AccountServiceImpl class:
```java
@Component
public class AccountServiceImpl implements AccountService {

    private List<Account> accounts = new ArrayList<>();

    private UserService userService;

    @Autowired
    public AccountServiceImpl(@Qualifier("UserServiceImpl") UserService userService) {
        this.userService = userService;
    }

    @Override
    public List<Account> getAllAccounts() {
        return accounts;
    }

    @PostConstructor
    public void demo() {
        User user = new User("Giovanni", "Giorgio");
        Account account = new Account(1, "giorgio", user);

        createAccount(account);
        List<Account> accounts = getAllAccounts();

        for (Account acc : accounts) {
            System.out.println(acc.toString());
        }

        Account accountById = getAccountById(1);
        System.out.println("Login: " + accountById.getLogin());
        userService.getFullName(user);

        removeAccount(accountById);
        System.out.println("All accounts have been deleted? " + getAllAccounts().isEmpty());
    }

    @Override
    public void createAccount(Account account) {
        accounts.add(account);
    }

    @Override
    public void removeAccount(Account account) {
        accounts.remove(account);
    }

    @Override
    public Account getAccountById(int id) {
        for (Account account : accounts) {
            if (account.getId() == id) {
                return account;
            }
        }

        return null;
    }
}
```
In order to run the program you must create the main entry point as follows:
```java
public class Main {
    public static void main(String[] args) {
        BeanFactory.run(Main.class);
    }
}
```

The output for this example would be:
```
Account(id=1, login=giorgio, user=User(name=Giovanni, surname=Giorgio))
Login: giorgio
My name is Giovanni Giorgio
All accounts have been deleted? true
```