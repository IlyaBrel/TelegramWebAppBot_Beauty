package ibrel.tgBeautyWebApp.service.master;

import ibrel.tgBeautyWebApp.model.master.MasterReview;

import java.util.List;

public interface MasterReviewService {
    MasterReview addReview(Long masterId, MasterReview review);
    MasterReview getById(Long reviewId);
    List<MasterReview> getByMaster(Long masterId);
    void delete(Long reviewId);
    double getAverageRating(Long masterId);
}
