package com.wilsonks1982.cashcard2;

import com.wilsonks1982.cashcard2.data_transfer.ApiError;
import com.wilsonks1982.cashcard2.data_transfer.CashCard;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
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
    public ResponseEntity<CashCard> createCashCard(
            @Valid @RequestBody CashCard cashCard,
            Principal principal) {
        log.info("Received request to create CashCard: " + cashCard);
        log.info("Authenticated user: " + principal.getName());

        CashCard newCashCard = new CashCard(
                null,
                cashCard.cardNumber(),
                cashCard.balance(),
                principal.getName()
        );

        CashCard savedCashCard = cashCardCustomRepository.insert(newCashCard);
        return ResponseEntity.created(
                java.net.URI.create("/cashcard/" + savedCashCard.id())
        ).body(savedCashCard);
    }


    @GetMapping("/{id}")
    public ResponseEntity<CashCard> getCashCardById(
            @PathVariable @Positive(message = "ID must be a positive number") Long id,
            Principal principal) {

        log.info("Received request for CashCard with id: " + id);
        log.info("Authenticated user: " + principal.getName());

        Optional<CashCard> cashCardOpt = cashCardDataRepository.findByIdAndOwner(id, principal.getName());
        return cashCardOpt.map(
                cashCard -> ResponseEntity.ok(cashCard)
        ).orElseGet(() -> ResponseEntity.notFound().build());

    }

    @GetMapping("/")
    public ResponseEntity<Iterable<CashCard>> getAllCashCard(
            Pageable pageable,
            Principal principal) {
        log.info("Received request for all CashCards with pagination: " + pageable);
        log.info("Authenticated user: " + principal.getName());

        String owner = principal.getName();

        Iterable<CashCard> cashCards = cashCardDataRepository.findByOwner(
                owner,
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by("id").ascending())
                )
        ).getContent();
        return ResponseEntity.ok(cashCards);
    }

    @GetMapping("/search")
    public ResponseEntity<CashCard> getCashCardByCardNumber(
            @RequestParam(defaultValue = "1111111111") @NotBlank @Pattern(regexp = "\\d{10}", message = "Card number must be exactly 10 digits")
            String cardNumber,
            Principal principal) {
        log.info("Received request for CashCard with card number: " + cardNumber);
        log.info("Authenticated user: " + principal.getName());

        Optional<CashCard> cashCardOpt = cashCardCustomRepository.findByCardNumber(cardNumber);
        return cashCardOpt.map(
                cashCard -> ResponseEntity.ok(cashCard)
        ).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CashCard> updateCashCard(
            @PathVariable @Positive(message = "ID must be a positive number") Long id,
            @RequestBody @Valid CashCard cashCard,
            Principal principal) {
        log.info("Received request to update CashCard with id: " + id);
        log.info("Update data: " + cashCard);
        log.info("Authenticated user: " + principal.getName());

        String owner = principal.getName();

        Optional<CashCard> existingCashCardOpt = cashCardDataRepository.findById(id);
        if (existingCashCardOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        //Check if owner change request is valid (should not be allowed)
        if (!existingCashCardOpt.get().owner().equals(cashCard.owner())) {
            //Owner change is not allowed, return forbidden
            return ResponseEntity.status(403).build();
        }

        CashCard updatedCashCard = existingCashCardOpt.map(
                existingCashCard -> new CashCard(
                        existingCashCard.id(),
                        cashCard.cardNumber(),
                        cashCard.balance(),
                        existingCashCard.owner()
                )
        ).orElseThrow(); //This should never happen since we already checked for existence
        cashCardDataRepository.save(updatedCashCard);
        return ResponseEntity.ok(updatedCashCard);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCashCard(
            @PathVariable @Positive(message = "ID must be a positive number") Long id,
            Principal principal) {
        log.info("Received request to delete CashCard with id: " + id);
        log.info("Authenticated user: " + principal.getName());

        Optional<CashCard> existingCashCardOpt = cashCardDataRepository.findByIdAndOwner(id, principal.getName());
        if (existingCashCardOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        cashCardDataRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }


}

