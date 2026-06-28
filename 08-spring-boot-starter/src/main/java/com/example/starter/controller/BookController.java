package com.example.starter.controller;

import com.example.starter.model.Book;
import com.example.starter.repository.BookRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookRepository repository;

    public BookController(BookRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Book> findAll() { return repository.findAll(); }

    @PostMapping
    public Book create(@RequestBody Book book) { return repository.save(book); }
}
