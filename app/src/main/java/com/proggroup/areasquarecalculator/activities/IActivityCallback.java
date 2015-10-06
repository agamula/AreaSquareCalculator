package com.proggroup.areasquarecalculator.activities;

import android.support.v4.app.Fragment;

public interface IActivityCallback {
    void popAllDefaultContainer();
    void startFragmentToDefaultContainer(Fragment fragment, boolean addToBackStack);
    void startFragment(Fragment fragment, int containerId, boolean addToBackStack);
    void closeDrawer();
}
