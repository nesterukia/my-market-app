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
    private static final Double BASE_BALANCE = 1000.0;

    @Id
    private Long id;

    @Column("user_id")
    private Long userId;

    @Column("current_balance")
    @Builder.Default
    private Double currentBalance = BASE_BALANCE;
}
