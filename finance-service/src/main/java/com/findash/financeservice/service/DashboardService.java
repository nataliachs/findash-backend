package com.findash.financeservice.service;

import com.findash.financeservice.dto.AccountSummary;
import com.findash.financeservice.dto.BillSummary;
import com.findash.financeservice.dto.DashboardSummaryResponse;
import com.findash.financeservice.dto.LoanSummary;
import com.findash.financeservice.dto.TransactionSummary;
import com.findash.financeservice.model.Account;
import com.findash.financeservice.model.Bill;
import com.findash.financeservice.model.Loan;
import com.findash.financeservice.model.Transaction;
import com.findash.financeservice.repository.AccountRepository;
import com.findash.financeservice.repository.BillRepository;
import com.findash.financeservice.repository.LoanRepository;
import com.findash.financeservice.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DashboardService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final BillRepository billRepository;
    private final LoanRepository loanRepository;

    public DashboardService(AccountRepository accountRepository,
                            TransactionRepository transactionRepository,
                            BillRepository billRepository,
                            LoanRepository loanRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.billRepository = billRepository;
        this.loanRepository = loanRepository;
    }

    public DashboardSummaryResponse getSummary(String ownerEmail) {
        List<Account> accounts = accountRepository.findByOwnerEmail(ownerEmail);
        List<Transaction> transactions =
                transactionRepository.findTop5ByOwnerEmailOrderByTransactionDateDesc(ownerEmail);
        List<Bill> upcomingBills =
                billRepository.findTop5ByOwnerEmailAndPaidFalseOrderByDueDateAsc(ownerEmail);
        List<Loan> loans = loanRepository.findByOwnerEmail(ownerEmail);

        BigDecimal totalBalance = accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDebt = loans.stream()
                .map(Loan::getRemainingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long unpaidBillCount = billRepository.countByOwnerEmailAndPaidFalse(ownerEmail);

        List<AccountSummary> accountSummaries = accounts.stream()
                .map(this::toAccountSummary)
                .toList();

        List<TransactionSummary> transactionSummaries = transactions.stream()
                .map(this::toTransactionSummary)
                .toList();

        List<BillSummary> billSummaries = upcomingBills.stream()
                .map(this::toBillSummary)
                .toList();

        List<LoanSummary> loanSummaries = loans.stream()
                .map(this::toLoanSummary)
                .toList();

        DashboardSummaryResponse response = new DashboardSummaryResponse();
        response.setTotalBalance(totalBalance);
        response.setTotalDebt(totalDebt);
        response.setUnpaidBillCount(unpaidBillCount);
        response.setAccounts(accountSummaries);
        response.setRecentTransactions(transactionSummaries);
        response.setUpcomingBills(billSummaries);
        response.setLoans(loanSummaries);

        return response;
    }

    private AccountSummary toAccountSummary(Account account) {
        AccountSummary summary = new AccountSummary();
        summary.setId(account.getId());
        summary.setName(account.getName());
        summary.setType(account.getType());
        summary.setInstitution(account.getInstitution());
        summary.setBalance(account.getBalance());
        return summary;
    }

    private TransactionSummary toTransactionSummary(Transaction transaction) {
        TransactionSummary summary = new TransactionSummary();
        summary.setId(transaction.getId());
        summary.setAccountId(transaction.getAccountId());
        summary.setType(transaction.getType());
        summary.setAmount(transaction.getAmount());
        summary.setDescription(transaction.getDescription());
        summary.setCategory(transaction.getCategory());
        summary.setTransactionDate(transaction.getTransactionDate());
        return summary;
    }

    private BillSummary toBillSummary(Bill bill) {
        BillSummary summary = new BillSummary();
        summary.setId(bill.getId());
        summary.setName(bill.getName());
        summary.setAmount(bill.getAmount());
        summary.setDueDate(bill.getDueDate());
        summary.setPaid(bill.isPaid());
        return summary;
    }

    private LoanSummary toLoanSummary(Loan loan) {
        LoanSummary summary = new LoanSummary();
        summary.setId(loan.getId());
        summary.setName(loan.getName());
        summary.setLender(loan.getLender());
        summary.setTotalAmount(loan.getTotalAmount());
        summary.setRemainingAmount(loan.getRemainingAmount());
        summary.setMonthlyPayment(loan.getMonthlyPayment());
        return summary;
    }
}
