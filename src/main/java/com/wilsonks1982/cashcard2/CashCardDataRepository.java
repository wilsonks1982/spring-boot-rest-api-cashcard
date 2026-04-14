package com.wilsonks1982.cashcard2;

import com.wilsonks1982.cashcard2.data_transfer.CashCard;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
public interface CashCardDataRepository extends CrudRepository<CashCard, Long>, PagingAndSortingRepository<CashCard, Long> {
}
