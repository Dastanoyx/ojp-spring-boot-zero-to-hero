package com.example.compare.controller;

import com.example.compare.model.Book;
import com.example.compare.repository.BookRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookRepository repository;
    private final DataSource dataSource;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    public BookController(BookRepository repository, DataSource dataSource) {
        this.repository = repository;
        this.dataSource = dataSource;
    }

    // Reveals which datasource implementation is actually wired in — proof of
    // which mode (direct JDBC pool vs OJP) is active at runtime.
    @GetMapping("/whoami")
    public Map<String, String> whoami() {
        return Map.of(
                "activeProfile", activeProfile,
                "dataSourceClass", dataSource.getClass().getName()
        );
    }

    @GetMapping
    public List<Book> findAll() { return repository.findAll(); }

    @PostMapping
    public Book create(@RequestBody Book book) { return repository.save(book); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { repository.deleteById(id); }
}
