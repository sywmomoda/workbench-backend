package com.feiniu.yx.welcome.entity;

import com.feiniu.yx.util.YXBaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @ClassName OsWelcome
 * @Decription TODO
 * @Author shiyouwei
 * @Date 15:26 2019/10/30
 */

@Setter
@Getter
public class OsWelcome extends YXBaseEntity {
    private Long id;

    private String name;

    private Date beginTime;

    private Date endTime;

    private Integer showTime;

    private Integer status;

    private String createId;

    private Date createTime;

    private String updateId;

    private Date updateTime;
}
