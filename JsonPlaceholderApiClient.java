import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class JsonPlaceholderApiClient {

    private static final String BASE_URL = "https://jsonplaceholder.typicode.com/users";

    public static void main(String[] args) {
        JsonPlaceholderApiClient apiClient = new JsonPlaceholderApiClient();

        String newUserJson = "{ \"name\": \"John Doe\", \"username\": \"johndoe\", \"email\": \"johndoe@example.com\" }";
        String createdUser = apiClient.createEntity(newUserJson);
        System.out.println("Created User: " + createdUser);

        String updatedUserJson = "{ \"name\": \"Updated Name\", \"username\": \"updatedusername\", \"email\": \"updated@example.com\" }";
        String updatedUser = apiClient.updateEntity(1, updatedUserJson);
        System.out.println("Updated User: " + updatedUser);

        int deletedUserId = 1;
        apiClient.deleteEntity(deletedUserId);
        System.out.println("User with id " + deletedUserId + " deleted successfully.");

        String allUsers = apiClient.getAllUsers();
        System.out.println("All Users: " + allUsers);

        int userId = 2;
        String userById = apiClient.getUserById(userId);
        System.out.println("User with id " + userId + ": " + userById);

        String username = "Antonette";
        String userByUsername = apiClient.getUserByUsername(username);
        System.out.println("User with username " + username + ": " + userByUsername);

        apiClient.getAllCommentsForLastPost(1);


        apiClient.printOpenTasksForUser(1);
    }

    private String createEntity(String jsonInput) {
        return sendHttpRequest("POST", BASE_URL, jsonInput);
    }

    private String updateEntity(int id, String jsonInput) {
        String url = BASE_URL + "/" + id;
        return sendHttpRequest("PUT", url, jsonInput);
    }

    private void deleteEntity(int id) {
        String url = BASE_URL + "/" + id;
        sendHttpRequest("DELETE", url, null);
    }

    private String getAllUsers() {
        return sendHttpRequest("GET", BASE_URL, null);
    }

    private String getUserById(int id) {
        String url = BASE_URL + "/" + id;
        return sendHttpRequest("GET", url, null);
    }

    private String getUserByUsername(String username) {
        String url = BASE_URL + "?username=" + username;
        return sendHttpRequest("GET", url, null);
    }

    private void getAllCommentsForLastPost(int userId) {
        try {
            String userPosts = sendHttpRequest("GET", BASE_URL + "/" + userId + "/posts", null);
            List<Post> posts = parsePostsFromString(userPosts);
            Post lastPost = posts.stream()
                    .max(Comparator.comparing(Post::getId))
                    .orElseThrow(() -> new RuntimeException("No posts found"));

            int lastPostId = lastPost.getId();
            String commentsUrl = "https://jsonplaceholder.typicode.com/posts/" + lastPostId + "/comments";
            String comments = sendHttpRequest("GET", commentsUrl, null);

            String fileName = "user-" + userId + "-post-" + lastPostId + "-comments.json";
            try (FileWriter fileWriter = new FileWriter(fileName)) {
                fileWriter.write(comments);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printOpenTasksForUser(int userId) {
        try {
            String todosUrl = BASE_URL + "/" + userId + "/todos";
            String todosJson = sendHttpRequest("GET", todosUrl, null);

            List<Task> tasks = parseTasksFromString(todosJson);
            List<Task> openTasks = tasks.stream()
                    .filter(task -> !task.isCompleted())
                    .collect(Collectors.toList());

            System.out.println("Open tasks for user " + userId + ":");
            for (Task task : openTasks) {
                System.out.println(task);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Task> parseTasksFromString(String todosJson) {
        return List.of(new Task(1, 1, "Task 1", false));
    }

    private List<Post> parsePostsFromString(String userPosts) {
        return List.of(new Post(1, 1, "Title 1", "Body 1"));
    }

    private static class Post {
        private int userId;
        private int id;
        private String title;
        private String body;

        public Post(int userId, int id, String title, String body) {
            this.userId = userId;
            this.id = id;
            this.title = title;
            this.body = body;
        }

        public int getId() {
            return id;
        }
    }

    private static class Task {
        private int userId;
        private int id;
        private String title;
        private boolean completed;

        public Task(int userId, int id, String title, boolean completed) {
            this.userId = userId;
            this.id = id;
            this.title = title;
            this.completed = completed;
        }

        public boolean isCompleted() {
            return completed;
        }

        @Override
        public String toString() {
            return "Task{" +
                    "userId=" + userId +
                    ", id=" + id +
                    ", title='" + title + '\'' +
                    ", completed=" + completed +
                    '}';
        }
    }

    private String sendHttpRequest(String method, String url, String jsonInput) {
        try {
            URL apiUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();

            connection.setRequestMethod(method);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            if (jsonInput != null) {
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonInput.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return response.toString();
            } finally {
                connection.disconnect();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
