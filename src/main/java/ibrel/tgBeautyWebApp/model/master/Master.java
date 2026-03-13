package ibrel.tgBeautyWebApp.model.master;

import ibrel.tgBeautyWebApp.model.booking.Appointment;
import ibrel.tgBeautyWebApp.model.master.service.MasterServiceWork;
import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Профиль мастера.
 *
 * Один пользователь (telegramId) может иметь НЕСКОЛЬКО профилей мастера —
 * например, «Маникюр в Москве» и «Брови в Питере».
 * Уникальность убрана с telegram_id; уникальность пары (telegramId, profileName)
 * обеспечивается на уровне сервиса.
 */
@Entity
@Table(name = "masters", indexes = {
        @Index(name = "idx_master_telegram_id", columnList = "telegram_id"),
        @Index(name = "idx_master_city_id",     columnList = "city_id"),
        @Index(name = "idx_master_active",       columnList = "active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Master {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * telegramId пользователя — больше НЕ уникален,
     * один пользователь может иметь несколько профилей мастера.
     */
    @Column(name = "telegram_id", nullable = false)
    private Long telegramId;

    /**
     * Название профиля мастера — помогает различать несколько аккаунтов.
     * Например: «Маникюр», «Брови и ресницы», «Стрижки».
     * Уникально в рамках одного telegramId.
     */
    @Column(name = "profile_name", length = 200)
    private String profileName;

    @Builder.Default
    private Boolean active = false;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    // ── Город ────────────────────────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    // ── Специализации ────────────────────────────────────────────────────────
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "master_specialties",
            joinColumns        = @JoinColumn(name = "master_id"),
            inverseJoinColumns = @JoinColumn(name = "specialty_id")
    )
    @JsonIgnore
    private List<Specialty> specialties;

    // ── Удобства (только admin назначает) ────────────────────────────────────
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "master_amenities",
            joinColumns        = @JoinColumn(name = "master_id"),
            inverseJoinColumns = @JoinColumn(name = "amenity_id")
    )
    @JsonIgnore
    private List<Amenity> amenities;

    @Column(name = "boosted_rating")
    @Builder.Default
    private Double boostedRating = 0.0;

    @Column(name = "cached_avg_rating")
    @Builder.Default
    private Double cachedAvgRating = 0.0;

    @Column(name = "review_count")
    @Builder.Default
    private Integer reviewCount = 0;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // ── Связи ─────────────────────────────────────────────────────────────────

    @OneToOne(mappedBy = "master", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private MasterPersonalData personalData;

    @OneToOne(mappedBy = "master", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private MasterAddress address;

    @OneToMany(mappedBy = "master", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<MasterServiceWork> services;

    @OneToMany(mappedBy = "master", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<WorkSlot> slots;

    @OneToMany(mappedBy = "master", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<MasterReview> reviews;

    @OneToMany(mappedBy = "master", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<MasterWorkExample> works;

    @OneToMany(mappedBy = "master")
    @JsonIgnore
    private List<Appointment> appointments;

    // ── Helper методы ─────────────────────────────────────────────────────────

    public void setPersonalDataBidirectional(MasterPersonalData pd) {
        this.personalData = pd;
        if (pd != null) pd.setMaster(this);
    }

    public void setAddressBidirectional(MasterAddress addr) {
        this.address = addr;
        if (addr != null) addr.setMaster(this);
    }

    public void recalculateRating(double newAvg, int newCount) {
        this.reviewCount     = newCount;
        this.cachedAvgRating = newCount > 0 ? newAvg : boostedRating;
    }
}