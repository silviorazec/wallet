package com.recargapay.code.assessment.audit;



import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class AuditLogAspect {

    @Before("execution(* com.recargapay.code.assessment.*.*(..))")
    public void logBeforeMethod(JoinPoint joinPoint) {
        log.info("Audit Log - Iniciando execução do método: {}", joinPoint.getSignature());
        log.info("Parâmetros: {}", joinPoint.getArgs());
    }

   
    @AfterReturning(pointcut = "execution(* com.recargapay.code.assessment.*.*(..))", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        log.info("Audit Log - Método concluído: {}", joinPoint.getSignature());
        log.info("Resultado: {}", result);
    }
}
