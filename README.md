# eBookstore
A text based application in Java for adding, removing, updating, and searching for records in 
a MySQL database that could be used by employees to manage the inventory of an online bookstore.

To use, set up a database called ebookstore and a table called books with columns for the id (int),
Title (varchar (by default length 100)), Author (varchar (by default length 50)), and Qty (int) of
books for each record. The id will be used as the primary key.

'''
create database if not exists ebookstore;
use ebookstore;
drop table if exists books;
create table books (id int, Title varchar(100), Author varchar(50), Qty int, PRIMARY KEY (id));
'''

Then in eBookstore.java, simply replace "myuser" and "XXXX" with your username and password on line 466
'''
Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ebookstore?allowPublicKeyRetrieval=true&useSSL=false" , "myuser" , "XXXX" );
'''

The application is now ready to launch. The user will be presented with relevant menus for ebookstore operations.