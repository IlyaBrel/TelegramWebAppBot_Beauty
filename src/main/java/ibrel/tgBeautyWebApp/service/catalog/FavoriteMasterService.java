package ibrel.tgBeautyWebApp.service.catalog;

import ibrel.tgBeautyWebApp.model.master.Master;

import java.util.List;

public interface FavoriteMasterService {
    void add(Long userTelegramId, Long masterId);
    void remove(Long userTelegramId, Long masterId);
    boolean isFavorite(Long userTelegramId, Long masterId);
    List<Master> getFavorites(Long userTelegramId);
}