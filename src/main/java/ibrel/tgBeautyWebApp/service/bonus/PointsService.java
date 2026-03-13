package ibrel.tgBeautyWebApp.service.bonus;

import ibrel.tgBeautyWebApp.model.points.PointTransaction;

import java.util.List;

public interface PointsService {

    /** Начислить баллы пользователю */
    PointTransaction grant(Long telegramId, int amount,
                           PointTransaction.PointReason reason, String taskType);

    List<PointTransaction> getHistory(Long telegramId);
}
