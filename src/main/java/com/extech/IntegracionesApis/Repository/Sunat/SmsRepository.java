package com.extech.IntegracionesApis.Repository.Sms;

import com.extech.IntegracionesApis.Domain.Model.Sms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SmsRepository extends JpaRepository<Sms, Long> {

    Optional<Sms> findByMessageId(String messageId);
    
    List<Sms> findByPhoneNumber(String phoneNumber);
    
    List<Sms> findByProvider(String provider);
    
    List<Sms> findBySuccess(Boolean success);
    
    List<Sms> findByPhoneNumberAndSuccess(String phoneNumber, Boolean success);
    
    @Query("SELECT s FROM Sms s WHERE s.fechaEnvio BETWEEN :fechaInicio AND :fechaFin")
    List<Sms> findByFechaEnvioBetween(@Param("fechaInicio") LocalDateTime fechaInicio, 
                                     @Param("fechaFin") LocalDateTime fechaFin);
    
    @Query("SELECT s FROM Sms s WHERE s.phoneNumber = :phoneNumber AND s.fechaEnvio BETWEEN :fechaInicio AND :fechaFin")
    List<Sms> findByPhoneNumberAndFechaEnvioBetween(@Param("phoneNumber") String phoneNumber,
                                                   @Param("fechaInicio") LocalDateTime fechaInicio,
                                                   @Param("fechaFin") LocalDateTime fechaFin);
    
    @Query("SELECT COUNT(s) FROM Sms s WHERE s.success = true AND s.fechaEnvio BETWEEN :fechaInicio AND :fechaFin")
    Long countSuccessfulSmsBetween(@Param("fechaInicio") LocalDateTime fechaInicio,
                                   @Param("fechaFin") LocalDateTime fechaFin);
    
    @Query("SELECT COUNT(s) FROM Sms s WHERE s.success = false AND s.fechaEnvio BETWEEN :fechaInicio AND :fechaFin")
    Long countFailedSmsBetween(@Param("fechaInicio") LocalDateTime fechaInicio,
                              @Param("fechaFin") LocalDateTime fechaFin);
    
    @Query("SELECT s.provider, COUNT(s) FROM Sms s WHERE s.fechaEnvio BETWEEN :fechaInicio AND :fechaFin GROUP BY s.provider")
    List<Object[]> countSmsByProviderBetween(@Param("fechaInicio") LocalDateTime fechaInicio,
                                           @Param("fechaFin") LocalDateTime fechaFin);
}
