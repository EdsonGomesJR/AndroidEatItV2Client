package com.edson.androideatitv2client.Remote;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ICloudFunctions {

    @GET("helloWorld")
    Observable<ResponseBody> getCustomToken(@Query("access_token")String accessToken);
}
