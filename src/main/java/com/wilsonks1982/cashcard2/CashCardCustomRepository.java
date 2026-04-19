package com.wilsonks1982.cashcard2;


import com.wilsonks1982.cashcard2.data_transfer.CashCard;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public class CashCardCustomRepository {
    private final JdbcTemplate jdbcTemplate;

    public CashCardCustomRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<CashCard> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM cash_card",
                (rs, rowNum) -> new CashCard(
                    rs.getLong("id"),
                    rs.getString("card_number"),
                    rs.getBigDecimal("balance"),
                    rs.getString("owner")

                )
        );
    }

    public Optional<CashCard> findByCardNumber(String cardNumber) {
        List<CashCard> results = jdbcTemplate.query(
                "SELECT * FROM cash_card WHERE card_number = ?",
                (rs, rowNum) -> new CashCard(
                        rs.getLong("id"),
                        rs.getString("card_number"),
                        rs.getBigDecimal("balance"),
                        rs.getString("owner")
                ),
                cardNumber
        );
        return results.stream().findFirst();
    }

    public Optional<CashCard> findById(Long id) {
        List<CashCard> results = jdbcTemplate.query(
                "SELECT * FROM cash_card WHERE id = ?",
                (rs, rowNum) -> new CashCard(
                        rs.getLong("id"),
                        rs.getString("card_number"),
                        rs.getBigDecimal("balance"),
                        rs.getString("owner")
                ),
                id
        );
        return results.stream().findFirst();
    }

    public CashCard insert(CashCard cashCard) {
        Long newId = nextId();
        jdbcTemplate.update(
                "INSERT INTO cash_card (card_number, balance, owner) VALUES (?, ?, ?)",
                cashCard.cardNumber(),
                cashCard.balance(),
                cashCard.owner()
        );
        return new CashCard(
                newId,
                cashCard.cardNumber(),
                cashCard.balance(),
                cashCard.owner()
        );
    }

    public Long nextId() {
        Long maxId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM cash_card", Long.class);

        return (maxId != null) ? maxId + 1 : 1L;
    }

    public BigDecimal creditBalance(Long id, BigDecimal amount) {
        jdbcTemplate.update(
                "UPDATE cash_card SET balance = balance + ? WHERE id = ?",
                amount,
                id
        );

        BigDecimal balance = jdbcTemplate.queryForObject(
                "SELECT balance FROM cash_card WHERE id = ?",
                BigDecimal.class,
                id
        );

        return balance;
    }

     public BigDecimal debitBalance(Long id, BigDecimal amount) {
        jdbcTemplate.update(
                "UPDATE cash_card SET balance = balance - ? WHERE id = ?",
                amount,
                id
        );

        BigDecimal balance = jdbcTemplate.queryForObject(
                "SELECT balance FROM cash_card WHERE id = ?",
                BigDecimal.class,
                id
        );

        return balance;
    }


}
