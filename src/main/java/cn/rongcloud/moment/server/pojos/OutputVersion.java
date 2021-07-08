package cn.rongcloud.moment.server.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Created by CZN on 2017/3/31.
 */
public class OutputVersion {

    @JsonProperty("version")
    private String buildVersion;

    @JsonProperty("branch")
    private String buildBranch;

    @JsonProperty("commit_id")
    private String buildCommitId;

    @JsonProperty("build_time")
    private String buildTime;

    public String getBuildVersion() {
        return buildVersion;
    }

    public void setBuildVersion(String buildVersion) {
        this.buildVersion = buildVersion;
    }

    public String getBuildBranch() {
        return buildBranch;
    }

    public void setBuildBranch(String buildBranch) {
        this.buildBranch = buildBranch;
    }

    public String getBuildCommitId() {
        return buildCommitId;
    }

    public void setBuildCommitId(String buildCommitId) {
        this.buildCommitId = buildCommitId;
    }

    public String getBuildTime() {
        return buildTime;
    }

    public void setBuildTime(String buildTime) {
        this.buildTime = buildTime;
    }
}
