package ru.gigasigma.blpscrud.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "airlines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Airline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "iata_code", nullable = false)
    private String iataCode;

    @Column(nullable = false)
    private String country;

    @Column(name = "website_url")
    private String websiteUrl;

    @OneToMany(mappedBy = "airline", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Flight> flights = new ArrayList<>();
}
