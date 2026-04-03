package ru.gigasigma.blpscrud.transaction;

import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

public interface ProgrammaticTransaction<T> {

    T executeInTransaction(
            TransactionBody<T> body,
            TransactionRollbackLogic rollbackLogic
    );

    @FunctionalInterface
    interface TransactionBody<T> {
        T execute();
    }

    @FunctionalInterface
    interface TransactionBodyVoid {
        void execute();
    }

    @FunctionalInterface
    interface TransactionRollbackLogic {
        void rollback(TransactionStatus status, Exception e);
    }

    static <T> T defaultTransaction(
            PlatformTransactionManager txManager,
            TransactionDefinition txDefinition,
            TransactionBody<T> transactionBody) {
        ProgrammaticTransaction<T> f = (body, rollbackLogic) -> {
            var status = txManager.getTransaction(txDefinition);
            try {
                var data =  body.execute();
                txManager.commit(status);
                return data;
            } catch (Exception e) {
                rollbackLogic.rollback(status, e);
                throw e;
            }
        };
        return f.executeInTransaction(transactionBody, ProgrammaticTransaction::rollbackWithLogs);
    }

    static <T> void defaultTransactionVoid(
            PlatformTransactionManager txManager,
            TransactionDefinition txDefinition,
            TransactionBodyVoid transactionBody) {
        ProgrammaticTransaction<T> f = (body, rollbackLogic) -> {
            var status = txManager.getTransaction(txDefinition);
            try {
                var data =  body.execute();
                txManager.commit(status);
                return data;
            } catch (Exception e) {
                rollbackLogic.rollback(status, e);
                throw e;
            }
        };
        f.executeInTransaction(() -> {transactionBody.execute(); return null; }, ProgrammaticTransaction::rollbackWithLogs);
    }

    static void rollbackWithLogs(TransactionStatus status, Exception e) {
        var log = LoggerFactory.getLogger(ProgrammaticTransaction.class);
        log.error("Transaction failed, rolling back. Reason: {}", e.getMessage(), e);
    }
}
