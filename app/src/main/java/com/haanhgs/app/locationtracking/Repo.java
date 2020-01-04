package com.haanhgs.app.locationtracking;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Repo {

    private static final String TAG = "D.Repo";
    private static final Executor executor = Executors.newSingleThreadExecutor();

    public static Task<String> getAddress(Context context, Location location){
        return Tasks.call(executor, () -> {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = null;
            String resultMessage;
            try{
                addresses = geocoder.getFromLocation(
                        location.getLatitude(),
                        location.getLongitude(),
                        // In this sample, get just a single address
                        1);
            }catch (IOException e){
                resultMessage = context.getString(R.string.service_not_available);
                Log.e(TAG, resultMessage, e);
            }catch (IllegalArgumentException e){
                resultMessage = context.getString(R.string.invalid_lat_long_used);
                Log.e(TAG, resultMessage + ". " +
                        "Latitude = " + location.getLatitude() +
                        ", Longitude = " + location.getLongitude(), e);
            }

            if (addresses == null || addresses.size() == 0){
                resultMessage = context.getString(R.string.no_address_found);
                Log.e(TAG, resultMessage);
            }else {
                Address address = addresses.get(0);
                List<String> addressParts = new ArrayList<>();
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    addressParts.add(address.getAddressLine(i));
                }
                resultMessage = TextUtils.join("\n", addressParts);
            }
            return resultMessage;
        });
    }
}
