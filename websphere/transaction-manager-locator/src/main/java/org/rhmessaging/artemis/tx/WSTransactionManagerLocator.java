package org.rhmessaging.artemis.tx;

import javax.transaction.TransactionManager;

import org.apache.activemq.artemis.service.extensions.transactions.TransactionManagerLocator;

public class WSTransactionManagerLocator implements TransactionManagerLocator {

   @Override
   public TransactionManager getTransactionManager() {
      return com.ibm.tx.jta.TransactionManagerFactory.getTransactionManager();
   }
}