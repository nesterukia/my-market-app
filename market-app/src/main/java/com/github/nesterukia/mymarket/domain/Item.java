package com.github.nesterukia.mymarket.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "items")
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Item {
    @Id
    private Long id;
    @Column("title")
    private String title;
    @Column("description")
    private String description;
    @Column("img_path")
    private String imgPath;
    @Column("price")
    private Long price;
}
