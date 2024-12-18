package com.recargapay.code.assessment.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.recargapay.code.assessment.model.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {


	@Query("SELECT t FROM Transaction t WHERE t.createdAt BETWEEN :startOfDay AND :endOfDay "
		     + "AND t.walletId = :walletId "
		     + "AND t.sequence = (SELECT MAX(t2.sequence) FROM Transaction t2 WHERE t2.createdAt BETWEEN :startOfDay AND :endOfDay AND t.walletId = :walletId )")
    Optional<Transaction> findTransactionsByDateAndWalletId(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay, @Param("walletId") UUID walletId);
	
	@Query("SELECT t FROM Transaction t WHERE t.walletId = :wallatId ORDER BY t.createdAt")
	List<Transaction> findByWalletId(@Param("wallatId") UUID wallatId );

}
