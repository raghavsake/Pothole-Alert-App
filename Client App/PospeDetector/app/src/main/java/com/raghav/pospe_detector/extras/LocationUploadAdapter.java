package com.raghav.pospe_detector.extras;

import com.raghav.pospe_detector.data.LocationData;
import com.raghav.pospe_detector.retrofit.IUploadAPI;
import com.raghav.pospe_detector.retrofit.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationUploadAdapter {



    private static IUploadAPI getAPIUpload() {
        return RetrofitClient.getClient().create(IUploadAPI.class);
    }


    public static boolean findSpeedBreakers()
    {
        final boolean[] result = {true};
        IUploadAPI mService= getAPIUpload();
        mService.getAllSpeedBreakers().enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.body()==null||response.body().length()==0)
                    return;
                String p[] = response.body().split("\n");
                for (String x : p) {
                    String res[] = x.split(",");
                    LocationData record = new LocationData(Double.parseDouble(res[1]), Double.parseDouble(res[2]));
                    Storage.speedBreakerData.add(record);
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                result[0] =false;
            }
        });
        return result[0];
    }

    public static boolean findPotholes()
    {
        final boolean[] result = {true};
        IUploadAPI mService= getAPIUpload();
        mService.getAllPotholes().enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.body()==null||response.body().length()==0)
                    return;
                String p[] = response.body().split("\n");
                for (String x : p) {
                    String res[] = x.split(",");
                    LocationData record = new LocationData(Double.parseDouble(res[1]), Double.parseDouble(res[2]));
                    Storage.potholeData.add(record);
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                result[0] =false;
            }
        });
        return result[0];
    }


}
