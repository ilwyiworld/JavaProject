package com.znv.filter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * 编码格式过滤器，限定为UTF-8
 */
public class EncodingFilter extends HttpServlet implements Filter {

    private static final long serialVersionUID = 1105261304693146283L;

    private static final Logger LOGGER = LogManager.getLogger(EncodingFilter.class.getName());

    /*
     * (non-Javadoc)
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig filterConfig) throws ServletException {
        // do nothing
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.GenericServlet#destroy()
     */
    public void destroy() {
        // do nothing
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
     * javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse rep = (HttpServletResponse) response;
        try {
            String encoding = "UTF-8";
            req.setCharacterEncoding(encoding);
            rep.setCharacterEncoding(encoding);
            rep.setContentType(encoding);

            if (filterChain != null) {
                filterChain.doFilter(request, response);
            }
        } catch (UnsupportedEncodingException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (ServletException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

}
