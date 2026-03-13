package ibrel.tgBeautyWebApp.service.user.impl;

import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.FavoriteMaster;
import ibrel.tgBeautyWebApp.model.master.Master;
import ibrel.tgBeautyWebApp.repository.catalog.FavoriteMasterRepository;
import ibrel.tgBeautyWebApp.repository.master.MasterRepository;
import ibrel.tgBeautyWebApp.service.catalog.FavoriteMasterService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteMasterServiceImpl implements FavoriteMasterService {

    private final FavoriteMasterRepository favRepo;
    private final MasterRepository         masterRepository;

    @Override
    @Transactional
    public void add(Long userTelegramId, Long masterId) {
        if (favRepo.existsByUserTelegramIdAndMaster_Id(userTelegramId, masterId)) return; // идемпотентно

        Master master = masterRepository.findById(masterId)
                .orElseThrow(() -> new EntityNotFoundException("Master not found id=" + masterId));

        favRepo.save(FavoriteMaster.builder()
                .userTelegramId(userTelegramId)
                .master(master)
                .createdAt(OffsetDateTime.now())
                .build());

        log.info("User {} added master {} to favorites", userTelegramId, masterId);
    }

    @Override
    @Transactional
    public void remove(Long userTelegramId, Long masterId) {
        favRepo.deleteByUserTelegramIdAndMaster_Id(userTelegramId, masterId);
        log.info("User {} removed master {} from favorites", userTelegramId, masterId);
    }

    @Override
    public boolean isFavorite(Long userTelegramId, Long masterId) {
        return favRepo.existsByUserTelegramIdAndMaster_Id(userTelegramId, masterId);
    }

    @Override
    public List<Master> getFavorites(Long userTelegramId) {
        return masterRepository.findFavoritesByUserTelegramId(userTelegramId);
    }
}