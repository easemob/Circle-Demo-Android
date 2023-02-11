package io.agora.service.model;

import android.app.Application;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.hyphenate.chat.EMCircleCategory;

import io.agora.service.net.Resource;
import io.agora.service.repo.CircleCagegoryReposity;
import io.agora.service.utils.SingleSourceLiveData;

/**
 * 对分组的操作放在这里
 */
public class CategoryViewModel extends ServiceViewModel{

    public ObservableField<String> cagegoryName = new ObservableField<>();
    private CircleCagegoryReposity categoryReposity = new CircleCagegoryReposity();
    public SingleSourceLiveData<Resource<EMCircleCategory>> createCategoryLiveData = new SingleSourceLiveData<>();
    public SingleSourceLiveData<Resource<Boolean>> deleteCategoryLiveData=new SingleSourceLiveData<>();
    public SingleSourceLiveData<Resource<Boolean>> updateCategoryLiveData=new SingleSourceLiveData<>();
    public SingleSourceLiveData<Resource<String>> transferCategoryLiveData=new SingleSourceLiveData<>();

    public CategoryViewModel(@NonNull Application application) {
        super(application);
    }

    public void delete(View view) {
        cagegoryName.set("");
    }

    public void createCategory(String serverId,String categoryName) {
        createCategoryLiveData.setSource(categoryReposity.createCategory(serverId, categoryName));
    }

    public void deleteCategory(String serverId, String categoryId) {
        deleteCategoryLiveData.setSource(categoryReposity.deleteCategory(serverId,categoryId));
    }

    public void updateCategory(String serverId, String categoryId,String categoryName) {
        updateCategoryLiveData.setSource(categoryReposity.updateCategory(serverId,categoryId,categoryName));
    }

    public void transferCategory(String serverId, String newCategoryId, String channelId){
        transferCategoryLiveData.setSource(categoryReposity.transferCategory(serverId,newCategoryId,channelId));
    }

}
