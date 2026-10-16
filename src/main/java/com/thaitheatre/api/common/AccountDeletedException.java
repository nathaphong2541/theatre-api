package com.thaitheatre.api.common;

/**
 * Thrown when someone tries to log in with the correct credentials of an
 * account that has already been (soft) deleted. We only surface this once the
 * password is verified, so an attacker who doesn't know the password still
 * can't tell a deleted account from a non-existent one.
 */
public class AccountDeletedException extends RuntimeException {

    public AccountDeletedException(String message) {
        super(message);
    }
}
