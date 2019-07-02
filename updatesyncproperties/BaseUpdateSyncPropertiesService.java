package com.kenshoo.koala.updatesyncproperties;

import com.kenshoo.koala.api.representation.EntityRepresentation;
import com.kenshoo.koala.api.representation.UpdateSyncPropertiesResult;
import com.kenshoo.koala.common.dataaccess.api.entities.SyncProcess;
import com.kenshoo.koala.common.metrics.Metrics;
import com.kenshoo.koala.common.utils.UserDataMarker;
import com.kenshoo.koala.common.utils.UserDataUtils;
import com.kenshoo.koala.facebook.representationentities.BaseRO;
import com.kenshoo.social.client.api.common.SocialResponse;
import com.kenshoo.social.client.api.syncproperties.SyncPropertiesClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.StopWatch;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.retry.Backoff;
import reactor.retry.Repeat;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Slf4j
public abstract class BaseUpdateSyncPropertiesService<T extends BaseRO> implements UpdateSyncPropertiesService<T> {

    private final Metrics metrics = Metrics.forClass(this.getClass());

    protected static final int POOLING_RETRY_COUNT = 25;
    protected static final Duration POOLING_RETRY_PERIOD = Duration.ofSeconds(1);

    @Override
    public Mono<SocialResponse<Long>> registerUpdateSyncPropertiesJob(List<EntityRepresentation<T>> messages, SyncProcess syncProcess) {
        List<T> entities = messages.stream()
                .map(EntityRepresentation::getEntity)
                .collect(Collectors.toList());
        return Mono.fromSupplier(() -> getSyncPropertiesClient(syncProcess).update(entities, UserDataUtils.getAdvertiserId(syncProcess)));
    }

    @Override
    public Mono<SocialResponse<List<UpdateSyncPropertiesResult>>> poolJobResult(Long id, SyncProcess syncProcess) {
        StopWatch stopWatch = new StopWatch();
        return Mono.just(id)
                .map(getSyncPropertiesClient(syncProcess)::getResults)
                .onErrorResume(f -> {
                    log.error(UserDataMarker.of(syncProcess), "Pooling update sync properties result failed for jobId {}", id, f);
                    return Mono.just(SocialResponse.failed(f.getMessage()));
                })
                .repeatWhen(Repeat
                        .times(POOLING_RETRY_COUNT)
                        .backoff(Backoff.fixed(POOLING_RETRY_PERIOD)))
                .takeUntil(f -> CollectionUtils.isNotEmpty(f.getData()))
                .last()
                .doOnSubscribe(f -> stopWatch.start())
                .doFinally(f-> metrics.timer(Metrics.EXECUTION_TIME_METRIC_PREFIX, "poolJobResult").update(stopWatch.getTime(), TimeUnit.MILLISECONDS))
                .log("UpdateSyncProperties result", Level.INFO, SignalType.ON_NEXT);
    }

    protected abstract SyncPropertiesClient<T> getSyncPropertiesClient(SyncProcess syncProcess);


}
