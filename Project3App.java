import java.sql.* ;
import java.util.Scanner;

class Project3App
{
    public static void main ( String [ ] args ) throws SQLException
    {
      // Unique table names.  Either the user supplies a unique identifier as a command line argument, or the program makes one up.
        int sqlCode=0;      // Variable to hold SQLCODE
        String sqlState="00000";  // Variable to hold SQLSTATE

        // Register the driver.  You must register the driver before you can use it.
        try { DriverManager.registerDriver ( new com.ibm.db2.jcc.DB2Driver() ) ; }
        catch (Exception cnfe){ System.out.println("Class not found"); }

        // This is the url you must use for DB2.
        //Note: This url may not valid now ! Check for the correct year and semester and server name.
        String url = "jdbc:db2://winter2026-comp421.cs.mcgill.ca:50000/comp421";

        //REMEMBER to remove your user id and password before submitting your code!!
        String your_userid = null;
        String your_password = null;
        //AS AN ALTERNATIVE, you can just set your password in the shell environment in the Unix (as shown below) and read it from there.
        //$  export SOCSPASSWD=yoursocspasswd 
        if(your_userid == null && (your_userid = System.getenv("SOCSUSER")) == null)
        {
          System.err.println("Error!! do not have a password to connect to the database!");
          System.exit(1);
        }
        if(your_password == null && (your_password = System.getenv("SOCSPASSWD")) == null)
        {
          System.err.println("Error!! do not have a password to connect to the database!");
          System.exit(1);
        }
        Connection con = DriverManager.getConnection (url,your_userid,your_password) ;
        Scanner in = new Scanner(System.in);

        boolean running = true;

        while (running){
          System.out.println();
            System.out.println("Limp Bakery Main Menu");
            System.out.println("  1. Look up expiration of Product");
            System.out.println("  2. Add new customer");
            System.out.println("  3. Update price of product");
            System.out.println("  4. Remove employee");
            System.out.println("  5. Customer Order History");
            System.out.println("  6. Task 6");
            System.out.println("  7. Quit");
            System.out.print("Enter your choice: ");

            String choice = in.nextLine();

            switch(choice){
              case "1":
                lookupProductExpiration(con, in);
                // Look up expiration of Product
                break;
              case "2":
                System.out.println("Not implemented yet");
                // Add new customer
                break;
              case "3":
                System.out.println("Not implemented yet");
                // Update price of product
                break;
              case "4":
                System.out.println("Not implemented yet");
                // Remove employee
                break;
              case "5":
                showCustomerOrderHistory(con, in);
                break;
                // Inventory specials
                break;
              case "6":
                System.out.println("Not implemented yet");
                // Task 6
                break;
              case "7":
                running = false;
                System.out.println("Goodbye!");
                break;
              default:
                System.out.println("Invalid choice. Please try again.");
            }
        }

        in.close();
        con.close();
    }
// Task 1
    private static void lookupProductExpiration(Connection con, Scanner in) throws SQLException {
      System.out.print("Enter product ID: ");
      String prodId = in.nextLine();

      String sql = "SELECT expirationDate FROM Product WHERE prodId = ?"; //PROBLEM HERE

      try (PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, Integer.parseInt(prodId));

        try (ResultSet rs = ps.executeQuery()) {
          if (rs.next()) {
            String productName = rs.getString("name");
            Date expirationDate = rs.getDate("expiration_date");
            System.out.println("Expiration date for product " + productName + " (ID: " + prodId + "): " + expirationDate);
          } else {
            System.out.println("Product not found.");
          }
        }
      } catch (NumberFormatException e) {
        System.out.println("Invalid product ID format. Please enter a valid integer.");
      } catch (SQLException e) {
        System.out.println("Database error while looking up product expiration.");
        System.out.println("Code: " + e.getErrorCode() + " SQLState: " + e.getSQLState());
        System.out.println(e.getMessage());
      }
    }

    //Task 5
    private static void showCustomerOrderHistory(Connection con, Scanner in) throws SQLException {
      try {
        System.out.print("Enter customer ID: ");
        int custId = Integer.parseInt(in.nextLine());

        //check if customer exists
        String customerSQL = "SELECT name, email FROM Customer WHERE customerId = ?";
        PreparedStatement customerStmt = con.prepareStatement(customerSQL);
        customerStmt.setInt(1, custId);
        ResultSet customerRs = customerStmt.executeQuery();

        if (!customerRs.next()) {
          System.out.println("Customer not found.");
          return;
        }

        String customerName = customerRs.getString("name");
        String customerEmail = customerRs.getString("email");

        System.out.println("\nCustomer found:");
        System.out.println("Name: " + customerName);
        System.out.println("Email: " + customerEmail);

        //get customer order
        String ordersSQL = "SELECT orderId, orderDate, totalAmount, tip, toGo, empId " +
                           "FROM Orders " +
                           "WHERE customerId = ? " +
                           "ORDER BY orderDate DESC, orderId DESC";
        PreparedStatement ordersStmt = con.prepareStatement(ordersSQL);
        ordersStmt.setInt(1, custId);
        ResultSet ordersRs = ordersStmt.executeQuery();

        boolean hasOrders = false;
        while (ordersRs.next()) {
          hasOrders = true;
          int orderId = ordersRs.getInt("orderId");
          Date orderDate = ordersRs.getDate("orderDate");
          double totalAmount = ordersRs.getDouble("totalAmount");
          double tip = ordersRs.getDouble("tip");
          boolean toGo = ordersRs.getBoolean("toGo");
          int empId = ordersRs.getInt("empId");

          System.out.println("\n------------------------------");
          System.out.println("Order ID: " + orderId);
          System.out.println("Order Date: " + orderDate);
          System.out.println("Total Amount: $" + totalAmount);
          System.out.println("Tip: $" + tip);
          System.out.println("To Go: " + (toGo ? "Yes" : "No"));
          System.out.println("Handled by Employee ID: " + empId);

          //get order items
          String itemsSQL = "SELECT OI.prodId, P.name, OI.quantity, OI.priceAtPurchase " +
                              "FROM OrderItem OI " +
                              "JOIN Product P ON OI.prodId = P.prodId " +
                              "WHERE OI.orderId = ?";
          PreparedStatement itemsStmt = con.prepareStatement(itemsSQL);
          itemsStmt.setInt(1, orderId);
          ResultSet itemsRs = itemsStmt.executeQuery();

          System.out.println("Items:");
          boolean hasItems = false;
          while (itemsRs.next()) {
            hasItems = true;
            int prodId = itemsRs.getInt("prodId");
            String prodName = itemsRs.getString("name");
            int quantity = itemsRs.getInt("quantity");
            double priceAtPurchase = itemsRs.getDouble("priceAtPurchase");

            String itemsSQL = "SELECT OI.prodId, P.name, OI.quantity, OI.priceAtPurchase " +
                              "FROM OrderItem OI " +
                              "JOIN Product P ON OI.prodId = P.prodId " +
                              "WHERE OI.orderId = ?";
          }
          if (!hasItems) {
            System.out.println("No items found for this order.");
          }
          itemsRs.close();
          itemsStmt.close();
        }
        if (!hasOrders) {
          System.out.println("No orders found for this customer.");
        }
        ordersRs.close();
        ordersStmt.close();
        customerRs.close();
        customerStmt.close();
      } catch (NumberFormatException e) {
        System.out.println("Invalid customer ID format. Please enter a valid integer.");
      } catch (SQLException e) {
        System.out.println("Database error while retrieving customer order history.");
        System.out.println("Code: " + e.getErrorCode() + " SQLState: " + e.getSQLState());
        System.out.println(e.getMessage());
      }
    }
}
