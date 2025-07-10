import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class MainApplication implements CommandLineRunner {
    @Autowired
    private UserDao userDao;

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

    @Override
    public void run(String... args) {
        String csvFile = "users.csv";
        String reportFile = "compare_report.txt";
        int diffCount = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile));
             BufferedWriter bw = new BufferedWriter(new FileWriter(reportFile))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) { isHeader = false; continue; }
                String[] parts = line.split(",");
                if (parts.length < 3) continue;
                int userId = Integer.parseInt(parts[0].trim());
                String userName = parts[1].trim();
                String type = parts[2].trim();
                List<User> dbUsers = userDao.getUsersById(userId); // 假设返回List<User>
                if (dbUsers == null || dbUsers.isEmpty()) {
                    bw.write("[数据库缺失] user_id=" + userId + ", user_name=" + userName + ", type=" + type + "\n");
                    diffCount++;
                    continue;
                }
                boolean allMatched = true;
                for (User dbUser : dbUsers) {
                    if (!Objects.equals(dbUser.getName(), userName)) {
                        allMatched = false;
                        break;
                    }
                }
                if (!allMatched) {
                    bw.write("[姓名不一致] user_id=" + userId + ", 文件: " + userName + ", 数据库: [" + dbUsers.stream().map(User::getName).reduce((a,b)->a+","+b).orElse("") + "]\n");
                    diffCount++;
                }
                // 如有 type 字段，也可比较
                // if (!Objects.equals(dbUser.getType(), type)) {
                //     bw.write("[类型不一致] user_id=" + userId + ", 文件: " + type + ", 数据库: " + dbUser.getType() + "\n");
                //     diffCount++;
                // }
            }
            // 新增代码：确保数据库查到的所有user name都在文件中该user id的所有记录里
            try (BufferedReader br2 = new BufferedReader(new FileReader(csvFile))) {
                String line2;
                boolean isHeader2 = true;
                while ((line2 = br2.readLine()) != null) {
                    if (isHeader2) { isHeader2 = false; continue; }
                    String[] parts2 = line2.split(",");
                    if (parts2.length < 3) continue;
                    int userId = Integer.parseInt(parts2[0].trim());
                    String userName = parts2[1].trim();
                    List<User> dbUsers = userDao.getUsersById(userId);
                    if (dbUsers == null || dbUsers.isEmpty()) continue;
                    // 统计该userId在文件中出现的所有userName
                    List<String> fileUserNames = new ArrayList<>();
                    fileUserNames.add(userName);
                    // 继续读取后续行，若userId相同则也加入fileUserNames
                    br2.mark(1024 * 1024); // 标记当前位置
                    String nextLine;
                    while ((nextLine = br2.readLine()) != null) {
                        String[] nextParts = nextLine.split(",");
                        if (nextParts.length < 3) continue;
                        int nextUserId = Integer.parseInt(nextParts[0].trim());
                        if (nextUserId != userId) {
                            br2.reset(); // 回到上次mark
                            break;
                        }
                        fileUserNames.add(nextParts[1].trim());
                        br2.mark(1024 * 1024); // 重新标记
                    }
                    // 比较数据库所有user的name都在fileUserNames中
                    boolean allDbInFile = true;
                    for (User dbUser : dbUsers) {
                        if (!fileUserNames.contains(dbUser.getName())) {
                            allDbInFile = false;
                            break;
                        }
                    }
                    if (!allDbInFile) {
                        bw.write("[姓名不一致] user_id=" + userId + ", 文件: [" + String.join(",", fileUserNames) + "], 数据库: [" + dbUsers.stream().map(User::getName).reduce((a,b)->a+","+b).orElse("") + "]\n");
                        diffCount++;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (diffCount == 0) {
                bw.write("所有数据一致\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("对比完成，报告见：" + reportFile);
    }
}
