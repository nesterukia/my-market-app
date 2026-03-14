package com.github.nesterukia.payment_service.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "accounts")
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class Account {
    @Id
    private Long id;

    @Column("user_id")
    private String userId;

    @Column("current_balance")
    private Double currentBalance;
}
