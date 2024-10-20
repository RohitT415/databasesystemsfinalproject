//Team: Rohit Tallapragada & Brian Jin

import java.sql.*;
import java.util.Scanner;

public class p3 {

    // JDBC connection implementation
    private static final String ORACLE_CONNECTION_STRING = "jdbc:oracle:thin";
    private static final String ORACLE_DRIVER = "oracle.jdbc.driver.OracleDriver";
    private static final String SERVER_HOSTNAME = "csorcl.cs.wpi.edu";
    private static final int PORT = 1521;
    private static final String SERVICE_NAME = "orcl";

    public static void main(String[] args) {
        String USERID = "";
        String PASSWORD = "";
        int CHOICE = 0;

        switch (args.length) {
            case 0:
            case 1:
            default:
                System.out.println("You need to include your UserID and Password on the command line");
                return;
            case 2:
                System.out.println("You need to include your UserID, Password, and Choice on the command line!");
                return;
            case 3:
                USERID = args[0];
                PASSWORD = args[1];
                CHOICE = Integer.parseInt(args[2]);

                Connection connection = DBConnect(USERID, PASSWORD);
                if (connection == null) {
                    // Connection debugging statement
                    System.out.println("Failed to establish connection.");
                    return;
                }

                switch (CHOICE) {
                    case 1:
                        member(connection);
                        break;

                    case 2:
                        ranklist(connection);
                        break;

                    case 3:
                        updateMemberLevel(connection);
                        break;

                    default:
                        System.out.println("Invalid argument.");
                        break;
                }

                // More extensive error handling
                try {
                    connection.close();
                } catch (SQLException e) {
                    System.out.println("Error in closing the connection: " + e.getMessage());
                }
                break;
        }
    }

    public static Connection DBConnect(String userID, String password) {
        Connection connection = null;
        try {

            Class.forName(ORACLE_DRIVER);
            String url = ORACLE_CONNECTION_STRING + ":@" + SERVER_HOSTNAME + ":" + PORT + ":" + SERVICE_NAME;

            connection = DriverManager.getConnection(url, userID, password);
            System.out.println("Connection successful.");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
        return connection;
    }


    public static void member(Connection connection) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Member email: ");
        String email = scanner.nextLine();

        try {

            String query = "SELECT P.firstName, P.lastName, M.email, P.city, P.state, M.memberID, M.memberLevel " +
                    "FROM Member M JOIN Participant P ON M.email = P.email " +
                    "WHERE M.email = ?";

            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("Member Information");
                System.out.println("Name: " + rs.getString("firstName") + " " + rs.getString("lastName"));
                System.out.println("Email: " + rs.getString("email"));
                System.out.println("Address: " + rs.getString("city") + ", " + rs.getString("state"));
                System.out.println("Member ID: " + rs.getString("memberID"));
                System.out.println("Member Level: " + rs.getString("memberLevel"));
            } else {
                System.out.println("No member found.");
            }
        } catch (SQLException e) {
            System.out.println("Error getting member: " + e.getMessage());
        }
    }


    public static void ranklist(Connection connection) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Customer Email: ");
        String email = scanner.nextLine();

        System.out.print("Enter Year: ");
        int year = scanner.nextInt();

        try {
            String query = "SELECT P.firstName || ' ' || P.lastName AS customerName, " +
                    "r.rankOrder, PA.firstName || ' ' || PA.lastName AS artistName, aw.title AS artworkTitle " +
                    "FROM RankList r " +
                    "JOIN Artwork aw ON r.artworkID = aw.artworkID AND r.year = aw.year " +
                    "JOIN Participant PA ON aw.email = PA.email " +
                    "JOIN Customer C ON LOWER(r.email) = LOWER(C.email) " +
                    "JOIN Participant P ON LOWER(C.email) = LOWER(P.email) " +
                    "WHERE LOWER(r.email) = LOWER(?) AND r.year = ? " +
                    "ORDER BY r.rankOrder";



            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, email);
            stmt.setInt(2, year);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                System.out.println("Customer Rank List");
                System.out.println("Customer Name: " + rs.getString("customerName"));
                System.out.println("Year: " + year);

                do {
                    System.out.println(" ");
                    System.out.println("Rank: " + rs.getInt("rankOrder"));
                    System.out.println("Artist Name: " + rs.getString("artistName"));
                    System.out.println("Artwork Title: " + rs.getString("artworkTitle"));
                } while (rs.next());
            } else {
                System.out.println(" ");
                System.out.println("No rank list found.");
            }
        } catch (SQLException e) {
            System.out.println("Error getting rank list: " + e.getMessage());
        }
    }




    public static void updateMemberLevel(Connection connection) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Member ID: ");
        String memberID = scanner.nextLine();

        System.out.print("Enter Member Level: ");
        String memberLevel = scanner.nextLine();

        try {
            String updateQuery = "UPDATE Member SET memberLevel = ? WHERE memberID = ?";
            PreparedStatement stmt = connection.prepareStatement(updateQuery);
            stmt.setString(1, memberLevel);
            stmt.setString(2, memberID);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Member level updated.");
            } else {
                System.out.println("No member found.");
            }
        } catch (SQLException e) {
            System.out.println("Error updating level: " + e.getMessage());
        }
    }
}
