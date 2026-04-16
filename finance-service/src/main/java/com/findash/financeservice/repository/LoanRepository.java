package com.findash.financeservice.repository;

import com.findash.financeservice.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, String> {
    List<Loan> findByOwnerEmail(String ownerEmail);
}
