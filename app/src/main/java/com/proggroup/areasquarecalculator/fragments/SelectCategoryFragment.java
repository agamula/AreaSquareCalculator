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
import com.proggroup.areasquarecalculator.activities.MainActivity;
import com.proggroup.areasquarecalculator.loaders.LoadCategoriesLoader;

import java.util.List;

public class SelectCategoryFragment extends ListFragment implements LoaderManager
        .LoaderCallbacks<List<String>> {

    private static final int LOAD_CATEGORIES_LOADER_ID = 0;

    @Override
    public void onAttach(Context context) {
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
            getView().setBackgroundColor(getResources().getColor(R.color.app_color));
            setListAdapter(new ArrayAdapter<>(activity, R.layout.item_select_category, R.id
                    .select_category_text, strings));
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        switch (position) {
            case 0:
                MainActivity activity = (MainActivity) getActivity();
                activity.popAllDefaultContainer();
                activity.startFragmentToDefaultContainer(new CalculateSquareAreaFragment(), false);
                activity.closeDrawer();
                break;
            case 1:
                break;
            case 2:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<List<String>> loader) {

    }
}
