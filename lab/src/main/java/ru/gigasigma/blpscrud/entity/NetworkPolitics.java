package ru.gigasigma.blpscrud.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "network_politics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NetworkPolitics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ElementCollection
    @CollectionTable(name = "network_politics_roles", joinColumns = @JoinColumn(name = "politics_id"))
    @Column(name = "role")
    @Builder.Default
    private List<String> roles = new ArrayList<>();

    @OneToMany(mappedBy = "politics", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<NetworkAddress> addresses = new ArrayList<>();

    @Entity
    @Table(name = "network_addresses")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NetworkAddress {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @NotBlank
        @Column(nullable = false)
        private String addr;

        @Column(columnDefinition = "TEXT")
        private String description;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "politics_id", nullable = false)
        private NetworkPolitics politics;
    }
}
