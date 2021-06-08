package cn.rongcloud.moment.server.common.jwt.filter;

import cn.rongcloud.moment.server.common.config.PassUrls;
import cn.rongcloud.moment.server.common.jwt.enums.UserAgentTypeEnum;
import cn.rongcloud.moment.server.common.rest.RestException;
import cn.rongcloud.moment.server.common.rest.RestResult;
import cn.rongcloud.moment.server.common.rest.RestResultCode;
import cn.rongcloud.moment.server.common.utils.GsonUtil;
import cn.rongcloud.moment.server.common.utils.UserHolder;
import cn.rongcloud.moment.server.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.GenericFilterBean;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by sunyinglong on 2020/6/25
 */
@Slf4j
@Component
public class JwtFilter extends GenericFilterBean {
    public static final String JWT_AUTH_DATA = "JWT_AUTH_DATA";
    public static final String USER_AGENT_TYPE = "USER_AGENT_TYPE";

    @Resource
    PassUrls passUrls;

    @Resource
    AuthService authService;

    @Override
    public void doFilter(final ServletRequest req,
                         final ServletResponse res,
                         final FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) req;

        log.debug("doFilter: " + httpReq.getRequestURL().toString());
        String userAgent = httpReq.getHeader("user-agent");
        log.info("the request IP:{}, UA: {}", getIpAddr(httpReq), userAgent);
        if (null != userAgent) {
            UserAgentTypeEnum type = UserAgentTypeEnum.getEnumByUserAgent(userAgent);
            httpReq.setAttribute(USER_AGENT_TYPE, type);
        }

        String reqMethod = httpReq.getMethod().toLowerCase();

        AntPathMatcher antPathMatcher = new AntPathMatcher();
        boolean excludes = passUrls.getAnonymousUrls().stream().filter(exclude ->
                antPathMatcher.match(exclude, httpReq.getRequestURI())).findFirst().isPresent();
        boolean access = true;
        if (!excludes && !reqMethod.equals("options")) {
            final String authorization = httpReq.getHeader("authorization");
            if (authorization != null) {
                try {
                    this.authService.checkAuth(authorization);
                    httpReq.setAttribute(JWT_AUTH_DATA, UserHolder.getUser());
                } catch (RestException e) {
                    access = false;
                    this.failOver(res, e.getRestResult());
                }
            }else{
                access = false;
                this.failOver(res, RestResult.generic(RestResultCode.ERR_REQ_WITHOUT_REQIRED_AUTHORIZATION_HEADER));
            }

//            判断需要是否继续执行
//            log.info("resp -> status: {}, data: {}", httpResp.getStatus(), httpResp.getOutputStream());
        }
        if (access) {
            chain.doFilter(httpReq, res);
            UserHolder.clear();
        }
    }

    public  String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }
        if (ip.split(",").length > 1) {
            ip = ip.split(",")[0];
        }
        return ip;
    }

    private void failOver(ServletResponse resp, RestResult restResult) {
        resp.setContentType(MediaType.APPLICATION_JSON_VALUE);
        try {
            resp.getWriter().write(GsonUtil.toJson(restResult));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
