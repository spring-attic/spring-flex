package org.springframework.flex.hibernate4.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("CC")
public class CreditCard extends BillingDetails{

    private String number;
    
    private String expMonth;
    
    private String expYear;

    public void setNumber(String number) {
        this.number = number;
    }

    public String getNumber() {
        return number;
    }

    public void setExpMonth(String expMonth) {
        this.expMonth = expMonth;
    }

    public String getExpMonth() {
        return expMonth;
    }

    public void setExpYear(String expYear) {
        this.expYear = expYear;
    }

    public String getExpYear() {
        return expYear;
    }
}
