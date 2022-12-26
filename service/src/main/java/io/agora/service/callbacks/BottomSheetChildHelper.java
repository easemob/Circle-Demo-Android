package io.agora.service.callbacks;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.hyphenate.easeui.widget.EaseTitleBar;

import io.agora.service.base.ContainerBottomSheetFragment;


public interface BottomSheetChildHelper {

    default void onContainerTitleBarInitialize(EaseTitleBar titlebar){}

    Fragment getParentFragment();

    default void startFragment(Fragment fragment, String tag) {
        Fragment parentFragment = getParentFragment();
        if (parentFragment != null && parentFragment instanceof BottomSheetContainerHelper) {
            ((BottomSheetContainerHelper) parentFragment).startFragment(fragment, tag);
        }
    }

    default void hide() {
        Fragment parentFragment = getParentFragment();
        if (parentFragment != null && parentFragment instanceof BottomSheetContainerHelper) {
            ((BottomSheetContainerHelper) parentFragment).hide();
        }
    }

    default void back() {
        Fragment parentFragment = getParentFragment();
        if (parentFragment != null && parentFragment instanceof BottomSheetContainerHelper) {
            ((BottomSheetContainerHelper) parentFragment).back();
        }
    }

    default  void show(FragmentManager supportFragmentManager) {
        Fragment instance= (Fragment) this;
        MyContainerBottomSheetFragment bottomSheetFragment = new MyContainerBottomSheetFragment(instance);
        bottomSheetFragment.show(supportFragmentManager, instance.getClass().getSimpleName());
    }

    default  void show(FragmentManager supportFragmentManager,int topOffset) {
        Fragment instance= (Fragment) this;
        MyContainerBottomSheetFragment bottomSheetFragment = new MyContainerBottomSheetFragment(instance,topOffset);
        bottomSheetFragment.show(supportFragmentManager, instance.getClass().getSimpleName());
    }



    class MyContainerBottomSheetFragment extends ContainerBottomSheetFragment{

         private final Fragment fragment;

         public MyContainerBottomSheetFragment(Fragment fragment) {
            this.fragment=fragment;
         }
         public MyContainerBottomSheetFragment(Fragment fragment,int topOffset) {
            this.fragment=fragment;
            setTopOffset(topOffset);
         }

        @NonNull
        @Override
        protected Fragment getChildFragment() {
            return fragment;
        }
    }
}
