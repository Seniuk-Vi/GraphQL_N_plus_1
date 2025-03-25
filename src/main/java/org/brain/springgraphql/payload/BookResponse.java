package org.brain.springgraphql.payload;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookResponse {
    private String id;
    private String title;
}
