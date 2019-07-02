package com.kenshoo.koala.updatesyncproperties;

import com.kenshoo.koala.api.representation.EntityRepresentation;
import com.kenshoo.koala.api.representation.UpdateSyncPropertiesResult;
import com.kenshoo.koala.common.dataaccess.api.entities.SyncProcess;
import com.kenshoo.koala.facebook.representationentities.BaseRO;
import com.kenshoo.social.client.api.common.SocialResponse;
import reactor.core.publisher.Mono;

import java.util.List;

public interface UpdateSyncPropertiesService<T extends BaseRO> {

    Mono<SocialResponse<Long>> registerUpdateSyncPropertiesJob(List<EntityRepresentation<T>> messages, SyncProcess syncProcess);

    Mono<SocialResponse<List<UpdateSyncPropertiesResult>>> poolJobResult(Long id, SyncProcess syncProcess);

}
