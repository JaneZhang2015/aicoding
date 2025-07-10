import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.sql.Date;

@Repository
public class UserDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<User> getUsersByDateAndName(Date dateParam, String nameParam) {
        String sql = "SELECT id, name FROM user WHERE created_date = ? AND name = ?";
        return jdbcTemplate.query(sql, new Object[]{dateParam, nameParam},
            (rs, rowNum) -> {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setName(rs.getString("name"));
                return u;
            });
    }
}
