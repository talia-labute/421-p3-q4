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
            System.out.println("  5. Inventory specials");
            System.out.println("  6. Task 6");
            System.out.println("  7. Quit");
            System.out.print("Enter your choice: ");

            String choice = in.nextLine();

            switch(choice){
              case "1":
                System.out.println("Not implemented yet");
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
                System.out.println("Not implemented yet");
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
}
