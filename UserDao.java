import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Repository
public class UserDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<User> getAllUsers() {
        return jdbcTemplate.query("SELECT id, name FROM user",
            (rs, rowNum) -> {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setName(rs.getString("name"));
                return u;
            });
    }
}
