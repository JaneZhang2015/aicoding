import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@SpringBootApplication
public class MainApplication implements CommandLineRunner {
    @Autowired
    private UserDao userDao;

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

    @Override
    public void run(String... args) {
        List<User> users = userDao.getAllUsers();
        for (User u : users) {
            System.out.println(u.getId() + " " + u.getName());
        }
    }
}
