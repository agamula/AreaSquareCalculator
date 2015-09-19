package com.proggroup.areasquarecalculator.loaders;

import android.content.res.Resources;
import android.database.Cursor;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.OperationCanceledException;
import android.support.v4.content.AsyncTaskLoader;

import com.proggroup.areasquarecalculator.InterpolationCalculator;
import com.proggroup.areasquarecalculator.R;

import java.util.ArrayList;
import java.util.List;

public class LoadCategoriesLoader extends AsyncTaskLoader<List<String>> {

    private CancellationSignal mCancellationSignal;
    private List<String> res;

    public LoadCategoriesLoader() {
        super(InterpolationCalculator.getInstance().getApplicationContext());
    }

    @Override
    public List<String> loadInBackground() {

        if(Build.VERSION.SDK_INT >= 16) {
            synchronized (this) {
                if (isLoadInBackgroundCanceled()) {
                    throw new OperationCanceledException();
                }
                mCancellationSignal = new CancellationSignal();
            }
        } else {
            mCancellationSignal = null;
        }
        try {
            Resources resources = getContext().getResources();
            res = new ArrayList<>();
            res.add(resources.getString(R.string.test_calculate_square));
            res.add(resources.getString(R.string.test_simple_measure_material));
            res.add(resources.getString(R.string.test_expanded_measure_material));
            return res;
        } finally {
            synchronized (this) {
                mCancellationSignal = null;
            }
        }
    }

    @Override
    public void cancelLoadInBackground() {
        super.cancelLoadInBackground();

        synchronized (this) {
            if (Build.VERSION.SDK_INT >= 16 && mCancellationSignal != null) {
                mCancellationSignal.cancel();
            }
        }
    }

    @Override
    public void deliverResult(List<String> res) {
        if (isReset()) {
            return;
        }

        if (isStarted()) {
            super.deliverResult(res);
        }
    }

    @Override
    protected void onStartLoading() {
        if (res != null) {
            deliverResult(res);
        }
        if (takeContentChanged() || res == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        res.clear();
        res = null;
    }
}
