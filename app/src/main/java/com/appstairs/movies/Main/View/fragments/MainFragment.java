package com.appstairs.movies.Main.View.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appstairs.movies.R;
import com.appstairs.movies.Main.Controller.MainController;
import com.appstairs.movies.Main.Model.MovieModel;
import com.appstairs.movies.Main.Controller.MoviesAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.appstairs.movies.Main.Model.iMovie.MOVIES_DB;

public class MainFragment extends Fragment implements SurfaceHolder.Callback {
    private static final String TAG = MainFragment.class.getSimpleName();
    private static final int CAMERA_REQUEST_CODE = 1001;

    private Activity act;
    private View v;
    private SurfaceView qrSurfaceView;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<MovieModel> movieModels;
    private CameraSource cameraSource;
    private Camera camera;
    private MainFragmentListener mListener;

    public MainFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v  = inflater.inflate(R.layout.fragment_main, container, false);
        act = getActivity();

        return v;
    }

    public void setViews(List<MovieModel> movieModels) {
        this.movieModels = movieModels;

        qrSurfaceView = v.findViewById(R.id.scanQr_surfaceView);

        recyclerView = (RecyclerView)v.findViewById(R.id.rv_movies);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(act);
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new MoviesAdapter(act, movieModels);
        recyclerView.setAdapter(mAdapter);

        final FloatingActionButton fab = v.findViewById(R.id.fabAddMovie);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                qrSurfaceView.setVisibility(View.VISIBLE);
                getCameraPermission();
                mListener.onSurfaceViewModeChange(true);
            }
        });
    }

    public void updateViews(MovieModel movieModel) {
        //check if already exists
        for (MovieModel movieModel1 : movieModels) {
            if (new Gson().toJson(movieModel1).equals(new Gson().toJson(movieModel))) {
                Snackbar.make(v.getRootView(), getString(R.string.error_alreadyExists), Snackbar.LENGTH_LONG).show();
                return;
            }
        }

        List<MovieModel> newMovieModel = new ArrayList<>(1);
        newMovieModel.add(movieModel);
        SQLiteDatabase moviesDB = act.openOrCreateDatabase(MOVIES_DB, MODE_PRIVATE, null);
        MainController.getInstance().saveOnSqlLite(moviesDB, newMovieModel, false);

        movieModels.add(movieModel);
        mAdapter.notifyDataSetChanged();
        Snackbar.make(v.getRootView(), movieModel.getTitle() + " added.", Snackbar.LENGTH_LONG).show();
    }

    private void getCameraPermission() {
        if (checkCameraHardware(act) && requestPermission(act, Manifest.permission.CAMERA)){
            requestPermission(act, Manifest.permission.CAMERA);
        }
    }

    //if permission already grandted, open camera. If not, ask user for permission. If user granted now, go immediately to
    //opening camera.
    private boolean requestPermission(Activity activity, @NonNull String permission) {
        if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{permission}, CAMERA_REQUEST_CODE);
            requestPermission(activity, permission);
            return false;
        } else {
            startCameraSource();
            createQrCodeDetector();
            openCamera();
            return true;
        }
    }

    public boolean checkCameraHardware(Activity activity) {
        if (activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            Toast.makeText(activity, getString(R.string.error_noCamera), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private void openCamera() {
        SurfaceHolder surfaceHolder = qrSurfaceView.getHolder();
        surfaceHolder.addCallback(this);
        camera = Camera.open();
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    private void startCameraSource() throws SecurityException {
        final int RC_HANDLE_GMS = 9001;

        //check that the device has play services
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(act);
        if (code != ConnectionResult.SUCCESS) {
            GoogleApiAvailability.getInstance().getErrorDialog(act, code, RC_HANDLE_GMS).show();
        }

        if (cameraSource != null) {
            try {
                cameraSource.start();
            } catch (IOException e) {
                Toast.makeText(act, getString(R.string.error_startCamera) + e, Toast.LENGTH_LONG).show();
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    private void createQrCodeDetector() throws SecurityException {
        final BarcodeDetector detector = new BarcodeDetector.Builder(act)
                .setBarcodeFormats(Barcode.QR_CODE).build();
        if(!detector.isOperational()){
            Log.e(TAG, "Could not set up the detector!");
            return;
        }
        detector.setFocus(0);

        cameraSource = new CameraSource.Builder(act, detector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1280, 1024)
                .setRequestedFps(2.0f)
                .setAutoFocusEnabled(true)
                .build();
        barcodeModule(detector);
    }

    private void barcodeModule(@NonNull BarcodeDetector detector) {
        detector.setProcessor(new Detector.Processor<Barcode>() {
            @Override public void release() { }
            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrCodes = detections.getDetectedItems();

                if (qrCodes != null && qrCodes.size() > 0) {
                    Barcode barcode = qrCodes.valueAt(0);
                    if (barcode != null && barcode.format == Barcode.QR_CODE) {
                        Log.d(TAG, "receiveDetections: "+barcode.displayValue);
                        MovieModel movieModel = MainController.getInstance().parseValue(barcode.displayValue);
                        mListener.onFragmentComplete(movieModel);
                    } else {
                        act.runOnUiThread(new Runnable() { public void run() {
                            Toast.makeText(act, getString(R.string.error_unknownQr)
                                    + qrCodes.valueAt(0).displayValue, Toast.LENGTH_LONG).show();
                        }});
                    }
                }
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MainFragmentListener) {
            mListener = (MainFragmentListener)context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mListener.onSurfaceViewModeChange(false);
        mListener = null;

        if (cameraSource != null) {
            cameraSource.stop();
            cameraSource.release();
        }

        if (camera != null) {
            camera.stopPreview();
            camera = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            cameraSource.start(qrSurfaceView.getHolder());
        } catch (IOException | SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        camera.stopPreview();
    }

    public interface MainFragmentListener {
        void onFragmentComplete(MovieModel movieModel);
        void onSurfaceViewModeChange(boolean isQrScanMode);
    }
}