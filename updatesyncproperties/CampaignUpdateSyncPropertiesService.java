package com.kenshoo.koala.updatesyncproperties;

import com.kenshoo.koala.common.dataaccess.api.entities.SyncProcess;
import com.kenshoo.koala.facebook.representationentities.AdCampaignRO;
import com.kenshoo.koala.social.client.impl.webclient.SocialClientFactory;
import com.kenshoo.social.client.api.syncproperties.SyncPropertiesClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CampaignUpdateSyncPropertiesService extends BaseUpdateSyncPropertiesService<AdCampaignRO> {

    private final SocialClientFactory socialClientFactory;

    @Override
    protected SyncPropertiesClient<AdCampaignRO> getSyncPropertiesClient(SyncProcess syncProcess) {
        return socialClientFactory.getClient(syncProcess.getSyncRequest()).getCampaignSyncPropertiesClient();
    }
}
