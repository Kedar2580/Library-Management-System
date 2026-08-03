package com.library.service;

import com.library.model.Author;
import com.library.model.Book;
import com.library.model.Category;
import com.library.model.Publisher;
import com.library.repository.AuthorRepository;
import com.library.repository.BookRepository;
import com.library.repository.CategoryRepository;
import com.library.repository.PublisherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;
    private final ActivityService activityService;

    public BookService(BookRepository bookRepository, CategoryRepository categoryRepository,
                       AuthorRepository authorRepository, PublisherRepository publisherRepository,
                       ActivityService activityService) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
        this.authorRepository = authorRepository;
        this.publisherRepository = publisherRepository;
        this.activityService = activityService;
    }

    // ----- Books -----

    public List<Book> allBooks() {
        return bookRepository.findAll();
    }

    public List<Book> search(String q) {
        String query = q == null ? "" : q;
        return bookRepository.search(query);
    }

    public Book getBook(Long id) {
        return bookRepository.findById(id).orElse(null);
    }

    public Optional<Book> findBook(Long id) {
        return bookRepository.findById(id);
    }

    @Transactional
    public Book saveBook(Book book) {
        if (book.getId() == null) {
            activityService.log("Added book: " + book.getTitle(), activityService.username());
        } else {
            activityService.log("Updated book: " + book.getTitle(), activityService.username());
        }
        return bookRepository.save(book);
    }

    public void deleteBook(Long id) {
        bookRepository.findById(id).ifPresent(b -> {
            activityService.log("Deleted book: " + b.getTitle(), activityService.username());
            bookRepository.delete(b);
        });
    }

    public boolean isbnExists(String isbn) {
        return isbn != null && !isbn.isBlank() && bookRepository.existsByIsbn(isbn);
    }

    // ----- Categories -----

    public List<Category> allCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategory(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    public Category saveCategory(Category category) {
        if (category.getId() == null) {
            activityService.log("Added category: " + category.getName(), activityService.username());
        } else {
            activityService.log("Updated category: " + category.getName(), activityService.username());
        }
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        categoryRepository.findById(id).ifPresent(c -> {
            activityService.log("Deleted category: " + c.getName(), activityService.username());
            categoryRepository.delete(c);
        });
    }

    public Category findOrCreateCategory(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return categoryRepository.findByNameIgnoreCase(name.trim())
                .orElseGet(() -> {
                    Category c = new Category();
                    c.setName(name.trim());
                    return categoryRepository.save(c);
                });
    }

    // ----- Authors -----

    public List<Author> allAuthors() {
        return authorRepository.findAll();
    }

    public Author getAuthor(Long id) {
        return authorRepository.findById(id).orElse(null);
    }

    public Author saveAuthor(Author author) {
        if (author.getId() == null) {
            activityService.log("Added author: " + author.getName(), activityService.username());
        } else {
            activityService.log("Updated author: " + author.getName(), activityService.username());
        }
        return authorRepository.save(author);
    }

    public void deleteAuthor(Long id) {
        authorRepository.findById(id).ifPresent(a -> {
            activityService.log("Deleted author: " + a.getName(), activityService.username());
            authorRepository.delete(a);
        });
    }

    public Author findOrCreateAuthor(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return authorRepository.findByNameIgnoreCase(name.trim())
                .orElseGet(() -> {
                    Author a = new Author();
                    a.setName(name.trim());
                    return authorRepository.save(a);
                });
    }

    // ----- Publishers -----

    public List<Publisher> allPublishers() {
        return publisherRepository.findAll();
    }

    public Publisher getPublisher(Long id) {
        return publisherRepository.findById(id).orElse(null);
    }

    public Publisher savePublisher(Publisher publisher) {
        if (publisher.getId() == null) {
            activityService.log("Added publisher: " + publisher.getName(), activityService.username());
        } else {
            activityService.log("Updated publisher: " + publisher.getName(), activityService.username());
        }
        return publisherRepository.save(publisher);
    }

    public void deletePublisher(Long id) {
        publisherRepository.findById(id).ifPresent(p -> {
            activityService.log("Deleted publisher: " + p.getName(), activityService.username());
            publisherRepository.delete(p);
        });
    }

    public Publisher findOrCreatePublisher(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return publisherRepository.findByNameIgnoreCase(name.trim())
                .orElseGet(() -> {
                    Publisher p = new Publisher();
                    p.setName(name.trim());
                    return publisherRepository.save(p);
                });
    }

    // ----- Search module -----

    public List<Book> searchByTitleOrIsbn(String q) {
        return bookRepository.searchByTitleOrIsbn(q);
    }

    public List<Book> searchByAuthor(String q) {
        return bookRepository.searchByAuthor(q);
    }

    public List<Book> searchByCategory(String q) {
        return bookRepository.searchByCategory(q);
    }
}
