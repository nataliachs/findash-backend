package com.findash.financeservice.repository;

import com.findash.financeservice.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, String> {
    List<Account> findByOwnerEmail(String ownerEmail);
}
