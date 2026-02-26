package ibrel.tgBeautyWebApp.service.master;

import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.master.service.FixedServiceDetails;
import ibrel.tgBeautyWebApp.model.master.service.MasterServiceWork;
import ibrel.tgBeautyWebApp.model.master.service.VariableServiceDetails;

import java.util.List;

/**
 * Операции CRUD и вспомогательные операции для услуг мастера.
 * Методы валидируют входные параметры и бросают понятные исключения при ошибках.
 */
public interface MasterServiceWorkService {

    /**
     * Создать услугу для мастера.
     *
     * @param masterId id мастера; не может быть null; мастер должен существовать.
     * @param service  сущность услуги; не может быть null; если тип FIXED — должен содержать fixedDetails.
     * @return сохранённая сущность {@link MasterServiceWork} с заполненным id.
     * @throws IllegalArgumentException если входные данные некорректны.
     * @throws EntityNotFoundException  если мастер не найден.
     */
    MasterServiceWork create(Long masterId, MasterServiceWork service);

    /**
     * Обновить существующую услугу (частичное обновление).
     *
     * @param serviceId id услуги; не может быть null.
     * @param service   объект с новыми значениями; null‑поля игнорируются.
     * @return обновлённая сущность {@link MasterServiceWork}.
     * @throws EntityNotFoundException  если услуга не найдена.
     * @throws IllegalArgumentException если переданы некорректные значения.
     */
    MasterServiceWork update(Long serviceId, MasterServiceWork service);

    /**
     * Получить услугу по id.
     *
     * @param serviceId id услуги; не может быть null.
     * @return найденная сущность {@link MasterServiceWork}.
     * @throws EntityNotFoundException если услуга не найдена.
     */
    MasterServiceWork getById(Long serviceId);

    /**
     * Получить все услуги мастера.
     *
     * @param masterId id мастера; не может быть null.
     * @return список услуг; пустой список если услуг нет.
     * @throws EntityNotFoundException если мастер не найден.
     */
    List<MasterServiceWork> getByMaster(Long masterId);

    /**
     * Удалить услугу.
     *
     * @param serviceId id услуги; не может быть null.
     * @throws EntityNotFoundException если услуга не найдена.
     * @throws IllegalStateException   если услуга используется в существующих записях.
     */
    void delete(Long serviceId);

    /**
     * Добавить или обновить fixedDetails для услуги.
     *
     * @param serviceId    id услуги; не может быть null.
     * @param fixedDetails объект fixedDetails; не может быть null; price >= 0; durationMinutes > 0.
     * @return сохранённый {@link FixedServiceDetails}.
     * @throws EntityNotFoundException  если услуга не найдена.
     * @throws IllegalArgumentException если fixedDetails некорректны.
     */
    FixedServiceDetails addOrUpdateFixedDetails(Long serviceId, FixedServiceDetails fixedDetails);

    /**
     * Удалить fixedDetails у услуги.
     *
     * @param serviceId id услуги; не может быть null.
     * @throws EntityNotFoundException если услуга не найдена.
     * @throws IllegalStateException   если услуга используется в записях.
     */
    void removeFixedDetails(Long serviceId);

    /**
     * Добавить переменную деталь (factor) к VARIABLE услуге.
     *
     * @param serviceId      id услуги; не может быть null.
     * @param variableDetail объект VariableServiceDetails; не может быть null.
     * @return сохранённый {@link VariableServiceDetails}.
     * @throws EntityNotFoundException если услуга не найдена.
     */
    VariableServiceDetails addVariableDetail(Long serviceId, VariableServiceDetails variableDetail);

    /**
     * Обновить переменную деталь.
     *
     * @param variableDetailId id детали; не может быть null.
     * @param variableDetail   объект с новыми значениями; null‑поля игнорируются.
     * @return обновлённая {@link VariableServiceDetails}.
     * @throws EntityNotFoundException  если деталь не найдена.
     * @throws IllegalArgumentException если значения некорректны.
     */
    VariableServiceDetails updateVariableDetail(Long variableDetailId, VariableServiceDetails variableDetail);

    /**
     * Удалить переменную деталь.
     *
     * @param variableDetailId id детали; не может быть null.
     * @throws EntityNotFoundException если деталь не найдена.
     * @throws IllegalStateException   если услуга/деталь используется в записях.
     */
    void removeVariableDetail(Long variableDetailId);
}
