package com.example.howabout.functions;

import android.content.SharedPreferences;

import com.example.howabout.Vo.LoginDTO;

//앱 데이터 저장 함숫
public class SaveData {
    public void save_login_Data(SharedPreferences.Editor editor, LoginDTO loginDTO, boolean CODE_check_autoLogin){
        editor.putString("token", loginDTO.getToken());
        editor.putString("u_nick", loginDTO.getMsg());
        editor.putBoolean("auto", CODE_check_autoLogin);
        editor.commit();
    }
}
