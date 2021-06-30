package cn.rongcloud.moment.server.enums;

/**
 * 动态状态
 */
public enum LikeStatus {

    /**
     * 正常
     */
    NORMAL(0),

    /**
     * 删除
     */
    DELETED(1);

    private int value;

    LikeStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
