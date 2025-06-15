import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

public class JiraEpicFetcher {
    // 配置Jira API信息
    private static final String JIRA_URL = "https://your-jira-domain.atlassian.net";
    private static final String JIRA_USER = "your-email@example.com";
    private static final String JIRA_TOKEN = "your-api-token";

    // 发送GET请求
    private static String sendGet(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        String auth = JIRA_USER + ":" + JIRA_TOKEN;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
        conn.setRequestProperty("Accept", "application/json");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        return content.toString();
    }

    // 获取所有team=aaa, 状态=inprocessing的epic
    private static JSONArray getEpics() throws Exception {
        String jql = "project = YOUR_PROJECT AND issuetype = Epic AND team = aaa AND status = inprocessing";
        String url = JIRA_URL + "/rest/api/2/search?jql=" + java.net.URLEncoder.encode(jql, "UTF-8");
        String response = sendGet(url);
        JSONObject json = new JSONObject(response);
        return json.getJSONArray("issues");
    }

    // 获取Epic下的kanban story
    private static JSONArray getKanbanStories(String epicKey) throws Exception {
        String jql = String.format("project = YOUR_PROJECT AND issuetype = Story AND 'Epic Link' = %s AND board = kanban", epicKey);
        String url = JIRA_URL + "/rest/api/2/search?jql=" + java.net.URLEncoder.encode(jql, "UTF-8");
        String response = sendGet(url);
        JSONObject json = new JSONObject(response);
        return json.getJSONArray("issues");
    }

    // 获取Story下类型为DOD的task
    private static JSONArray getDODTasks(String storyKey) throws Exception {
        String jql = String.format("project = YOUR_PROJECT AND issuetype = Task AND parent = %s AND type = DOD", storyKey);
        String url = JIRA_URL + "/rest/api/2/search?jql=" + java.net.URLEncoder.encode(jql, "UTF-8");
        String response = sendGet(url);
        JSONObject json = new JSONObject(response);
        return json.getJSONArray("issues");
    }

    public static void main(String[] args) throws Exception {
        JSONArray epics = getEpics();
        for (int i = 0; i < epics.length(); i++) {
            JSONObject epic = epics.getJSONObject(i);
            String epicKey = epic.getString("key");
            System.out.println("Epic: " + epicKey);

            JSONArray stories = getKanbanStories(epicKey);
            for (int j = 0; j < stories.length(); j++) {
                JSONObject story = stories.getJSONObject(j);
                String storyKey = story.getString("key");
                System.out.println("  Kanban Story: " + storyKey);

                JSONArray tasks = getDODTasks(storyKey);
                for (int k = 0; k < tasks.length(); k++) {
                    JSONObject task = tasks.getJSONObject(k);
                    String taskKey = task.getString("key");
                    System.out.println("    DOD Task: " + taskKey);
                }
            }
        }
    }
}