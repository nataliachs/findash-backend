package com.findash.financeservice.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardSummaryResponse {

    private BigDecimal totalBalance;
    private BigDecimal totalDebt;
    private long unpaidBillCount;
    private List<AccountSummary> accounts;
    private List<TransactionSummary> recentTransactions;
    private List<BillSummary> upcomingBills;
    private List<LoanSummary> loans;

    public BigDecimal getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(BigDecimal totalBalance) {
        this.totalBalance = totalBalance;
    }

    public BigDecimal getTotalDebt() {
        return totalDebt;
    }

    public void setTotalDebt(BigDecimal totalDebt) {
        this.totalDebt = totalDebt;
    }

    public long getUnpaidBillCount() {
        return unpaidBillCount;
    }

    public void setUnpaidBillCount(long unpaidBillCount) {
        this.unpaidBillCount = unpaidBillCount;
    }

    public List<AccountSummary> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<AccountSummary> accounts) {
        this.accounts = accounts;
    }

    public List<TransactionSummary> getRecentTransactions() {
        return recentTransactions;
    }

    public void setRecentTransactions(List<TransactionSummary> recentTransactions) {
        this.recentTransactions = recentTransactions;
    }

    public List<BillSummary> getUpcomingBills() {
        return upcomingBills;
    }

    public void setUpcomingBills(List<BillSummary> upcomingBills) {
        this.upcomingBills = upcomingBills;
    }

    public List<LoanSummary> getLoans() {
        return loans;
    }

    public void setLoans(List<LoanSummary> loans) {
        this.loans = loans;
    }
}
