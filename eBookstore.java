import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Scanner;

import java.lang.Integer;

/* The eBookstore class provides a basic text user interface for a user to interact with a MySQL database of books.
 * The user is prompted with a main menu allowing them to insert, delete, update, and select from the database by
 * calling associated methods which prompt the user with simple questions to build and execute a custom SQL statement.
 */
public class eBookstore {
	//Initialize length limits for title, author, declare Scanner used for all user input
	private static final int titleMaxSize = 100;
	private static final int authorMaxSize = 50;
	private static Scanner userInputScanner;
	
	//Initialize database-wide menu options
	private static final String idOption = "ID Number";
	private static final String titleOption = "Title";
	private static final String authorOption = "Author";
	private static final String qtyOption = "Quantity";
	
	//Used to verify user inputted integers are >=0 and user inputted Strings are length >=0
	private static final int inputMinimum = 0;

	//Prompts user for the information on a new book and inserts the book into the database.
	//Returns the number of records added to the database (1 if book is entered successfully, 0 if user quits mid-entry)
	public static int enterBook(Statement stmt) throws SQLException {
		//Minimum of 0 used to ensure all ints are >0 and all Strings are length >0
		int id, qty, recordsAffected = 0;
		String title, author, insertStatement = "INSERT INTO books VALUES (";

		System.out.println();
		System.out.println("-- ENTER BOOK INTO THE EBOOKSTORE --");
		System.out.println("Enter \"exit\" at any time to return to the main menu.");
		
		//Prompts user for an id; will repeat until a valid id not currently in the database is entered
		while(true) {
			id = getIntFromUser("Please enter the book "+idOption+": ","The id must be a unique non-negative integer.",inputMinimum);
			if(id < inputMinimum)
				return recordsAffected;
			else {
				//Check the new id isn't already in the database
				ResultSet checkIDSet = stmt.executeQuery("SELECT * FROM books WHERE id = "+id);
				
				if (checkIDSet.next()) { //id invalid
					System.out.print("That "+idOption+" is already assigned to the following book: ");
					printResultSet(checkIDSet);
					System.out.println("Please choose a unique "+idOption+".");
					checkIDSet.close();
					
				} else { //id valid
					break;
				}
			}
		}
		
		title = getStringFromUser("Please enter the book title: ","title",inputMinimum,titleMaxSize);
		if(title == "Exit")
			return recordsAffected;
		
		author = getStringFromUser("Please enter the book author: ","author",inputMinimum,authorMaxSize);
		if(author == "Exit")
			return recordsAffected;
		
		qty = getIntFromUser("Please enter the quantity of this book: ","The quantity must be a non-negative integer.",inputMinimum);
		if(qty < inputMinimum)
			return recordsAffected;
		
		insertStatement += id + ", '" + title + "', '" + author + "', " + qty + ")";
		
		System.out.println();	
		System.out.println("Query: "+insertStatement+";");

		recordsAffected = stmt.executeUpdate(insertStatement);
		
		return recordsAffected;
	}
	
	/* Updates a book currently in the database. Allows the user to specify either one column to be updated or update all columns at once.
	 * Returns the number of books affected (0 if update was unsuccessful, 1 if successful)
	 */
	public static int updateBook(Statement stmt) throws SQLException {
		int id, qty, recordsAffected = 0, currentID = -1;
		String title, author, columnToUpdate = "", updateStatement = "UPDATE books SET";
		ResultSet bookToUpdate = null;
		
		//Initialize update menu specific menu prompt and options
		final String updateBookMenuPrompt = "Please enter the number or name of the column to update below:"+"\n";
		final String allOption = "All Columns";
				
		//Welcome Message
		System.out.println("-- UPDATE BOOK IN THE EBOOKSTORE --");
		System.out.println("Enter \"exit\" at any time to return to the main menu.");
		
		//Get id of book to update from user, check whether id exists in database, repeat until a valid id is entered
		do {
			if(currentID != -1) //Prints on subsequent loops if the user inputed id was unable to be located
				System.out.println("The database does not have a book associated with id "+ currentID + ". Please check the ID and try again.");
				
			currentID = getIntFromUser("Please enter the book id to be updated: ","The id must be a non-negative integer.",inputMinimum);
			if(currentID < inputMinimum)
				return recordsAffected;

			bookToUpdate = stmt.executeQuery("SELECT * FROM books WHERE id = "+currentID);
		} while(!bookToUpdate.next());
		
		//Get ResultSet, print current values for user reference (id is primary key, so ResultSet can have only 1 result)
		System.out.println("The book's current information is: ");
		printResultSet(bookToUpdate);
		
		//Prompts the user for the column(s) to be updated until either a valid number/column name is specified or 'exit' is entered
		columnToUpdate = promptUserWithMenu(updateBookMenuPrompt, idOption, titleOption, authorOption, qtyOption, allOption);

		//Update id
		if (columnToUpdate.equalsIgnoreCase(idOption) || columnToUpdate.equalsIgnoreCase(allOption)) {
			//Prompts user for an id; will repeat until a valid id not currently in the database is entered
			while(true) {
				id = getIntFromUser("Please enter the new book "+idOption+": ","The id must be a unique non-negative integer.",inputMinimum);
				if(id < inputMinimum)
					return recordsAffected;
				else {
					//Check the new id isn't already in the database
					ResultSet checkIDSet = stmt.executeQuery("SELECT * FROM books WHERE id = "+id);
					
					if (checkIDSet.next()) { //id invalid
						System.out.print("That "+idOption+" is already assigned to the following book: ");
						printResultSet(checkIDSet);
						System.out.println("Please choose a unique "+idOption+".");
						checkIDSet.close();
						
					} else { //id valid
						updateStatement += " id = " + id;
						if(columnToUpdate.equalsIgnoreCase(allOption))
							updateStatement += ",";
						
						break;
					}
				}
			}
					
		} 
		
		//Update title
		if(columnToUpdate.equalsIgnoreCase(titleOption) || columnToUpdate.equalsIgnoreCase(allOption)) { 
			title = getStringFromUser("Please enter the new book title: ","title",inputMinimum, titleMaxSize);
			if(title == "quit")
				return recordsAffected;
			else {
				updateStatement += " title = '" + title +"'";
				if(columnToUpdate.equalsIgnoreCase(allOption))
					updateStatement += ",";
			}
		} 
		
		 //Update author
		if(columnToUpdate.equalsIgnoreCase(authorOption) || columnToUpdate.equalsIgnoreCase(allOption)) {
			author = getStringFromUser("Please enter the new book author: ","author",inputMinimum, authorMaxSize);
			if(author == "quit")
				return recordsAffected;
			else {
				updateStatement += " author = '" + author +"'";
				if(columnToUpdate.equalsIgnoreCase(allOption))
					updateStatement += ",";
			}
		} 
		
		//Update quantity	
		if(columnToUpdate.equalsIgnoreCase(qtyOption) || columnToUpdate.equalsIgnoreCase(allOption)) {	
			qty = getIntFromUser("Please enter the new book qty: ","The qty must be a non-negative integer.",inputMinimum);
			if(qty < inputMinimum)
				return recordsAffected;
			else
				updateStatement += " qty = " + qty;		
		}
			
		//Exit case
		if(columnToUpdate.equals("Exit")) {
			return recordsAffected;
		}
		
		updateStatement += " WHERE id = " + currentID;
		
		System.out.println();	
		System.out.println("Query: "+updateStatement+";");

		recordsAffected = stmt.executeUpdate(updateStatement);
		
		return recordsAffected;
	}
	
	//Allows a user to delete a book from the database by specifying its id. The book's current information will be printed and
	// the user will then have to confirm the deletion. Returns the number of books deleted (0 if deletion was unsuccessful, 1 if successful)
	public static int deleteBook(Statement stmt) throws SQLException {
		String deletionConfirmed, deleteStatement = "DELETE FROM books WHERE id = ";
		int recordsAffected = 0, id = -1;
		ResultSet bookToDelete;
		
		//Get id of book to delete from user, check whether id exists in database, repeat until a valid id is entered
		do {
			if(id != -1) //Prints on subsequent loops if the user inputed id was unable to be located
				System.out.println("The database does not have a book associated with id "+ id + ". Please check the ID and try again.");
				
			//Get new id
			id = getIntFromUser("Please enter the book id to be deleted: ","The id must be a non-negative integer.",inputMinimum);
			if(id < inputMinimum)
				return recordsAffected;

			//Check if valid
			bookToDelete = stmt.executeQuery("SELECT * FROM books WHERE id = "+id);
		} while(!bookToDelete.next());
				
		System.out.print("The book's current information is: ");
		printResultSet(bookToDelete);
		
		//Initialize deletion confirmation specific menu prompt and options
		final String deleteBookMenuPrompt = "Please confirm this book should be deleted (cannot be undone):"+"\n";
		final String yesOption = "Yes";
		final String noOption = "No";
		
		deletionConfirmed = promptUserWithMenu(deleteBookMenuPrompt, yesOption, noOption);
			
		if (deletionConfirmed.equals(noOption) || deletionConfirmed.equals("Exit")) //Exit without deleting
				return recordsAffected;
		else { //Delete book
			deleteStatement += id;
			recordsAffected = stmt.executeUpdate(deleteStatement);
			return recordsAffected;
		}
	}

	//Search for books by a user selected column and criteria, prints the query and the values of any books found, returns number of books found.
	public static int searchBooks(Statement stmt) throws SQLException {
		String menuSelection, criteriaValue, criteriaOperator = "=", selectStatement = "SELECT * FROM books WHERE ";
		int recordsFound = 0;
		boolean searchingByInt;
		
		//Initialize main method menu prompt
		final String searchBooksMenuPrompt = "Please enter the number or name of a search criteria below:"+"\n";

		//Welcome message
		System.out.println();
		System.out.println("-- SEARCH BOOKS IN THE EBOOKSTORE --");
		System.out.println("Enter \"Exit\" at any time to return to the main menu without making changes.");
		
		//Prompts user for a column to search
		menuSelection = promptUserWithMenu(searchBooksMenuPrompt, idOption, titleOption, authorOption, qtyOption);
			
		//Calls appropriate method for user selection
		if(menuSelection.equalsIgnoreCase(idOption)) {
			selectStatement += "id ";
			criteriaValue = Integer.toString(getIntFromUser("Please enter the "+menuSelection+" to search by: ","The"+menuSelection+"must be a non-negative integer.",inputMinimum));
			searchingByInt = true;
			
		} else if(menuSelection.equalsIgnoreCase(titleOption)) { 
			selectStatement += "title ";
			criteriaValue = getStringFromUser("Please enter the "+menuSelection+" to search by: ","title",inputMinimum,titleMaxSize);
			searchingByInt = false;
			
		} else if(menuSelection.equalsIgnoreCase(authorOption)) { 
			selectStatement += "author ";
			criteriaValue = getStringFromUser("Please enter the "+menuSelection+" to search by: ","author",inputMinimum,authorMaxSize);
			searchingByInt = false;
			
		} else if(menuSelection.equalsIgnoreCase(qtyOption)) { 
			selectStatement += "qty ";
			criteriaValue = Integer.toString(getIntFromUser("Please enter the "+menuSelection+" to search by: ","The"+menuSelection+"must be a non-negative integer.",inputMinimum));
			searchingByInt = true;
			
		} else { //Exit case
			return recordsFound;
		}	
		
		//If selected column is int data type, the criteriaOperator can also be < or >
		if (searchingByInt) {
			//Check criteriaValue for exit condition
			if (criteriaValue.equals(Integer.toString((inputMinimum-1))))
				return recordsFound;
			
			//Set up criteriaOperator specific menu prompts
			final String criteriaOperatorMenuPrompt = "Please indicate whether to search for values equal to, less than, or greater than "+criteriaValue+":";
			final String equalsOption = "=";
			final String lessThanOption = "<";
			final String greaterThanOption = ">";

			//Prompt for criteriaOperator, check for exit condition
			criteriaOperator = promptUserWithMenu(criteriaOperatorMenuPrompt, equalsOption, lessThanOption, greaterThanOption);
			if (criteriaOperator == "Exit")
				return recordsFound;
			
			//Add operator and value to select statement, print query and execute
			selectStatement += criteriaOperator + " " + criteriaValue;
		} else { //Searching by String
			//Check criteriaValue for exit condition
			if (criteriaValue == "Exit")
				return recordsFound;
			
			//Add operator and value in quotes to select statement, print query and execute
			selectStatement += criteriaOperator + " '" + criteriaValue + "'";
		}
		
		System.out.println("\n"+"Query: "+selectStatement+";");
		
		ResultSet searchResultSet = stmt.executeQuery(selectStatement);
		
		while ( searchResultSet.next ()) {
			recordsFound++;
			
			System.out.println();
			printResultSet(searchResultSet);
		}
		
		return recordsFound;
	}
	
	/* Continuously prompts a user for an integer with a given prompt message.
	 * Non-integer input besides "quit" or "exit" will be ignored and print the errorPrompt and Prompt messages.
	 * Integer input below  the min will be ignored and print a generic error message and the Prompt message.
	 * If user enters "quit" or "exit" the method returns the minimum minus 1 which will cause the caller to return to the main menu.
	 */
	public static int getIntFromUser(String prompt, String errorPrompt, int min) {
		int userInput;
		String stringInput;
		
		//Breaks on "exit" or valid integer
		while (true) {
			System.out.print("\n"+prompt);
			if(!userInputScanner.hasNextInt()) {
				stringInput = userInputScanner.nextLine();
				if(stringInput.equalsIgnoreCase("exit")) {
					return min - 1;
				} else {
					//Non-integer input other than "exit"
					System.out.println(errorPrompt);
				}
			} else {
				userInput = userInputScanner.nextInt();
				userInputScanner.nextLine();
				//Check integer is at least the minimum
				if (userInput < min) {
					System.out.println("The integer must be "+min+" or greater.");
				} else {
					return userInput;
				}
			}
		}
	}
	
	//Continuously prompts a user for a String with a given prompt message.
	//Strings that don't meet the length requirements (min/max inclusive) will be rejected and print an error message and the prompt
	//User can escape the prompt by typing "exit" (returns "Exit")
	public static String getStringFromUser(String prompt, String dataName, int minLength, int maxLength) {
		String userInput;
		
		while (true) {
			System.out.print("\n"+prompt);
			userInput = userInputScanner.nextLine();
			if(userInput.equalsIgnoreCase("exit")) {
				return "Exit";
			} else {
				if (userInput.length() < minLength || userInput.length() > maxLength) {
					System.out.println("The "+dataName+" must be at least "+minLength+" characters and at most "+maxLength+" characters.");
				} else {
					return userInput;
				}
			}
		}
	}
	
	
	/* Presents the user with numbered options passed in through the menuOptions parameter, along with an option to exit appended.
	 * Users can select an option by entering either it's name or number, returning the option name
	 * Any other input will be rejected, and the menu will be re-displayed
	 * Thus, the return value can only be an element of menuOptions or "Exit"
	 */
	public static String promptUserWithMenu(String menuPrompt, String... menuOptions) {
		String menuOptionsString = "", userSelection = "";
		int maxLength = 0;
		boolean inputValid = false;
		
		//Number each option (1, 2, 3...) and add it to menuOptionsString, track the length of the longest option
		for (int optionNumber = 1; optionNumber < (menuOptions.length+1); optionNumber++) {
			String newOption = optionNumber+ ". " + menuOptions[optionNumber-1] + "\n";
			
			menuOptionsString += newOption;
			
			if (newOption.length() > maxLength)
				maxLength = newOption.length();
		}
		
		//Add quit option (numbered 0)
		menuOptionsString += "0. Exit" + "\n\n";
		
		//Present menu, get user selection, repeat until valid input is received
		//Valid input defined as user input matching to a menuOption or the number of a menuOption
		while (!inputValid) {
			userSelection = getStringFromUser(menuPrompt + "\n" + menuOptionsString,"selection",inputMinimum,maxLength);
			
			//Try parsing user input as Integer (option number)
			if(parsableAsInt(userSelection)) {
				int userSelectionAsInt = Integer.parseInt(userSelection);
				
				if(userSelectionAsInt == 0)
					return "Exit";
				else {
					//Check if input matches the number of a menuOption
					for (int optionNumber = 1; optionNumber < (menuOptions.length+1); optionNumber++) {
						if (userSelectionAsInt == optionNumber) {
							userSelection = menuOptions[optionNumber-1];
							inputValid = true;
							break;
						}	
					}
				}
			//Try parsing user input as String (option name)
			} else {
				if(userSelection.equalsIgnoreCase("Exit"))
					return "Exit";
				else {
					//Check if input matches a menuOption
					for (String menuOption : menuOptions) {
						if (userSelection.equalsIgnoreCase(menuOption)) {
							userSelection = menuOption;
							inputValid = true;
							break;
						}	
					}
				}
			}
			
			if(!inputValid) //No matches: input invalid, repeat prompt.
				System.out.println("Your selection was not recognized. Please try again.");
		}
		
		return userSelection;
	}
	
	//Returns true if String can be parsed as an Integer, else false
	public static boolean parsableAsInt(String stringToParse) {
		try {
			Integer.parseInt(stringToParse);
			return true;
		} catch (NumberFormatException nfe) {
			return false;
		}
	}
	
	//Prints out the id, title, author, and qty of the provided ResultSet
	public static void printResultSet(ResultSet rset) throws SQLException{
		System.out.println("("+ idOption + ": " + rset.getInt("id") + ", "
			+ titleOption + ": " + rset.getString("title") + ", "
			+ authorOption + ": " + rset.getString("author") + ", "
			+ qtyOption + ": " + rset.getInt("qty") + ")");
	}
	
	
	//Main method presents user with a main menu that calls the appropriate method or exits based on their selection.
	public static void main ( String [] args ) {
		System.out.print("Connecting to database... ");
		try (
			//Allocate the Connection and Statement resources, replace XXXX with user password.
			Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ebookstore?allowPublicKeyRetrieval=true&useSSL=false" , "myuser" , "XXXX" );
			Statement stmt = conn.createStatement ();
		) {
			//Initialize Scanner and main-menu-specific prompts
			userInputScanner = new Scanner(System.in);
			final String mainMenuPrompt = "-- MAIN MENU --"+"\n"+
					   					  "Please enter the number or name of an option below:"+"\n";
			final String enterBookOption = "Enter Book";
			final String updateBookOption = "Update Book";
			final String deleteBookOption = "Delete Book";
			final String searchBooksOption = "Search Books";
			
			//Variables regarding input and output of queries
			String statementVerb = ""; //statementVerb is used to describe the effect of the query (e.g. "updated","deleted")
			String menuSelection;	
			int recordsAffected;
			
			//Welcome Message
			System.out.println("Success!");
			System.out.println("Welcome to the eBookstore!");
			
			//Continually prompts user for a menu option input until a valid input is received
			//The appropriate method is then called  based on the input
			//The loop is only broken when the exit input option is received
			while (true) {
				menuSelection = promptUserWithMenu(mainMenuPrompt, enterBookOption, updateBookOption, deleteBookOption, searchBooksOption);
				
				//Calls appropriate method for user selection
				if(menuSelection.equalsIgnoreCase(enterBookOption)) {
					recordsAffected = enterBook(stmt);
					statementVerb = "entered";
					
				} else if(menuSelection.equalsIgnoreCase(updateBookOption)) {
					recordsAffected = updateBook(stmt);
					statementVerb = "updated";
					
				} else if(menuSelection.equalsIgnoreCase(deleteBookOption)) {
					recordsAffected = deleteBook(stmt);
					statementVerb = "deleted";
					
				} else if(menuSelection.equalsIgnoreCase(searchBooksOption)) {
					recordsAffected = searchBooks(stmt);
					statementVerb = "found";
					
				} else { //Exit Case, terminate loop
					break; 
				}
				
				//Print result of operation
				if (recordsAffected == 0)
					System.out.println("\n"+"The book was not " + statementVerb + ".");
				else if (recordsAffected == 1)
					System.out.println("\n"+"The book was " + statementVerb + " successfully.");
				else if (recordsAffected > 1)
					System.out.println("\n"+recordsAffected + " books were " + statementVerb + " successfully.");
			}
			
			//Exit message, close Scanner (Statement/Connection closed automatically by try-with-resources)
			System.out.println();
			System.out.println("Program ended. Thank you for using the eBookstore!");
			userInputScanner.close();

		} catch ( SQLException ex ) {
			userInputScanner.close();
			ex.printStackTrace();
		}
	}
}