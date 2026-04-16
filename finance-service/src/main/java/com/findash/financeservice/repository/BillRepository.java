package com.findash.financeservice.repository;

import com.findash.financeservice.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillRepository extends JpaRepository<Bill, String> {
    long countByOwnerEmailAndPaidFalse(String ownerEmail);
    List<Bill> findTop5ByOwnerEmailAndPaidFalseOrderByDueDateAsc(String ownerEmail);
}
