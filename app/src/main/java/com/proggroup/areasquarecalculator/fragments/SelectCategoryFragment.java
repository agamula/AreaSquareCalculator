package com.proggroup.areasquarecalculator.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.proggroup.areasquarecalculator.R;
import com.proggroup.areasquarecalculator.activities.IActivityCallback;
import com.proggroup.areasquarecalculator.loaders.LoadCategoriesLoader;

import java.util.List;

public class SelectCategoryFragment extends ListFragment implements LoaderManager
        .LoaderCallbacks<List<String>> {

    private static final int LOAD_CATEGORIES_LOADER_ID = 0;

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        getLoaderManager().initLoader(LOAD_CATEGORIES_LOADER_ID, null, this);
    }

    @Override
    public Loader<List<String>> onCreateLoader(int id, Bundle bundle) {
        switch (id) {
            case LOAD_CATEGORIES_LOADER_ID:
                return new LoadCategoriesLoader();
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<List<String>> loader, List<String> strings) {
        if(loader.getId() == LOAD_CATEGORIES_LOADER_ID && isAdded()) {
            Activity activity = getActivity();
            getView().setBackgroundColor(getResources().getColor(R.color.drawer_color));
            setListAdapter(new ArrayAdapter<>(activity, R.layout.item_select_category, R.id
                    .select_category_text, strings));
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Activity activity = getActivity();
        IActivityCallback callback = activity instanceof IActivityCallback ? (IActivityCallback)
                 activity : null;

        switch (position) {
            case 0:
                callback.popAllDefaultContainer();
                callback.startFragmentToDefaultContainer(new CalculateSquareAreaFragment(), false);
                callback.closeDrawer();
                break;
            case 1:
                callback.popAllDefaultContainer();
                callback.startFragmentToDefaultContainer(new CalculatePpmSimpleFragment(), false);
                callback.closeDrawer();
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<List<String>> loader) {

    }
}
