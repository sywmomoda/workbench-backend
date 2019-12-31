package com.feiniu.yx.welcome.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @ClassName OsWelcomeImg
 * @Decription TODO
 * @Author shiyouwei
 * @Date 15:26 2019/10/30
 */

@Setter
@Getter
public class OsWelcomeImg {
    private Long id;

    private Long welcomeId;

    private String imgUrl;

    private String imgSize;

    private String btnImgUrl;

    private String btnCustomUrl;

    private String btnImgSize;

    private Integer index;

    private String createId;

    private Date createTime;

    private String updateId;

    private Date updateTime;
}
