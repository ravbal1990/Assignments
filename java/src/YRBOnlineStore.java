import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.sql.Date;
import java.util.List;
import java.util.Scanner;

public class YRBOnlineStore {
	private Connection conDB; // Connection to the database system.

	public YRBOnlineStore() {
		// Set up the DB connection.
		try {
			// Register the driver with DriverManager.
			Class.forName("com.ibm.db2.jcc.DB2Driver").newInstance();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(0);
		} catch (InstantiationException e) {
			e.printStackTrace();
			System.exit(0);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			System.exit(0);
		}

		// URL: Which database?
		String url = "jdbc:db2:YRB";

		// Initialize the connection.
		try {
			// Connect with a fall-thru id & password
			conDB = DriverManager.getConnection(url);
		} catch (SQLException e) {
			System.out.print("\nSQL: database connection error.\n");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Let's have autocommit turned off. No particular reason here.
		try {
			conDB.setAutoCommit(false);
		} catch (SQLException e) {
			System.out.print("\nFailed trying to turn autocommit off.\n");
			e.printStackTrace();
			System.exit(0);
		}
		start();
	}

	public static void main(String[] args) {
		YRBOnlineStore yrbOnlineStore = new YRBOnlineStore();
	}

	public void start() {
		System.out.println("************* YRB Online Bookstore *************");
		performSteps();

	}

	public void performSteps() {
		Scanner scanner = new Scanner(System.in);
		boolean customerFound = false;
		int customerId = -1;
		while (!customerFound) {
			System.out.println("Customer Id: ##");
			customerId = scanner.nextInt();
			customerFound = findCustomer(customerId);
		}

		System.out.println("Would you like to update the customer information? (Y/N)");
		String updateCustInfo = scanner.next();

		if (updateCustInfo.equalsIgnoreCase("y")) {
			System.out.println("Customer Name:");
			String newCustName = scanner.nextLine();
			System.out.println("Customer City:");
			String newCustCity = scanner.nextLine();
		}

		List<Book> books = new ArrayList<Book>();

		while (books.isEmpty()) {
			List<String> categories = fetchCategories();
			System.out.println("************* Book Categories *************");
			int i = 1;
			for (String category : categories) {
				System.out.println(i++ + ".\t" + category);
			}

			System.out.println("Choose a category:");
			int categorySelected = scanner.nextInt();

			System.out.println("Category " + categories.get(categorySelected - 1) + " is selected.");

			System.out.println("Title:");
			String title = scanner.nextLine();

			books = fetchBooks(title, categories.get(categorySelected - 1));

			if (books.size() > 0) {
				System.out.println("TITLE \t\t\t\t YEAR \t\t LANGUAGE \t\t CAT \t\t WEIGHT");

				for (Book book : books) {
					System.out.println(book.getTitle() + " \t\t\t\t " + book.getYear() + " \t\t " + book.getLanguage()
							+ " \t\t " + book.getCat() + " \t\t " + book.getWeight());
				}

				System.out.println("Select a book to purchase:");
				int bookSelected = scanner.nextInt();

				Book selectedBookObject = books.get(bookSelected - 1);

				double minPrice = findMinPriceForTheBook(selectedBookObject.getTitle(), selectedBookObject.getYear());

				System.out.println("The minimum price of the book selected is " + minPrice);

				System.out.println("Please enter the number of books to buy");
				int quantityOfBooks = scanner.nextInt();

				double totalPrice = quantityOfBooks * minPrice;

				System.out.println("The total price is " + totalPrice);

				System.out.println("Would you like to purchase the book/books? (Y/N)");

				String confirmPurchase = scanner.next();

				if (confirmPurchase.equalsIgnoreCase("y")) {
					String club = findClubName(title, selectedBookObject.getYear(), minPrice);
					if (insertPurchaseDetails(customerId, club, quantityOfBooks, title, selectedBookObject.getYear())) {
						System.out.println("Thank you for your purchase.");
					}

				} else {
					System.out.println("Would you like to continue? (Y/N)");
					String continueShopping = scanner.next();
					if (continueShopping.equalsIgnoreCase("y")) {
						books = new ArrayList<Book>();
					} else {
						System.out.println("Good bye!");
						System.exit(1);
					}
				}
			}
		}

		scanner.close();
	}

	public boolean findCustomer(int customerId) {

		boolean inDB = false;
		PreparedStatement querySt = null; // The query handle.
		ResultSet result = null; // A cursor.
		String findCustomerQueryText = "SELECT * FROM YRB_CUSTOMER WHERE CID = ?";

		// Prepare the query.
		try {
			querySt = conDB.prepareStatement(findCustomerQueryText);
		} catch (SQLException e) {
			System.out.println("SQL#1 failed in prepare");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Execute the query.
		try {
			querySt.setInt(1, customerId);
			result = querySt.executeQuery();
		} catch (SQLException e) {
			System.out.println("SQL#1 failed in execute");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Any answer?
		try {
			if (result.next()) {
				inDB = true;
				String custId = result.getString("cid");
				String custName = result.getString("name");
				String custCity = result.getString("city");

				System.out.println(
						"Hello " + custName + " your customer ID is " + custId + " and your city is " + custCity);
			}
		} catch (SQLException e) {
			System.out.println("SQL#1 failed in cursor.");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Close the cursor.
		try {
			result.close();
		} catch (SQLException e) {
			System.out.print("SQL#1 failed closing cursor.\n");
			System.out.println(e.toString());
			System.exit(0);
		}

		// We're done with the handle.
		try {
			querySt.close();
		} catch (SQLException e) {
			System.out.print("SQL#1 failed closing the handle.\n");
			System.out.println(e.toString());
			System.exit(0);
		}

		return inDB;
	}

	public boolean updateCustInfo(String custName, String custCity, int custId) {

		boolean updated = false;
		PreparedStatement querySt = null; // The query handle.
		int result = -1; // No. of rows updated.
		String updateCustomerQueryText = "UPDATE YRB_CUSTOMER SET NAME=?, CITY=? WHERE CID = ?";

		// Prepare the query.
		try {
			querySt = conDB.prepareStatement(updateCustomerQueryText);
		} catch (SQLException e) {
			System.out.println("SQL#2 failed in prepare");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Execute the update query.
		try {
			querySt.setString(1, custName);
			querySt.setString(2, custCity);
			querySt.setInt(3, custId);
			result = querySt.executeUpdate();
		} catch (SQLException e) {
			System.out.println("SQL#2 failed in execute");
			System.out.println(e.toString());
			System.exit(0);
		}
		if (result == 1) {
			updated = true;
			System.out.println("Cust info is updated");
		} else {
			System.out.println("The cust info is was not updated");
		}

		// We're done with the handle.
		try {
			querySt.close();
		} catch (SQLException e) {
			System.out.print("SQL#2 failed closing the handle.\n");
			System.out.println(e.toString());
			System.exit(0);
		}

		return updated;

	}

	public List<String> fetchCategories() {
		List<String> categories = new ArrayList<String>();

		PreparedStatement querySt = null; // The query handle.
		ResultSet result = null; // A cursor.
		String findCategoriesQueryText = "SELECT * FROM YRB_CATEGORY";

		// Prepare the query.
		try {
			querySt = conDB.prepareStatement(findCategoriesQueryText);
		} catch (SQLException e) {
			System.out.println("SQL#3 failed in prepare");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Execute the query.
		try {
			result = querySt.executeQuery();
		} catch (SQLException e) {
			System.out.println("SQL#3 failed in execute");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Any category?
		try {
			while (result.next()) {
				categories.add(result.getString("cat"));
			}
		} catch (SQLException e) {
			System.out.println("SQL#3 failed in cursor.");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Close the cursor.
		try {
			result.close();
		} catch (SQLException e) {
			System.out.print("SQL#3 failed closing cursor.\n");
			System.out.println(e.toString());
			System.exit(0);
		}

		// We're done with the handle.
		try {
			querySt.close();
		} catch (SQLException e) {
			System.out.print("SQL#3 failed closing the handle.\n");
			System.out.println(e.toString());
			System.exit(0);
		}

		return categories;
	}

	public List<Book> fetchBooks(String title, String category) {
		List<Book> books = new ArrayList<Book>();

		PreparedStatement querySt = null; // The query handle.
		ResultSet result = null; // A cursor.
		String findBooksQueryText = "SELECT * FROM YRB_BOOK WHERE TITLE = ? and CAT=?";

		// Prepare the query.
		try {
			querySt = conDB.prepareStatement(findBooksQueryText);
		} catch (SQLException e) {
			System.out.println("SQL#4 failed in prepare");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Execute the query.
		try {
			querySt.setString(1, title);
			querySt.setString(2, category);
			result = querySt.executeQuery();
		} catch (SQLException e) {
			System.out.println("SQL#4 failed in execute");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Any book?
		try {
			while (result.next()) {
				Book book = new Book();
				book.setCat(result.getString("cat"));
				book.setLanguage(result.getString("language"));
				book.setTitle(result.getString("title"));
				book.setWeight(result.getInt("weight"));
				book.setYear(result.getInt("year"));

				books.add(book);
			}
		} catch (SQLException e) {
			System.out.println("SQL#4 failed in cursor.");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Close the cursor.
		try {
			result.close();
		} catch (SQLException e) {
			System.out.print("SQL#4 failed closing cursor.\n");
			System.out.println(e.toString());
			System.exit(0);
		}

		// We're done with the handle.
		try {
			querySt.close();
		} catch (SQLException e) {
			System.out.print("SQL#4 failed closing the handle.\n");
			System.out.println(e.toString());
			System.exit(0);
		}

		return books;
	}

	public double findMinPriceForTheBook(String title, int year) {

		double minPrice = -1;
		PreparedStatement querySt = null; // The query handle.
		ResultSet result = null; // A cursor.
		String findMinPriceForTheBookQueryText = "SELECT min(price) as price FROM YRB_OFFER WHERE TITLE = ? and YEAR=?";

		// Prepare the query.
		try {
			querySt = conDB.prepareStatement(findMinPriceForTheBookQueryText);
		} catch (SQLException e) {
			System.out.println("SQL#5 failed in prepare");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Execute the query.
		try {
			querySt.setString(1, title);
			querySt.setInt(2, year);
			result = querySt.executeQuery();
		} catch (SQLException e) {
			System.out.println("SQL#5 failed in execute");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Any answer?
		try {
			if (result.next()) {
				minPrice = result.getDouble("price");
			}
		} catch (SQLException e) {
			System.out.println("SQL#5 failed in cursor.");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Close the cursor.
		try {
			result.close();
		} catch (SQLException e) {
			System.out.print("SQL#5 failed closing cursor.\n");
			System.out.println(e.toString());
			System.exit(0);
		}

		// We're done with the handle.
		try {
			querySt.close();
		} catch (SQLException e) {
			System.out.print("SQL#5 failed closing the handle.\n");
			System.out.println(e.toString());
			System.exit(0);
		}

		return minPrice;
	}

	public boolean insertPurchaseDetails(int cid, String club, int qty, String title, int year) {

		boolean updated = false;
		PreparedStatement querySt = null; // The query handle.
		int result = -1; // No. of rows updated.
		String updateCustomerQueryText = "INSERT INTO YRB_PURCHASE (CID,CLUB,QNTY,TITLE,WHEN,YEAR) VALUES (?,?,?,?,?,?)";

		// Prepare the query.
		try {
			querySt = conDB.prepareStatement(updateCustomerQueryText);
		} catch (SQLException e) {
			System.out.println("SQL#6 failed in prepare");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Execute the update query.
		try {
			querySt.setInt(1, cid);
			querySt.setString(2, club);
			querySt.setInt(3, qty);
			querySt.setString(4, title);
			querySt.setDate(5, new Date(new java.util.Date().getTime()));
			querySt.setInt(6, year);
			result = querySt.executeUpdate();
		} catch (SQLException e) {
			System.out.println("SQL#6 failed in execute");
			System.out.println(e.toString());
			System.exit(0);
		}
		if (result == 1) {
			updated = true;
			System.out.println("Inserted the purchase details");
		} else {
			System.out.println("Was not able to insert the purchase details");
		}

		// We're done with the handle.
		try {
			querySt.close();
		} catch (SQLException e) {
			System.out.print("SQL#6 failed closing the handle.\n");
			System.out.println(e.toString());
			System.exit(0);
		}

		return updated;
	}

	public String findClubName(String title, int year, double price) {

		String club = null;
		PreparedStatement querySt = null; // The query handle.
		ResultSet result = null; // A cursor.
		String findMinPriceForTheBookQueryText = "SELECT club as price FROM YRB_OFFER WHERE TITLE = ? and YEAR=? and PRICE =?";

		// Prepare the query.
		try {
			querySt = conDB.prepareStatement(findMinPriceForTheBookQueryText);
		} catch (SQLException e) {
			System.out.println("SQL#7 failed in prepare");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Execute the query.
		try {
			querySt.setString(1, title);
			querySt.setInt(2, year);
			querySt.setDouble(3, price);
			result = querySt.executeQuery();
		} catch (SQLException e) {
			System.out.println("SQL#7 failed in execute");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Any answer?
		try {
			if (result.next()) {
				club = result.getString("club");
			}
		} catch (SQLException e) {
			System.out.println("SQL#7 failed in cursor.");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Close the cursor.
		try {
			result.close();
		} catch (SQLException e) {
			System.out.print("SQL#7 failed closing cursor.\n");
			System.out.println(e.toString());
			System.exit(0);
		}

		// We're done with the handle.
		try {
			querySt.close();
		} catch (SQLException e) {
			System.out.print("SQL#7 failed closing the handle.\n");
			System.out.println(e.toString());
			System.exit(0);
		}

		return club;

	}

}

class Book {
	private String title;
	private int year;
	private String language;
	private String cat;
	private int weight;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getCat() {
		return cat;
	}

	public void setCat(String cat) {
		this.cat = cat;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

};