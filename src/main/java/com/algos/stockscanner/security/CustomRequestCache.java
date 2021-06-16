package com.algos.stockscanner.security;

import org.springframework.security.web.savedrequest.*;

import javax.servlet.http.*;

/**
 * HttpSessionRequestCache that avoids saving internal framework requests.
 */
public class CustomRequestCache extends HttpSessionRequestCache {
    /**
     * {@inheritDoc}
     * <p>
     * If the method is considered an internal request from the framework, we skip
     * saving it.
     *
     * @see SecurityUtils#isFrameworkInternalRequest(HttpServletRequest)
     */
    @Override
    public void saveRequest(HttpServletRequest request, HttpServletResponse response) {
        if (!SecurityUtils.isFrameworkInternalRequest(request)) {
            super.saveRequest(request, response);
        }
    }

}