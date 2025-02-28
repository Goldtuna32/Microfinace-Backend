package com.sme.entity;

import lombok.Getter;

@Getter
public enum Status {

    ACTIVE(1), INACTIVE(2),

    PENDING(3),APPROVED(4),REJECTED(5),

    COMPLETED(6),CANCELLED(7), SUSPENDED(8);

    private final int code;

    Status(int code) {
        this.code = code;
    }


    public static Status fromCode(int code) {
      for (Status status: Status.values()) {
          if (status.getCode() == code) {
              return status;
          }
      }

      throw new IllegalArgumentException("Invalid Status code : "+ code);
  }
}
