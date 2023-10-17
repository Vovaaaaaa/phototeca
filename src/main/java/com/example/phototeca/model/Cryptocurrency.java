package com.example.phototeca.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "cryptocurrency")

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Cryptocurrency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "symbol")
    private String symbol;

    @Column(name = "price")
    private long price;


}
