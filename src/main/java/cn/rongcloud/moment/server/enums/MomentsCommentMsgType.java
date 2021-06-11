package cn.rongcloud.moment.server.enums;

/**
 * @author renchaoyang
 * @date 2021/6/11
 */
public enum MomentsCommentMsgType {
    COMMENTTYPE(1), LIKETYPE(2);

    private int action;

    MomentsCommentMsgType(int action) {
        this.action = action;
    }

    public int getAction() {
        return action;
    }
}
