package org.brain.springgraphql.service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.brain.springgraphql.JsonUtils;
import org.brain.springgraphql.model.Book;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class BooksService {

    private final List<Book>  books;

    public BooksService() {
        this.books = JsonUtils.loadListFromJsonFile("/books.json", Book[].class);

    }

    // get Books by authorId
    public List<Book> getBooksByAuthorId(String authorId) {
        log.info("REST API: get all books by authorId: {}", authorId);
        return books.stream().filter(book -> authorId.equals(book.getAuthorId())).toList();
    }

    // find only first book of authorId
    public List<Book> getPrimaryBookForAuthors(List<String> authorIds) {
        log.info("REST API: get primary books for multiple authorIds {}", authorIds);
        List<Book> allBooks = books.stream().filter(book -> authorIds.contains(book.getAuthorId())).toList();

        return allBooks.stream()
                .collect(Collectors.groupingBy(Book::getAuthorId))
                .values()
                .stream()
                .map(books -> books.stream().findFirst().orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }



    public Book getPrimaryBookForAuthor(String authorId) {
        log.info("REST API: get primary book for authorId {}", authorId);
        return books.stream().filter(book -> authorId.equals(book.getAuthorId())).findFirst().orElse(null);
    }

    // get Books for multiple authorIds
    public List<Book> getBooksForAuthors(List<String> authorIds) {
        log.info("REST API: get all books for multiple authorIds {}", authorIds);
        return books.stream().filter(book -> authorIds.contains(book.getAuthorId())).toList();
    }

}
