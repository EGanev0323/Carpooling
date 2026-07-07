package bg.tu_sofia.carpooling.rides.domain;

import bg.tu_sofia.carpooling.geo.domain.City;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "ride_stops")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ride_id", nullable = false)
    private Ride ride;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @Column(name = "stop_order", nullable = false)
    private Short stopOrder;

    @Column(name = "arrive_at")
    private OffsetDateTime arriveAt;
}
