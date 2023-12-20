package com.zz.zmovie.po;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String username;
    private String account;
    private String password;
    private String avatar;
    private Date createTime;
    private String history;
    private String collection;
    private int status;
    private String salt;
    private String label;

    public User(String account ,String password){
        this.account = account;
        this.password = password;
    }

}
