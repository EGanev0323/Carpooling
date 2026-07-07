package bg.tu_sofia.carpooling.bookings.domain;

import bg.tu_sofia.carpooling.auth.domain.User;
import bg.tu_sofia.carpooling.rides.domain.Ride;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, updatable = false,
            columnDefinition = "UUID NOT NULL DEFAULT gen_random_uuid()")
    @Builder.Default
    private UUID publicId = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ride_id", nullable = false)
    private Ride ride;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "passenger_id", nullable = false)
    private User passenger;

    @Column(name = "seats", nullable = false)
    @Builder.Default
    private Short seats = 1;

    @Column(name = "status", nullable = false, length = 16)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "message", length = 500)
    private String message;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
