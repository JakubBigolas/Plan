package com.byd.wsg.services;

import com.byd.wsg.com.wsg.byd.plan.raw.RawTimeTable;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by Jakub on 2016-06-06.
 */
public interface StudentGroupService {

    @POST("studentGroups/{id}/timetable")
    Call<RawTimeTable> getTimeTable(@Path("id") int groupId, @Body RequestTimeTable requestTimeTable);

}
