package com.library.config;

import com.library.model.*;
import com.library.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;
    private final BookRepository bookRepository;
    private final SettingRepository settingRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, CategoryRepository categoryRepository,
                      AuthorRepository authorRepository, PublisherRepository publisherRepository,
                      BookRepository bookRepository, SettingRepository settingRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.authorRepository = authorRepository;
        this.publisherRepository = publisherRepository;
        this.bookRepository = bookRepository;
        this.settingRepository = settingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedSettings();
        if (userRepository.count() == 0) {
            seedUsers();
        }
        seedCatalog();
    }

    private void seedSettings() {
        setSetting("libraryName", "Community Library");
        setSetting("loanPeriodDays", "14");
        setSetting("maxBooksPerMember", "5");
        setSetting("finePerDay", "1.0");
        setSetting("contactEmail", "library@example.com");
        setSetting("contactPhone", "(555) 123-4567");
        setSetting("address", "123 Main Street, Springfield");
    }

    private void setSetting(String key, String value) {
        if (settingRepository.findByKey(key).isEmpty()) {
            Setting s = new Setting();
            s.setKey(key);
            s.setValue(value);
            settingRepository.save(s);
        }
    }

    private void seedUsers() {
        createUser("admin", "admin@library.com", "System Administrator", Role.ADMIN, "admin123", "MGT-001");
        createUser("librarian", "librarian@library.com", "Jane Librarian", Role.LIBRARIAN, "lib123", "LIB-001");
        createUser("alice", "alice@example.com", "Alice Johnson", Role.MEMBER, "alice123", "M-1001");
        createUser("bob", "bob@example.com", "Bob Smith", Role.MEMBER, "bob123", "M-1002");
        createUser("carol", "carol@example.com", "Carol Davis", Role.MEMBER, "carol123", "M-1003");
    }

    private void createUser(String username, String email, String fullName, Role role,
                            String rawPassword, String membershipNo) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setFullName(fullName);
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.setRole(role);
        u.setMembershipNo(membershipNo);
        u.setMembershipStatus(MembershipStatus.ACTIVE);
        u.setJoinedDate(LocalDate.now().minusMonths(3));
        userRepository.save(u);
    }

    private void seedCatalog() {
        for (String cat : new String[]{"Motivational", "Science", "Fiction", "Biography", "History", "Business", "Programming"}) {
            category(cat, "Books in the " + cat + " collection");
        }

        String[][] books = {
                // ---- Motivational (17) ----
                {"How to Win Friends and Influence People", "9780671027032", "Motivational", "Dale Carnegie", "Simon & Schuster", "1936"},
                {"Think and Grow Rich", "9781585424337", "Motivational", "Napoleon Hill", "TarcherPerigee", "1937"},
                {"The Power of Positive Thinking", "9780743234801", "Motivational", "Norman Vincent Peale", "Touchstone", "1952"},
                {"Atomic Habits", "9780735211292", "Motivational", "James Clear", "Avery", "2018"},
                {"The 7 Habits of Highly Effective People", "9780743269513", "Motivational", "Stephen Covey", "Free Press", "1989"},
                {"Awaken the Giant Within", "9780743409384", "Motivational", "Tony Robbins", "Free Press", "1991"},
                {"The Secret", "9781582701707", "Motivational", "Rhonda Byrne", "Atria Books", "2006"},
                {"Mindset: The New Psychology of Success", "9780345472328", "Motivational", "Carol Dweck", "Ballantine Books", "2006"},
                {"The Power of Now", "9781577314806", "Motivational", "Eckhart Tolle", "New World Library", "1997"},
                {"You Are a Badass", "9780762447695", "Motivational", "Jen Sincero", "Running Press", "2013"},
                {"Grit: The Power of Passion and Perseverance", "9781501111105", "Motivational", "Angela Duckworth", "Scribner", "2016"},
                {"The Subtle Art of Not Giving a F*ck", "9780062457714", "Motivational", "Mark Manson", "HarperOne", "2016"},
                {"Can't Hurt Me", "9781544507866", "Motivational", "David Goggins", "Lioncrest Publishing", "2018"},
                {"The Four Agreements", "9781878424310", "Motivational", "Don Miguel Ruiz", "Amber-Allen Publishing", "1997"},
                {"Meditations", "9780140449334", "Motivational", "Marcus Aurelius", "Penguin Classics", "180"},
                {"Drive: The Surprising Truth About What Motivates Us", "9781594488849", "Motivational", "Daniel Pink", "Riverhead Books", "2009"},
                {"The Alchemist", "9780062315007", "Motivational", "Paulo Coelho", "HarperOne", "1988"},

                // ---- Science (17) ----
                {"A Brief History of Time", "9780553380163", "Science", "Stephen Hawking", "Bantam", "1988"},
                {"Sapiens: A Brief History of Humankind", "9780062316110", "Science", "Yuval Noah Harari", "Harper", "2011"},
                {"Cosmos", "9780345539435", "Science", "Carl Sagan", "Ballantine Books", "1980"},
                {"The Selfish Gene", "9780198788607", "Science", "Richard Dawkins", "Oxford University Press", "1976"},
                {"The Gene: An Intimate History", "9781476733500", "Science", "Siddhartha Mukherjee", "Scribner", "2016"},
                {"Astrophysics for People in a Hurry", "9780393609394", "Science", "Neil deGrasse Tyson", "W.W. Norton", "2017"},
                {"The Origin of Species", "9780140439120", "Science", "Charles Darwin", "Penguin Classics", "1859"},
                {"A Short History of Nearly Everything", "9780767908184", "Science", "Bill Bryson", "Broadway Books", "2003"},
                {"The Elegant Universe", "9780375708114", "Science", "Brian Greene", "Vintage", "1999"},
                {"The Code Breaker", "9781982115852", "Science", "Walter Isaacson", "Simon & Schuster", "2021"},
                {"Silent Spring", "9780618249060", "Science", "Rachel Carson", "Mariner Books", "1962"},
                {"The Double Helix", "9780743216302", "Science", "James Watson", "Touchstone", "1968"},
                {"Godel, Escher, Bach", "9780465026562", "Science", "Douglas Hofstadter", "Basic Books", "1979"},
                {"The Demon-Haunted World", "9780345409461", "Science", "Carl Sagan", "Ballantine Books", "1995"},
                {"Life 3.0", "9781101946596", "Science", "Max Tegmark", "Knopf", "2017"},
                {"The Fabric of the Cosmos", "9780375727207", "Science", "Brian Greene", "Vintage", "2004"},
                {"The Immortal Life of Henrietta Lacks", "9781400052189", "Science", "Rebecca Skloot", "Crown", "2010"},

                // ---- Fiction (17) ----
                {"1984", "9780451524935", "Fiction", "George Orwell", "Signet Classic", "1949"},
                {"The Hobbit", "9780547928227", "Fiction", "J.R.R. Tolkien", "Houghton Mifflin", "1937"},
                {"The Lord of the Rings", "9780618640157", "Fiction", "J.R.R. Tolkien", "Houghton Mifflin", "1954"},
                {"To Kill a Mockingbird", "9780061120084", "Fiction", "Harper Lee", "Harper Perennial", "1960"},
                {"Pride and Prejudice", "9780141439518", "Fiction", "Jane Austen", "Penguin Classics", "1813"},
                {"The Great Gatsby", "9780743273565", "Fiction", "F. Scott Fitzgerald", "Scribner", "1925"},
                {"Crime and Punishment", "9780143058144", "Fiction", "Fyodor Dostoevsky", "Penguin Classics", "1866"},
                {"One Hundred Years of Solitude", "9780060883287", "Fiction", "Gabriel Garcia Marquez", "Harper Perennial", "1967"},
                {"The Catcher in the Rye", "9780316769488", "Fiction", "J.D. Salinger", "Little, Brown", "1951"},
                {"Brave New World", "9780060850524", "Fiction", "Aldous Huxley", "Harper Perennial", "1932"},
                {"Frankenstein", "9780486282114", "Fiction", "Mary Shelley", "Dover Publications", "1818"},
                {"The Old Man and the Sea", "9780684801223", "Fiction", "Ernest Hemingway", "Scribner", "1952"},
                {"War and Peace", "9781400079988", "Fiction", "Leo Tolstoy", "Vintage", "1869"},
                {"The Handmaid's Tale", "9780385490818", "Fiction", "Margaret Atwood", "Anchor", "1985"},
                {"Beloved", "9781400033416", "Fiction", "Toni Morrison", "Vintage", "1987"},
                {"The Kite Runner", "9781594631931", "Fiction", "Khaled Hosseini", "Riverhead Books", "2003"},
                {"Life of Pi", "9780156027328", "Fiction", "Yann Martel", "Mariner Books", "2001"},

                // ---- Biography (17) ----
                {"The Diary of a Young Girl", "9780553577129", "Biography", "Anne Frank", "Bantam", "1947"},
                {"Steve Jobs", "9781451648539", "Biography", "Walter Isaacson", "Simon & Schuster", "2011"},
                {"The Story of My Life", "9780486292496", "Biography", "Helen Keller", "Dover Publications", "1903"},
                {"Long Walk to Freedom", "9780316548182", "Biography", "Nelson Mandela", "Little, Brown", "1994"},
                {"Einstein: His Life and Universe", "9780743264747", "Biography", "Walter Isaacson", "Simon & Schuster", "2007"},
                {"Becoming", "9781524763138", "Biography", "Michelle Obama", "Crown", "2018"},
                {"Leonardo da Vinci", "9781501139154", "Biography", "Walter Isaacson", "Simon & Schuster", "2017"},
                {"Alexander Hamilton", "9780143034759", "Biography", "Ron Chernow", "Penguin Books", "2004"},
                {"The Autobiography of Malcolm X", "9780345350688", "Biography", "Malcolm X", "Ballantine Books", "1965"},
                {"Benjamin Franklin: An American Life", "9780743258074", "Biography", "Walter Isaacson", "Simon & Schuster", "2003"},
                {"I Am Malala", "9780316322409", "Biography", "Malala Yousafzai", "Little, Brown", "2013"},
                {"Into the Wild", "9780385486804", "Biography", "Jon Krakauer", "Anchor", "1996"},
                {"Unbroken", "9780812974492", "Biography", "Laura Hillenbrand", "Random House", "2010"},
                {"Churchill: A Life", "9780141981253", "Biography", "Andrew Roberts", "Penguin Books", "2018"},
                {"Frida: A Biography of Frida Kahlo", "9780060885892", "Biography", "Hayden Herrera", "Harper Perennial", "1983"},
                {"Born to Run", "9781501141515", "Biography", "Bruce Springsteen", "Simon & Schuster", "2016"},
                {"Educated", "9780399590504", "Biography", "Tara Westover", "Random House", "2018"},

                // ---- History (16) ----
                {"A People's History of the United States", "9780060838652", "History", "Howard Zinn", "Harper Perennial", "1980"},
                {"Guns, Germs, and Steel", "9780393354324", "History", "Jared Diamond", "W.W. Norton", "1997"},
                {"The Rise and Fall of the Third Reich", "9781451651683", "History", "William Shirer", "Simon & Schuster", "1960"},
                {"1776", "9780743226721", "History", "David McCullough", "Simon & Schuster", "2005"},
                {"SPQR: A History of Ancient Rome", "9781631492228", "History", "Mary Beard", "Liveright", "2015"},
                {"The Silk Roads", "9781101912379", "History", "Peter Frankopan", "Vintage", "2015"},
                {"Team of Rivals", "9780743270755", "History", "Doris Kearns Goodwin", "Simon & Schuster", "2005"},
                {"Collapse: How Societies Choose to Fail or Succeed", "9780143036555", "History", "Jared Diamond", "Penguin Books", "2005"},
                {"The Wright Brothers", "9781476728759", "History", "David McCullough", "Simon & Schuster", "2015"},
                {"Genghis Khan and the Making of the Modern World", "9780609809648", "History", "Jack Weatherford", "Crown", "2004"},
                {"The History of the Ancient World", "9780393059748", "History", "Susan Wise Bauer", "W.W. Norton", "2007"},
                {"Why Nations Fail", "9780307719225", "History", "Daron Acemoglu", "Crown", "2012"},
                {"The Cold War: A New History", "9780143038277", "History", "John Lewis Gaddis", "Penguin Books", "2005"},
                {"Salt: A World History", "9780142001615", "History", "Mark Kurlansky", "Penguin Books", "2002"},
                {"A Short History of Byzantium", "9780679772699", "History", "John Julius Norwich", "Vintage", "1997"},
                {"The Peloponnesian War", "9780142004371", "History", "Donald Kagan", "Penguin Books", "2003"},

                // ---- Business (16) ----
                {"Good to Great", "9780066620992", "Business", "Jim Collins", "HarperBusiness", "2001"},
                {"The Lean Startup", "9780307887894", "Business", "Eric Ries", "Crown Business", "2011"},
                {"Zero to One", "9780804139298", "Business", "Peter Thiel", "Crown Business", "2014"},
                {"The Intelligent Investor", "9780060555665", "Business", "Benjamin Graham", "HarperBusiness", "1949"},
                {"Rich Dad Poor Dad", "9781612680194", "Business", "Robert Kiyosaki", "Plata Publishing", "1997"},
                {"The 4-Hour Workweek", "9780307465351", "Business", "Tim Ferriss", "Crown Business", "2007"},
                {"Thinking, Fast and Slow", "9780374533557", "Business", "Daniel Kahneman", "Farrar, Straus and Giroux", "2011"},
                {"The Hard Thing About Hard Things", "9780062273208", "Business", "Ben Horowitz", "HarperBusiness", "2014"},
                {"Crossing the Chasm", "9780062292988", "Business", "Geoffrey Moore", "HarperBusiness", "1991"},
                {"The E-Myth Revisited", "9780887307287", "Business", "Michael Gerber", "HarperBusiness", "1995"},
                {"Built to Last", "9780060566104", "Business", "Jim Collins", "HarperBusiness", "1994"},
                {"The Innovator's Dilemma", "9780062060242", "Business", "Clayton Christensen", "HarperBusiness", "1997"},
                {"Shoe Dog: A Memoir", "9781501135910", "Business", "Phil Knight", "Scribner", "2016"},
                {"Principles", "9781501124020", "Business", "Ray Dalio", "Simon & Schuster", "2017"},
                {"The Effective Executive", "9780060833459", "Business", "Peter Drucker", "HarperBusiness", "1966"},
                {"Traction", "9781937451868", "Business", "Gino Wickman", "BenBella Books", "2011"},

                // ---- Programming (2, merged from old database) ----
                {"Clean Code", "9780132350884", "Programming", "Robert C. Martin", "Prentice Hall", "2008"},
                {"Introduction to Algorithms", "9780262033848", "Programming", "Thomas H. Cormen", "MIT Press", "2009"},

                // ---- Fiction (1, merged from old database) ----
                {"The Night Before Christmas", "9780394835789", "Fiction", "Clement C. Moore", "Scholastic", "1823"},
        };

        int i = 0;
        for (String[] b : books) {
            addBook(b[0], b[1], b[2], b[3], b[4], Integer.parseInt(b[5]), 3 + (i % 4), shelfPrefix(b[2]) + "-" + String.format("%02d", i + 1));
            i++;
        }
    }

    private String shelfPrefix(String category) {
        return switch (category) {
            case "Motivational" -> "MO";
            case "Science" -> "SC";
            case "Fiction" -> "FI";
            case "Biography" -> "BI";
            case "History" -> "HI";
            case "Business" -> "BU";
            case "Programming" -> "PR";
            default -> "GE";
        };
    }

    private Category category(String name, String desc) {
        return categoryRepository.findByNameIgnoreCase(name).orElseGet(() -> {
            Category c = new Category();
            c.setName(name);
            c.setDescription(desc);
            return categoryRepository.save(c);
        });
    }

    private Author author(String name) {
        return authorRepository.findByNameIgnoreCase(name).orElseGet(() -> {
            Author a = new Author();
            a.setName(name);
            a.setBiography(name + " is a noted author.");
            a.setBirthYear(1900);
            return authorRepository.save(a);
        });
    }

    private Publisher publisher(String name) {
        return publisherRepository.findByNameIgnoreCase(name).orElseGet(() -> {
            Publisher p = new Publisher();
            p.setName(name);
            p.setAddress("New York, USA");
            return publisherRepository.save(p);
        });
    }

    private void addBook(String title, String isbn, String categoryName, String authorName,
                         String publisherName, int year, int copies, String shelf) {
        if (bookRepository.existsByIsbn(isbn) || bookRepository.existsByTitleIgnoreCase(title)) {
            return;
        }
        Book b = new Book();
        b.setTitle(title);
        b.setIsbn(isbn);
        b.setCategory(category(categoryName, "Books in the " + categoryName + " collection"));
        b.setAuthor(author(authorName));
        b.setPublisher(publisher(publisherName));
        b.setPublicationYear(year);
        b.setTotalCopies(copies);
        b.setAvailableCopies(copies);
        b.setShelfLocation(shelf);
        b.setDescription("A top " + categoryName + " book recommended by the library.");
        bookRepository.save(b);
    }
}
