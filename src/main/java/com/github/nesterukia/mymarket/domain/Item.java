package com.github.nesterukia.mymarket.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "items")
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    private String title;
    private String description;
    @Column(name = "img_path")
    private String imgPath;
    private Long price;

    @OneToMany(mappedBy = "item")
    private List<CartItem> cartItems = new ArrayList<>();
    @OneToMany(mappedBy = "item")
    private List<OrderItem> orderItems = new ArrayList<>();
}
