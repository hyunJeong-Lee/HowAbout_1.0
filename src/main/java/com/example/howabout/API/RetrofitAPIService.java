package com.example.howabout.API;

import com.example.howabout.Search.CategoryResult;
import com.example.howabout.DTO.UserDTO;
import com.example.howabout.DTO.LoginDTO;


import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface RetrofitAPIService {

    @POST("/splash/test") //autoLogin. token 적용 필요
    Call<String> test(@Header("Authorization") String token);

    @POST("/splash/autoLogin") //autoLogin. token 적용 필요
    Call<Map<String, String>> autoLogin(@Header("Authorization") String token);

    @POST("/login/signUp/idCheck") //아이디 중복 여부
    Call<Integer> idCheck(@Body Map id_check);

    @POST("/login/signUp/nickCheck") //닉네임 중복 여부
    Call<Integer> nickCheck(@Body Map nickName_check);

    @POST("/login/signUp/emailCheck") //닉네임 중복 여부
    Call<Integer> emailCheck(@Body Map email_check);

    @POST("/login/signUp") //회원가입 성공 여부
    Call<Integer> signUp(@Body UserDTO userVo);

    @POST("/login/signIn") //로그인 성공 여부
    Call<LoginDTO> signIn(@Body Map id_pw);

    @POST("/login/findMyId") //아이디 찾기
    Call<Map<String, String>> search_id(@Body Map email);

    @POST("/login/checkMyInfo") //비밀번호 재설정 회원확인
    Call<Integer> usercheck(@Body Map repwcheck);

    @POST("/login/setNewPw") //비밀번호 재설정
    Call<Integer> repw(@Body Map postrepw);

    //Find Course..............................................................
    @POST("/findCourse/rest") //식당 리스트
    Call<ArrayList<CategoryResult>> rest(@Body ArrayList<JSONObject> arrayList);

    @POST("/findCourse/cafe") //카페 리스트
    Call<ArrayList<CategoryResult>> cafe(@Body ArrayList<JSONObject> arrayList);

    @POST("/myCourse/saveCourse") //코스 저장. token 적용 필요
    Call<Map> saveCourse(@Body ArrayList<Object> arrayList, @Header("Authorization") String token);

    @POST("/myCourse/courseDibs")//내 코스 저장. token 적용 필요
    Call<Integer> courseDibs(@Body Map saveMyCourse_data, @Header("Authorization") String token);

    @POST("/findCourse/getLocationInfo") //가게 정보
    Call<Map<String, String>> getLocationInfo(@Body Map place_info); //반환값 map으로 변경

    //MyPage................................................................

    @POST("/myPage/myInfo/getMyData") //마이페이지 내정보 가지고 오기 . token 적용 필요
    Call<UserDTO> myDataUp(@Header("Authorization") String token);

    @POST("/myPage/myInfo/withdrawal") //회원탈퇴. token 적용 필요
    Call<Integer> withdrawal(@Body ArrayList<JSONObject> userInfo, @Header("Authorization") String token);

    @POST("/myPage/myInfo/updateInfo") //회원정보 수정. token 적용 필요
    Call<Map<String, String>> updateUser(@Body UserDTO user, @Header("Authorization") String token);

    @POST("/myPage/myInfo/CheckPW") //비밀번호 확인. token 적용 필요
    Call<Integer> checkPW(@Body Map u_pw, @Header("Authorization") String token);

    //PopularCourse........................................................

    @GET("/popularCourse/getDo") //DB에서 ‘도’ 를 반환
    Call<ArrayList<String>> getDo();

    @POST("/popularCourse/getSi") //DB에서 ‘도’ 기반 ‘시’ 를 반환
    Call<ArrayList<String>> getSi(@Body String getsi);

    @POST("/popularCourse/getCatCourse") //인기코스 구하기
    Call<ArrayList<JSONObject>> getCatCourse(@Body ArrayList<JSONObject> po);
}
