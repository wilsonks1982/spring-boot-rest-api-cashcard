package com.wilsonks1982.cashcard2.data_transfer;

import java.time.LocalDateTime;

public record ApiError(
        LocalDateTime timestamp,
        int status,
        String StatusText,
        String message,
        String path
) {
}
