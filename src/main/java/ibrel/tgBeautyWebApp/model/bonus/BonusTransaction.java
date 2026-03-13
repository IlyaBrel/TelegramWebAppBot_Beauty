package ibrel.tgBeautyWebApp.model.bonus;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bonus_transactions", indexes = {
        @Index(name = "idx_btx_account_id", columnList = "bonus_account_id"),
        @Index(name = "idx_btx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BonusTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bonus_account_id", nullable = false)
    private BonusAccount bonusAccount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionSource source;

    /** ID записи (Appointment), если применимо */
    @Column(name = "order_id")
    private Long orderId;

    @Column(length = 500)
    private String comment;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum TransactionType {
        CREDIT, DEBIT
    }

    public enum TransactionSource {
        TASK_REWARD,
        REFERRAL,
        CASHBACK,
        ADMIN_TOPUP,
        MASTER_GIFT,
        PROMO_CODE,
        ORDER_PAYMENT,
        POINTS_CONVERSION
    }
}
