package org.davidmendoza.esu.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * @author J. David Mendoza <jdmendoza@swau.edu>
 */
@Configuration
@EnableRedisHttpSession
public class HttpSessionConfig {
}
