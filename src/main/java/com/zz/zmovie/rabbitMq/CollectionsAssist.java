package com.zz.zmovie.rabbitMq;

import lombok.Data;

/**
 * 辅助类，用于收藏业务，内嵌 isCollected 属性用于 mq 判断是收藏or取消收藏
 */
@Data
public class CollectionsAssist {
    private Long movieId;
    private Long userId;

    private boolean isCollected;
}
