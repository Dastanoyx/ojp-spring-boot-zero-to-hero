package com.example.hello.repository;

import com.example.hello.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

// Plain Spring Data JPA — nothing here knows or cares that OJP is in the path.
// That's the point: your data access code is unchanged.
public interface BookRepository extends JpaRepository<Book, Long> {
}
