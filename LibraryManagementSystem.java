import java.io.*;
import java.util.*;

class Book implements Serializable {
    String title;
    String author;
    String ISBN;
    boolean available = true;

    public Book(String title, String author, String ISBN) {
        this.title = title;
        this.author = author;
        this.ISBN = ISBN;
    }

    @Override
    public String toString() {
        return String.format("Title: %s | Author: %s | ISBN: %s | Available: %s",
                title, author, ISBN, available);
    }
}

class Member implements Serializable {
    String memberId;
    String name;
    ArrayList<String> borrowedISBNs = new ArrayList<>();

    public Member(String memberId, String name) {
        this.memberId = memberId;
        this.name = name;
    }
}

class Library implements Serializable {
    ArrayList<Book> books = new ArrayList<>();
    ArrayList<Member> members = new ArrayList<>();

    public void addBook(Book b) { books.add(b); }

    public void removeBook(String isbn) {
        books.removeIf(b -> b.ISBN.equals(isbn));
    }

    public Book findByISBN(String isbn) {
        for (Book b : books)
            if (b.ISBN.equals(isbn)) return b;
        return null;
    }

    public ArrayList<Book> searchByTitle(String keyword) {
        ArrayList<Book> res = new ArrayList<>();
        for (Book b : books)
            if (b.title.toLowerCase().contains(keyword.toLowerCase())) res.add(b);
        return res;
    }

    public void displayAllBooks() {
        if (books.isEmpty()) {
            System.out.println("No books in library.");
            return;
        }
        for (Book b : books) System.out.println(b);
    }

    public void addMember(Member m) { members.add(m); }

    public Member findMember(String memberId) {
        for (Member m : members)
            if (m.memberId.equals(memberId)) return m;
        return null;
    }

    public boolean issueBook(String memberId, String isbn) {
        Member m = findMember(memberId);
        Book b = findByISBN(isbn);
        if (m == null) { System.out.println("Member not found."); return false; }
        if (b == null) { System.out.println("Book not found."); return false; }
        if (!b.available) { System.out.println("Book is already issued."); return false; }
        b.available = false;
        m.borrowedISBNs.add(isbn);
        System.out.println("Book issued successfully.");
        return true;
    }

    public double returnBook(String memberId, String isbn, int daysKept) {
        Member m = findMember(memberId);
        Book b = findByISBN(isbn);
        if (m == null || b == null) { System.out.println("Invalid return operation."); return 0; }
        if (!m.borrowedISBNs.remove(isbn)) {
            System.out.println("This member did not borrow this ISBN.");
            return 0;
        }
        b.available = true;
        int allowedDays = 14;
        double fine = 0;
        if (daysKept > allowedDays) fine = (daysKept - allowedDays) * 10.0;
        System.out.println("Book returned. Fine: " + fine);
        return fine;
    }
}

public class LibraryManagementSystem {
    static final String DATA_FILE = "library_data.ser";

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        Library lib = loadLibrary();

        // Seed with sample data
        if (lib.books.isEmpty()) {
            lib.addBook(new Book("Clean Code", "Robert C. Martin", "9780132350884"));
            lib.addBook(new Book("Introduction to Algorithms", "Cormen et al", "9780262033848"));
            lib.addBook(new Book("Effective Java", "Joshua Bloch", "9780134685991"));
        }
        if (lib.members.isEmpty()) {
            lib.addMember(new Member("M001", "Alice"));
            lib.addMember(new Member("M002", "Bob"));
        }

        while (true) {
            System.out.println("\n--- Digital Library ---");
            System.out.println("1. Admin Login");
            System.out.println("2. User Menu");
            System.out.println("3. Save & Exit");
            System.out.print("Choose: ");
            int ch = sc.nextInt(); sc.nextLine();
            if (ch == 1) {
                System.out.print("Enter admin password: ");
                String pwd = sc.nextLine();
                if (!pwd.equals("admin123")) {
                    System.out.println("Wrong password.");
                    continue;
                }
                adminMenu(sc, lib);
            } else if (ch == 2) {
                userMenu(sc, lib);
            } else if (ch == 3) {
                saveLibrary(lib);
                System.out.println("Data saved. Exiting.");
                break;
            }
        }
        sc.close();
    }

    static void adminMenu(Scanner sc, Library lib) {
        while (true) {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1. Add Book");
            System.out.println("2. Remove Book");
            System.out.println("3. Add Member");
            System.out.println("4. View All Books");
            System.out.println("5. View All Members");
            System.out.println("6. Back");
            System.out.print("Choose: ");
            int ch = sc.nextInt(); sc.nextLine();
            if (ch == 1) {
                System.out.print("Title: ");
                String t = sc.nextLine();
                System.out.print("Author: ");
                String a = sc.nextLine();
                System.out.print("ISBN: ");
                String i = sc.nextLine();
                lib.addBook(new Book(t, a, i));
                System.out.println("Book added.");
            } else if (ch == 2) {
                System.out.print("ISBN to remove: ");
                String i = sc.nextLine();
                lib.removeBook(i);
                System.out.println("Book removed (if existed).");
            } else if (ch == 3) {
                System.out.print("Member Id: ");
                String id = sc.nextLine();
                System.out.print("Name: ");
                String name = sc.nextLine();
                lib.addMember(new Member(id, name));
                System.out.println("Member added.");
            } else if (ch == 4) {
                lib.displayAllBooks();
            } else if (ch == 5) {
                for (Member m : lib.members) {
                    System.out.println("Id: " + m.memberId + " Name: " + m.name + " Borrowed: " + m.borrowedISBNs);
                }
            } else break;
        }
    }

    static void userMenu(Scanner sc, Library lib) {
        while (true) {
            System.out.println("\n--- User Menu ---");
            System.out.println("1. Search by Title");
            System.out.println("2. Display All Books");
            System.out.println("3. Issue Book");
            System.out.println("4. Return Book");
            System.out.println("5. Back");
            System.out.print("Choose: ");
            int ch = sc.nextInt(); sc.nextLine();
            if (ch == 1) {
                System.out.print("Keyword: ");
                String k = sc.nextLine();
                var res = lib.searchByTitle(k);
                for (Book b : res) System.out.println(b);
            } else if (ch == 2) {
                lib.displayAllBooks();
            } else if (ch == 3) {
                System.out.print("Member Id: ");
                String mid = sc.nextLine();
                System.out.print("ISBN: ");
                String isbn = sc.nextLine();
                lib.issueBook(mid, isbn);
            } else if (ch == 4) {
                System.out.print("Member Id: ");
                String mid = sc.nextLine();
                System.out.print("ISBN: ");
                String isbn = sc.nextLine();
                System.out.print("Days kept: ");
                int days = sc.nextInt(); sc.nextLine();
                lib.returnBook(mid, isbn, days);
            } else break;
        }
    }

    static Library loadLibrary() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            return (Library) ois.readObject();
        } catch (Exception e) {
            return new Library();
        }
    }

    static void saveLibrary(Library lib) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(lib);
        } catch (Exception e) {
            System.out.println("Failed to save data: " + e.getMessage());
        }
    }
}
