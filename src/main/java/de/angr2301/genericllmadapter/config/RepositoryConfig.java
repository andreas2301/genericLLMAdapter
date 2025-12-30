package de.angr2301.genericllmadapter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;

/**
 * JDBC HTTP Session Configuration
 *
 * Manages user login sessions in PostgreSQL (reliable).
 * This is separate from ChatSession entities (chat history) which are in
 * PostgreSQL.
 */
@Configuration
public class RepositoryConfig {

    @Bean
    public org.springframework.session.web.http.CookieSerializer cookieSerializer() {
        org.springframework.session.web.http.DefaultCookieSerializer serializer = new org.springframework.session.web.http.DefaultCookieSerializer();
        serializer.setCookieName("SESSION");
        serializer.setCookiePath("/");
        serializer.setSameSite("Lax");
        serializer.setUseSecureCookie(true);
        serializer.setUseHttpOnlyCookie(true);
        return serializer;
    }
}
