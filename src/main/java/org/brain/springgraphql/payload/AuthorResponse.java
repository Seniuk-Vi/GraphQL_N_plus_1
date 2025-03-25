package org.brain.springgraphql.payload;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthorResponse {
    private String id;
    private String name;
    private List<BookResponse> books;
    private BookResponse primaryBook;
}
