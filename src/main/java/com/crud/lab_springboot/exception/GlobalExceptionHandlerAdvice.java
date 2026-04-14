package com.crud.lab_springboot.exception;

import com.crud.lab_springboot.projo.ResponseMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice // 统一处理
public class GlobalExceptionHandlerAdvice {

    Logger log = LoggerFactory.getLogger(GlobalExceptionHandlerAdvice.class);

    // 处理 NoResourceFoundException（Spring Boot 3.4+，无匹配控制器时当作静态资源请求）
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseMessage handleNoResourceFound(NoResourceFoundException e, HttpServletRequest request) {
        String path = request.getRequestURI();
        log.warn("请求路径无对应接口，请检查 URL 或重启应用使新接口生效: {}", path);
        return new ResponseMessage(HttpStatus.NOT_FOUND.value(),
                "接口不存在: " + path + "，请确认地址正确。若为新增接口，请重新编译并重启应用。", null);
    }

    // 处理HTTP方法不支持异常
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseMessage handleHttpMethodNotSupported(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        String method = e.getMethod();
        String requestURI = request.getRequestURI();
        String supportedMethods = e.getSupportedHttpMethods() != null ? e.getSupportedHttpMethods().toString() : "未知";
        
        log.error("HTTP方法不支持异常 - 请求方法: {}, 请求路径: {}, 支持的HTTP方法: {}", method, requestURI, supportedMethods);
        
        return new ResponseMessage(405, 
            String.format("不支持的HTTP方法: %s。请求路径: %s。支持的HTTP方法: %s", method, requestURI, supportedMethods), 
            null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseMessage handleDataIntegrityViolation(DataIntegrityViolationException e) {
        String detail = e.getMostSpecificCause() != null
                ? e.getMostSpecificCause().getMessage()
                : e.getMessage();
        log.error("数据完整性异常: {}", detail, e);
        if (detail != null && detail.contains("doesn't have a default value")
                && (detail.contains("'manage_id'") || detail.contains("'manager_id'"))) {
            return new ResponseMessage(500,
                    "表 lab 上同时存在 manage_id 与 manager_id 且均为 NOT NULL 时，需两列同时写入。"
                            + "当前版本已在保存实验室时自动同步两列；若仍报错请 SHOW COLUMNS FROM lab。"
                            + "长期建议在库中只保留 manager_id 并删除 manage_id，再移除实体 Lab.manageIdShadow 字段。详见 sql/fix_lab_manage_id_typo.sql。",
                    null);
        }
        return new ResponseMessage(500,
                "数据保存违反约束: " + (detail != null ? detail : "未知原因"),
                null);
    }

    @ExceptionHandler({Exception.class})// 所有异常的统一处理
    public ResponseMessage handlerException(Exception e, HttpServletRequest request, HttpServletResponse response){
        log.error("统一异常：",e);
        return new ResponseMessage(500,"error",null);
    }

}