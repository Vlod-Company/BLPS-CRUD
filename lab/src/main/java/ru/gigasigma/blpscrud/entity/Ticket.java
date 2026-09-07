package ru.gigasigma.blpscrud.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.gigasigma.blpscrud.enums.SeatClass;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "seat_number", nullable = false)
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_class", nullable = false)
    private SeatClass seatClass;

    @Column(name = "has_baggage", nullable = false)
    private Boolean hasBaggage;

    @Column(name = "passenger_name", nullable = false)
    private String passengerName;

    @Column(name = "passenger_passport", nullable = false)
    private String passengerPassport;
}
