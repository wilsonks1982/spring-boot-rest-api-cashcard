package com.wilsonks1982.cashcard2;

import com.wilsonks1982.cashcard2.data_transfer.CashCard;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/cashcard")
public class CashCardController {

    private final CashCardDataRepository cashCardDataRepository;
    private final CashCardCustomRepository cashCardCustomRepository;

    public CashCardController(CashCardDataRepository cashCardDataRepository, CashCardCustomRepository cashCardCustomRepository) {
        this.cashCardDataRepository = cashCardDataRepository;
        this.cashCardCustomRepository = cashCardCustomRepository;
    }

    @PostMapping("/")
    public ResponseEntity<CashCard> createCashCard(@RequestBody CashCard cashCard) {
        log.info("Received request to create CashCard: " + cashCard);

        CashCard savedCashCard = cashCardCustomRepository.insert(cashCard);
        return ResponseEntity.created(
                java.net.URI.create("/cashcard/" + savedCashCard.id())
        ).body(savedCashCard);
    }


    @GetMapping("/{id}")
    public ResponseEntity<CashCard> getCashCardById(@PathVariable Long id) {

        log.info("Received request for CashCard with id: " + id);
        Optional<CashCard> cashCardOpt = cashCardCustomRepository.findById(id);
        return cashCardOpt.map(
                cashCard -> ResponseEntity.ok(cashCard)
        ).orElseGet(() -> ResponseEntity.notFound().build());

    }

    @GetMapping("/")
    public ResponseEntity<Iterable<CashCard>> getAllCashCard(Pageable pageable) {
        log.info("Received request for all CashCards with pagination: " + pageable);
        Iterable<CashCard> cashCards = cashCardDataRepository.findAll(
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by("id").ascending())
                )
        ).getContent();
        return ResponseEntity.ok(cashCards);
    }

    @GetMapping("/cardnumber/{cardNumber}")
    public ResponseEntity<CashCard> getCashCardByCardNumber(@PathVariable String cardNumber) {
        log.info("Received request for CashCard with card number: " + cardNumber);
        Optional<CashCard> cashCardOpt = cashCardCustomRepository.findByCardNumber(cardNumber);
        return cashCardOpt.map(
                cashCard -> ResponseEntity.ok(cashCard)
        ).orElseGet(() -> ResponseEntity.notFound().build());
    }

}

