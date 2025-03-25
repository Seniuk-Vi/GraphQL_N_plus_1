package org.brain.springgraphql.controller;


import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

import org.brain.springgraphql.model.Book;
import org.brain.springgraphql.payload.AuthorResponse;
import org.brain.springgraphql.payload.BookResponse;
import org.brain.springgraphql.service.AuthorService;
import org.brain.springgraphql.service.BooksService;
import org.dataloader.DataLoader;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.graphql.execution.BatchLoaderRegistry;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;


@Controller
@Slf4j
public class GraphQLController {

    private final AuthorService authorService;
    private final BooksService booksService;
    private final BatchLoaderRegistry batchLoaderRegistry;


    public GraphQLController(AuthorService authorService, BooksService booksService, BatchLoaderRegistry batchLoaderRegistry) {
        this.authorService = authorService;
        this.booksService = booksService;
        this.batchLoaderRegistry = batchLoaderRegistry;

        batchLoaderRegistry.forTypePair(AuthorResponse.class, List.class).registerBatchLoader(
                (author, environment ) -> books(author));

        batchLoaderRegistry.forTypePair(AuthorResponse.class, BookResponse.class).registerBatchLoader(
                (authors, environment) -> primaryBooks(authors));
    }

    @QueryMapping
    public List<AuthorResponse> authors() {
        return authorService.getAuthors().stream()
                .map(author -> new AuthorResponse(
                        author.getId(),
                        author.getName(),
                        booksService.getBooksByAuthorId(author.getId()).stream()
                                .map(book -> new BookResponse(
                                        book.getId(),
                                        book.getTitle())
                                )
                                .toList(),
                        Optional.ofNullable(booksService.getPrimaryBookForAuthor(author.getId()))
                                .map(book -> new BookResponse(
                                        book.getId(),
                                        book.getTitle()
                                ))
                                .orElse(null)
                        )
                )
                .toList();
    }

    @QueryMapping
    public List<AuthorResponse> authorsWithDataLoader() {
        return authorService.getAuthors().stream()
                .map(author -> new AuthorResponse(
                        author.getId(),
                        author.getName(),
                        null,
                        null)
                )
                .toList();
    }

    @SchemaMapping
    public CompletableFuture<List<BookResponse>> books (AuthorResponse authorResponse, DataLoader<AuthorResponse, List<BookResponse>> dataLoader) {
        if (authorResponse.getBooks() != null && !authorResponse.getBooks().isEmpty()) {
            return CompletableFuture.completedFuture(authorResponse.getBooks());
        }
        log.info("Fetching books for author with id=[{}]", authorResponse.getId());
        return dataLoader.load(authorResponse);
    }

    private Flux<List> books(List<AuthorResponse> authors) {
        List<String> authorIds = authors.stream().map(AuthorResponse::getId).toList();
        return Flux.fromIterable(getBooksViaBatchHTTPApi(authorIds));
    }

    private List<List<BookResponse>> getBooksViaBatchHTTPApi(List<String> authorIds) {
        return booksService.getBooksForAuthors(new LinkedList<>(authorIds))
                .stream()
                .collect(Collectors.groupingBy(Book::getAuthorId))
                .values()
                .stream()
                .map(apiBooks -> apiBooks.stream()
                        .map(apiBook -> new BookResponse(
                                apiBook.getId(),
                                apiBook.getTitle()
                        ))
                        .toList()
                )
                .toList();
    }

    @SchemaMapping
    public CompletableFuture<BookResponse> primaryBook(AuthorResponse authorResponse, DataLoader<AuthorResponse, BookResponse> dataLoader) {
        if (authorResponse.getPrimaryBook() != null) {
            return CompletableFuture.completedFuture(authorResponse.getPrimaryBook());
        }
        log.info("Fetching primary book for author with id=[{}]", authorResponse.getId());
        return dataLoader.load(authorResponse);
    }

    private Flux<BookResponse> primaryBooks(List<AuthorResponse> authors) {
        List<String> authorIds = authors.stream().map(AuthorResponse::getId).toList();
        return Flux.fromIterable(getPrimaryBooksViaBatchHTTPApi(authorIds));
    }

    private List<BookResponse> getPrimaryBooksViaBatchHTTPApi(List<String> authorIds) {
        return booksService.getPrimaryBookForAuthors(authorIds).stream()
                .map(book -> new BookResponse(
                        book.getId(),
                        book.getTitle()
                ))
                .toList();
    }
}