package ru.gigasigma.blpscrud.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
