package io.agora.service.global;

import io.agora.service.managers.PreferenceManager;

public class OptionsHelper {

    private static OptionsHelper instance;

    private OptionsHelper(){}

    public static OptionsHelper getInstance() {
        if(instance == null) {
            synchronized (OptionsHelper.class) {
                if(instance == null) {
                    instance = new OptionsHelper();
                }
            }
        }
        return instance;
    }

    public void setAllowNotifyAllMessages(boolean notifyAllMessages){
        PreferenceManager.getInstance().setAllowNotifyAllMessages(notifyAllMessages);
    }

    public boolean IsAllowNotifyAllMessages(){
        return PreferenceManager.getInstance().isAllowNotifyAllMessages();
    }
}
