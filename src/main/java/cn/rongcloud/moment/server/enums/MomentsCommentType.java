package cn.rongcloud.moment.server.enums;

/**
 * @author renchaoyang
 * @date 2021/6/11
 */
public enum MomentsCommentType {

    COMMENT(1),
    LIKE(2);

    private int type;

    MomentsCommentType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
