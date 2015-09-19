package com.proggroup.areasquarecalculator.activities;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.proggroup.areasquarecalculator.R;
import com.proggroup.areasquarecalculator.fragments.CalculateSquareAreaFragment;
import com.proggroup.areasquarecalculator.fragments.SelectCategoryFragment;

public class MainActivity extends AppCompatActivity{

    private DrawerLayout mDrawerLayout;
    private View mFragmentContainerView;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        setupDrawer(R.id.fragment_select_category, (DrawerLayout) findViewById(R.id.drawer_layout));

        Fragment fragment;
        FragmentManager manager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            fragment = new CalculateSquareAreaFragment();
        } else {
            fragment = manager.findFragmentById(R.id.fragment_container);
        }
        manager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }

    public void startFragment(Fragment fragment, int containerId, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(containerId, fragment);
        if(addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    public void startFragmentToDefaultContainer(Fragment fragment, boolean addToBackStack) {
        startFragment(fragment, R.id.fragment_container, addToBackStack);
    }

    public void popAll(int containerId) {
        getSupportFragmentManager().popBackStack(containerId, FragmentManager
                .POP_BACK_STACK_INCLUSIVE);
    }

    public void popAllDefaultContainer() {
        popAll(R.id.fragment_container);
    }

    public void closeDrawer() {
        mDrawerLayout.closeDrawer(mFragmentContainerView);
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    public void setupDrawer(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayShowTitleEnabled(false);
        ab.setDisplayUseLogoEnabled(false);
        ab.setDisplayShowCustomEnabled(true);

        ab.setCustomView(R.layout.toolbar);

        ((TextView)ab.getCustomView().findViewById(R.id.title)).setText(getString(R.string
                 .app_name));

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                toolbar,
                R.string.drawer_cont_desc_open,
                R.string.drawer_cont_desc_close) {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }

            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };

        closeDrawer();

        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.setDrawerIndicatorEnabled(true);
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }
}
