package com.fredfonseca.bookstoremanager.rentals.exception;

import javax.persistence.EntityExistsException;
import java.time.LocalDate;

public class InvalidDateException extends EntityExistsException {
    public InvalidDateException() {
        super("Rental date should not be in a future date ");
    }
}
