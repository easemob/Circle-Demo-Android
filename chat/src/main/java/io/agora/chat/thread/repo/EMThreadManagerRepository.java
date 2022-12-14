package io.agora.chat.thread.repo;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMChatThread;
import com.hyphenate.chat.EMCursorResult;
import com.hyphenate.chat.EMMessage;

import java.util.ArrayList;
import java.util.List;

import io.agora.service.callbacks.ResultCallBack;
import io.agora.service.db.entity.CircleUser;
import io.agora.service.net.ErrorCode;
import io.agora.service.net.NetworkOnlyResource;
import io.agora.service.net.Resource;
import io.agora.service.net.Status;
import io.agora.service.repo.ServiceReposity;

public class EMThreadManagerRepository extends ServiceReposity {
    /**
     * Get thread info from server
     * @param threadId
     * @return
     */
    public LiveData<Resource<EMChatThread>> getThreadFromServer(String threadId) {
        return new NetworkOnlyResource<EMChatThread>() {

            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<EMChatThread>> callBack) {
                if(!isLoggedIn()) {
                    callBack.onError(ErrorCode.NOT_LOGIN);
                    return;
                }
                getThreadManager().getChatThreadFromServer(threadId, new EMValueCallBack<EMChatThread>() {
                    @Override
                    public void onSuccess(EMChatThread value) {
                        callBack.onSuccess(createLiveData(value));
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        callBack.onError(error, errorMsg);
                    }
                });
            }

        }.asLiveData();
    }

    /**
     * Leave from thread
     * @param threadId
     * @return
     */
    public LiveData<Resource<Boolean>> leaveThread(String threadId) {
        return new NetworkOnlyResource<Boolean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                getThreadManager().leaveChatThread(threadId, new EMCallBack() {
                    @Override
                    public void onSuccess() {
                        callBack.onSuccess(createLiveData(true));
                    }

                    @Override
                    public void onError(int code, String error) {
                        callBack.onError(code, error);
                    }

                    @Override
                    public void onProgress(int progress, String status) {

                    }
                });
            }
        }.asLiveData();
    }

    /**
     * Destroy thread
     * @param threadId
     * @param parentMsgId
     * @return
     */
    public LiveData<Resource<Boolean>> destroyThread(String threadId, String parentMsgId) {
        return new NetworkOnlyResource<Boolean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                getThreadManager().destroyChatThread(threadId, new EMCallBack() {
                    @Override
                    public void onSuccess() {
                        EMMessage message = getChatManager().getMessage(parentMsgId);
                        if(message != null) {
                            removeLocalNotifyMessage(message.conversationId(), threadId);
                        }
                        callBack.onSuccess(createLiveData(true));
                    }

                    @Override
                    public void onError(int code, String error) {
                        callBack.onError(code, error);
                    }

                    @Override
                    public void onProgress(int progress, String status) {

                    }
                });
            }
        }.asLiveData();
    }

    private void removeLocalNotifyMessage(String conversationId, String threadId) {
        getChatManager().getConversation(conversationId).removeMessage(threadId);
    }

    /**
     * Change thread name
     * @param threadId
     * @return
     */
    public LiveData<Resource<Boolean>> changeThreadName(String threadId, String newThreadName) {
        return new NetworkOnlyResource<Boolean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                getThreadManager().updateChatThreadName(threadId, newThreadName, new EMCallBack() {
                    @Override
                    public void onSuccess() {
                        callBack.onSuccess(createLiveData(true));
                    }

                    @Override
                    public void onError(int code, String error) {
                        callBack.onError(code, error);
                    }

                    @Override
                    public void onProgress(int progress, String status) {

                    }
                });
            }
        }.asLiveData();
    }

    /**
     * Get thread member by thread id, limit and cursor
     * @param threadId
     * @param limit
     * @param cursor
     * @return
     */
    public LiveData<Resource<EMCursorResult<String>>> getThreadMembers(String threadId, int limit, String cursor) {
        return new NetworkOnlyResource<EMCursorResult<String>>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<EMCursorResult<String>>> callBack) {
                getThreadManager().getChatThreadMembers(threadId, limit, cursor, new EMValueCallBack<EMCursorResult<String>>() {
                    @Override
                    public void onSuccess(EMCursorResult<String> value) {
                        callBack.onSuccess(createLiveData(value));
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        callBack.onError(error, errorMsg);
                    }
                });
            }
        }.asLiveData();
    }

    /**
     * Get thread members by thread id
     * @param threadId
     * @return
     */
    public LiveData<Resource<List<CircleUser>>> getThreadMembers(Context context,String threadId) {
        List<String> users = new ArrayList<>();
        LiveData<Resource<EMCursorResult<String>>> result = getResult(threadId, 20, "", users);
        return Transformations.switchMap(result, response -> {
            if (response.status == Status.SUCCESS) {
                List<CircleUser> easeUsers = CircleUser.parseListIds(users);
                return createLiveData(Resource.success(easeUsers));
            } else if (response.status == Status.LOADING) {
                return createLiveData(new Resource<>(Status.LOADING, null, ErrorCode.EM_NO_ERROR));
            }
            return createLiveData(new Resource<>(Status.ERROR, null, response.errorCode, response.getMessage(context)));
        });
    }

    private LiveData<Resource<EMCursorResult<String>>> getResult(String threadId, int limit, String cursor, List<String> result) {
        return Transformations.switchMap(getThreadMembers(threadId, limit, cursor), response -> {
            if(response.status == Status.SUCCESS) {
                List<String> data = response.data.getData();
                if(data != null) {
                    result.addAll(data);
                    if(data.size() == limit) {
                        return getResult(threadId, limit, response.data.getCursor(), result);
                    }
                    return createLiveData(response);
                }
                return createLiveData(Resource.error(-1, response.data));

            }
            return createLiveData(response);
        });
    }

    /**
     * Remove member from thread
     * @param threadId
     * @param username
     * @return
     */
    public LiveData<Resource<Boolean>> removeThreadMember(String threadId, String username) {
        return new NetworkOnlyResource<Boolean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                getThreadManager().removeMemberFromChatThread(threadId, username, new EMCallBack() {
                    @Override
                    public void onSuccess() {
                        callBack.onSuccess(createLiveData(true));
                    }

                    @Override
                    public void onError(int code, String error) {
                        callBack.onError(code, error);
                    }

                    @Override
                    public void onProgress(int progress, String status) {

                    }
                });
            }
        }.asLiveData();
    }

}
