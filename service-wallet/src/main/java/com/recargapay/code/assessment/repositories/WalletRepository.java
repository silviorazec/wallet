package com.recargapay.code.assessment.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.recargapay.code.assessment.model.Wallet;


@Repository
public interface WalletRepository extends JpaRepository<Wallet,UUID> {
	
	@Query("SELECT w FROM Wallet w WHERE w.userId = :userId")
	public Optional<Wallet> findByUserId(@Param("userId") String userId);
	
	@Query("SELECT w FROM Wallet w WHERE w.id in (:w1, :w2)")
	List<Wallet> findWalletsByIds( @Param("w1") UUID w1,@Param("w2") UUID w2);
	


}
