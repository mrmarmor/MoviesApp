package com.appstairs.movies.Main.View.fragments;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.appstairs.movies.Main.Model.MovieModel;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.util.List;

public interface iMainFragment {
    void setViews(List<MovieModel> movieModels);
    void updateViews(MovieModel movieModel);
    void getCameraPermission();
    boolean requestPermission(Activity activity, @NonNull String permission);
    boolean checkCameraHardware(Activity activity);
    void openCamera();
    void startCameraSource();
    void createQrCodeDetector();
    void barcodeModule(@NonNull BarcodeDetector detector);
    void parseValue(String value);
}
