package com.rently.rently.reservation.customer;

public record CustomerOptionResponse(String id, String firstName, String lastName, String phone, IdType idType, String idNumber) {
}
