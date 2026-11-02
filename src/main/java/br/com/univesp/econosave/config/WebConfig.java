package br.com.univesp.econosave.config;

import jakarta.servlet.http.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                response.setHeader("Cache-Control", "no-store");
                if (!java.util.Set.of("GET", "HEAD", "OPTIONS").contains(request.getMethod())
                        && !"EconoSave".equals(request.getHeader("X-Requested-With"))) {
                    response.setStatus(403);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"message\":\"Origem da solicitação inválida.\"}");
                    return false;
                }
                return true;
            }
        }).addPathPatterns("/api/**");
    }
}