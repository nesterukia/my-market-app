package com.github.nesterukia.payment_service.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table(name = "transactions")
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class Transaction implements Persistable<UUID> {
    @Id
    private UUID id;

    @Column("user_id")
    private Long userId;

    @Column("account_id")
    private Long accountId;

    @Column("status")
    private Status status;

    @Override
    public boolean isNew() {
        return true;
    }
}
