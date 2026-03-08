package com.github.nesterukia.mymarket.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "orders")
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    private Long id;

    @Column("user_id")
    private String userId;
}
