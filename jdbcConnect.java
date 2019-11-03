// Imports for database connection
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Scanner; 

// Import from JSch (Java Secure Channel) jar file - http://www.jcraft.com/jsch/ 
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class jdbcConnect {
	public  Session session;						// SSH tunnel session
	Connection connection;
	
	/**
	 * Open SSH Tunnel to SSH server and forward the specified port on the local machine to the MySQL port on the MySQL server on the SSH server
	 * @param sshUser SSH username
	 * @param sshPassword SSH password
	 * @param sshHost hostname or IP of SSH server
	 * @param sshPort SSH port on SSH server
	 * @param remoteHost hostname or IP of MySQL server on SSH server (from the perspective of the SSH Server)
	 * @param localPort port on the local machine to be forwarded
	 * @param remotePort MySQL port on remoteHost 
	 */
	 
	private void openSSHTunnel( String sshUser, String sshPassword, String sshHost, int sshPort, String remoteHost, int localPort, int remotePort ){
		try{
			final JSch jsch = new JSch();							// Create a new Java Secure Channel
			session = jsch.getSession( sshUser, sshHost, sshPort);	// Get the tunnel
			session.setPassword(sshPassword );						// Set the password for the tunnel

			final Properties config = new Properties();				// Create a properties object 
			config.put( "StrictHostKeyChecking", "no" );			// Set some properties
			session.setConfig( config );							// Set the properties object to the tunnel

			session.connect();										// Open the tunnel
			System.out.println("\nSSH Connecting");
			System.out.println("Success: SSH tunnel open - you are connecting to "+sshHost+ "on port "+sshPort+ " with username " + sshUser);

			// Set up port forwarding from a port on your local machine to a port on the MySQL server on the SSH server
			session.setPortForwardingL(localPort, remoteHost, remotePort);							
			// Output a list of the ports being forwarded 
			
			System.out.println("Success: Port forwarded - You have forwared port "+ localPort + " on the local machine to port " + remotePort + " on " + remoteHost + " on " +sshHost);
		}
		catch(Exception e ){
			e.printStackTrace();
		}
	}
	
	/**
	 * Close SSH tunnel to a remote server
	 */
	private void closeSshTunnel(int localPort){
		try {
			// Remove the port forwarding and output a status message
			System.out.println("\nSSH Connection Closing");
			session.delPortForwardingL(localPort);
			System.out.println("Success: Port forwarding removed");
			// Catch any exceptions	
		} catch (JSchException e) {
			System.out.println("Error: port forwarding removal issue");
			e.printStackTrace();
		}
		// Disconnect the SSH tunnel
		session.disconnect();
		System.out.println("Success: SSH tunnel closed\n");
	}

	/**
	 * Open a connection with MySQL server. If there is an SSH Tunnel required it will open this too. 
	 */
	public void openConnection(String mysqlHost, int localPort, String mysqlDatabaseName, String mysqlUsername, String mysqlPassword){
		try{
			// Create a new JDBC driver to facilitate the conversion of MySQL to java and vice versa
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();

			// Connect to the MySQL database through the SSH tunnel you have created using the variable above
			String jdbcConnectionString = "jdbc:mysql://"+mysqlHost+":"+localPort+"/"+mysqlDatabaseName+"?user="+mysqlUsername+"&password="+mysqlPassword;
			System.out.println("\nMySQL Connecting");
			System.out.println("JDBC connection string "+jdbcConnectionString);
			connection = DriverManager.getConnection(jdbcConnectionString);
			System.out.println("Connection:"+connection.toString());
			System.out.println("Success: MySQL connection open");

			// Testing connection 
			// TestConnection();

		}
		// Catch various exceptions and print error messages
		catch (SQLException e){ 
			System.err.println("> SQLException: " + e.getMessage());
			e.printStackTrace();
		}
		catch (InstantiationException e) {
			System.err.println("> InstantiationException: " + e.getMessage());
			e.printStackTrace();
		}
		catch (IllegalAccessException e) {
			System.err.println("> IllegalAccessException: " + e.getMessage());
			e.printStackTrace();
		}
		catch (ClassNotFoundException e) {
			System.err.println("> ClassNotFoundException: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void closeConnection(){
		System.out.println("\nMySQL Connection Closing");
		try {
			connection.close(); // Close database connection
			System.out.println("Success: MySQL connection closed");
		} catch (SQLException e) {
			System.out.println("Error: Could not close MySQL connection");
			System.err.println(e);	
			e.printStackTrace();}
	}
	
	public void registerNewCustomer() // Define method to register new customer 
	{
		try {
			Statement st = connection.createStatement(); // Create an SQL statement
			
			// Prompt user to input customer details 
			Scanner input=new Scanner(System.in); 
			
			System.out.println ("Enter CustomerID: ");
			String customerID=input.nextLine();
			
			System.out.println ("Enter Name: ");
			String name=input.nextLine();
			
			System.out.println ("Enter City: ");
			String city=input.nextLine();
			
			System.out.println ("Enter Street: ");
			String street=input.nextLine();
			
			System.out.println ("Enter Address: ");
			String address=input.nextLine();
			
			// SQL statement by user input variables 
			String insertNewCustomer= "insert into customer (customerID, Name, City, Street, Address) values ("+customerID+" , "+name+ " , "+ city+ " , "+street+ " , "+ address+")"; 
			
            st.executeUpdate(insertNewCustomer);
			
			ResultSet rs = st.executeQuery("SELECT * from customer");  // Retrieve an SQL results set

			// Output total customers registered to user
			System.out.println("New customer details:");

			while (rs.next()){
				
				customerID = rs.getString("customerID"); 
				String Name = rs.getString("Name");
				
				System.out.print(customerID + " ");
				System.out.print(Name + " \n");
			}
			
			if (st != null) {
				st.close();		// Close the SQL statement
			}
			if (rs != null){	// Close the Result Set
				rs.close();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void viewRegistration() // Define method to view films reserved by any customer 
	{
		try {
			Statement st = connection.createStatement(); // Create an SQL statement
			
			// Prompt user to input customer ID 
			System.out.println("enter customer ID: ");
			Scanner input=new Scanner (System.in);
			
			String customerID=input.nextLine(); 
			
			// SQL statement to print films reserved by customer 
			String reservedFilmsbyCustomer="select * from film where filmID in (select filmID from reserved where customerID="+customerID + ")";
			ResultSet rs = st.executeQuery(reservedFilmsbyCustomer);
			
			// Output the results set to the user
			System.out.println("Film/Films reserved by this customer:");
			
			while (rs.next()){
				
				int filmID = rs.getInt("filmID");
				String Title = rs.getString("Title");
				String rentalPrice = rs.getString("rentalPrice");
				String Kind = rs.getString("Kind");
				
				System.out.print(filmID + " ");
				System.out.print(Title + " ");
				System.out.print(rentalPrice + " ");
				System.out.print(Kind + " \n");

			}
			
			if (st != null) {
				st.close();		// Close the SQL statement
			}
			if (rs != null){	// Close the Result Set
				rs.close();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void makeReservation() // Define method to make reservation 
	{
		try {
			Statement st = connection.createStatement(); // Create an SQL statement
			
			// Print customer IDs and names  
			String customerInfo="select * from customer";
			ResultSet rs = st.executeQuery(customerInfo);
			
			System.out.println("list of customers are: ");

			while (rs.next()){

				int customerID = rs.getInt("customerID");
				String Name = rs.getString("Name");
				
				System.out.print(customerID + " ");
				System.out.print(Name + " \n");
			}
			
			// Prompt user to input a customer ID 
			System.out.println("enter customer ID: ");
			Scanner input=new Scanner (System.in); 
			String customerID=input.nextLine();
			
			// Print all films 
			String filmInfo="select * from film";
			rs = st.executeQuery(filmInfo);
			
			System.out.println("list of films are: ");
			
			// Output the results set to the user
			while (rs.next()){
				int filmID = rs.getInt("filmID");
				String Title = rs.getString("Title");
				String RentalPrice = rs.getString("RentalPrice");
				String Kind = rs.getString("Kind");
				
				System.out.print(filmID + " ");
				System.out.print(Title + " ");
				System.out.print(RentalPrice + " ");
				System.out.print(Kind + " \n");
			}
			System.out.println("enter film ID: ");
			String filmID=input.nextLine();
			
			String insertReservation="insert into reserved(customerID,filmID) VALUES("+customerID+","+filmID+")";
			
			st.executeUpdate(insertReservation);
			
			System.out.println("");
			
			if (st != null) {
				st.close();		//Close the SQL statement
			}
			if (rs != null){	//Close the Result Set
				rs.close();
			}

		} 
		catch (SQLIntegrityConstraintViolationException e) {
			System.out.println("This film already reserved by this customer");
			
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void showStatistics()
	{
		try {
			Statement st = connection.createStatement(); // Create an SQL statement
			
			// To print details of films rented more than two times
			System.out.println("Details of Films Rented More Than Two Times: ");
			String popularFilms="select * from film where filmID in (select filmID from reserved group by filmID having count(filmID)>2);";
			ResultSet rs = st.executeQuery(popularFilms);
			
			// Output the results set to the user
			while (rs.next()){
				
				int filmID = rs.getInt("filmID");
				String Title = rs.getString("Title");
				String rentalPrice = rs.getString("rentalPrice");
				String Kind = rs.getString("Kind");
				
				System.out.print(filmID + " ");
				System.out.print(Title + " ");
				System.out.print(rentalPrice + " ");
				System.out.print(Kind + " \n");
			}
			
			// To print Customer Id and Names who rented most expensive film/films
			System.out.println(" ");
			System.out.println("Customer Id and Names Who Rented Most Expensive Film/Films: ");
			String customerRentedMostExpensiveFilm= "Select customerID, Name from customer where customer.customerID in (select customer.customerID from reserved, customer, film where reserved.customerID= customer.customerID and film.filmID= reserved.filmID and film.filmID in (select filmID from film where rentalPrice in (select max(rentalPrice) from film)));"; 
			rs = st.executeQuery(customerRentedMostExpensiveFilm); 
			
			while (rs.next()){ 
				
				int customerID = rs.getInt("customerID");
				String Name = rs.getString("Name");
				
				System.out.print(customerID + " ");
				System.out.print(Name + " \n");
			}
			
			// To print the average rental price
			System.out.println(" ");
            System.out.println("The Average Rental Price: ");
            String values5 = "Select avg(RentalPrice) as RentalPrice from film,reserved where film.filmID = reserved.filmID;";
            rs = st.executeQuery(values5);

            while (rs.next()){
            	
            int AverageRentalPrice = rs.getInt("RentalPrice");

            System.out.println(AverageRentalPrice);
            }
			
            // To print customers who reserved film and live in Dublin 
            System.out.println(" ");
			System.out.println("Customers Who Reserved Film and Live in Dublin: ");
			String customersInDublin= "Select Name from customer where City='Dublin' and exists (select * from reserved where reserved.customerID=customer.customerID);"; 
			rs = st.executeQuery(customersInDublin);
				
			while (rs.next()){
				
			String Name = rs.getString("Name");
			System.out.print(Name + " \n");}
				
			if (st != null) {
				st.close();		// Close the SQL statement
			}
			if (rs != null){	// Close the Result Set
				rs.close();
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		System.out.println("Starting");
		String mysqlUsername = "xxxxx";
		String mysqlPassword = "xxxxx";
		String mysqlDatabaseName = "xxxx";
		String sshUsername = "xxxx";
		String sshPassword = "xxxx";
		String sshRemoteHost = "xxxx";
		int shhRemotePort = 22;                     
		int localPort = 3310;           	
		String mysqlHost="localhost"; 
		int remoteMySQLPort = 3306;
		
		jdbcConnect con = new jdbcConnect();
		
		con.openSSHTunnel(sshUsername, sshPassword, sshRemoteHost, shhRemotePort, mysqlHost, localPort, remoteMySQLPort);
		con.openConnection(mysqlHost, localPort, mysqlDatabaseName, mysqlUsername, mysqlPassword);
		
		// Print selection menu 
		System.out.println("please enter: \n1: to register new customer: \n2: to view reservatins: \n3: to make a reservatins: \n4: to show statistics:");
		
		// Prompt user to select a method 
		Scanner input=new Scanner(System.in);
		
		int Q= input.nextInt();
		
		// Direct to one of four methods 
		if (Q==1) {
		con.registerNewCustomer();
		}
		if (Q==2) {
			con.viewRegistration();
		}
		if (Q==3) {
			con.makeReservation();
		}
		if (Q==4) {
			con.showStatistics();
		}
		con.closeConnection();
		con.closeSshTunnel(localPort);
	}
}