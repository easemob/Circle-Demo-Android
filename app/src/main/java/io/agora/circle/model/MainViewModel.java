package io.agora.circle.model;


import android.app.Application;

import androidx.annotation.NonNull;

import io.agora.common.base.BaseViewModel;
import io.agora.service.bean.GetRTCTokenBean;
import io.agora.service.net.Resource;
import io.agora.service.repo.ServiceReposity;
import io.agora.service.utils.SingleSourceLiveData;

public class MainViewModel extends BaseViewModel {
    private ServiceReposity serviceReposity = new ServiceReposity();

    public SingleSourceLiveData<Resource<GetRTCTokenBean>> getRTCTokenLiveData = new SingleSourceLiveData<>();

    public MainViewModel(@NonNull Application application) {
        super(application);
    }

    public void getRTCToken(String channelName,String agoraUid,String userName) {
        getRTCTokenLiveData.setSource(serviceReposity.getRTCToken(channelName, agoraUid, userName));
    }
}
