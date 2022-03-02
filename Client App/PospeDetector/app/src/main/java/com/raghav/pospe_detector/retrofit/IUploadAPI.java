package com.raghav.pospe_detector.retrofit;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface IUploadAPI {


    @Multipart
    @POST("/report_pothole")
    Call<String> reportPothole(@Part MultipartBody.Part file,
                               @Part("latitude") RequestBody latitude,
                               @Part("longitude") RequestBody longitude);

    @Multipart
    @POST("/report_pothole")
    Call<String> reportPothole(@Part("latitude") RequestBody latitude,
                               @Part("longitude") RequestBody longitude);

    @Multipart
    @POST("/report_speedbreaker")
    Call<String> reportSpeedBreaker(@Part MultipartBody.Part file,
                                    @Part("latitude") RequestBody latitude,
                                    @Part("longitude") RequestBody longitude);

    @Multipart
    @POST("/report_speedbreaker")
    Call<String> reportSpeedBreaker(@Part("latitude") RequestBody latitude,
                                    @Part("longitude") RequestBody longitude);

    @GET("/get_all_potholes")
    Call<String> getAllPotholes();

    @GET("/get_all_speedbreakers")
    Call<String> getAllSpeedBreakers();

    @Multipart
    @POST("/record_ata")
    Call<String> recordData(@Part("filename")RequestBody filename,
                            @Part("latitude") RequestBody latitude,
                            @Part("longitude") RequestBody longitude,
                            @Part("datetime") RequestBody datetime,
                            @Part("Ax") RequestBody ax,
                            @Part("Ay") RequestBody ay,
                            @Part("Az") RequestBody az,
                            @Part("Gx") RequestBody gx,
                            @Part("Gy") RequestBody gy,
                            @Part("Gz") RequestBody gz);

    @Multipart
    @POST("/process_data")
    Call<String> processData(@Part("filename")RequestBody filename);

}
