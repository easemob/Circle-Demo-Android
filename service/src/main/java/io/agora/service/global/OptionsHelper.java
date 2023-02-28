package io.agora.service.global;

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
}
