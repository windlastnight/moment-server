package cn.rongcloud.moment.server.common.im;

public enum  ConversationType {
    PRIVATE(1),
    DISCUSSION(2),
    GROUP(3),
    SYSTEM(6);

    private int value;

    private ConversationType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static ConversationType valueOf(int intValue) {
        switch (intValue) {
            case 1:
                return PRIVATE;
            case 2:
                return DISCUSSION;
            case 3:
                return GROUP;
            case 6:
                return SYSTEM;

            default:
                return null;
        }
    }
}
