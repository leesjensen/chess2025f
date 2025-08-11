import dataaccess.MySQLDBManager;
import server.Server;

public class Main {
    public static void main(String[] args) {
        try {
            var dbManager = new MySQLDBManager();
            Server server = new Server(dbManager);
            server.run(8080);

            System.out.println("♕ 240 Chess Server");
        } catch (Exception ex) {
            System.out.println("Unable to start server: " + ex);
        }
    }
}