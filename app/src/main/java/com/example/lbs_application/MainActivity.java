package com.example.lbs_application;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log; // Tambahkan ini untuk logging jika ingin menggunakan Log.d

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private TextView latitudeTextView;
    private TextView longitudeTextView;

    private static final String TAG = "LBS_App"; // Digunakan untuk Log.d

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latitudeTextView = findViewById(R.id.latitudeTextView);
        longitudeTextView = findViewById(R.id.longitudeTextView);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        createLocationRequest();
        createLocationCallback();

        checkLocationPermission();
    }

    // Metode untuk membuat LocationRequest
    private void createLocationRequest() {
        locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, // Akurasi tinggi, menggunakan GPS
                5000 // Interval pembaruan yang diinginkan: setiap 5 detik (milidetik)
        )
                .setMinUpdateIntervalMillis(2500) // Interval pembaruan tercepat: tidak lebih cepat dari 2.5 detik
                // .setWaitForActivityUpdates(false) // Baris ini DIHAPUS karena error "Cannot resolve method"
                .build();
    }

    // Metode untuk membuat LocationCallback
    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                // Dipanggil setiap kali ada pembaruan lokasi
                // locationResult tidak akan null di sini, tapi daftar lokasi bisa kosong
                if (locationResult == null) {
                    Log.d(TAG, "LocationResult is null, returning.");
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        updateLocationUI(location); // Perbarui UI dengan lokasi baru
                        Log.d(TAG, "Location updated: " + location.getLatitude() + ", " + location.getLongitude());
                    }
                }
            }
        };
    }

    // Metode untuk memeriksa dan meminta izin lokasi
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Izin sudah diberikan, mulai mendapatkan lokasi
            Log.d(TAG, "Izin lokasi sudah diberikan. Mencoba mendapatkan lokasi.");
            getLastLocation();
            startLocationUpdates();
        } else {
            // Izin belum diberikan, minta izin
            Log.d(TAG, "Izin lokasi belum diberikan. Meminta izin...");
            Toast.makeText(this, "Meminta izin lokasi...", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    // Metode untuk mendapatkan lokasi terakhir yang diketahui
    private void getLastLocation() {
        // Periksa lagi izin sebelum memanggil, meskipun sudah dicek di checkLocationPermission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Lokasi terakhir diketahui tersedia
                            if (location != null) {
                                updateLocationUI(location);
                                Toast.makeText(MainActivity.this, "Lokasi terakhir ditemukan!", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "Last known location: " + location.getLatitude() + ", " + location.getLongitude());
                            } else {
                                Toast.makeText(MainActivity.this, "Tidak dapat menemukan lokasi terakhir. Mencoba pembaruan.", Toast.LENGTH_LONG).show();
                                Log.d(TAG, "Last known location is null.");
                            }
                        }
                    });
        }
    }

    // Metode untuk memulai pembaruan lokasi
    private void startLocationUpdates() {
        // Periksa izin sebelum memulai pembaruan
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    Looper.getMainLooper()); // Gunakan Looper utama untuk callback
            Toast.makeText(this, "Mulai pembaruan lokasi...", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Location updates started.");
        } else {
            Log.d(TAG, "Cannot start location updates: permission denied.");
            Toast.makeText(this, "Tidak bisa memulai pembaruan: izin lokasi tidak ada.", Toast.LENGTH_SHORT).show();
        }
    }

    // Metode untuk menghentikan pembaruan lokasi
    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
        Toast.makeText(this, "Pembaruan lokasi dihentikan.", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Location updates stopped.");
    }

    // Metode untuk memperbarui UI dengan data lokasi
    private void updateLocationUI(Location location) {
        if (location != null) {
            latitudeTextView.setText("Latitude: " + String.format("%.6f", location.getLatitude()));
            longitudeTextView.setText("Longitude: " + String.format("%.6f", location.getLongitude()));
        } else {
            latitudeTextView.setText("Latitude: N/A");
            longitudeTextView.setText("Longitude: N/A");
        }
    }

    // Callback untuk hasil permintaan izin
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults); // Panggil superclass
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Izin diberikan, mulai mendapatkan lokasi
                Toast.makeText(this, "Izin lokasi diberikan!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Location permission granted.");
                getLastLocation();
                startLocationUpdates();
            } else {
                // Izin ditolak
                Toast.makeText(this, "Izin lokasi ditolak. Aplikasi mungkin tidak berfungsi dengan benar.", Toast.LENGTH_LONG).show();
                Log.d(TAG, "Location permission denied.");
                latitudeTextView.setText("Izin Ditolak");
                longitudeTextView.setText("Izin Ditolak");
            }
        }
    }

    // Lifecycle methods untuk mengelola pembaruan lokasi
    @Override
    protected void onResume() {
        super.onResume();
        // Lanjutkan pembaruan lokasi saat aplikasi kembali aktif
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
            Log.d(TAG, "Activity resumed. Restarting location updates.");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Hentikan pembaruan lokasi saat aplikasi di-pause untuk menghemat baterai
        stopLocationUpdates();
        Log.d(TAG, "Activity paused. Stopping location updates.");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Pastikan pembaruan lokasi dihentikan saat Activity dihancurkan
        stopLocationUpdates();
        Log.d(TAG, "Activity destroyed. Stopping location updates.");
    }
}