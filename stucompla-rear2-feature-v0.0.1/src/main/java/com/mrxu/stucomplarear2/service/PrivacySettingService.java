package com.mrxu.stucomplarear2.service;

import com.mrxu.stucomplarear2.entity.PrivacySetting;
import com.mrxu.stucomplarear2.utils.response.Result;

public interface PrivacySettingService {
    PrivacySetting getMyPrivacy(String userId);
    Result updatePrivacy(PrivacySetting setting);
    boolean checkVisibility(String targetUserId, String viewerUserId, String field);
    boolean checkVisibility(String targetUserId, String viewerUserId, String field, String viewerRole);
}
