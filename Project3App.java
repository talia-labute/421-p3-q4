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
            System.out.println("  3. Add order item to order");
            System.out.println("  4. Update product price");
            System.out.println("  5. Customer Order History");
            System.out.println("  6. Inventory Menu");
            System.out.println("  7. Quit");
            System.out.print("Enter your choice: ");

            String choice = in.nextLine();

            switch(choice){
              case "1":
                lookupProductExpiration(con, in);
                // Look up expiration of Product
                break;
              case "2":
                addNewCustomer(con, in);
                // Add new customer
                break;
              case "3":
                addOrderItemToOrder(con, in);
                // Update price of product
                break;
              case "4":
                updateProductPrice(con, in);
                // Update product price
                break;
              case "5":
                showCustomerOrderHistory(con, in);
                break;
                // Inventory specials
              case "6":
                inventoryMenu(con,in);
                // inventory menu with subqueries
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

      String sql = "SELECT name, expirationDate FROM Product WHERE prodId = ?"; //PROBLEM HERE

      try (PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, Integer.parseInt(prodId));

        try (ResultSet rs = ps.executeQuery()) {
          if (rs.next()) {
            String productName = rs.getString("name");
            Date expirationDate = rs.getDate("expirationDate");
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

    //Task 2
    private static void addNewCustomer(Connection con, Scanner in) throws SQLException {
      try {
        System.out.print("Enter customer name: ");
        String name = in.nextLine();

        System.out.print("Enter customer email: ");
        String email = in.nextLine();

        System.out.print("Enter customer phone: ");
        String phone = in.nextLine();

        System.out.println("Enter customer allergies (or leave blank):");
        String allergies = in.nextLine();

        //check if email exists
        String checkEmailSQL = "SELECT customerId FROM Customer WHERE email = ?";
        PreparedStatement checkEmailStmt = con.prepareStatement(checkEmailSQL);
        checkEmailStmt.setString(1, email);
        ResultSet emailRs = checkEmailStmt.executeQuery();

        if (emailRs.next()){
          System.out.println("A customer with this email already exists");
          return;
        }

        //check if phone exists
        String checkPhoneSQL = "SELECT customerId FROM Customer WHERE phoneNum = ?";
        PreparedStatement checkPhoneStmt = con.prepareStatement(checkPhoneSQL);
        checkPhoneStmt.setString(1, phone);
        ResultSet phoneRs = checkPhoneStmt.executeQuery();

        if (phoneRs.next()){
          System.out.println("A customer with this phone number already exists");
          return;
        }

        //generate new customer ID
        String maxIdSQL = "SELECT MAX(customerId) AS maxId FROM Customer";
        Statement maxIdStmt = con.createStatement();
        ResultSet maxIdRs = maxIdStmt.executeQuery(maxIdSQL);

        int newCustomerId = 1;
        if (maxIdRs.next()) {
          newCustomerId = maxIdRs.getInt("maxId") + 1;
        }

        //insert new customer
        String insertSQL = "INSERT INTO Customer (customerId, email, phoneNum, name, allergies) " +
                           "VALUES (?, ?, ?, ?, ?)";
        PreparedStatement insertStmt = con.prepareStatement(insertSQL);
        insertStmt.setInt(1, newCustomerId);
        insertStmt.setString(2, email);
        insertStmt.setString(3, phone);
        insertStmt.setString(4, name);
        insertStmt.setString(5, allergies.isEmpty() ? null : allergies);

        insertStmt.executeUpdate();
        System.out.println("Customer added successfully with ID: " + newCustomerId);

      } catch (SQLException e) {
        System.out.println("Database error while adding new customer.");
        System.out.println("Code: " + e.getErrorCode() + " SQLState: " + e.getSQLState());
        System.out.println(e.getMessage());
      } catch (NumberFormatException e) {
        System.out.println("Invalid input format. Please enter valid data.");
      } 
    }

    //Task 3
    private static void addOrderItemToOrder(Connection con, Scanner in) throws SQLException {
      try {
        System.out.print("Enter Order ID: ");
        int orderId = Integer.parseInt(in.nextLine());

        System.out.print("Enter Product ID: ");
        int prodId = Integer.parseInt(in.nextLine());

        System.out.print("Enter Quantity: ");
        int quantity = Integer.parseInt(in.nextLine());

        if (quantity <= 0){
          System.out.println("Please enter a valid quantity");
          return;
        }

        //check order exists
        String orderSQL = "SELECT orderId, totalAmount FROM Orders WHERE orderId = ?";
        PreparedStatement orderStmt = con.prepareStatement(orderSQL);
        orderStmt.setInt(1, orderId);
        ResultSet orderRs = orderStmt.executeQuery();

        if (!orderRs.next()) {
            System.out.println("Order not found.");
            orderRs.close();
            orderStmt.close();
            return;
        }

        double oldTotal = orderRs.getDouble("totalAmount");
        orderRs.close();
        orderStmt.close();

        //check that product exists and is still in stock
        String productSQL = "SELECT name, price, inStock FROM Product WHERE prodId = ?";
        PreparedStatement productStmt = con.prepareStatement(productSQL);
        productStmt.setInt(1, prodId);
        ResultSet productRs = productStmt.executeQuery();

        if (!productRs.next()) {
            System.out.println("Product not found.");
            productRs.close();
            productStmt.close();
            return;
        }

        String productName = productRs.getString("name");
        double price = productRs.getDouble("price");
        boolean inStock = productRs.getBoolean("inStock");

        productRs.close();
        productStmt.close();

        if (!inStock) {
            System.out.println("That product is currently out of stock.");
            return;
        }

        //insert order item
        //price at purchase computed form product price 
        String insertSQL = "INSERT INTO OrderItem(orderId, prodId, quantity, priceAtPurchase) VALUES (?, ?, ?, ?)";
        PreparedStatement insertStmt = con.prepareStatement(insertSQL);
        insertStmt.setInt(1, orderId);
        insertStmt.setInt(2, prodId);
        insertStmt.setInt(3, quantity);
        insertStmt.setDouble(4, price);

        insertStmt.executeUpdate();
        insertStmt.close();

        //show new total after trigger fired
        String confirmSQL = "SELECT totalAmount FROM Orders WHERE orderId = ?";
        PreparedStatement confirmStmt = con.prepareStatement(confirmSQL);
        confirmStmt.setInt(1, orderId);
        ResultSet confirmRs = confirmStmt.executeQuery();

        double newTotal = oldTotal;
        if (confirmRs.next()) {
            newTotal = confirmRs.getDouble("totalAmount");
        }

        confirmRs.close();
        confirmStmt.close();

        System.out.println("Added item successfully.");
        System.out.println("Product: " + productName);
        System.out.println("Quantity: " + quantity);
        System.out.println("Price at purchase: $" + price);
        System.out.println("Old order total: $" + oldTotal);
        System.out.println("New order total: $" + newTotal);
        System.out.println("Trigger updated the order total automatically.");

      } catch (NumberFormatException e){
        System.out.println("Invalid numeric input.");
      } catch (SQLException e){
        System.out.println("Database error while adding order item.");
        System.out.println("Code: " + e.getErrorCode() + " SQLState: " + e.getSQLState());
        System.out.println(e.getMessage());
      }
    }

    //Task 4
    private static void updateProductPrice(Connection con, Scanner in) {
        try {
            System.out.print("Enter product ID: ");
            int prodId = Integer.parseInt(in.nextLine());
 
            // First show the current price (SELECT)
            String selectSQL = "SELECT name, price FROM Product WHERE prodId = ?";
            String productName;
            double currentPrice;
            try (PreparedStatement selectStmt = con.prepareStatement(selectSQL)) {
                selectStmt.setInt(1, prodId);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Product not found.");
                        return;
                    }
                    productName = rs.getString("name");
                    currentPrice = rs.getDouble("price");
                }
            }
 
            System.out.printf("Product: %s (ID: %d)%n", productName, prodId);
            System.out.printf("Current price: $%.2f%n", currentPrice);
            System.out.print("Enter new price: $");
 
            double newPrice;
            try {
                newPrice = Double.parseDouble(in.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid price format. Please enter a valid number.");
                return;
            }
 
            if (newPrice < 0) {
                System.out.println("Price cannot be negative.");
                return;
            }
 
            // Perform the UPDATE
            String updateSQL = "UPDATE Product SET price = ? WHERE prodId = ?";
            try (PreparedStatement updateStmt = con.prepareStatement(updateSQL)) {
                updateStmt.setDouble(1, newPrice);
                updateStmt.setInt(2, prodId);
                int rows = updateStmt.executeUpdate();
                if (rows > 0) {
                    System.out.printf("Price updated successfully: \"%s\" is now $%.2f.%n",
                                      productName, newPrice);
                } else {
                    System.out.println("Update failed. Product not found.");
                }
            }
 
        } catch (NumberFormatException e) {
            System.out.println("Invalid product ID format. Please enter a valid integer.");
        } catch (SQLException e) {
            System.out.println("Database error while updating product price.");
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

            System.out.println(name + " x" + quantity + " ($" + price + ")");
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

    // task 6

private static void inventoryMenu(Connection con, Scanner in) {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println();
            System.out.println("--- Inventory Menu ---");
            System.out.println("Choose one out of 1-4");
            System.out.println("  1. Show all products");
            System.out.println("  2. Show baked goods only");
            System.out.println("  3. Show drinks only");
            System.out.println("  4. Back to main menu");
            System.out.print("Enter your choice: ");
 
            String choice = in.nextLine();
            switch (choice) {
                case "1":
                    showProducts(con, null);
                    break;
                case "2":
                    showProducts(con, "BakedGood");
                    break;
                case "3":
                    showProducts(con, "Drink");
                    break;
                case "4":
                    inMenu = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
 
    // Shared helper for the inventory sub-menu
    private static void showProducts(Connection con, String category) {
        String sql;
        if (category == null) {
            sql = "SELECT prodId, name, price, inStock, expirationDate " +
                  "FROM Product " +
                  "ORDER BY prodId";
        } else if (category.equals("BakedGood")) {
            sql = "SELECT P.prodId, P.name, P.price, P.inStock, P.expirationDate " +
                  "FROM Product P " +
                  "JOIN BakedGood B ON P.prodId = B.prodId " +
                  "ORDER BY P.prodId";
        } else { // Drink
            sql = "SELECT P.prodId, P.name, P.price, P.inStock, P.expirationDate " +
                  "FROM Product P " +
                  "JOIN Drink D ON P.prodId = D.prodId " +
                  "ORDER BY P.prodId";
        }
 
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
 
            String header = (category == null) ? "All Products"
                          : (category.equals("BakedGood") ? "Baked Goods" : "Drinks");
            System.out.println("\n" + header);
            System.out.printf("%-6s  %-25s  %8s  %-8s  %s%n",
                              "ID", "Name", "Price", "In Stock", "Expiration");
 
            boolean found = false;
            while (rs.next()) {
                found = true;
                int prodId = rs.getInt("prodId");
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                boolean inStock = rs.getBoolean("inStock");
                Date expDate = rs.getDate("expirationDate");
                System.out.printf("%-6d  %-25s  $%7.2f  %-8s  %s%n",
                                  prodId, name, price,
                                  inStock ? "Yes" : "No",
                                  expDate != null ? expDate.toString() : "N/A");
            }
            if (!found) {
                System.out.println("No products found.");
            }
 
        } catch (SQLException e) {
            System.out.println("Database error while retrieving inventory.");
            System.out.println("Code: " + e.getErrorCode() + " SQLState: " + e.getSQLState());
            System.out.println(e.getMessage());
        }
    }
}
