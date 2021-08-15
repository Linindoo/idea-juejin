package cn.olange.pins.model;


import java.util.Date;

public class Config {
    private boolean logined;
    private String nickname;
    private String userId;
    private String userName;
    private String password;
    private String curentCatalog;
    private String cookieValue;
    private String cookieType = Constant.cookieType.DIRECT.name();
    private boolean dailySign;
    private boolean enableAutoSign;
    private Date signDate;

    public boolean isDailySign() {
        return dailySign;
    }

    public void setDailySign(boolean dailySign) {
        this.dailySign = dailySign;
    }

    public Date getSignDate() {
        return signDate;
    }

    public void setSignDate(Date signDate) {
        this.signDate = signDate;
    }

    private int messageRefreshInterval = 60;

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

    public String getCookieType() {
        return cookieType;
    }

    public void setCookieType(String cookieType) {
        this.cookieType = cookieType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getMessageRefreshInterval() {
        return messageRefreshInterval;
    }

    public void setMessageRefreshInterval(int messageRefreshInterval) {
        this.messageRefreshInterval = messageRefreshInterval;
    }

    public boolean isEnableAutoSign() {
        return enableAutoSign;
    }

    public void setEnableAutoSign(boolean enableAutoSign) {
        this.enableAutoSign = enableAutoSign;
    }
}
