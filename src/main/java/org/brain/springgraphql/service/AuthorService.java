package org.brain.springgraphql.service;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.brain.springgraphql.JsonUtils;
import org.brain.springgraphql.model.Author;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class AuthorService {

    private final List<Author>  authors;

    public AuthorService() {
        this.authors = JsonUtils.loadListFromJsonFile("/authors.json", Author[].class);

    }

    // /api/authors
    public List<Author> getAuthors() {
        log.info("REST API: get all authors");
        return authors;
    }


}
