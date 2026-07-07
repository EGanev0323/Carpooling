package bg.tu_sofia.carpooling.geo.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "cities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "slug", nullable = false, unique = true, length = 64)
    private String slug;

    @Column(name = "name_bg", nullable = false, length = 128)
    private String nameBg;

    @Column(name = "name_en", nullable = false, length = 128)
    private String nameEn;

    @Column(name = "region_bg", length = 128)
    private String regionBg;

    @Column(name = "latitude", precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
