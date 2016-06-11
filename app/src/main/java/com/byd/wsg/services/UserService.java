package com.byd.wsg.services;

import com.byd.wsg.com.wsg.byd.plan.raw.RawAuthenticate;
import com.byd.wsg.com.wsg.byd.plan.raw.RawStudent;
import com.byd.wsg.com.wsg.byd.plan.raw.RawTeacher;
import com.byd.wsg.com.wsg.byd.plan.raw.RawTimeTable;
import com.byd.wsg.com.wsg.byd.plan.raw.RawUser;

import java.util.Collection;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Jakub on 2016-06-05.
 */
public interface UserService {
    @GET("users")
    Call<Collection<RawUser>> getUsers();

    @GET("users/{login}")
    Call<RawUser> getUser(@Path("login") String login);

    @GET("teachers/{id}")
    Call<RawTeacher> getTeacher(@Path("id") int id);

    @GET("teachers")
    Call<RawTeacher[]> getTeachers();

    @GET("students/{id}")
    Call<RawStudent> getStudent(@Path("id") int id);

    @GET("students")
    Call<RawStudent[]> getStudents();

    @POST("authenticate")
    Call<RawAuthenticate> getToken(@Query("username") String login, @Query("password") String password);
}