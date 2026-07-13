package com.sb13.findex.sync.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class IpAddressService {

    private final ObjectProvider<HttpServletRequest> requestProvider;

    public String getClientIp() {

        HttpServletRequest request = requestProvider.getObject();


        String ip = request.getHeader("X-FORWARDED-FOR");

        if(StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)){
            return ip.split(",")[0].trim();
        }

        ip = request.getHeader("X-Real-IP");

        if(StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)){
            return ip;
        }

        return request.getRemoteAddr();
    }

}