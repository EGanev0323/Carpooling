package bg.tu_sofia.carpooling.rides.domain;

import bg.tu_sofia.carpooling.auth.domain.User;
import bg.tu_sofia.carpooling.cars.domain.Car;
import bg.tu_sofia.carpooling.geo.domain.City;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "rides")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, updatable = false,
            columnDefinition = "UUID NOT NULL DEFAULT gen_random_uuid()")
    @Builder.Default
    private UUID publicId = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private User driver;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "origin_city_id", nullable = false)
    private City originCity;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "destination_city_id", nullable = false)
    private City destinationCity;

    @Column(name = "departure_at", nullable = false)
    private OffsetDateTime departureAt;

    @Column(name = "arrival_at_estimate")
    private OffsetDateTime arrivalAtEstimate;

    @Column(name = "total_seats", nullable = false)
    private Short totalSeats;

    @Column(name = "price_per_seat", nullable = false, precision = 8, scale = 2)
    private BigDecimal pricePerSeat;

    @Column(name = "route_polyline", columnDefinition = "TEXT")
    private String routePolyline;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "status", nullable = false, length = 16)
    @Builder.Default
    private String status = "ACTIVE";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "ride", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RideStop> stops = new ArrayList<>();
}
