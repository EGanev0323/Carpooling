package bg.tu_sofia.carpooling.cars.domain;

import bg.tu_sofia.carpooling.auth.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "cars")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "make", nullable = false, length = 64)
    private String make;

    @Column(name = "model", nullable = false, length = 64)
    private String model;

    @Column(name = "year", nullable = false)
    private Short year;

    @Column(name = "color", length = 32)
    private String color;

    @Column(name = "license_plate", nullable = false, length = 20)
    private String licensePlate;

    @Column(name = "seats", nullable = false)
    private Short seats;

    @Column(name = "amenities", columnDefinition = "JSONB")
    @Builder.Default
    private String amenities = "{}";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
