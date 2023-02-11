package io.agora.service.repo;


import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMCircleCategory;
import com.jeremyliao.liveeventbus.LiveEventBus;

import io.agora.service.callbacks.ResultCallBack;
import io.agora.service.db.entity.CircleCategory;
import io.agora.service.db.entity.CircleChannel;
import io.agora.service.event.CategoryEvent;
import io.agora.service.global.Constants;
import io.agora.service.net.NetworkOnlyResource;
import io.agora.service.net.Resource;

public class CircleCagegoryReposity extends ServiceReposity {

    public LiveData<Resource<EMCircleCategory>> createCategory(String serverId, String categoryName) {
        return new NetworkOnlyResource<EMCircleCategory>() {

            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<EMCircleCategory>> callBack) {
                getCircleManager().createCategory(serverId, categoryName, new EMValueCallBack<EMCircleCategory>() {
                    @Override
                    public void onSuccess(EMCircleCategory value) {
                        callBack.onSuccess(createLiveData(value));
                        //插入数据库
                        getCategoryDao().insert(new CircleCategory(value));
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        callBack.onError(error, errorMsg);
                    }
                });
            }
        }.asLiveData();
    }


    public LiveData<Resource<Boolean>> deleteCategory(String serverId, String categoryId) {
        return new NetworkOnlyResource<Boolean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                getCircleManager().destroyCategory(serverId, categoryId, new EMCallBack() {
                    @Override
                    public void onSuccess() {
                        callBack.onSuccess(createLiveData(true));
                        //从数据库总删除
                        getCategoryDao().deleteByCagegoryId(categoryId);
                        //发出广播通知分组删除
                        CategoryEvent categoryEvent = new CategoryEvent();
                        categoryEvent.serverId = serverId;
                        categoryEvent.categoryId = categoryId;
                        LiveEventBus.get(Constants.CATEGORY_DELETE, CategoryEvent.class).post(categoryEvent);
                    }

                    @Override
                    public void onError(int code, String error) {
                        callBack.onError(code, error);
                    }
                });
            }
        }.asLiveData();
    }


    public LiveData<Resource<Boolean>> updateCategory(String serverId, String categoryId, String categoryName) {
        return new NetworkOnlyResource<Boolean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                getCircleManager().updateCategory(serverId, categoryId, categoryName, new EMValueCallBack<EMCircleCategory>() {
                    @Override
                    public void onSuccess(EMCircleCategory value) {
                        getCategoryDao().updateCategory(new CircleCategory(value));
                        callBack.onSuccess(createLiveData(true));
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        callBack.onError(error, errorMsg);
                    }
                });
            }
        }.asLiveData();
    }

    public LiveData<Resource<String>> transferCategory(String serverId, String newCategoryId, String channelId) {
        return new NetworkOnlyResource<String>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<String>> callBack) {
                getCircleManager().transferChannel(serverId,channelId, newCategoryId, new EMCallBack() {
                    @Override
                    public void onSuccess() {
                        //更新数据库
                        CircleChannel channel = getChannelDao().getChannelByChannelID(channelId);
                        channel.categoryId = newCategoryId;
                        getChannelDao().insert(channel);
                        //发出广播，channel更新拉
                        LiveEventBus.get(Constants.CHANNEL_CHANGED).post(channel);

                        callBack.onSuccess(createLiveData(newCategoryId));
                    }

                    @Override
                    public void onError(int code, String error) {
                        callBack.onError(code, error);
                    }
                });
            }
        }.asLiveData();
    }
}
