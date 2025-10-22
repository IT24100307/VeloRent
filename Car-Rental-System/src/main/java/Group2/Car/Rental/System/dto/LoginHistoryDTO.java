package Group2.Car.Rental.System.dto;

import java.time.LocalDateTime;

public class LoginHistoryDTO {
    private Long loginId;
    private Long userId;
    private String username;
    private String accountType;
    private LocalDateTime loginTime;

    // Constructors
    public LoginHistoryDTO() {}

    public LoginHistoryDTO(Long loginId, Long userId, String username, 
                          String accountType, LocalDateTime loginTime) {
        this.loginId = loginId;
        this.userId = userId;
        this.username = username;
        this.accountType = accountType;
        this.loginTime = loginTime;
    }

    // Getters and setters
    public Long getLoginId() {
        return loginId;
    }

    public void setLoginId(Long loginId) {
        this.loginId = loginId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }
}