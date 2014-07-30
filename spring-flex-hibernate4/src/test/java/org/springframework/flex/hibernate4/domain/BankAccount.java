package org.springframework.flex.hibernate4.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("BA")
public class BankAccount extends BillingDetails{

    private String account;
    
    private String bankName;
    
    private String swift;

    public void setAccount(String account) {
        this.account = account;
    }

    public String getAccount() {
        return account;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankName() {
        return bankName;
    }

    public void setSwift(String swift) {
        this.swift = swift;
    }

    public String getSwift() {
        return swift;
    }
}
