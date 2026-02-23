package com.github.nesterukia.payment_service.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table(name = "accounts")
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Builder.Default
    @Id
    private UUID id = UUID.randomUUID();

    @Column("user_id")
    private Long userId;

    @Column("account_id")
    private Long accountId;

    @Column("status")
    private Status status;
}
