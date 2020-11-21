package cn.olange.pins.model;

import java.util.HashMap;
import java.util.Map;

public class Config {
    private boolean logined;
    private String nickname;
    private String userName;
    private String password;
    private String curentCatalog;
    private String cookieValue;

    private Map<String, String> userCookie = new HashMap<>();

    public Config() {
        this.curentCatalog = "hot";
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCurentCatalog() {
        return curentCatalog;
    }

    public void setCurentCatalog(String curentCatalog) {
        this.curentCatalog = curentCatalog;
    }

    public Map<String, String> getUserCookie() {
        return userCookie;
    }

    public void setUserCookie(Map<String, String> userCookie) {
        this.userCookie = userCookie;
    }

    public boolean isLogined() {
        return logined;
    }

    public void setLogined(boolean logined) {
        this.logined = logined;
    }

    public String getCookieValue() {
        return cookieValue;
    }

    public void setCookieValue(String cookieValue) {
        this.cookieValue = cookieValue;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
