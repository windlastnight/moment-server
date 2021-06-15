package cn.rongcloud.moment.server.enums;

/**
 * 动态状态
 */
public enum MessageStatus {

    /**
     * 正常
     */
    NORMAL(0),

    /**
     * 删除
     */
    DELETED(1);

    private int value;

    MessageStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
