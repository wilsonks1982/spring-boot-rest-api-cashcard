package com.wilsonks1982.cashcard2;

import com.wilsonks1982.cashcard2.data_transfer.CashCard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface CashCardDataRepository extends CrudRepository<CashCard, Long>, PagingAndSortingRepository<CashCard, Long> {
    Optional<CashCard> findByIdAndOwner(Long id, String owner);
    Page<CashCard> findByOwner(String owner, Pageable pageable);
}
